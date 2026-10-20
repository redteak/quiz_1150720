package com.example.quiz_1150720.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.quiz_1150720.constant.QuizResponseMessage;
import com.example.quiz_1150720.dto.QuizRequest;
import com.example.quiz_1150720.dto.QuizResponse;
import com.example.quiz_1150720.dto.QuizResultResponse;
import com.example.quiz_1150720.dto.QuizSubmissionRequest;
import com.example.quiz_1150720.eneity.Question;
import com.example.quiz_1150720.eneity.QuestionOption;
import com.example.quiz_1150720.eneity.Quiz;
import com.example.quiz_1150720.eneity.QuizAnswer;
import com.example.quiz_1150720.eneity.QuizAnswerOption;
import com.example.quiz_1150720.eneity.QuizResponseRecord;
import com.example.quiz_1150720.enums.QuestionType;
import com.example.quiz_1150720.repository.QuizAnswerOptionRepository;
import com.example.quiz_1150720.repository.QuizAnswerRepository;
import com.example.quiz_1150720.repository.QuizRepository;
import com.example.quiz_1150720.repository.QuizResponseRepository;

@Service
public class QuizService {
	// 使用 final 宣告，透過建構方法注入 (Constructor Injection)，確保依賴不可變且便於單元測試
	private final QuizRepository quizRepository;
	private final QuizResponseRepository quizResponseRepository;
	private final QuizAnswerRepository quizAnswerRepository;
	private final QuizAnswerOptionRepository quizAnswerOptionRepository;

	public QuizService(QuizRepository quizRepository, QuizResponseRepository quizResponseRepository,
			QuizAnswerRepository quizAnswerRepository, QuizAnswerOptionRepository quizAnswerOptionRepository) {

		this.quizRepository = quizRepository;
		this.quizResponseRepository = quizResponseRepository;
		this.quizAnswerRepository = quizAnswerRepository;
		this.quizAnswerOptionRepository = quizAnswerOptionRepository;
	}

	// 驗證問卷日期
	private void validateQuizDates(LocalDate startDate, LocalDate endDate) {

		// 取得今天日期
		LocalDate today = LocalDate.now();

		// 開始日期不可早於今天
		if (startDate.isBefore(today)) {
			throw new IllegalArgumentException("開始日期不可早於今天");
		}

		// 結束日期不可早於今天
		if (endDate.isBefore(today)) {
			throw new IllegalArgumentException("結束日期不可早於今天");
		}

		// 開始日期必須早於結束日期
		if (!startDate.isBefore(endDate)) {
			throw new IllegalArgumentException("開始日期必須早於結束日期，兩個日期不可相同");
		}
	}

	/**
	 * 1. 創建問卷邏輯
	 */
	@Transactional
	public QuizResponse createQuiz(QuizRequest request, String accountId) {
		validateQuizDates(request.getStartDate(), request.getEndDate());
		validateQuizLogic(request);

		Quiz quiz = convertRequestToEntity(request);

		// 設定問卷圖示及填寫時間
		quiz.setIcon(request.getIcon());
		quiz.setAnswerTime(request.getAnswerTime());

		// 設定建立者帳號
		quiz.setSurveyCreatorId(accountId);

		Quiz savedQuiz = quizRepository.save(quiz);
		return convertEntityToResponse(savedQuiz);
	}

	/**
	 * 2. 更新問卷邏輯
	 */
	@Transactional
	public QuizResponse updateQuiz(QuizRequest request) {
		validateQuizDates(request.getStartDate(), request.getEndDate());
		if (request.getId() == null) {
			throw new RuntimeException(QuizResponseMessage.QUIZ_NOT_FOUND_FOR_UPDATE);
		}

		Quiz existingQuiz = quizRepository.findById(request.getId())
				.orElseThrow(() -> new RuntimeException(QuizResponseMessage.QUIZ_NOT_FOUND_FOR_UPDATE));

		validateQuizLogic(request);

		Quiz quiz = convertRequestToEntity(request);

		// 保留原本的建立者帳號
		quiz.setSurveyCreatorId(existingQuiz.getSurveyCreatorId());

		// 更新圖示及填寫時間
		quiz.setIcon(request.getIcon());
		quiz.setAnswerTime(request.getAnswerTime());

		Quiz updatedQuiz = quizRepository.save(quiz);

		return convertEntityToResponse(updatedQuiz);
	}

