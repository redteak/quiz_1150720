package com.example.quiz_1150720.eneity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuizAnswerOptionId implements Serializable {

    // 序列化版本編號，避免 IDE 提示警告
    private static final long serialVersionUID = 1L;

    private Long answer;
    private Long questionOption;
}