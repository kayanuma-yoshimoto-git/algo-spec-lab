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
 * specs/dice-game/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/dice-game
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class DiceGameControllerTest {

    private static final String ENDPOINT = "/api/algorithms/dice-game";
    private static final String DIE = "[1,2,3,4,5,6]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と A が選ぶべきサイコロの番号を返す")
    void returnsOkWithChosenDice() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[[1,2,3,4,5,6],[3,3,3,3,4,4],[1,3,3,4,4,4],[1,1,4,4,5,5]]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dice", contains(1, 4)));
    }

    @Test
    @DisplayName("dice が未指定の場合は400を返す")
    void rejectsMissingDice() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "サイコロ {0} 個の場合は400を返す")
    @ValueSource(ints = {1, 12})
    @DisplayName("dice の要素数が範囲(2〜10)を外れる場合は400を返す")
    void rejectsDiceCountOutOfRange(int count) throws Exception {
        String dice = "[" + String.join(",", Collections.nCopies(count, DIE)) + "]";

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(dice)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "サイコロ {0} の場合は400を返す")
    @ValueSource(strings = {"[1,2,3,4,5]", "[1,2,3,4,5,6,7]", "[0,2,3,4,5,6]", "[1,2,3,4,5,101]", "[1,2,3,4,5,null]"})
    @DisplayName("面数が6でない、面の数が範囲外または null の場合は400を返す")
    void rejectsInvalidDie(String invalidDie) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[" + DIE + "," + invalidDie + "]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("サイコロの数が奇数(3)の場合は400を返す")
    void rejectsOddDiceCount() throws Exception {
        String dice = "[" + String.join(",", Collections.nCopies(3, DIE)) + "]";

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(dice)))
                .andExpect(status().isBadRequest());
    }

    private static String body(String dice) {
        return "{\"dice\":" + dice + "}";
    }
}
