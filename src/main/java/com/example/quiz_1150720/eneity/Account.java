
package com.example.quiz_1150720.eneity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account")
public class Account {

	// 帳號是資料表的主鍵
	@Id
	@Column(name = "account", length = 50)
	private String account;

	// 密碼
	@Column(name = "password", nullable = false, length = 255)
	private String password;

	// 使用者名稱
	@Column(name = "user_name", nullable = false, length = 50)
	private String userName;

	// 權限：1 = 管理員，0 = 一般使用者
	@Column(name = "permissions")
	private Integer permissions;

	// 帳號狀態
	@Column(name = "status")
	private Integer status;

	// JPA 需要的無參數建構子
	public Account() {
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getPermissions() {
		return permissions;
	}

	public void setPermissions(Integer permissions) {
		this.permissions = permissions;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
