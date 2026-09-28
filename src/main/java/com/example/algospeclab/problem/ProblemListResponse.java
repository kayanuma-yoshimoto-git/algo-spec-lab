package com.example.algospeclab.problem;

import java.util.List;

/** 課題マスタ一覧のレスポンス。 */
public record ProblemListResponse(List<ProblemResponse> problems) {
}
