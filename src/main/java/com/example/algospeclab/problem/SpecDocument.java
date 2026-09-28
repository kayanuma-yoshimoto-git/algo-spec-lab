package com.example.algospeclab.problem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 設計書 (specs/&lt;slug&gt;/README.md) から課題マスタに登録する項目を取り出した結果。
 *
 * @param title 課題名（最初の見出し 1）
 * @param description 課題説明（Markdown 全文）
 * @param arguments 引数（「リクエスト DTO」行の括弧内。例: {@code List<Integer> players, int m, int k}）
 */
public record SpecDocument(String title, String description, String arguments) {

    /** problem.title 列（VARCHAR(200)）の長さ上限。 */
    static final int MAX_TITLE_LENGTH = 200;

    private static final Pattern TITLE =Pattern.compile("^# (.+?)\\s*$", Pattern.MULTILINE);
    private static final Pattern REQUEST_DTO =
            Pattern.compile("^- リクエスト DTO: `\\w+\\((.*)\\)`", Pattern.MULTILINE);

    /**
     * 設計書の Markdown を解析する。
     *
     * @throws IllegalArgumentException 見出し 1 または「リクエスト DTO」行が見つからない場合、
     *     または課題名が {@value #MAX_TITLE_LENGTH} 文字を超える場合
     */
    public static SpecDocument parse(String markdown) {
        String title = find(TITLE, markdown, "見出し 1（# 課題名）");
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "課題名（見出し 1）は " + MAX_TITLE_LENGTH + " 文字以内にしてください。");
        }
        String arguments = find(REQUEST_DTO, markdown, "「- リクエスト DTO: `XxxRequest(...)`」行");
        return new SpecDocument(title, markdown, arguments);
    }

    private static String find(Pattern pattern, String markdown, String label) {
        Matcher matcher = pattern.matcher(markdown);
        if (!matcher.find()) {
            throw new IllegalArgumentException("設計書に " + label + " がありません。");
        }
        return matcher.group(1).strip();
    }
}
