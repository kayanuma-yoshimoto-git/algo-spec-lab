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
 * specs/odd-even-tree/README.md の「2.2 REST API」「4. 異常系 — REST API 層」に対応するテスト。
 * POST /api/algorithms/odd-even-tree
 */
@WebMvcTest(AlgorithmController.class)
@Import(GlobalExceptionHandler.class)
class OddEvenTreeControllerTest {

    private static final String ENDPOINT = "/api/algorithms/odd-even-tree";
    private static final String VALID_NODES = "[11,9,3,2,4,6]";
    private static final String VALID_EDGES = "[[9,11],[2,3],[6,3],[3,4]]";

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("正常な入力の場合、200と偶奇ツリー・逆偶奇ツリーになれる木の数を返す")
    void returnsOkWithCounts() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_NODES, VALID_EDGES)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oddEven").value(1))
                .andExpect(jsonPath("$.reverseOddEven").value(0));
    }

    @Test
    @DisplayName("nodes が未指定の場合は400を返す")
    void rejectsMissingNodes() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"edges\":" + VALID_EDGES + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("nodes が空配列の場合は400を返す")
    void rejectsEmptyNodes() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[]", VALID_EDGES)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "nodes の要素 {0} の場合は400を返す")
    @ValueSource(strings = {"0", "1000001", "null"})
    @DisplayName("nodes の要素が範囲(1〜1000000)を外れる、または null の場合は400を返す")
    void rejectsInvalidNodeElement(String invalid) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[1," + invalid + "]", "[[1,2]]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("edges が未指定の場合は400を返す")
    void rejectsMissingEdges() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nodes\":" + VALID_NODES + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("edges が空配列の場合は400を返す")
    void rejectsEmptyEdges() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(VALID_NODES, "[]")))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "edges={0} の場合は400を返す")
    @ValueSource(strings = {"[[1]]", "[[1,2,3]]", "[[0,1]]", "[[1,1000001]]", "[null]"})
    @DisplayName("edges の要素の長さが2でない、端点が範囲外、または null の場合は400を返す")
    void rejectsInvalidEdgeElement(String edges) throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("[1,2]", edges)))
                .andExpect(status().isBadRequest());
    }

    private static String body(String nodes, String edges) {
        return "{\"nodes\":" + nodes + ",\"edges\":" + edges + "}";
    }
}
