package com.example.quiz_1150720.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionType {

    SINGLE("radio"),
    MULTIPLE("checkbox"),
    TEXT("text");

    private final String code;

    QuestionType(String code) {
        this.code = code;
    }

    // 後端 Enum 轉成 JSON 時，回傳前端代號
    @JsonValue
    public String getCode() {
        return code;
    }

    // 接受前端代號或 Enum 名稱
    @JsonCreator
    public static QuestionType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (QuestionType type : QuestionType.values()) {
            if (
                type.code.equalsIgnoreCase(code) ||
                type.name().equalsIgnoreCase(code)
            ) {
                return type;
            }
        }

        throw new IllegalArgumentException(
            "不支援的問題類型：" + code
        );
    }
}