package com.kaiserdapar.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Integer accountId) {
        super("Account not found: " + accountId);
    }

}
