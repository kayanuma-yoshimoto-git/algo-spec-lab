package com.example.algospeclab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * specs/server-scale-out/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/server-scale-out
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class ServerScaleOutControllerTest {

    private static final String VALID_PLAYERS =
            "[0,2,3,3,1,2,0,0,0,0,4,2,0,6,0,4,2,13,3,5,10,0,1,5]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200とアルゴリズム関数と同じadditionsを返す")
    void returnsOkWithExpectedAdditions() throws Exception {
        mockMvc.perform(post("/api/algorithms/server-scale-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\":" + VALID_PLAYERS + ",\"m\":3,\"k\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.additions").value(7));
    }

    @Test
    @DisplayName("playersの長さが24でない場合は400を返す")
    void rejectsWrongPlayersLength() throws Exception {
        mockMvc.perform(post("/api/algorithms/server-scale-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\":[0,1,2],\"m\":3,\"k\":5}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "players[0]={0} の場合は400を返す")
    @ValueSource(strings = {"-1", "1001", "null"})
    @DisplayName("players[i]が範囲(0〜1000)を外れる、またはnullの場合は400を返す")
    void rejectsPlayersValueOutOfRange(String invalidValue) throws Exception {
        String players = VALID_PLAYERS.replaceFirst("^\\[0", "[" + invalidValue);

        mockMvc.perform(post("/api/algorithms/server-scale-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\":" + players + ",\"m\":3,\"k\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("playersが指定されていない場合は400を返す")
    void rejectsMissingPlayers() throws Exception {
        mockMvc.perform(post("/api/algorithms/server-scale-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"m\":3,\"k\":5}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "m={0}, k={1} の場合は400を返す")
    @CsvSource({"0, 5", "1001, 5", "3, 0", "3, 25"})
    @DisplayName("mが範囲(1〜1000)、またはkが範囲(1〜24)を外れる場合は400を返す")
    void rejectsMOrKOutOfRange(int m, int k) throws Exception {
        mockMvc.perform(post("/api/algorithms/server-scale-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\":" + VALID_PLAYERS + ",\"m\":" + m + ",\"k\":" + k + "}"))
                .andExpect(status().isBadRequest());
    }
}
