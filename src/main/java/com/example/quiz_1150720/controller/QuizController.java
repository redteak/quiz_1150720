package com.example.quiz_1150720.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quiz_1150720.constant.QuizResponseMessage;
import com.example.quiz_1150720.dto.ApiResponse;
import com.example.quiz_1150720.dto.QuizRequest;
import com.example.quiz_1150720.dto.QuizResponse;
import com.example.quiz_1150720.dto.QuizResultResponse;
import com.example.quiz_1150720.dto.QuizSubmissionRequest;
import com.example.quiz_1150720.service.QuizService;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api/quiz")
public class QuizController {
	// 使用 final 宣告，並透過建構方法注入 (不再使用 @Autowired)
	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	// 1. 創建問卷 -> POST
	@PostMapping("/create")
	public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@Valid @RequestBody QuizRequest request,
			Authentication authentication) {

		String accountId = authentication.getName();

		QuizResponse response = quizService.createQuiz(request, accountId);

		return ResponseEntity.ok(ApiResponse.success(QuizResponseMessage.CREATE_SUCCESS, response));
	}

	// 2. 更新問卷 -> POST
	@PostMapping("/update")
	public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(@Valid @RequestBody QuizRequest request) {
		QuizResponse response = quizService.updateQuiz(request);
		return ResponseEntity.ok(ApiResponse.success(QuizResponseMessage.UPDATE_SUCCESS, response));
	}

	// 3. 取得問卷列表 -> GET
	@GetMapping("/list")
	public ResponseEntity<ApiResponse<List<QuizResponse>>> getAllQuizzes() {
		List<QuizResponse> responses = quizService.getAllQuizzes();
		return ResponseEntity.ok(ApiResponse.success(QuizResponseMessage.GET_LIST_SUCCESS, responses));
	}

	// 4. 根據問卷 id 取得該問卷的問題以及選項 -> GET (透過路徑變數傳遞 id)
	@GetMapping("/detail/{id}")
	public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(@PathVariable("id") Long id) {
		QuizResponse response = quizService.getQuizById(id);
		return ResponseEntity.ok(ApiResponse.success(QuizResponseMessage.GET_DETAIL_SUCCESS, response));
	}

	/**
	 * 提交問卷回答 POST /api/quiz/{quizId}/submit
	 */
	@PostMapping("/{quizId}/submit")
	public ResponseEntity<ApiResponse<Long>> submitQuiz(@PathVariable("quizId") Long quizId,
			@Valid @RequestBody QuizSubmissionRequest request, Authentication authentication) {
		String accountId = authentication.getName();

		Long responseId = quizService.submitQuiz(quizId, accountId, request);

		return ResponseEntity.ok(ApiResponse.success("問卷提交成功", responseId));
	}

	/**
	 * 查詢問卷統計結果 GET /api/quiz/{quizId}/results
	 */
	@GetMapping("/{quizId}/results")
	public ResponseEntity<ApiResponse<QuizResultResponse>> getQuizResults(@PathVariable("quizId") Long quizId) {

		QuizResultResponse result = quizService.getQuizResults(quizId);

		return ResponseEntity.ok(ApiResponse.success("取得問卷結果成功", result));
	}

	// 刪除問卷
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable("id") Long id, Authentication authentication) {

		String accountId = authentication.getName();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		quizService.deleteQuiz(id, accountId, isAdmin);

		return ResponseEntity.ok(ApiResponse.success("問卷刪除成功", null));
	}

	// 查詢目前登入的使用者是否填寫過問卷
	@GetMapping("/{quizId}/answered")
	public ResponseEntity<ApiResponse<Boolean>> hasAnswered(@PathVariable("quizId") Long quizId,
			Authentication authentication) {

		String accountId = authentication.getName();

		boolean answered = quizService.hasAnswered(quizId, accountId);

		return ResponseEntity.ok(ApiResponse.success("查詢成功", answered));
	}

	/**
	 * 查詢目前登入者的問卷回答 GET /api/quiz/{quizId}/my-answers
	 */
	@GetMapping("/{quizId}/my-answers")
	public ResponseEntity<ApiResponse<QuizSubmissionRequest>> getMyAnswers(@PathVariable("quizId") Long quizId,
			Authentication authentication) {

		String accountId = authentication.getName();

		QuizSubmissionRequest answers = quizService.getMyAnswers(quizId, accountId);

		return ResponseEntity.ok(ApiResponse.success("取得填答紀錄成功", answers));
	}
}