	/**
	 * 3. 取得所有問卷列表邏輯
	 */
	public List<QuizResponse> getAllQuizzes() {
		// 呼叫 Repository 中的原生 SQL 查詢所有問卷
		List<Quiz> quizzes = quizRepository.findAllQuizzesNative();

		// 將 Entity 清單透過 Stream API 轉換為 Response DTO 清單
		return quizzes.stream().map(this::convertEntityToResponse).collect(Collectors.toList());
	}

	/**
	 * 4. 根據問卷 ID 取得單筆問卷詳情邏輯
	 */
	public QuizResponse getQuizById(Long id) {
		// 呼叫 Repository 中的原生 SQL 根據 ID 查詢問卷
		Quiz quiz = quizRepository.findQuizByIdNative(id);

		// 若找不到該問卷則拋出例外
		if (quiz == null) {
			throw new RuntimeException(QuizResponseMessage.QUIZ_NOT_FOUND);
		}

		return convertEntityToResponse(quiz);
	}

	/**
	 * 【程式碼檢查邏輯】： 1. 檢查結束日期是否早於開始日期。 2. 針對不同題型（SINGLE, MULTIPLE, TEXT）檢查選項是否合規。
	 */
	private void validateQuizLogic(QuizRequest request) {
		// 檢查日期合理性：結束日期不得早於開始日期(兩個日期皆不得為 null 已透過 Validation 檢查了)
		if (request.getEndDate().isBefore(request.getStartDate())) {
			throw new RuntimeException(QuizResponseMessage.END_DATE_BEFORE_START_DATE);
		}
		// 若無問題清單則直接返回
		if (request.getQuestions() == null)
			return;
		// 迴圈檢查每個問題的細節
		for (QuizRequest.QuestionRequest qReq : request.getQuestions()) {
			QuestionType type = qReq.getType();
			// 若為「單選」或「多選」題
			if (type == QuestionType.SINGLE || type == QuestionType.MULTIPLE) {
				// 必須包含選項，且選項不可為空
				if (qReq.getOptions() == null || qReq.getOptions().isEmpty()) {
					throw new RuntimeException(
							String.format(QuizResponseMessage.OPTION_REQUIRED_FOR_CHOICE, qReq.getTitle()));
				}
				// 檢查每個選項的編號與內容是否完整
				for (QuizRequest.QuestionOptionRequest oReq : qReq.getOptions()) {
					if (oReq.getOptionNo() == null) {
						throw new RuntimeException(
								String.format(QuizResponseMessage.OPTION_NO_NOT_NULL_MSG, qReq.getTitle()));
					}
					// StringUtils.hasText(): 判斷不是 1.null 或 2.空字串 或 3.全空白字串 就會返回 true
					if (!StringUtils.hasText(oReq.getOptionText())) {
						throw new RuntimeException(
								String.format(QuizResponseMessage.OPTION_TEXT_NOT_BLANK_MSG, qReq.getTitle()));
					}
				}
			}
			// 若為「簡答」題
			else if (type == QuestionType.TEXT) {
				// 簡答題不得包含選項
				if (qReq.getOptions() != null && !qReq.getOptions().isEmpty()) {
					throw new RuntimeException(
							String.format(QuizResponseMessage.OPTION_NOT_ALLOWED_FOR_TEXT, qReq.getTitle()));
				}
			}
		}
	}

