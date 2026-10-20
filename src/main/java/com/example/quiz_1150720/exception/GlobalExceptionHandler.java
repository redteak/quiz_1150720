package com.example.quiz_1150720.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.example.quiz_1150720.dto.ApiResponse;

/**
 * 1. @RestControllerAdvice 包含了 @ControllerAdvice 和 @ResponseBody <br>
 * 1.1. @ControllerAdvice: 使用於 class 的註釋，用來定義「全域」的例外處理策略<br>
 * 1.2. @ResponseBody: 這是一個告訴 Spring 每次 response 都需要將返回的物件序列化為 JSON 或 XML 格式，
 * 然後放入 HTTP 回應的內容中<br>
 * 2. @ExceptionHandler: 使用於 method 中的註釋，提供了在 controller 處理例外的功能<br>
 * 3. ResponseEntity: 可以做為 controller 的返回值
 */
@RestControllerAdvice // 用來表示這是一個全域的 REST 例外處理器
public class GlobalExceptionHandler {
	/*
	 * 在 Java 的 @ExceptionHandler 中，優先級（匹配精確度）非常重要。更具體（Specific）的例外類別 （如
	 * HttpMessageNotReadableException）必須放在越上面，才不會被範圍較大的 RuntimeException 或
	 * Exception 給攔截走。
	 */
	// 1. 攔截 JSON 格式錯誤或型態轉換失敗（例如字串轉整數失敗）
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("請求的 JSON 格式錯誤或資料型態不符"));
	}

	// 1-1. 攔截 Query Param / Path Variable 型態轉換失敗
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		String fieldName = e.getName();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.failure("參數 [" + fieldName + "] 的資料格式不正確"));
	}

	// 2. 攔截業務邏輯例外 (例如：結束日期早於開始日期、選擇題無選項)
	@ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
	public ResponseEntity<ApiResponse> handleRuntimeException(Exception e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(e.getMessage()));
	}

	// 3. 攔截 @Valid 驗證失敗的例外 (如：標題為空)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException e) {
		String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(defaultMessage));
	}

	// 4. 攔截其他未知的系統例外
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleGeneralException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.failure("Internal Server Error: " + e.getMessage()));
	}
}
