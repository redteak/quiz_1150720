package com.example.quiz_1150720.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quiz_1150720.eneity.QuizAnswer;

public interface QuizAnswerRepository
        extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByResponse_Id(Long responseId);

    // 一次查詢多筆提交紀錄的所有答案
    List<QuizAnswer> findByResponse_IdIn(Collection<Long> responseIds);
    void deleteByResponse_Id(Long responseId);
}