	/**
	 * 【轉換工具】：QuizRequest (DTO) ➔ Quiz (Entity)
	 *
	 * 【為什麼 DTO 轉換時需要保留並設定 id？】： 因為本系統的 QuizRequest 同時被共用在「建立 (Create)」與「更新
	 * (Update)」兩種情境中， 在轉換時必須透過 id 來讓 JPA/Hibernate 判斷該執行 INSERT 還是 UPDATE： 1. 針對
	 * Quiz 本身： - 建立新問卷時：request.getId() 為 null，JPA 視其為新物件並執行 INSERT。 -
	 * 更新問卷時：request.getId() 帶有數值，JPA 視其為既有資料並執行 UPDATE。
	 *
	 * 2. 針對 Question 與 QuestionOption（子物件）： - 既有項目：帶有原本的資料庫流水號 id，JPA 會進行欄位更新。 -
	 * 新增項目：id 為 null（使用者在修改問卷時新加的題目或選項），JPA 會自動執行 INSERT。 - 這能讓 Hibernate
	 * 精準分辨哪些是舊資料、哪些是新增加的項目，並配合 orphanRemoval 正確刪除被移除的子項目。
	 */
	private Quiz convertRequestToEntity(QuizRequest request) {
		Quiz quiz = new Quiz();
		quiz.setId(request.getId());
		quiz.setTitle(request.getTitle());
		quiz.setDescription(request.getDescription());
		quiz.setStartDate(request.getStartDate());
		quiz.setEndDate(request.getEndDate());
		quiz.setIsPublished(request.getIsPublished());
		quiz.setAllowMultipleSubmissions(
				request.getAllowMultipleSubmissions() != null ? request.getAllowMultipleSubmissions() : false);
		// 轉換問題清單
		if (request.getQuestions() != null) {
			List<Question> questions = request.getQuestions().stream().map(qReq -> {
				Question question = new Question();
				question.setId(qReq.getId());
				question.setQuestionNo(qReq.getQuestionNo());
				question.setTitle(qReq.getTitle());
				question.setType(qReq.getType());
				question.setIsRequired(qReq.getIsRequired());
				question.setQuiz(quiz); // 雙向關聯：子物件綁定父物件
				// 轉換選項清單
				if (qReq.getOptions() != null) {
					List<QuestionOption> options = qReq.getOptions().stream().map(oReq -> {
						QuestionOption option = new QuestionOption();
						option.setId(oReq.getId());
						option.setOptionNo(oReq.getOptionNo());
						option.setOptionText(oReq.getOptionText());
						option.setQuestion(question); // 雙向關聯：子物件綁定父物件
						return option;
					}).collect(Collectors.toList());
					question.setOptions(options);
				}
				return question;
			}).collect(Collectors.toList());
			quiz.setQuestions(questions);
		}
		return quiz;
	}

	/**
	 * 【轉換工具】：Quiz (Entity) ➔ QuizResponse (DTO)
	 */
	private QuizResponse convertEntityToResponse(Quiz quiz) {
		QuizResponse response = new QuizResponse();
		response.setId(quiz.getId());
		response.setTitle(quiz.getTitle());
		response.setDescription(quiz.getDescription());
		response.setStartDate(quiz.getStartDate());
		response.setEndDate(quiz.getEndDate());
		response.setIsPublished(quiz.getIsPublished());
		response.setAllowMultipleSubmissions(quiz.getAllowMultipleSubmissions());
		// 取得問卷圖示
		response.setIcon(quiz.getIcon());

		// 取得問卷填寫時間
		response.setAnswerTime(quiz.getAnswerTime());

		// 取得建立者帳號
		String creatorId = quiz.getSurveyCreatorId();
		response.setSurveyCreatorId(creatorId);
		// 依照建立者帳號，查詢使用者名稱
		if (creatorId != null && !creatorId.isBlank()) {
			response.setSurveyCreator(quizRepository.findUserNameByAccount(creatorId));
		}

		// 轉換問題回應清單
		if (quiz.getQuestions() != null) {
			List<QuizResponse.QuestionResponse> qResponses = quiz.getQuestions().stream().map(q -> {
				QuizResponse.QuestionResponse qRes = new QuizResponse.QuestionResponse();
				qRes.setId(q.getId());
				qRes.setQuestionNo(q.getQuestionNo());
				qRes.setTitle(q.getTitle());
				qRes.setType(q.getType());
				qRes.setIsRequired(q.getIsRequired());
				// 轉換選項回應清單
				if (q.getOptions() != null) {
					List<QuizResponse.QuestionOptionResponse> oResponses = q.getOptions().stream().map(o -> {
						QuizResponse.QuestionOptionResponse oRes = new QuizResponse.QuestionOptionResponse();
						oRes.setId(o.getId());
						oRes.setOptionNo(o.getOptionNo());
						oRes.setOptionText(o.getOptionText());
						return oRes;
					}).collect(Collectors.toList());
					qRes.setOptions(oResponses);
				}
				return qRes;
			}).collect(Collectors.toList());
			response.setQuestions(qResponses);
		}
		return response;
	}

