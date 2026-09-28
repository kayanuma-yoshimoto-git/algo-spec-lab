package com.example.algospeclab.algo.ancientbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/ancient-base/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link AncientBase#solve(String[])}(2〜9 進法の総当たりで候補を絞り、候補ごとの結果の一致を判定)。
 */
class AncientBaseTest {

    @Test
    @DisplayName("問題例1: 既知の式から8進法に確定し、消えた結果を埋める")
    void example1() {
        String[] expressions = {"14 + 3 = 17", "13 - 6 = X", "51 - 5 = 44"};

        assertThat(AncientBase.solve(expressions)).containsExactly("13 - 6 = 5");
    }

    @Test
    @DisplayName("問題例2: 候補(6〜9進法)で結果が変わる式は ?、変わらない式は確定値を埋める")
    void example2() {
        String[] expressions = {"1 + 1 = 2", "1 + 3 = 4", "1 + 5 = X", "1 + 2 = X"};

        assertThat(AncientBase.solve(expressions)).containsExactly("1 + 5 = ?", "1 + 2 = 3");
    }

    @Test
    @DisplayName("問題例3: 6進法に確定し、繰り上がり・繰り下がりを含む結果を埋める")
    void example3() {
        String[] expressions = {"10 - 2 = X", "30 + 31 = 101", "3 + 3 = X", "33 + 33 = X"};

        assertThat(AncientBase.solve(expressions))
                .containsExactly("10 - 2 = 4", "3 + 3 = 10", "33 + 33 = 110");
    }

    @Test
    @DisplayName("問題例4: 候補(8・9進法)で結果が変わる式だけ ? になる")
    void example4() {
        String[] expressions = {"2 - 1 = 1", "2 + 2 = X", "7 + 4 = X", "5 - 5 = X"};

        assertThat(AncientBase.solve(expressions)).containsExactly("2 + 2 = 4", "7 + 4 = ?", "5 - 5 = 0");
    }

    @Test
    @DisplayName("問題例5: 結果が消えた式に現れる数字8も候補を絞り、9進法に確定する")
    void example5() {
        String[] expressions = {"2 - 1 = 1", "2 + 2 = X", "7 + 4 = X", "8 + 4 = X"};

        assertThat(AncientBase.solve(expressions)).containsExactly("2 + 2 = 4", "7 + 4 = 12", "8 + 4 = 13");
    }

    @Test
    @DisplayName("結果が消えた式しかなく候補が2〜9進法すべてのとき、2進法だけ結果が変わる式は ? になる")
    void handlesAllBasesAsCandidates() {
        assertThat(AncientBase.solve(new String[] {"1 + 1 = X", "0 + 0 = X"}))
                .containsExactly("1 + 1 = ?", "0 + 0 = 0");
    }

    @Test
    @DisplayName("2進法に確定する場合も正しく埋める")
    void handlesBinary() {
        assertThat(AncientBase.solve(new String[] {"1 + 1 = 10", "11 + 1 = X"}))
                .containsExactly("11 + 1 = 100");
    }

    @Test
    @DisplayName("最大の数字(9進法の 88 + 88)で3桁の結果を埋める")
    void handlesLargestDigits() {
        assertThat(AncientBase.solve(new String[] {"88 + 88 = X", "1 + 1 = 2"}))
                .containsExactly("88 + 88 = 187");
    }

    @Test
    @DisplayName("expressions が null・要素数1・要素数101の場合は IllegalArgumentException を送出する")
    void rejectsInvalidSize() {
        assertThatThrownBy(() -> AncientBase.solve(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 + 1 = X"}))
                .isInstanceOf(IllegalArgumentException.class);
        String[] tooMany = new String[101];
        Arrays.fill(tooMany, "1 + 1 = X");
        assertThatThrownBy(() -> AncientBase.solve(tooMany))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("要素が null の場合は IllegalArgumentException を送出する")
    void rejectsNullExpression() {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 + 1 = X", null}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "\"{0}\" の場合は IllegalArgumentException を送出する")
    @ValueSource(strings = {"1+1 = X", "1 * 1 = X", "123 + 1 = X", "1 + 1 = 1234", "1 + 1 = x", "1 + 1 = X ", ""})
    @DisplayName("式の形式が不正な場合は IllegalArgumentException を送出する")
    void rejectsMalformedExpression(String malformed) {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 + 1 = X", malformed}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("結果が消えた式が1つもない場合は IllegalArgumentException を送出する")
    void rejectsWithoutErasedResult() {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 + 1 = 2", "2 + 2 = 4"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("式どうしが矛盾して候補の進法が無い場合は IllegalArgumentException を送出する")
    void rejectsContradictoryExpressions() {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 + 1 = 2", "1 + 1 = 10", "1 + 1 = X"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("2〜9進法で書けない数字9を含む場合は IllegalArgumentException を送出する")
    void rejectsDigitNine() {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"9 + 1 = X", "1 + 1 = 2"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("結果が消えた式の結果が負になる場合は IllegalArgumentException を送出する")
    void rejectsNegativeResult() {
        assertThatThrownBy(() -> AncientBase.solve(new String[] {"1 - 2 = X", "1 + 1 = 2"}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
