package com.kaiserdapar.wallet.controller;

import com.kaiserdapar.wallet.dto.CreateTransactionRequest;
import com.kaiserdapar.wallet.dto.TransactionResponse;
import com.kaiserdapar.wallet.exception.InvalidTransactionAmountException;
import com.kaiserdapar.wallet.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(@PathVariable Integer accountId) {
        return transactionService.getTransactions(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping
    public TransactionResponse createTransaction(
            @PathVariable Integer accountId,
            @RequestBody CreateTransactionRequest request
    ) {
        if (request.amountInCents() == 0) {
            throw new InvalidTransactionAmountException();
        }
        return TransactionResponse.from(transactionService.adjustBalance(accountId, request.amountInCents()));
    }
}