	/**
	 * 5. 提交問卷回答
	 */
	@Transactional
	public Long submitQuiz(Long quizId, String accountId, QuizSubmissionRequest request) {

		Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("找不到問卷"));

		LocalDate today = LocalDate.now();

		if (!Boolean.TRUE.equals(quiz.getIsPublished())) {
			throw new RuntimeException("此問卷尚未發布");
		}

		if (today.isBefore(quiz.getStartDate()) || today.isAfter(quiz.getEndDate())) {
			throw new RuntimeException("目前不在問卷填寫期間");
		}

		// 如果不允許重複填寫，先尋找原本的填答紀錄
		QuizResponseRecord existingRecord = null;

		if (!Boolean.TRUE.equals(quiz.getAllowMultipleSubmissions())) {
			existingRecord = quizResponseRepository.findByQuiz_IdAndAccountId(quizId, accountId).orElse(null);
		}

		if (request.getAnswers() == null) {
			throw new RuntimeException("回答內容不可為空");
		}

		Map<Long, Question> questionMap = quiz.getQuestions().stream()
				.collect(Collectors.toMap(Question::getId, Function.identity()));

		Map<Long, QuizSubmissionRequest.AnswerRequest> answerMap = new java.util.HashMap<>();

		// 第一階段：檢查題目 ID、重複題目及答案格式
		for (QuizSubmissionRequest.AnswerRequest answerRequest : request.getAnswers()) {

			if (answerRequest == null || answerRequest.getQuestionId() == null) {
				throw new RuntimeException("題目 ID 不可為空");
			}

			Long questionId = answerRequest.getQuestionId();

			if (!questionMap.containsKey(questionId)) {
				throw new RuntimeException("提交的題目不屬於此問卷");
			}

			if (answerMap.putIfAbsent(questionId, answerRequest) != null) {
				throw new RuntimeException("同一道題目不可重複提交");
			}

			Question question = questionMap.get(questionId);
			String answerText = answerRequest.getAnswerText();
			List<Long> optionIds = answerRequest.getOptionIds();

			boolean hasText = StringUtils.hasText(answerText);
			boolean hasOptions = optionIds != null && !optionIds.isEmpty();

			// 不允許同一題同時傳入簡答及選項答案
			if (hasText && hasOptions) {
				throw new RuntimeException("同一道題目不可同時提交文字與選項");
			}

			if (question.getType() == QuestionType.TEXT) {
				if (hasOptions) {
					throw new RuntimeException("簡答題不可選擇選項");
				}
				if (hasText && answerText.trim().isEmpty()) {
					throw new RuntimeException("簡答內容不可為空");
				}
			} else {
				if (hasText) {
					throw new RuntimeException("選擇題不可填入簡答內容");
				}

				if (hasOptions) {
					if (new HashSet<>(optionIds).size() != optionIds.size()) {
						throw new RuntimeException("不可重複選取相同選項");
					}

					if (question.getType() == QuestionType.SINGLE && optionIds.size() != 1) {
						throw new RuntimeException("單選題只能選擇一個選項");
					}

					Set<Long> validOptionIds = question.getOptions().stream().map(QuestionOption::getId)
							.collect(Collectors.toSet());

					if (!validOptionIds.containsAll(optionIds)) {
						throw new RuntimeException("選項不屬於此題目");
					}
				}
			}
		}

		// 第二階段：檢查所有必填題
		for (Question question : quiz.getQuestions()) {
			QuizSubmissionRequest.AnswerRequest answerRequest = answerMap.get(question.getId());

			boolean answered = false;

			if (answerRequest != null) {
				if (question.getType() == QuestionType.TEXT) {
					answered = StringUtils.hasText(answerRequest.getAnswerText());
				} else {
					answered = answerRequest.getOptionIds() != null && !answerRequest.getOptionIds().isEmpty();
				}
			}

			if (Boolean.TRUE.equals(question.getIsRequired()) && !answered) {
				throw new RuntimeException("必填題尚未回答：" + question.getTitle());
			}
		}

		// 第三階段：驗證通過後，建立新紀錄或更新既有紀錄
		QuizResponseRecord responseRecord;

