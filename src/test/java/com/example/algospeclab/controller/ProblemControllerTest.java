package com.example.algospeclab.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.algospeclab.problem.ProblemResponse;
import com.example.algospeclab.problem.ProblemService;
import com.example.algospeclab.web.GlobalExceptionHandler;

/**
 * specs/problem-list/README.md の「2.2 REST API」「4. REST API 層」に対応するテスト。
 * GET /api/problems
 */
@WebMvcTest(ProblemController.class)
@Import(GlobalExceptionHandler.class)
class ProblemControllerTest {

    private static final String ENDPOINT = "/api/problems";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProblemService problemService;

    @Test
    @DisplayName("課題が複数件ある場合、200と全件を順番どおりに7項目すべて含めて返す")
    void returnsAllProblemsWithAllFields() throws Exception {
        when(problemService.findAll()).thenReturn(List.of(
                new ProblemResponse(1L, "ancient-base", "古代文明の進法", "# 古代文明の進法\n本文",
                        "List<String> expressions", at(1), at(2)),
                new ProblemResponse(2L, "dice-game", "サイコロゲーム", "# サイコロゲーム\n本文",
                        "List<Integer> players, int m, int k", at(3), at(4))));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problems", hasSize(2)))
                .andExpect(jsonPath("$.problems[*].slug", contains("ancient-base", "dice-game")))
                .andExpect(jsonPath("$.problems[0].id").value(1))
                .andExpect(jsonPath("$.problems[0].slug").value("ancient-base"))
                .andExpect(jsonPath("$.problems[0].title").value("古代文明の進法"))
                .andExpect(jsonPath("$.problems[0].description").value("# 古代文明の進法\n本文"))
                .andExpect(jsonPath("$.problems[0].arguments").value("List<String> expressions"))
                .andExpect(jsonPath("$.problems[0].createdAt").exists())
                .andExpect(jsonPath("$.problems[0].updatedAt").exists());
    }

    @Test
    @DisplayName("課題が0件の場合、200と空の problems 配列を返す")
    void returnsEmptyList() throws Exception {
        when(problemService.findAll()).thenReturn(List.of());

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problems", hasSize(0)));
    }

    @Test
    @DisplayName("createdAt / updatedAt は数値ではなく ISO-8601 形式の文字列で返す")
    void serializesTimestampsAsIsoStrings() throws Exception {
        when(problemService.findAll()).thenReturn(List.of(
                new ProblemResponse(1L, "ancient-base", "古代文明の進法", "# 古代文明の進法",
                        "List<String> expressions",
                        OffsetDateTime.of(2026, 9, 29, 1, 2, 3, 0, ZoneOffset.UTC),
                        OffsetDateTime.of(2026, 9, 29, 4, 5, 6, 0, ZoneOffset.UTC))));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problems[0].createdAt").value("2026-09-29T01:02:03Z"))
                .andExpect(jsonPath("$.problems[0].updatedAt").value("2026-09-29T04:05:06Z"));
    }

    @Test
    @DisplayName("クエリパラメータを付けても無視して200と全件を返す")
    void ignoresQueryParameters() throws Exception {
        when(problemService.findAll()).thenReturn(List.of(
                new ProblemResponse(1L, "ancient-base", "古代文明の進法", "# 古代文明の進法",
                        "List<String> expressions", at(1), at(2))));

        mockMvc.perform(get(ENDPOINT).param("foo", "bar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problems", hasSize(1)));
    }

    @Test
    @DisplayName("POST の場合は405を返す")
    void rejectsPost() throws Exception {
        mockMvc.perform(post(ENDPOINT))
                .andExpect(status().isMethodNotAllowed());
    }

    private static OffsetDateTime at(int hour) {
        return OffsetDateTime.of(2026, 9, 29, hour, 0, 0, 0, ZoneOffset.UTC);
    }
}
