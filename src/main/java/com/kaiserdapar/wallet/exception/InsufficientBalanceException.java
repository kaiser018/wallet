package com.kaiserdapar.wallet.exception;

import com.kaiserdapar.wallet.entity.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Account account, int amountInCents) {
        super("Insufficient balance for account: " + account.getId() + ". Current balance in cents: " + account.getBalanceInCents() + ", attempted subtract amount in cents: " + amountInCents);
    }

}
