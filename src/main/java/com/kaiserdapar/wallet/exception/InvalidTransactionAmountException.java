package com.kaiserdapar.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionAmountException extends RuntimeException {

    public InvalidTransactionAmountException() {
        super("Transaction amount cannot be zero");
    }

}
