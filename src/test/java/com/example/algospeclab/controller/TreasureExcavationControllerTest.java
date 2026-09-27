package com.example.algospeclab.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
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
 * specs/treasure-excavation/README.md の「2.3 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/treasure-excavation
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class TreasureExcavationControllerTest {

    private static final String ENDPOINT = "/api/algorithms/treasure-excavation";
    private static final String VALID_DEPTH = "[1,2,3,4,5,6,7,8,9,10]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と宝の列・money以内の総コスト・宝の列で終わる掘削順序を返す")
    void returnsOkWithFoundColumn() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_DEPTH, 55, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.column").value(3))
                .andExpect(jsonPath("$.totalCost", lessThanOrEqualTo(55)))
                .andExpect(jsonPath("$.excavatedColumns[-1:]", contains(3)));
    }

    @Test
    @DisplayName("depth が指定されていない場合は400を返す")
    void rejectsMissingDepth() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"money\":55,\"treasureCol\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("depth の要素数が2未満の場合は400を返す")
    void rejectsTooShortDepth() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[1]", 1, 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("depth の要素数が200を超える場合は400を返す")
    void rejectsTooLongDepth() throws Exception {
        String depth = "[" + String.join(",", Collections.nCopies(201, "1")) + "]";

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(depth, 201, 1)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "depth[0]={0} の場合は400を返す")
    @ValueSource(strings = {"0", "-1", "100001", "null"})
    @DisplayName("depth[i] が範囲(1〜100000)を外れる、または null の場合は400を返す")
    void rejectsDepthValueOutOfRange(String invalidValue) throws Exception {
        String depth = VALID_DEPTH.replaceFirst("^\\[1", "[" + invalidValue);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(depth, 55, 3)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "money={0}, treasureCol={1} の場合は400を返す")
    @CsvSource({"0, 3", "-1, 3", "55, 0", "55, -1"})
    @DisplayName("money または treasureCol が0以下の場合は400を返す")
    void rejectsNonPositiveMoneyOrTreasureCol(int money, int treasureCol) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_DEPTH, money, treasureCol)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("treasureCol が depth の長さを超える場合は400を返す")
    void rejectsTreasureColBeyondWidth() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_DEPTH, 55, 11)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("money が最悪ケース最小コスト未満の場合は400を返す")
    void rejectsInsufficientMoney() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[1,1,1,1,1]", 2, 5)))
                .andExpect(status().isBadRequest());
    }

    private static String body(String depth, int money, int treasureCol) {
        return "{\"depth\":" + depth + ",\"money\":" + money + ",\"treasureCol\":" + treasureCol + "}";
    }
}
