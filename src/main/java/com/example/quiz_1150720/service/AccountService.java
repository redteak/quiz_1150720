package com.example.quiz_1150720.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quiz_1150720.dto.AccountManagementRequest;
import com.example.quiz_1150720.dto.AccountManagementResponse;
import com.example.quiz_1150720.dto.LoginRequest;
import com.example.quiz_1150720.dto.LoginResponse;
import com.example.quiz_1150720.dto.RegisterRequest;
import com.example.quiz_1150720.eneity.Account;
import com.example.quiz_1150720.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // 登入
    public LoginResponse login(LoginRequest request) {
        if (request == null
                || isBlank(request.getAccount())
                || isBlank(request.getPassword())) {
            return new LoginResponse(false, "帳號 / 密碼不能為空", null, null, null);
        }

        Account account = accountRepository.findByAccount(request.getAccount().trim());

        if (account == null) {
            return new LoginResponse(false, "帳號或密碼錯誤", null, null, null);
        }

        if (account.getStatus() == null || account.getStatus() != 1) {
            return new LoginResponse(false, "此帳號目前已停用", null, null, null);
        }

        String inputPassword = request.getPassword();
        String savedPassword = account.getPassword();

        if (savedPassword == null) {
            return new LoginResponse(false, "帳號或密碼錯誤", null, null, null);
        }

        if (isBCryptPassword(savedPassword)) {
            if (!passwordEncoder.matches(inputPassword, savedPassword)) {
                return new LoginResponse(false, "帳號或密碼錯誤", null, null, null);
            }
        } else {
            // 舊帳號登入成功後，將明碼升級為 BCrypt
            if (!inputPassword.equals(savedPassword)) {
                return new LoginResponse(false, "帳號或密碼錯誤", null, null, null);
            }

            account.setPassword(passwordEncoder.encode(inputPassword));
            accountRepository.save(account);
        }

        String token = jwtService.generateToken(account.getAccount());

        return new LoginResponse(
                true,
                "登入成功",
                account.getAccount(),
                account.getUserName(),
                account.getPermissions(),
                token);
    }

    // 註冊
    public LoginResponse register(RegisterRequest request) {
        if (request == null
                || isBlank(request.getUserName())
                || isBlank(request.getAccount())
                || isBlank(request.getPassword())) {
            return new LoginResponse(false, "請完整填寫註冊資料", null, null, null);
        }

        String userName = request.getUserName().trim();
        String accountName = request.getAccount().trim();
        String password = request.getPassword();

        if (userName.length() > 50 || accountName.length() > 50) {
            return new LoginResponse(false, "帳號或名稱不可超過 50 個字元", null, null, null);
        }

        if (accountRepository.existsById(accountName)) {
            return new LoginResponse(false, "此帳號已被註冊", null, null, null);
        }

        Account newAccount = new Account();
        newAccount.setUserName(userName);
        newAccount.setAccount(accountName);
        newAccount.setPassword(passwordEncoder.encode(password));
        newAccount.setPermissions(0);
        newAccount.setStatus(1);

        accountRepository.save(newAccount);

        return new LoginResponse(
                true,
                "註冊成功",
                newAccount.getAccount(),
                newAccount.getUserName(),
                newAccount.getPermissions());
    }

    // 查詢全部帳號
    public List<AccountManagementResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 新增帳號
    public AccountManagementResponse createAccount(AccountManagementRequest request) {
        if (request == null
                || isBlank(request.getAccount())
                || isBlank(request.getUserName())
                || isBlank(request.getPassword())
                || request.getPermissions() == null
                || request.getStatus() == null) {
            throw new IllegalArgumentException("請完整填寫帳號資料");
        }

        String accountName = request.getAccount().trim();
        String userName = request.getUserName().trim();
        String password = request.getPassword();

        validateNameLength(accountName, userName);
        validatePermissions(request.getPermissions());
        validateStatus(request.getStatus());

        if (accountRepository.existsById(accountName)) {
            throw new IllegalArgumentException("此帳號已存在");
        }

        Account account = new Account();
        account.setAccount(accountName);
        account.setUserName(userName);
        account.setPassword(passwordEncoder.encode(password));
        account.setPermissions(request.getPermissions());
        account.setStatus(request.getStatus());

        return toResponse(accountRepository.save(account));
    }

    // 修改帳號
    public AccountManagementResponse updateAccount(
            String accountName,
            AccountManagementRequest request) {
        if (isBlank(accountName)
                || request == null
                || isBlank(request.getUserName())
                || request.getPermissions() == null
                || request.getStatus() == null) {
            throw new IllegalArgumentException("請完整填寫帳號資料");
        }

        accountName = accountName.trim();

        Account account = findAccount(accountName);

        if (request.getAccount() != null
                && !accountName.equals(request.getAccount().trim())) {
            throw new IllegalArgumentException("帳號不可修改");
        }

        String userName = request.getUserName().trim();

        if (userName.length() > 50) {
            throw new IllegalArgumentException("使用者名稱不可超過 50 個字元");
        }

        validatePermissions(request.getPermissions());
        validateStatus(request.getStatus());

        account.setUserName(userName);
        account.setPermissions(request.getPermissions());
        account.setStatus(request.getStatus());

        // 有輸入新密碼才更新
        if (!isBlank(request.getPassword())) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(accountRepository.save(account));
    }

    // 修改帳號啟用狀態
    public AccountManagementResponse updateAccountStatus(
            String accountName,
            Integer status,
            String currentAccount) {
        if (isBlank(accountName)) {
            throw new IllegalArgumentException("帳號不可為空");
        }

        validateStatus(status);
        accountName = accountName.trim();

        if (accountName.equals(currentAccount) && status == 0) {
            throw new IllegalArgumentException("無法停用目前登入的帳號");
        }

        Account account = findAccount(accountName);
        account.setStatus(status);

        return toResponse(accountRepository.save(account));
    }

    // 刪除帳號
    public void deleteAccount(String accountName, String currentAccount) {
        if (isBlank(accountName)) {
            throw new IllegalArgumentException("帳號不可為空");
        }

        accountName = accountName.trim();

        if (accountName.equals(currentAccount)) {
            throw new IllegalArgumentException("無法刪除目前登入的帳號");
        }

        accountRepository.delete(findAccount(accountName));
    }

    // 查詢帳號，不存在時回報錯誤
    private Account findAccount(String accountName) {
        Account account = accountRepository.findByAccount(accountName);

        if (account == null) {
            throw new IllegalArgumentException("找不到此帳號");
        }

        return account;
    }

    // 將 Entity 轉成不包含密碼的 DTO
    private AccountManagementResponse toResponse(Account account) {
        return new AccountManagementResponse(
                account.getAccount(),
                account.getUserName(),
                account.getPermissions(),
                account.getStatus());
    }

    // 檢查字串是否為空
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // 判斷是否為 BCrypt 密碼
    private boolean isBCryptPassword(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }

    // 驗證帳號與名稱長度
    private void validateNameLength(String accountName, String userName) {
        if (accountName.length() > 50 || userName.length() > 50) {
            throw new IllegalArgumentException("帳號或名稱不可超過 50 個字元");
        }
    }

    // 驗證權限
    private void validatePermissions(Integer permissions) {
        if (permissions == null || (permissions != 0 && permissions != 1)) {
            throw new IllegalArgumentException("權限設定不正確");
        }
    }

    // 驗證帳號狀態
    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("帳號狀態設定不正確");
        }
    }
}