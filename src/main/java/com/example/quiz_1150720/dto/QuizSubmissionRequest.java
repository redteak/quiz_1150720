package com.example.quiz_1150720.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizSubmissionRequest {

    @NotEmpty(message = "回答內容不可為空")
    @Valid
    private List<AnswerRequest> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AnswerRequest {

        @NotNull(message = "題目 ID 不可為空")
        private Long questionId;

        // 簡答題使用
        private String answerText;

        // 單選題、多選題使用
        private List<Long> optionIds;
    }
}