package com.example.algospeclab.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpecDocumentTest {

    private static final String MARKDOWN = """
            # サーバー増設回数の最小化

            ## 2. 入出力の定義

            ### 2.2 REST API(controller 層)

            - リクエスト DTO: `ServerScaleOutRequest(List<Integer> players, int m, int k)`
            - レスポンス DTO: `ServerScaleOutResponse(int additions)`
            """;

    @Test
    @DisplayName("見出し 1 を課題名、リクエスト DTO の括弧内を引数、全文を課題説明として取り出す")
    void parsesTitleArgumentsAndDescription() {
        SpecDocument spec = SpecDocument.parse(MARKDOWN);

        assertThat(spec.title()).isEqualTo("サーバー増設回数の最小化");
        assertThat(spec.arguments()).isEqualTo("List<Integer> players, int m, int k");
        assertThat(spec.description()).isEqualTo(MARKDOWN);
    }

    @Test
    @DisplayName("見出し 1 が無い場合、IllegalArgumentException を送出する")
    void throwsWhenTitleMissing() {
        assertThatThrownBy(() -> SpecDocument.parse("## 見出し 2 のみ\n- リクエスト DTO: `XRequest(int n)`\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("リクエスト DTO 行が無い場合、IllegalArgumentException を送出する")
    void throwsWhenRequestDtoMissing() {
        assertThatThrownBy(() -> SpecDocument.parse("# 課題名\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("課題名がちょうど200文字の場合は取り出せる")
    void acceptsTitleAtMaxLength() {
        String title = "あ".repeat(SpecDocument.MAX_TITLE_LENGTH);

        SpecDocument spec = SpecDocument.parse("# " + title + "\n- リクエスト DTO: `XRequest(int n)`\n");

        assertThat(spec.title()).isEqualTo(title);
    }

    @Test
    @DisplayName("課題名が201文字の場合、problem.title 列に収まらないため IllegalArgumentException を送出する")
    void throwsWhenTitleTooLong() {
        String title = "あ".repeat(SpecDocument.MAX_TITLE_LENGTH + 1);

        assertThatThrownBy(() -> SpecDocument.parse("# " + title + "\n- リクエスト DTO: `XRequest(int n)`\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
