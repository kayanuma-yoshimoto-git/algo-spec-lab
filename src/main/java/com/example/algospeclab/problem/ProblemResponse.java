package com.example.algospeclab.problem;

import java.time.OffsetDateTime;

/**
 * 課題マスタ 1 件分のレスポンス。
 *
 * @param id 課題 ID
 * @param slug 課題の slug（specs/&lt;slug&gt; のディレクトリ名、API パスの末尾）
 * @param title 課題名（設計書の見出し 1）
 * @param description 課題説明（設計書 README.md の Markdown 全文）
 * @param arguments 引数（設計書のリクエスト DTO の引数リスト）
 * @param createdAt 登録日時
 * @param updatedAt 更新日時
 */
public record ProblemResponse(
        Long id,
        String slug,
        String title,
        String description,
        String arguments,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static ProblemResponse from(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getSlug(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getArguments(),
                problem.getCreatedAt(),
                problem.getUpdatedAt());
    }
}