		if (existingRecord != null) {

			// 保留原本的填答紀錄 ID
			responseRecord = existingRecord;

			// 取得原本的答案
			List<QuizAnswer> oldAnswers = quizAnswerRepository.findByResponse_Id(responseRecord.getId());

			// 取得舊答案 ID
			List<Long> oldAnswerIds = oldAnswers.stream().map(QuizAnswer::getId).collect(Collectors.toList());

			// 先刪除舊的選項關聯，避免外鍵衝突
			if (!oldAnswerIds.isEmpty()) {
				quizAnswerOptionRepository.deleteByAnswer_IdIn(oldAnswerIds);
			}

			// 再刪除這次填答原本的答案
			quizAnswerRepository.deleteByResponse_Id(responseRecord.getId());

			// 不建立新的 QuizResponseRecord

		} else {

			// 第一次填寫，或允許重複填寫時，建立新紀錄
			responseRecord = new QuizResponseRecord();
			responseRecord.setQuiz(quiz);
			responseRecord.setAccountId(accountId);

			responseRecord = quizResponseRepository.save(responseRecord);
		}

		// 第四階段：儲存每一道有作答的題目
		for (QuizSubmissionRequest.AnswerRequest answerRequest : request.getAnswers()) {

			Question question = questionMap.get(answerRequest.getQuestionId());
			String answerText = answerRequest.getAnswerText();
			List<Long> optionIds = answerRequest.getOptionIds();

			boolean hasText = StringUtils.hasText(answerText);
			boolean hasOptions = optionIds != null && !optionIds.isEmpty();

			// 選填題未回答，不建立答案紀錄
			if (!hasText && !hasOptions) {
				continue;
			}

			QuizAnswer answer = new QuizAnswer();
			answer.setResponse(responseRecord);
			answer.setQuestion(question);
			answer.setAnswerText(question.getType() == QuestionType.TEXT ? answerText.trim() : null);

			answer = quizAnswerRepository.save(answer);

			if (question.getType() != QuestionType.TEXT) {
				for (Long optionId : optionIds) {
					QuestionOption selectedOption = question.getOptions().stream()
							.filter(option -> option.getId().equals(optionId)).findFirst()
							.orElseThrow(() -> new RuntimeException("找不到所選選項"));

					QuizAnswerOption answerOption = new QuizAnswerOption();
					answerOption.setAnswer(answer);
					answerOption.setQuestionOption(selectedOption);

					quizAnswerOptionRepository.save(answerOption);
				}
			}
		}

