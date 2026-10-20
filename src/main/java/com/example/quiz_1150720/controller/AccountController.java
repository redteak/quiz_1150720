package com.example.quiz_1150720.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.quiz_1150720.dto.AccountManagementRequest;
import com.example.quiz_1150720.dto.AccountManagementResponse;
import com.example.quiz_1150720.dto.LoginRequest;
import com.example.quiz_1150720.dto.LoginResponse;
import com.example.quiz_1150720.dto.RegisterRequest;
import com.example.quiz_1150720.service.AccountService;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // 登入
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    // 註冊
    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    // 查詢全部帳號
    @GetMapping
    public List<AccountManagementResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // 新增帳號
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountManagementResponse createAccount(
            @RequestBody AccountManagementRequest request) {
        return accountService.createAccount(request);
    }

    // 修改帳號
    @PutMapping("/{account}")
    public AccountManagementResponse updateAccount(
    		@PathVariable("account") String account,
            @RequestBody AccountManagementRequest request) {
        return accountService.updateAccount(account, request);
    }

    // 啟用或停用帳號
    @PatchMapping("/{account}/status")
    public AccountManagementResponse updateAccountStatus(
            @PathVariable String account,
            @RequestParam Integer status,
            Authentication authentication) {
        String currentAccount = getCurrentAccount(authentication);
        return accountService.updateAccountStatus(account, status, currentAccount);
    }

    // 刪除帳號
    @DeleteMapping("/{account}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @PathVariable String account,
            Authentication authentication) {
        String currentAccount = getCurrentAccount(authentication);
        accountService.deleteAccount(account, currentAccount);
    }

    // 取得目前登入帳號
    private String getCurrentAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "請先登入");
        }
        return authentication.getName();
    }
}