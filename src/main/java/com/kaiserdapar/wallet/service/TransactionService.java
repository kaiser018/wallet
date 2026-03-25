package com.kaiserdapar.wallet.service;

import com.kaiserdapar.wallet.entity.Account;
import com.kaiserdapar.wallet.entity.Transaction;
import com.kaiserdapar.wallet.exception.AccountNotFoundException;
import com.kaiserdapar.wallet.repository.AccountRepository;
import com.kaiserdapar.wallet.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction adjustBalance(Integer accountId, int amountInCents) {

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        int newBalanceInCents = account.adjustBalance(amountInCents);

        Transaction transaction = new Transaction(
                account,
                amountInCents,
                newBalanceInCents
        );

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(Integer accountId) {

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

}
