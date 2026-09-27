package com.example.algospeclab.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.jayway.jsonpath.JsonPath;

/**
 * specs/number-baseball/README.md の「2.3 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/number-baseball
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class NumberBaseballControllerTest {

    private static final String ENDPOINT = "/api/algorithms/number-baseball";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と暗証番号・n以内の提出回数・提出回数と同じ件数の履歴を返す")
    void returnsOkWithAnswerAndHistory() throws Exception {
        String json = mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(6, 1357)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(1357))
                .andReturn().getResponse().getContentAsString();

        int submitCount = JsonPath.read(json, "$.submitCount");
        List<Object> history = JsonPath.read(json, "$.history");
        assertThat(submitCount).isBetween(1, 6);
        assertThat(history).hasSize(submitCount);
    }

    @ParameterizedTest(name = "n={0} の場合は400を返す")
    @ValueSource(ints = {5, 3025})
    @DisplayName("n が範囲(6〜3024)を外れる場合は400を返す")
    void rejectsNOutOfRange(int n) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(n, 1357)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "secret={0} の場合は400を返す")
    @ValueSource(ints = {999, 10000})
    @DisplayName("secret が範囲(1000〜9999)を外れる場合は400を返す")
    void rejectsSecretOutOfRange(int secret) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(6, secret)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "secret={0} の場合は400を返す")
    @ValueSource(ints = {1023, 1123})
    @DisplayName("secret が 1〜9 の互いに異なる4桁でない場合は400を返す")
    void rejectsInvalidSecretDigits(int secret) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(6, secret)))
                .andExpect(status().isBadRequest());
    }

    private static String body(int n, int secret) {
        return "{\"n\":" + n + ",\"secret\":" + secret + "}";
    }
}
