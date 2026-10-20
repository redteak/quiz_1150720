
package com.example.quiz_1150720.dto;

public class LoginRequest {

	// 前端傳來的帳號
	private String account;

	// 前端傳來的密碼
	private String password;

	public LoginRequest() {
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
