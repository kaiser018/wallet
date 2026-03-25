package com.kaiserdapar.wallet.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@Entity
@Table(
        indexes = {
                @Index(name = "idx_transactions_account_id", columnList = "account_id")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount_in_cents", nullable = false)
    private int amountInCents;

    @Column(name = "balance_in_cents", nullable = false)
    private int balanceInCents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Transaction() {
    }

    public Transaction(Account account, int amountInCents, int balanceInCents) {
        this.account = account;
        this.amountInCents = amountInCents;
        this.balanceInCents = balanceInCents;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public int getAmountInCents() {
        return amountInCents;
    }

    public void setAmountInCents(int amountInCents) {
        this.amountInCents = amountInCents;
    }

    public int getBalanceInCents() {
        return balanceInCents;
    }

    public void setBalanceInCents(int balanceInCents) {
        this.balanceInCents = balanceInCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}
