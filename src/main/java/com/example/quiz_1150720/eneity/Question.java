package com.example.quiz_1150720.eneity;

import java.util.List;
import com.example.quiz_1150720.enums.QuestionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor // JPA 強制需要無參建構子
@AllArgsConstructor // 方便你在程式碼中 new 物件
@Entity
@Table(name = "question")
public class Question {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column(nullable = false)
   private Integer questionNo;
   @Column(nullable = false)
   private String title;
   /*
    * 【@Enumerated(EnumType.STRING) 說明】：
    * - 用來告訴 JPA 如何把 Java 的 Enum 對應到資料庫欄位。
    * - 使用 EnumType.STRING 代表「將 Enum 的文字名稱（例如 'SINGLE'、'MULTIPLE'、'TEXT'）直接存入資料庫」，
    *   而不是預設的數字順序（0, 1, 2，即EnumType.ORDINAL）。
    * - 好處：資料庫中的資料直觀易讀，且未來如果調整 Enum 的順序或新增項目時，不會導致舊資料對應錯亂。
    */
   @Enumerated(EnumType.STRING)
   @Column(nullable = false, length = 20)
   private QuestionType type;
   @Column(nullable = false)
   private Boolean isRequired = false;
   /*
    * 【FetchType.LAZY (延遲載入) 與 FetchType.EAGER (立即載入) 的對比與說明】：
    *
    * 1. 什麼是 LAZY (延遲載入)？
    *    - 查詢問題時，預先「不會」把關聯的問卷詳細資料撈出來，只有當你在程式中主動呼叫 `question.getQuiz()` 時，
    *      它才會額外發送 SQL 去查詢。
    *    - 好處：省去不必要的效能與記憶體開銷。
    *    - 實際舉例：後台要列出所有「問題列表」時，我們只需要問題本身的標題，不需要知道整張問卷的說明與起訖時間，
    *      此時用 LAZY 就可以避免每次查問題都附帶把龐大的問卷內容一起載入。
    *
    * 2. 相反的概念：EAGER (立即載入 / 急切載入)
    *    - 只要你查詢問題，框架就會立刻透過 `JOIN` 把整張問卷強制一口氣撈出來。
    *    - 缺點：如果不需要問卷資訊時，會造成嚴重的資源浪費。
    *
    * - @JoinColumn：指定資料庫中的外來鍵欄位名稱為 quiz_id，且不可為空。
    */
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "quiz_id", nullable = false)
   private Quiz quiz;
   @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<QuestionOption> options;
}

