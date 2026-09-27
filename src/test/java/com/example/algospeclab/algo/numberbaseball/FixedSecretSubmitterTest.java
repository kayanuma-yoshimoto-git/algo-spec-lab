package com.example.algospeclab.algo.numberbaseball;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/number-baseball/README.md の「2.2 固定暗証番号シミュレーター」に対応するテスト。
 */
class FixedSecretSubmitterTest {

    @ParameterizedTest(name = "暗証番号1357に {0} を提出すると \"{1}\" を返す")
    @CsvSource({
            "7000, 0S 1B",
            "2244, 0S 0B",
            "3333, 1S 3B",
            "3457, 2S 1B",
            "7531, 0S 4B",
            "1357, 4S 0B"})
    @DisplayName("問題文の表(暗証番号1357)の6例すべてで同じ手がかりを返す")
    void returnsClueAsInProblemTable(int guess, String expectedClue) {
        FixedSecretSubmitter submitter = new FixedSecretSubmitter(1357);

        assertThat(submitter.submit(guess)).isEqualTo(expectedClue);
    }

    @Test
    @DisplayName("提出履歴(提出値と手がかりの組)と提出回数を記録する")
    void recordsHistoryAndSubmitCount() {
        FixedSecretSubmitter submitter = new FixedSecretSubmitter(1357);

        submitter.submit(7000);
        submitter.submit(1357);

        assertThat(submitter.history())
                .containsExactly(new Attempt(7000, "0S 1B"), new Attempt(1357, "4S 0B"));
        assertThat(submitter.submitCount()).isEqualTo(2);
    }

    @ParameterizedTest(name = "secret={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {1023, 1123, 999, 10000, 0, -1357})
    @DisplayName("secret が 1〜9 の互いに異なる4桁でない場合は IllegalArgumentException を送出する")
    void rejectsInvalidSecret(int secret) {
        assertThatThrownBy(() -> new FixedSecretSubmitter(secret))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "guess={0} を提出すると IllegalArgumentException を送出する")
    @ValueSource(ints = {999, 10000})
    @DisplayName("提出値が範囲(1000〜9999)を外れる場合は IllegalArgumentException を送出する")
    void rejectsGuessOutOfRange(int guess) {
        FixedSecretSubmitter submitter = new FixedSecretSubmitter(1357);

        assertThatThrownBy(() -> submitter.submit(guess))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
