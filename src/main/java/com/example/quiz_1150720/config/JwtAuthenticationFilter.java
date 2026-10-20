package com.example.quiz_1150720.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.quiz_1150720.eneity.Account;
import com.example.quiz_1150720.repository.AccountRepository;
import com.example.quiz_1150720.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AccountRepository accountRepository) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 沒有 Token，就交給 Spring Security 後續判斷是否允許存取
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 從 Token 取得登入帳號
            String accountName = jwtService.extractAccount(token);

            // 查詢資料庫中的帳號
            Account account = accountRepository.findByAccount(accountName);

            // 帳號不存在、已停用或 Token 無效，就不建立登入身分
            if (account == null
                    || account.getStatus() == null
                    || account.getStatus() != 1
                    || !jwtService.isTokenValid(token, accountName)) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 依照資料庫權限決定角色
            String role;

            if (account.getPermissions() != null
                    && account.getPermissions() == 1) {
                role = "ROLE_ADMIN";
            } else {
                role = "ROLE_USER";
            }

            // 建立 Spring Security 認證資訊
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            accountName,
                            null,
                            List.of(new SimpleGrantedAuthority(role)));

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            // Token 格式錯誤、簽章錯誤或已過期
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
