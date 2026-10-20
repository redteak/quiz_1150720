package com.example.quiz_1150720.dto;

import java.time.LocalDate;
import java.util.List;
import com.example.quiz_1150720.constant.QuizResponseMessage;
import com.example.quiz_1150720.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
	private Long id;
	@NotBlank(message = QuizResponseMessage.TITLE_NOT_BLANK)
	private String title;
	private String description;
	@NotNull(message = QuizResponseMessage.START_DATE_NOT_NULL)
	private LocalDate startDate;
	@NotNull(message = QuizResponseMessage.END_DATE_NOT_NULL)
	private LocalDate endDate;
	private Boolean isPublished = false;
	// 問卷圖示
	private String icon;

	// 預估填寫時間（分鐘）
	private Integer answerTime;
	// 是否允許同一帳號重複填寫
	// false：只能填一次
	// true：允許重複填寫
	private Boolean allowMultipleSubmissions = false;
	@NotEmpty(message = QuizResponseMessage.QUESTIONS_NOT_EMPTY)
	@Valid
	private List<QuestionRequest> questions;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuestionRequest {
		private Long id;
		@NotNull(message = QuizResponseMessage.Q_NO_NOT_NULL)
		private Integer questionNo;
		@NotBlank(message = QuizResponseMessage.Q_TITLE_NOT_BLANK)
		private String title;
		/*
		 * 【關於 type 欄位的型別防禦與驗證機制說明】： 1. JSON 反序列化階段（Jackson 的第一道防線）： - 因為 type 的型別是
		 * QuestionType (Enum)，當前端傳遞不合法的字串（例如 `"UNKNOWN"`）或錯誤的資料型態時， Jackson 在解析 JSON 轉成
		 * Java 物件的過程中就會直接失敗，並拋出 HttpMessageNotReadableException。 - 該例外會被
		 * GlobalExceptionHandler 攔截，回傳「請求的 JSON 格式錯誤或資料型態不符」。
		 *
		 * 2. @NotNull 驗證階段（第二道防線）： - 配合下方的 @NotNull 標註，若前端傳入 null 或漏傳該欄位，則會被 @Valid 攔截，
		 * 並回傳 QuizResponseMessage.Q_TYPE_NOT_NULL（即「問題類型不得為空」）。
		 *
		 * 結論： 透過上述兩道防線，當資料順利進入 Service 邏輯層時，type 保證會是 QuestionType Enum 中定義的合法值，
		 * 不需要再額外寫程式碼去檢查它是否超出範圍。
		 */
		@NotNull(message = QuizResponseMessage.Q_TYPE_NOT_NULL)
		private QuestionType type;
		private Boolean isRequired = false;
		private List<QuestionOptionRequest> options;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuestionOptionRequest {
		private Long id;
		private Integer optionNo;
		private String optionText;
	}
}
