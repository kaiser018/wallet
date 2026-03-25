#!/usr/bin/env bash
set -euo pipefail

# ── Usage ────────────────────────────────────────────────────────────────────
usage() {
  echo "Usage: $0 <num_transactions> [max_concurrency]"
  echo ""
  echo "  num_transactions  Number of transactions to send (positive integer)"
  echo "  max_concurrency   Max concurrent requests (default: 1 = sequential)"
  echo "  API URL is fixed to http://localhost:8080"
  exit 1
}

# ── Validate arguments ──────────────────────────────────────────────────────
if [[ $# -lt 1 ]]; then
  usage
fi

NUM_TRANSACTIONS="$1"
MAX_CONCURRENCY="${2:-1}"
API_URL="http://localhost:8080"

for arg_name in NUM_TRANSACTIONS MAX_CONCURRENCY; do
  if ! [[ "${!arg_name}" =~ ^[1-9][0-9]*$ ]]; then
    echo "Error: ${arg_name} must be a positive integer (got '${!arg_name}')"
    usage
  fi
done

# ── Check dependencies ──────────────────────────────────────────────────────
for cmd in curl jq; do
  if ! command -v "$cmd" &>/dev/null; then
    echo "Error: '$cmd' is required but not installed."
    exit 1
  fi
done

# ── Config ───────────────────────────────────────────────────────────────────
RUN_ID="$(date +%s)-$$"
DEPOSIT_CHANCE=70       # percentage chance a transaction is a deposit
MAX_DEPOSIT=10000       # max deposit in cents
MAX_BALANCE=2147483647  # max balance in cents (Integer.MAX_VALUE)

# ── Temp directory for concurrent result files ───────────────────────────────
WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

# ── Semaphore (fifo-based concurrency limiter) ──────────────────────────────
SEM_FIFO=$(mktemp -u)
mkfifo "$SEM_FIFO"
exec 3<>"$SEM_FIFO"
rm "$SEM_FIFO"
for ((s = 0; s < MAX_CONCURRENCY; s++)); do echo >&3; done

echo "============================================================"
echo " Wallet API Test"
echo "============================================================"
echo " Run ID:          $RUN_ID"
echo " API URL:         $API_URL"
echo " Transactions:    $NUM_TRANSACTIONS"
echo " Max concurrency: $MAX_CONCURRENCY"
echo "============================================================"
echo ""

# ── Phase 1: Create Account ─────────────────────────────────────────────────
USERNAME="test-user-${RUN_ID}"

echo "── Creating account..."

response=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/api/accounts" \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"${USERNAME}\"}")

http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')

if [[ "$http_code" -ge 200 && "$http_code" -lt 300 ]]; then
  ACCOUNT_ID=$(echo "$body" | jq -r '.id')
  echo "   ✓ Created account #${ACCOUNT_ID} (${USERNAME})"
else
  echo "   ✗ Failed to create account '${USERNAME}' (HTTP ${http_code}): ${body}"
  exit 1
fi

echo ""

# ── Phase 2: Execute Transactions Concurrently ──────────────────────────────
echo "── Sending transactions (concurrency: $MAX_CONCURRENCY)..."

# Background worker: sends one transaction and writes result to a file
do_transaction() {
  set +e  # don't exit-on-error inside this subshell
  local account_id="$1" amount="$2" result_file="$3"

  response=$(curl -s -w "\n%{http_code}" --max-time 30 -X POST \
    "${API_URL}/api/accounts/${account_id}/transactions" \
    -H "Content-Type: application/json" \
    -d "{\"amountInCents\": ${amount}}")

  http_code=$(echo "$response" | tail -1)
  body=$(echo "$response" | sed '$d')

  if [[ -n "$http_code" && "$http_code" -ge 200 && "$http_code" -lt 300 ]]; then
    echo "OK ${amount}" > "$result_file"
  else
    local error_msg
    error_msg=$(echo "$body" | jq -r '.message // .error // empty' 2>/dev/null || echo "$body")
    printf 'FAIL %s %s %s\n' "${amount}" "${http_code:-000}" "${error_msg}" > "$result_file"
  fi

  echo >&3  # release semaphore slot
}

# Pre-generate valid amounts sequentially (track balance to avoid invalid states)
sim_balance=0
declare -a amounts=()

for ((t = 0; t < NUM_TRANSACTIONS; t++)); do
  roll=$(( RANDOM % 100 ))
  if [[ $roll -lt $DEPOSIT_CHANCE ]] || [[ $sim_balance -eq 0 ]]; then
    # Deposit: cap at remaining headroom
    headroom=$(( MAX_BALANCE - sim_balance ))
    if [[ $headroom -le 0 ]]; then
      # Balance at max — force a withdrawal instead
      amount=$(( -((RANDOM % sim_balance) + 1) ))
    else
      max=$(( headroom < MAX_DEPOSIT ? headroom : MAX_DEPOSIT ))
      amount=$(( (RANDOM % max) + 1 ))
    fi
  else
    # Withdrawal: cap at current balance
    max=$(( sim_balance < MAX_DEPOSIT ? sim_balance : MAX_DEPOSIT ))
    amount=$(( -((RANDOM % max) + 1) ))
  fi

  sim_balance=$(( sim_balance + amount ))
  amounts+=("$amount")
done

# Send all pre-generated transactions concurrently
for ((t = 0; t < NUM_TRANSACTIONS; t++)); do
  read -u 3  # acquire semaphore slot (blocks if at max concurrency)
  do_transaction "$ACCOUNT_ID" "${amounts[$t]}" "${WORK_DIR}/${t}" &
done

wait  # wait for all background jobs to finish

echo "   Sent $NUM_TRANSACTIONS transaction(s)."
echo ""

# ── Phase 3: Tally Results ──────────────────────────────────────────────────
echo "── Transaction results (Account #${ACCOUNT_ID} — ${USERNAME}):"
echo ""

EXPECTED=0
TOTAL_SUCCEEDED=0
TOTAL_FAILED=0

for ((t = 0; t < NUM_TRANSACTIONS; t++)); do
  line=$(cat "${WORK_DIR}/${t}")
  status=$(echo "$line" | cut -d' ' -f1)
  amount=$(echo "$line" | cut -d' ' -f2)

  if [[ "$status" == "OK" ]]; then
    EXPECTED=$(( EXPECTED + amount ))
    TOTAL_SUCCEEDED=$((TOTAL_SUCCEEDED + 1))
    printf "   [%7d/%d] %+7d → OK (running expected: %d)\n" "$((t+1))" "$NUM_TRANSACTIONS" "$amount" "$EXPECTED"
  else
    http_code=$(echo "$line" | cut -d' ' -f3)
    error_msg=$(echo "$line" | cut -d' ' -f4-)
    TOTAL_FAILED=$((TOTAL_FAILED + 1))
    printf "   [%7d/%d] %+7d → FAILED (HTTP %s) %s\n" "$((t+1))" "$NUM_TRANSACTIONS" "$amount" "$http_code" "$error_msg"
  fi
done

echo ""
echo "   → ${TOTAL_SUCCEEDED} succeeded, ${TOTAL_FAILED} failed"
echo ""

# ── Phase 4: Verify Balance ─────────────────────────────────────────────────
echo "── Verifying balance..."
echo ""

response=$(curl -s -w "\n%{http_code}" -X GET "${API_URL}/api/accounts/${ACCOUNT_ID}")
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')

if [[ "$http_code" -ge 200 && "$http_code" -lt 300 ]]; then
  ACTUAL=$(echo "$body" | jq -r '.balanceInCents')
else
  ACTUAL="ERROR"
fi

if [[ "$ACTUAL" == "$EXPECTED" ]]; then
  RESULT="PASS"
else
  RESULT="FAIL"
fi

printf "%-12s %-28s %18s %18s %8s\n" "Account ID" "Username" "Expected Balance" "Actual Balance" "Result"
printf "%-12s %-28s %18s %18s %8s\n" "----------" "----------------------------" "------------------" "------------------" "------"
printf "%-12s %-28s %18s %18s %8s\n" "$ACCOUNT_ID" "$USERNAME" "$EXPECTED" "$ACTUAL" "$RESULT"

echo ""
echo "============================================================"
echo " Summary"
echo "============================================================"
echo " Account:                #${ACCOUNT_ID} (${USERNAME})"
echo " Transactions attempted: $NUM_TRANSACTIONS"
echo " Transactions succeeded: $TOTAL_SUCCEEDED"
echo " Transactions failed:    $TOTAL_FAILED"
echo " Max concurrency:        $MAX_CONCURRENCY"
echo " Balance check:          $RESULT"
echo "============================================================"

if [[ "$RESULT" == "FAIL" ]]; then
  exit 1
fi
