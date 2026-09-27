package com.example.algospeclab.algo.numberbaseball;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/number-baseball/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link NumberBaseball#solve(int, Submitter)}(候補の絞り込み + ミニマックスによる提出値選択)。
 *
 * <p>solve 1回に1秒弱かかるため、全3,024通りの網羅は行わず代表的な暗証番号に絞る
 * (全網羅での最悪5回は設計時にオフラインで検証済み)。</p>
 */
class NumberBaseballTest {

    private static final int STRICTEST_N = 6;
    private static final int WORST_CASE_SUBMITS = 5;

    @ParameterizedTest(name = "n={0}、暗証番号 {1} を特定する")
    @CsvSource({"3024, 1357", "3024, 3986", "33, 7685"})
    @DisplayName("問題例1〜3: 暗証番号を n 回以内の提出で特定する")
    void findsSecretInExamples(int n, int secret) {
        assertFound(n, secret);
    }

    @ParameterizedTest(name = "n=6、暗証番号 {0} を特定する")
    @ValueSource(ints = {1357, 3986, 7685, 1234, 9876})
    @DisplayName("最も厳しい n=6 で、問題例と境界的な暗証番号を特定する")
    void findsSecretWithStrictestLimit(int secret) {
        assertFound(STRICTEST_N, secret);
    }

    @Test
    @DisplayName("固定シードで選んだ10個の暗証番号を、n=6 かつ最悪5回以内の提出で特定する")
    void findsRandomSecretsWithinWorstCase() {
        Random random = new Random(20260928L);
        for (int trial = 0; trial < 10; trial++) {
            int secret = randomSecret(random);

            FixedSecretSubmitter submitter = assertFound(STRICTEST_N, secret);

            assertThat(submitter.submitCount()).as("暗証番号=%d", secret).isLessThanOrEqualTo(WORST_CASE_SUBMITS);
        }
    }

    @Test
    @DisplayName("手がかり \"4S 0B\" を受け取ったら、それ以降は提出せずにその提出値を返す")
    void stopsSubmittingAfterFullStrike() {
        Random random = new Random(7L);
        for (int trial = 0; trial < 10; trial++) {
            int secret = randomSecret(random);
            FixedSecretSubmitter submitter = new FixedSecretSubmitter(secret);

            int answer = NumberBaseball.solve(STRICTEST_N, submitter);

            List<Attempt> history = submitter.history();
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).clue().equals("4S 0B")) {
                    assertThat(i).as("暗証番号=%d", secret).isEqualTo(history.size() - 1);
                    assertThat(answer).isEqualTo(history.get(i).guess());
                }
            }
        }
    }

    @Test
    @DisplayName("決定性: 同じ暗証番号なら、提出する数の順序は毎回同じになる")
    void submissionOrderIsDeterministic() {
        FixedSecretSubmitter first = new FixedSecretSubmitter(7685);
        FixedSecretSubmitter second = new FixedSecretSubmitter(7685);

        NumberBaseball.solve(STRICTEST_N, first);
        NumberBaseball.solve(STRICTEST_N, second);

        assertThat(first.history()).isEqualTo(second.history());
    }

    @Test
    @DisplayName("タイブレーク規則により、最初の提出は常に1123になる")
    void firstSubmissionIs1123() {
        FixedSecretSubmitter submitter = new FixedSecretSubmitter(1357);

        NumberBaseball.solve(STRICTEST_N, submitter);

        assertThat(submitter.history().get(0).guess()).isEqualTo(1123);
    }

    @Test
    @DisplayName("solve 1回が1秒以内に完了する")
    void completesWithinTimeLimit() {
        NumberBaseball.solve(STRICTEST_N, new FixedSecretSubmitter(2468)); // JIT のウォームアップ

        assertTimeout(Duration.ofSeconds(1), () -> assertFound(STRICTEST_N, 8642));
    }

    @ParameterizedTest(name = "n={0} の場合は IllegalArgumentException を送出し、1回も提出しない")
    @ValueSource(ints = {5, 0, -1, 3025})
    @DisplayName("n が範囲(6〜3024)を外れる場合は、1回も提出せずに IllegalArgumentException を送出する")
    void rejectsNOutOfRangeWithoutSubmitting(int n) {
        AtomicInteger calls = new AtomicInteger();
        Submitter counting = guess -> {
            calls.incrementAndGet();
            return "0S 0B";
        };

        assertThatThrownBy(() -> NumberBaseball.solve(n, counting))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(calls).hasValue(0);
    }

    @Test
    @DisplayName("submitter が null の場合は IllegalArgumentException を送出する")
    void rejectsNullSubmitter() {
        assertThatThrownBy(() -> NumberBaseball.solve(STRICTEST_N, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "手がかり \"{0}\" の場合は IllegalStateException を送出する")
    @ValueSource(strings = {"abc", "1S", "", "1S2B", "-1S 0B", "3S 2B", "0S 5B"})
    @DisplayName("手がかりが \"xS yB\" 形式でない、または x+y>4 などの不正な値なら IllegalStateException を送出する")
    void rejectsMalformedClue(String clue) {
        assertThatThrownBy(() -> NumberBaseball.solve(STRICTEST_N, guess -> clue))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("手がかりが null の場合は IllegalStateException を送出する")
    void rejectsNullClue() {
        assertThatThrownBy(() -> NumberBaseball.solve(STRICTEST_N, guess -> null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("常に \"0S 0B\" を返すなど、候補が0個になる矛盾した手がかりなら IllegalStateException を送出する")
    void rejectsContradictoryClues() {
        assertThatThrownBy(() -> NumberBaseball.solve(3024, guess -> "0S 0B"))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * 暗証番号を固定して solve を実行し、正解条件(返り値が暗証番号・提出回数 ≤ n)を検証する。
     *
     * @return 提出履歴を確認するためのシミュレーター
     */
    private static FixedSecretSubmitter assertFound(int n, int secret) {
        FixedSecretSubmitter submitter = new FixedSecretSubmitter(secret);

        int answer = NumberBaseball.solve(n, submitter);

        assertThat(answer).as("n=%d, 暗証番号=%d", n, secret).isEqualTo(secret);
        assertThat(submitter.submitCount()).as("n=%d, 暗証番号=%d", n, secret).isLessThanOrEqualTo(n);
        return submitter;
    }

    /** 1〜9 の互いに異なる4桁の暗証番号をランダムに生成する。 */
    private static int randomSecret(Random random) {
        List<Integer> digits = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        Collections.shuffle(digits, random);
        return digits.get(0) * 1000 + digits.get(1) * 100 + digits.get(2) * 10 + digits.get(3);
    }
}
