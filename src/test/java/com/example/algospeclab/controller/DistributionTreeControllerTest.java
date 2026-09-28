package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.web.GlobalExceptionHandler;

/**
 * specs/distribution-tree/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/distribution-tree
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class DistributionTreeControllerTest {

    private static final String ENDPOINT = "/api/algorithms/distribution-tree";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200とリーフノード数の最大値を返す")
    void returnsOkWithMaximumLeaves() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(3, 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaves").value(6));
    }

    @ParameterizedTest(name = "distLimit={0}, splitLimit={1} の場合は400を返す")
    @CsvSource({"-1, 6", "1000000001, 6", "3, 0", "3, 1000000001"})
    @DisplayName("distLimit(0〜10^9)または splitLimit(1〜10^9)が範囲外の場合は400を返す")
    void rejectsOutOfRange(long distLimit, long splitLimit) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(distLimit, splitLimit)))
                .andExpect(status().isBadRequest());
    }

    private static String body(long distLimit, long splitLimit) {
        return "{\"distLimit\":" + distLimit + ",\"splitLimit\":" + splitLimit + "}";
    }
}
