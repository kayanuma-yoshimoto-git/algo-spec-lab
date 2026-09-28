package com.example.algospeclab.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * specs/problem-list/README.md の「2.1 サービス」「4. 正常系・異常系 — サービス層」に対応するテスト。
 * リポジトリはモックに差し替えるため DB は不要。
 */
class ProblemServiceTest {

    private static final Sort BY_ID = Sort.by("id");

    private ProblemRepository repository;
    private ProblemService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProblemRepository.class);
        service = new ProblemService(repository);
    }

    @Test
    @DisplayName("課題が複数件ある場合、リポジトリの返却順を保って全件返し、各要素の7項目がエンティティの値と一致する")
    void returnsAllProblemsWithAllFields() {
        Problem first = problem(1L, "ancient-base", "古代文明の進法", "# 古代文明の進法\n本文",
                "List<String> expressions", at(1), at(2));
        Problem second = problem(2L, "dice-game", "サイコロゲーム", "# サイコロゲーム\n本文",
                "List<Integer> players, int m, int k", at(3), at(4));
        when(repository.findAll(BY_ID)).thenReturn(List.of(first, second));

        List<ProblemResponse> result = service.findAll();

        assertThat(result).containsExactly(
                new ProblemResponse(1L, "ancient-base", "古代文明の進法", "# 古代文明の進法\n本文",
                        "List<String> expressions", at(1), at(2)),
                new ProblemResponse(2L, "dice-game", "サイコロゲーム", "# サイコロゲーム\n本文",
                        "List<Integer> players, int m, int k", at(3), at(4)));
    }

    @Test
    @DisplayName("課題が1件の場合、1件のリストを返す")
    void returnsSingleProblem() {
        Problem only = problem(7L, "nth-spell", "n番目の呪文", "# n番目の呪文", "long n, List<String> bans",
                at(5), at(6));
        when(repository.findAll(BY_ID)).thenReturn(List.of(only));

        List<ProblemResponse> result = service.findAll();

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.id()).isEqualTo(7L);
            assertThat(response.slug()).isEqualTo("nth-spell");
        });
    }

    @Test
    @DisplayName("課題が0件の場合、例外にせず空リストを返す")
    void returnsEmptyListWhenNoProblems() {
        when(repository.findAll(BY_ID)).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("リポジトリに id 昇順のソート条件を渡して取得する")
    void queriesRepositorySortedById() {
        when(repository.findAll(BY_ID)).thenReturn(List.of());

        service.findAll();

        verify(repository).findAll(BY_ID);
    }

    /** 採番・日時の自動設定は DB 側で行われるため、テストではリフレクションで値を入れる。 */
    private static Problem problem(Long id, String slug, String title, String description, String arguments,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        Problem problem = new Problem(slug);
        problem.apply(new SpecDocument(title, description, arguments));
        ReflectionTestUtils.setField(problem, "id", id);
        ReflectionTestUtils.setField(problem, "createdAt", createdAt);
        ReflectionTestUtils.setField(problem, "updatedAt", updatedAt);
        return problem;
    }

    private static OffsetDateTime at(int hour) {
        return OffsetDateTime.of(2026, 9, 29, hour, 0, 0, 0, ZoneOffset.UTC);
    }
}
