package com.kaiserdapar.wallet.repository;

import com.kaiserdapar.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Integer accountId);
}
