
package com.example.quiz_1150720.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quiz_1150720.eneity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

	@Query(value = "SELECT * FROM quiz ORDER BY id DESC", nativeQuery = true)
	List<Quiz> findAllQuizzesNative();

	@Query(value = "SELECT * FROM quiz WHERE id = :id", nativeQuery = true)
	Quiz findQuizByIdNative(@Param("id") Long id);

	// 依照建立者帳號，查詢使用者名稱
	@Query(value = "SELECT user_name FROM account WHERE account = :account", nativeQuery = true)
	String findUserNameByAccount(@Param("account") String account);
}
