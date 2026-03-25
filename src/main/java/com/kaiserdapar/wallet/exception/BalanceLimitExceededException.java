package com.kaiserdapar.wallet.exception;

import com.kaiserdapar.wallet.entity.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BalanceLimitExceededException extends RuntimeException {

    public BalanceLimitExceededException(Account account, long nextBalanceInCents) {
        super("Balance exceeds max supported limit of " + Integer.MAX_VALUE + " for  account: " + account.getId() + ". Current balance in cents: " + account.getBalanceInCents() + ", attempted additional amount in cents: " + nextBalanceInCents);
    }

}
