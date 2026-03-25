package com.kaiserdapar.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AccountAlreadyExistsException extends RuntimeException {

    public AccountAlreadyExistsException(String name) {
        super("Account with name '" + name + "' already exists");
    }

}
