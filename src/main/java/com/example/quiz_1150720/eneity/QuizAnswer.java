
package com.example.quiz_1150720.eneity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "quiz_answer",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_response_question",
            columnNames = {"response_id", "question_id"}
        )
    }
)
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 屬於哪一次問卷提交
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private QuizResponseRecord response;

    // 回答的是哪一道題目
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 文字題的回答；選擇題可以為 null
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
}
