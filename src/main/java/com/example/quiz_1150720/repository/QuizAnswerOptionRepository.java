package com.example.quiz_1150720.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quiz_1150720.eneity.QuizAnswerOption;
import com.example.quiz_1150720.eneity.QuizAnswerOptionId;

public interface QuizAnswerOptionRepository
        extends JpaRepository<QuizAnswerOption, QuizAnswerOptionId> {

    List<QuizAnswerOption> findByAnswer_Id(Long answerId);

    // 一次查詢多筆答案所選的所有選項
    List<QuizAnswerOption> findByAnswer_IdIn(Collection<Long> answerIds);
    void deleteByAnswer_IdIn(Collection<Long> answerIds);
}