
package com.example.quiz_1150720.dto;

import java.time.LocalDate;
import java.util.List;
import com.example.quiz_1150720.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
	// 建立者帳號，用來區分每個使用者建立的問卷
	private String surveyCreatorId;
	private Long id;
	private String title;
	private String description;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean isPublished;
	private Boolean allowMultipleSubmissions;
	// 新增欄位
	private String icon;
	private Integer answerTime;
	private String surveyCreator;
	// 問卷編號
	private Long quizId;
	// 總回答人數
	private long totalResponses;
	private List<QuestionResponse> questions;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuestionResponse {
		private Long id;
		private Integer questionNo;
		private String title;
		private QuestionType type;
		private Boolean isRequired;
		private List<QuestionOptionResponse> options;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuestionOptionResponse {
		private Long id;
		private Integer optionNo;
		private String optionText;
	}
}