		return responseRecord.getId();
	}

	/**
	 * 6. 查詢問卷回答結果 不論問卷尚未開始、進行中或已結束，都可以查看目前的回答狀況
	 */
	@Transactional(readOnly = true)
	public QuizResultResponse getQuizResults(Long quizId) {

		// 1. 根據問卷 ID 查詢問卷
		Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("找不到問卷"));

		// 2. 查詢這份問卷目前所有的回答紀錄
		List<QuizResponseRecord> responses = quizResponseRepository.findByQuiz_Id(quizId);

		QuizResultResponse result = new QuizResultResponse();

		// 3. 設定問卷基本資料
		result.setQuizId(quiz.getId());
		result.setTitle(quiz.getTitle());
		result.setTotalResponses(responses.size());

		// 4. 取得所有回答紀錄的 ID
		List<Long> responseIds = responses.stream().map(QuizResponseRecord::getId).collect(Collectors.toList());

		// 5. 查詢所有回答內容
		List<QuizAnswer> allAnswers = responseIds.isEmpty() ? List.of()
				: quizAnswerRepository.findByResponse_IdIn(responseIds);

		// 6. 取得所有答案的 ID
		List<Long> answerIds = allAnswers.stream().map(QuizAnswer::getId).collect(Collectors.toList());

		// 7. 查詢所有選項答案
		List<QuizAnswerOption> allAnswerOptions = answerIds.isEmpty() ? List.of()
				: quizAnswerOptionRepository.findByAnswer_IdIn(answerIds);

		// 8. 依照題目 ID 分組答案
		Map<Long, List<QuizAnswer>> answersByQuestion = allAnswers.stream()
				.collect(Collectors.groupingBy(answer -> answer.getQuestion().getId()));

		// 9. 計算每個選項被選取的次數
		Map<Long, Long> optionCounts = allAnswerOptions.stream().collect(
				Collectors.groupingBy(answerOption -> answerOption.getQuestionOption().getId(), Collectors.counting()));

		// 10. 逐題整理統計結果
		List<QuizResultResponse.QuestionResult> questionResults = quiz.getQuestions().stream().map(question -> {

			List<QuizAnswer> questionAnswers = answersByQuestion.getOrDefault(question.getId(), List.of());

			QuizResultResponse.QuestionResult questionResult = new QuizResultResponse.QuestionResult();

			questionResult.setQuestionId(question.getId());
			questionResult.setQuestionNo(question.getQuestionNo());
			questionResult.setTitle(question.getTitle());
			questionResult.setType(question.getType());
			questionResult.setAnswerCount(questionAnswers.size());

			// 文字題：整理目前收到的文字回答
			if (question.getType() == QuestionType.TEXT) {

				questionResult.setTextAnswers(questionAnswers.stream().map(QuizAnswer::getAnswerText)
						.filter(StringUtils::hasText).collect(Collectors.toList()));

			} else {

				// 選擇題：統計每個選項的回答人次
				List<QuizResultResponse.OptionResult> optionResults = question.getOptions().stream().map(option -> {

					long count = optionCounts.getOrDefault(option.getId(), 0L);

					QuizResultResponse.OptionResult optionResult = new QuizResultResponse.OptionResult();

					optionResult.setOptionId(option.getId());
					optionResult.setOptionText(option.getOptionText());
					optionResult.setCount(count);

					// 以整份問卷的提交人數作為比例分母
					double percentage = responses.isEmpty() ? 0.0 : count * 100.0 / responses.size();

					optionResult.setPercentage(Math.round(percentage * 10.0) / 10.0);

					return optionResult;

				}).collect(Collectors.toList());

				questionResult.setOptions(optionResults);
			}

			return questionResult;

		}).collect(Collectors.toList());

		// 11. 將逐題統計結果放進回傳資料
		result.setQuestions(questionResults);

		return result;
	}

	/**
	 * 刪除問卷 管理員可以刪除所有問卷 一般使用者只能刪除自己建立的問卷
	 */
	@Transactional
	public void deleteQuiz(Long quizId, String accountId, boolean isAdmin) {

		// 查詢問卷，不存在就回報錯誤
		Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new IllegalArgumentException("找不到此問卷"));

		// 一般使用者只能刪除自己建立的問卷
		if (!isAdmin && !accountId.equals(quiz.getSurveyCreatorId())) {
			throw new IllegalArgumentException("只能刪除自己建立的問卷");
		}

		// 執行刪除
		quizRepository.delete(quiz);
	}

	// 檢查目前登入的使用者是否填寫過指定問卷
	public boolean hasAnswered(Long quizId, String accountId) {
		return quizResponseRepository.existsByQuiz_IdAndAccountId(quizId, accountId);
	}

	/**
	 * 取得目前登入者在指定問卷的回答
	 */
	@Transactional(readOnly = true)
	public QuizSubmissionRequest getMyAnswers(Long quizId, String accountId) {

		QuizResponseRecord record = quizResponseRepository.findByQuiz_IdAndAccountId(quizId, accountId)
				.orElseThrow(() -> new RuntimeException("找不到你的填答紀錄"));

		List<QuizAnswer> savedAnswers = quizAnswerRepository.findByResponse_Id(record.getId());

		List<QuizSubmissionRequest.AnswerRequest> answers = savedAnswers.stream().map(savedAnswer -> {

			QuizSubmissionRequest.AnswerRequest answer = new QuizSubmissionRequest.AnswerRequest();

			answer.setQuestionId(savedAnswer.getQuestion().getId());
			answer.setAnswerText(savedAnswer.getAnswerText());

			List<Long> optionIds = quizAnswerOptionRepository.findByAnswer_Id(savedAnswer.getId()).stream()
					.map(item -> item.getQuestionOption().getId()).collect(Collectors.toList());

			answer.setOptionIds(optionIds);

			return answer;

		}).collect(Collectors.toList());

		QuizSubmissionRequest result = new QuizSubmissionRequest();
		result.setAnswers(answers);

		return result;
	}
}
