
package com.example.quiz_1150720.dto;

public class LoginResponse {

	private boolean success;
	private String message;
	private String account;
	private String userName;
	private Integer permissions;
	private String token;

	public LoginResponse() {
	}

	// 保留原本的建構子，避免影響既有程式
	public LoginResponse(boolean success, String message, String account, String userName, Integer permissions) {
		this(success, message, account, userName, permissions, null);
	}

	// 新增可以回傳 Token 的建構子
	public LoginResponse(boolean success, String message, String account, String userName, Integer permissions,
			String token) {
		this.success = success;
		this.message = message;
		this.account = account;
		this.userName = userName;
		this.permissions = permissions;
		this.token = token;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getAccount() {
		return account;
	}

	public String getUserName() {
		return userName;
	}

	public Integer getPermissions() {
		return permissions;
	}

	public String getToken() {
		return token;
	}
}
	