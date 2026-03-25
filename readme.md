# Wallet API


- All values are stored in integer cents:
  - Example: `1050` means 10.50 in currency units.
- Wallet balance cannot go below `0` (no negative balance).
- Wallet balance has a maximum of `2,147,483,647` cents (~21M currency units).
- [Postman Collection](https://postman.co/workspace/kaiserdapar~2abd39d8-663f-4815-b3df-eed86dfd689d/collection/562401-2e811059-5ae7-4459-aab2-420f17ca92e9?action=share&creator=562401)

## Technology

- **Java**: 25
- **JDK**: temurin-25.0.2
- **Maven**: 3.9.14
- **Spring Boot**: 4.0.4
- **Spring Framework**
  - Spring Web
  - Spring Data JPA
  - PostgreSQL Driver
- **Database**: PostgreSQL
- **Containers**: Docker, Docker Compose
  - PostgreSQL (Alpine)
  - NGINX (Alpine)
- **Testing & Scripting**: Bash, Curl, jq

## Run
```bash
docker compose up --build
```

## API Documentation

API entrypoint via nginx:`http://localhost:8080`

### 1. Create Account

`POST /api/accounts`

Request:

```json
{
	"username": "kai"
}
```

Response `200 OK`:

```json
{
	"id": 1,
	"username": "kai",
	"balanceInCents": 0
}
```

Errors:

- `400 Bad Request` when username is null or blank
- `409 Conflict` when username already exists

### 2. Get Balance

`GET /api/accounts/{accountId}`

Response `200 OK`:

```json
{
	"id": 1,
	"username": "kai",
	"balanceInCents": 5000
}
```

Errors:

- `404 Not Found` if account does not exist

### 3. Transfer

`POST /api/accounts/{accountId}/transactions`

Request:

```json
{
	"amountInCents": -1500
}
```

Rules:

- Positive value = deposit
- Negative value = withdrawal
- Zero is not allowed

Response `200 OK`:

```json
{
	"id": "transaction-uuid",
	"amountInCents": -1500,
	"balanceInCents": 3500,
	"createdAt": "2026-03-25T12:00:00Z"
}
```

Errors:

- `404 Not Found` if account does not exist
- `400 Bad Request` when `amountInCents` is zero
- `400 Bad Request` when withdrawal would make balance negative
- `400 Bad Request` when resulting balance exceeds the supported max (`2,147,483,647` cents)

### 4. List Transactions

`GET /api/accounts/{accountId}/transactions`

Response `200 OK`:

```json
[
	{
		"id": "transaction-uuid",
		"amountInCents": -1500,
		"balanceInCents": 3500,
		"createdAt": "2026-03-25T12:00:00Z"
	}
]
```

Errors:

- `404 Not Found` if account does not exist

## Test

Requires `curl` and `jq`.

```bash
./test.sh <num_transactions> [max_concurrency]
```

- `num_transactions` — number of random transactions to send
- `max_concurrency` — max parallel requests (default: `1`)

Example:

Creates an account, sends 100 random transactions (5 concurrent), then compares expected vs actual balance.
```bash
./test.sh 100 5
```

Run multiple instances in parallel to test across accounts.
```bash
./test.sh 100 5 &
./test.sh 100 5 &
./test.sh 100 5 &
```