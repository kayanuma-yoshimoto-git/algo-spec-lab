package com.example.algospeclab.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.web.GlobalExceptionHandler;

/**
 * specs/ancient-base/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/ancient-base
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class AncientBaseControllerTest {

    private static final String ENDPOINT = "/api/algorithms/ancient-base";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と埋めた式の一覧を返す")
    void returnsOkWithResults() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[\"14 + 3 = 17\",\"13 - 6 = X\",\"51 - 5 = 44\"]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", contains("13 - 6 = 5")));
    }

    @Test
    @DisplayName("expressions が未指定の場合は400を返す")
    void rejectsMissingExpressions() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("expressions の要素数が1の場合は400を返す")
    void rejectsTooFewExpressions() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[\"1 + 1 = X\"]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("expressions の要素数が101の場合は400を返す")
    void rejectsTooManyExpressions() throws Exception {
        String expressions = "[" + String.join(",", Collections.nCopies(101, "\"1 + 1 = X\"")) + "]";

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(expressions)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "要素 {0} の場合は400を返す")
    @ValueSource(strings = {"\"1+1 = X\"", "\"1 * 1 = X\"", "\"123 + 1 = X\"", "\"1 + 1 = x\"", "null"})
    @DisplayName("要素の形式が不正、または null の場合は400を返す")
    void rejectsMalformedExpression(String invalid) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[\"1 + 1 = X\"," + invalid + "]")))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "expressions={0} の場合は400を返す")
    @ValueSource(strings = {
            "[\"1 + 1 = 2\",\"2 + 2 = 4\"]",
            "[\"1 + 1 = 2\",\"1 + 1 = 10\",\"1 + 1 = X\"]",
            "[\"1 - 2 = X\",\"1 + 1 = 2\"]"})
    @DisplayName("結果が消えた式が無い・候補の進法が無い・結果が負の場合は400を返す")
    void rejectsCrossExpressionViolations(String expressions) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(expressions)))
                .andExpect(status().isBadRequest());
    }

    private static String body(String expressions) {
        return "{\"expressions\":" + expressions + "}";
    }
}
