package com.example.quiz_1150720.config;

import com.example.quiz_1150720.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.quiz_1150720.service.JwtService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
public class SecurityConfig {

	// 設定後端 API 的安全規則

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
			AccountRepository accountRepository) throws Exception {

		JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, accountRepository);

		http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable()).formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth

						// 登入與註冊不需要先登入
						.requestMatchers(HttpMethod.POST, "/api/account/login", "/api/account/register").permitAll()

						// 所有帳號管理 API 都必須是管理員
						.requestMatchers("/api/account/**").hasRole("ADMIN")

						// 問卷 GET API 維持公開讀取
						.requestMatchers(HttpMethod.GET, "/api/quiz/**").permitAll()

						// 其他 API 仍然需要登入
						.anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// 設定密碼雜湊工具，後續由 AccountService 使用
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 設定 Angular 前端的跨來源請求
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(List.of("http://localhost:4200"));

		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		// 不使用 Cookie Session，因此不開啟 credentials
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
