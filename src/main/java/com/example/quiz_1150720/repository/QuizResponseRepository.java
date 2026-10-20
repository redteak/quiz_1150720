package com.example.quiz_1150720.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quiz_1150720.eneity.QuizResponseRecord;

public interface QuizResponseRepository
        extends JpaRepository<QuizResponseRecord, Long> {

    boolean existsByQuiz_IdAndAccountId(Long quizId, String accountId);

    long countByQuiz_Id(Long quizId);

    List<QuizResponseRecord> findByQuiz_Id(Long quizId);
    Optional<QuizResponseRecord> findByQuiz_IdAndAccountId(
    	    Long quizId,
    	    String accountId
    	);
}