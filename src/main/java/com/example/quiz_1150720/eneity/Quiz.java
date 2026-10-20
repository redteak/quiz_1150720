
package com.example.quiz_1150720.eneity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz")
public class Quiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private Boolean isPublished = false;

	// 問卷圖示，對應 quiz.icon
	@Column(name = "icon")
	private String icon;

	// 填寫所需時間，對應 quiz.answer_time
	@Column(name = "answer_time")
	private Integer answerTime;
	
	// 是否允許同一帳號重複填寫
	// false：只能填一次
	// true：允許重複填寫
	@Column(name = "allow_multiple_submissions", nullable = false)
	private Boolean allowMultipleSubmissions = false;
	
	// 建立者帳號，對應 quiz.survey_creator_id
	// 此處只存帳號值，不建立 Account Entity 關聯
	@Column(name = "survey_creator_id")
	private String surveyCreatorId;

	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions;
}
