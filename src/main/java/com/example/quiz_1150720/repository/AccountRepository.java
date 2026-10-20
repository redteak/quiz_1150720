
package com.example.quiz_1150720.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.quiz_1150720.eneity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

	// 根據帳號查詢使用者
	Account findByAccount(String account);
}
