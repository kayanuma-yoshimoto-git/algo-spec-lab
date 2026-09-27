package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.web.GlobalExceptionHandler;

/**
 * specs/yellow-light-sync/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/yellow-light-sync
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class YellowLightSyncControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200とアルゴリズム関数と同じtimeを返す")
    void returnsOkWithExpectedTime() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[2,1,2],[5,1,1]]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value(13));
    }

    @Test
    @DisplayName("信号機が全て黄色にならない場合、200とtime=-1を返す")
    void returnsOkWithMinusOneWhenNeverAllYellow() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[1,1,4],[2,1,3],[3,1,2],[4,1,1]]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value(-1));
    }

    @Test
    @DisplayName("signalsの要素数が下限(2)未満の場合は400を返す")
    void rejectsTooFewSignals() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[2,1,2]]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("signalsの要素数が上限(5)を超える場合は400を返す")
    void rejectsTooManySignals() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[1,1,1],[1,1,1],[1,1,1],[1,1,1],[1,1,1],[1,1,1]]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("signals[i]の要素数が3でない場合は400を返す")
    void rejectsWrongElementLength() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[2,1],[5,1,1]]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("G・Y・Rが値域(1〜18)を外れる場合は400を返す")
    void rejectsDurationOutOfRange() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[0,1,2],[5,1,1]]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("G+Y+Rの合計が上限(20)を超える場合は400を返す")
    void rejectsSumOutOfRange() throws Exception {
        mockMvc.perform(post("/api/algorithms/yellow-light-sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signals\":[[18,18,18],[5,1,1]]}"))
                .andExpect(status().isBadRequest());
    }
}
