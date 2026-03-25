package com.kaiserdapar.wallet.controller;

import com.kaiserdapar.wallet.dto.CreateAccountRequest;
import com.kaiserdapar.wallet.entity.Account;
import com.kaiserdapar.wallet.exception.AccountNotFoundException;
import com.kaiserdapar.wallet.service.AccountService;
import org.springframework.web.bind.annotation.*;

/**
 * @author Kaiser Dapar (kaiserdapar@gmail.com)
 * @since 2026-03-25
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public Account createAccount(@RequestBody CreateAccountRequest account) {
        return accountService.createAccount(new Account(account.username()));
    }

    @GetMapping("{accountId}")
    public Account getAccount(@PathVariable Integer accountId) {
        return accountService.getAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

}
