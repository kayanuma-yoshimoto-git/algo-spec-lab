package com.example.algospeclab.algo.nthspell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/nth-spell/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link NthSpell#solve(long, String[])}(全単射 26 進数の通し番号 + 削除番号による n の補正)。
 */
class NthSpellTest {

    private static final long MAX_N = 1_000_000_000_000_000L;
    private static final int MAX_BANS = 300_000;

    @Test
    @DisplayName("問題例1: 30番目の呪文は \"ah\"")
    void example1() {
        assertThat(NthSpell.solve(30, new String[] {"d", "e", "bb", "aa", "ae"})).isEqualTo("ah");
    }

    @Test
    @DisplayName("問題例2: 7388番目の呪文は \"jxk\"")
    void example2() {
        String[] bans = {"gqk", "kdn", "jxj", "jxi", "fug", "jxg", "ewq", "len", "bhc"};

        assertThat(NthSpell.solve(7388, bans)).isEqualTo("jxk");
    }

    @Test
    @DisplayName("答えより後ろにある削除呪文は結果に影響しない")
    void ignoresBansAfterAnswer() {
        assertThat(NthSpell.solve(3, new String[] {"zz"})).isEqualTo("c");
    }

    @Test
    @DisplayName("連続する削除呪文を順に飛ばし、bans の並び順にも依存しない")
    void skipsConsecutiveBansRegardlessOfOrder() {
        assertThat(NthSpell.solve(1, new String[] {"a", "b", "c"})).isEqualTo("d");
        assertThat(NthSpell.solve(1, new String[] {"c", "a", "b"})).isEqualTo("d");
    }

    @ParameterizedTest(name = "n={0}, bans=[{1}] → \"{2}\"")
    @CsvSource({"26, a, aa", "26, b, aa", "52, zz, az", "53, az, bb"})
    @DisplayName("文字数の境界をまたぐ場合や桁上がりする場合も正しい呪文を返す")
    void handlesLengthBoundaryAndCarry(long n, String ban, String expected) {
        assertThat(NthSpell.solve(n, new String[] {ban})).isEqualTo(expected);
    }

    @Test
    @DisplayName("総当たりとの一致: 小さい n とランダムな削除呪文で、先頭から数え上げる総当たりと一致する")
    void matchesBruteForceOnSmallInputs() {
        List<String> spells = enumerateSpellsUpToLength(3);
        Random random = new Random(20260928L);
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + random.nextInt(2000);
            Set<String> bans = new HashSet<>();
            int banCount = 1 + random.nextInt(50);
            while (bans.size() < banCount) {
                bans.add(spells.get(random.nextInt(3000)));
            }
            String[] banArray = bans.toArray(new String[0]);

            assertThat(NthSpell.solve(n, banArray))
                    .as("n=%d, bans=%s", n, Arrays.toString(banArray))
                    .isEqualTo(bruteForce(spells, n, bans));
        }
    }

    @Test
    @DisplayName("最小: n=1 で先頭以外を削除すると \"a\"、先頭を削除すると \"b\"")
    void handlesMinimumN() {
        assertThat(NthSpell.solve(1, new String[] {"b"})).isEqualTo("a");
        assertThat(NthSpell.solve(1, new String[] {"a"})).isEqualTo("b");
    }

    @Test
    @DisplayName("最大の n=10^15 で、11文字の呪文を返す")
    void handlesMaximumN() {
        assertThat(NthSpell.solve(MAX_N, new String[] {"zzzzzzzzzzz"})).isEqualTo("gbdpxgrzxjl");
        assertThat(NthSpell.solve(MAX_N, new String[] {"a"})).isEqualTo("gbdpxgrzxjm");
    }

    @Test
    @DisplayName("bans が最大件数(300,000件)でも1秒以内に完了する")
    void completesWithinTimeLimitOnMaximumBans() {
        String[] bans = distinctFiveLetterSpells(MAX_BANS);
        NthSpell.solve(MAX_N, bans); // JIT のウォームアップ

        assertTimeout(Duration.ofSeconds(1), () -> NthSpell.solve(MAX_N, bans));
    }

    @ParameterizedTest(name = "n={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(longs = {0, -1, MAX_N + 1})
    @DisplayName("n が範囲(1〜10^15)を外れる場合は IllegalArgumentException を送出する")
    void rejectsNOutOfRange(long n) {
        assertThatThrownBy(() -> NthSpell.solve(n, new String[] {"a"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("bans が null・空配列・300,001件の場合は IllegalArgumentException を送出する")
    void rejectsInvalidBansSize() {
        assertThatThrownBy(() -> NthSpell.solve(1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NthSpell.solve(1, new String[0]))
                .isInstanceOf(IllegalArgumentException.class);
        String[] tooMany = distinctFiveLetterSpells(MAX_BANS + 1);
        assertThatThrownBy(() -> NthSpell.solve(1, tooMany))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "bans の要素 \"{0}\" の場合は IllegalArgumentException を送出する")
    @ValueSource(strings = {"", "aaaaaaaaaaaa", "A", "a1", "ab c"})
    @DisplayName("bans の要素が空文字・12文字以上・英小文字以外を含む場合は IllegalArgumentException を送出する")
    void rejectsInvalidBanElement(String invalid) {
        assertThatThrownBy(() -> NthSpell.solve(1, new String[] {"b", invalid}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("bans の要素が null の場合は IllegalArgumentException を送出する")
    void rejectsNullBanElement() {
        assertThatThrownBy(() -> NthSpell.solve(1, new String[] {"b", null}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("bans に重複がある場合は IllegalArgumentException を送出する")
    void rejectsDuplicateBans() {
        assertThatThrownBy(() -> NthSpell.solve(1, new String[] {"a", "b", "a"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 長さ maxLength 以下の呪文を、呪文書の順(文字数の少ない順 → 辞書順)で列挙する。
     * 実装の番号変換とは独立に、長さごとに辞書順で文字列を生成する。
     */
    private static List<String> enumerateSpellsUpToLength(int maxLength) {
        List<String> spells = new ArrayList<>();
        List<String> current = List.of("");
        for (int length = 1; length <= maxLength; length++) {
            List<String> next = new ArrayList<>();
            for (String prefix : current) {
                for (char ch = 'a'; ch <= 'z'; ch++) {
                    next.add(prefix + ch);
                }
            }
            spells.addAll(next);
            current = next;
        }
        return spells;
    }

    /** 呪文書を先頭から数え上げ、削除されていない n 番目の呪文を返す(テスト用オラクル)。 */
    private static String bruteForce(List<String> spells, int n, Set<String> bans) {
        int count = 0;
        for (String spell : spells) {
            if (!bans.contains(spell) && ++count == n) {
                return spell;
            }
        }
        throw new IllegalStateException("列挙範囲内に n 番目の呪文がありません");
    }

    /** 互いに異なる5文字の呪文を count 件生成する("aaaaa", "aaaab", … の順)。 */
    private static String[] distinctFiveLetterSpells(int count) {
        String[] spells = new String[count];
        for (int i = 0; i < count; i++) {
            char[] chars = new char[5];
            int value = i;
            for (int pos = 4; pos >= 0; pos--) {
                chars[pos] = (char) ('a' + value % 26);
                value /= 26;
            }
            spells[i] = new String(chars);
        }
        return spells;
    }
}
