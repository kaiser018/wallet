package com.kaiserdapar.wallet.dto;

import com.kaiserdapar.wallet.entity.Transaction;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
public record TransactionResponse(
        UUID id,
        int amountInCents,
        int balanceInCents,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmountInCents(),
                t.getBalanceInCents(),
                t.getCreatedAt()
        );
    }
}
