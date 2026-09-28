package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * specs/nth-spell/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/nth-spell
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class NthSpellControllerTest {

    private static final String ENDPOINT = "/api/algorithms/nth-spell";
    private static final String VALID_BANS = "[\"d\",\"e\",\"bb\",\"aa\",\"ae\"]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と n 番目の呪文を返す")
    void returnsOkWithSpell() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(30, VALID_BANS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spell").value("ah"));
    }

    @ParameterizedTest(name = "n={0} の場合は400を返す")
    @ValueSource(longs = {0, 1_000_000_000_000_001L})
    @DisplayName("n が範囲(1〜10^15)を外れる場合は400を返す")
    void rejectsNOutOfRange(long n) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(n, VALID_BANS)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("bans が指定されていない場合は400を返す")
    void rejectsMissingBans() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":30}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("bans が空配列の場合は400を返す")
    void rejectsEmptyBans() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(30, "[]")))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "bans の要素 {0} の場合は400を返す")
    @ValueSource(strings = {"\"\"", "\"A\"", "\"aaaaaaaaaaaa\"", "null"})
    @DisplayName("bans の要素が空文字・英小文字以外・12文字以上・null の場合は400を返す")
    void rejectsInvalidBanElement(String invalidElement) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(30, "[\"b\"," + invalidElement + "]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("bans に重複がある場合は400を返す")
    void rejectsDuplicateBans() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(30, "[\"a\",\"b\",\"a\"]")))
                .andExpect(status().isBadRequest());
    }

    private static String body(long n, String bans) {
        return "{\"n\":" + n + ",\"bans\":" + bans + "}";
    }
}
