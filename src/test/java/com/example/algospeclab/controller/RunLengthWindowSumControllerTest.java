package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.web.GlobalExceptionHandler;

/**
 * specs/run-length-window-sum/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/run-length-window-sum
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class RunLengthWindowSumControllerTest {

    private static final String ENDPOINT = "/api/algorithms/run-length-window-sum";
    private static final String VALID_ARR = "[3,2,3,1,1]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200とアルゴリズム関数と同じ k, c を返す")
    void returnsOkWithExpectedResult() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_ARR, 5, 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.k").value(8))
                .andExpect(jsonPath("$.c").value(2));
    }

    @Test
    @DisplayName("arr が指定されていない場合は400を返す")
    void rejectsMissingArr() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"l\":1,\"r\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("arr が空配列の場合は400を返す")
    void rejectsEmptyArr() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[]", 1, 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("arr の要素数が100000を超える場合は400を返す")
    void rejectsTooLongArr() throws Exception {
        String arr = "[" + String.join(",", Collections.nCopies(100_001, "1")) + "]";

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(arr, 1, 1)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "arr[0]={0} の場合は400を返す")
    @ValueSource(strings = {"0", "-1", "100001", "null"})
    @DisplayName("arr[i] が範囲(1〜100000)を外れる、または null の場合は400を返す")
    void rejectsArrValueOutOfRange(String invalidValue) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[" + invalidValue + ",2,3,1,1]", 1, 1)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "l={0}, r={1} の場合は400を返す")
    @CsvSource({"0, 3", "-1, 3", "1, 0", "1, -1"})
    @DisplayName("l または r が0以下の場合は400を返す")
    void rejectsNonPositiveBounds(long l, long r) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_ARR, l, r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("l > r の場合は400を返す")
    void rejectsLeftGreaterThanRight() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_ARR, 5, 4)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("r が brr の長さ(arr の総和)を超える場合は400を返す")
    void rejectsRightBoundBeyondLength() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_ARR, 1, 11)))
                .andExpect(status().isBadRequest());
    }

    private static String body(String arr, long l, long r) {
        return "{\"arr\":" + arr + ",\"l\":" + l + ",\"r\":" + r + "}";
    }
}
