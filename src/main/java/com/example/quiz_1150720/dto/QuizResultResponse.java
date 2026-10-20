package com.example.quiz_1150720.dto;

import java.util.List;

import com.example.quiz_1150720.enums.QuestionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizResultResponse {

    private Long quizId;
    private String title;
    private long totalResponses;
    private List<QuestionResult> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class QuestionResult {

        private Long questionId;
        private Integer questionNo;
        private String title;
        private QuestionType type;
        private long answerCount;

        private List<OptionResult> options;

        // 簡答題的回答內容
        private List<String> textAnswers;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OptionResult {

        private Long optionId;
        private String optionText;
        private long count;
        private double percentage;
    }
}