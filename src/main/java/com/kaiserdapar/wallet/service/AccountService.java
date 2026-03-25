package com.kaiserdapar.wallet.service;

import com.kaiserdapar.wallet.entity.Account;
import com.kaiserdapar.wallet.exception.AccountAlreadyExistsException;
import com.kaiserdapar.wallet.exception.InvalidUsernameException;
import com.kaiserdapar.wallet.repository.AccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {

        if (account.getUsername() == null || account.getUsername().isBlank()) {
            throw new InvalidUsernameException();
        }

        try {
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw new AccountAlreadyExistsException(account.getUsername());
        }
    }

    public Optional<Account> getAccount(Integer id) {
        return accountRepository.findById(id);
    }

}
