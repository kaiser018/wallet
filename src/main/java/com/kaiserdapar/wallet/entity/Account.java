package com.kaiserdapar.wallet.entity;

import com.kaiserdapar.wallet.exception.BalanceLimitExceededException;
import com.kaiserdapar.wallet.exception.InsufficientBalanceException;
import jakarta.persistence.*;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "balance_in_cents", nullable = false, columnDefinition = "integer default 0")
    private int balanceInCents = 0;


    public Account() {
    }

    public Account(String username) {
        this.username = username;
    }

    public int adjustBalance(int amountInCents) {

        long nextBalanceInCents = (long) this.balanceInCents + amountInCents;

        if (nextBalanceInCents < 0) {
            throw new InsufficientBalanceException(this, amountInCents);
        }

        if (nextBalanceInCents > Integer.MAX_VALUE) {
            throw new BalanceLimitExceededException(this, nextBalanceInCents);
        }

        this.balanceInCents = (int) nextBalanceInCents;
        return this.balanceInCents;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBalanceInCents() {
        return balanceInCents;
    }

    public void setBalanceInCents(int balanceInCents) {
        this.balanceInCents = balanceInCents;
    }

}
