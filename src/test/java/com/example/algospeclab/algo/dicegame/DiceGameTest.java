package com.example.algospeclab.algo.dicegame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/dice-game/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link DiceGame#solve(int[][])}(選び方の全列挙 + 合計値の分布 DP による勝ち数の計算)。
 */
class DiceGameTest {

    @Test
    @DisplayName("問題例1: 勝率が最大になるのは #1, #4")
    void example1() {
        int[][] dice = {{1, 2, 3, 4, 5, 6}, {3, 3, 3, 3, 4, 4}, {1, 3, 3, 4, 4, 4}, {1, 1, 4, 4, 5, 5}};

        assertThat(DiceGame.solve(dice)).containsExactly(1, 4);
    }

    @Test
    @DisplayName("問題例2: サイコロ2個なら #2 を選ぶ")
    void example2() {
        int[][] dice = {{1, 2, 3, 4, 5, 6}, {2, 2, 4, 4, 6, 6}};

        assertThat(DiceGame.solve(dice)).containsExactly(2);
    }

    @Test
    @DisplayName("問題例3: 勝率が最大になるのは #1, #3")
    void example3() {
        int[][] dice = {
                {40, 41, 42, 43, 44, 45}, {43, 43, 42, 42, 41, 41}, {1, 1, 80, 80, 80, 80}, {70, 70, 1, 1, 70, 70}};

        assertThat(DiceGame.solve(dice)).containsExactly(1, 3);
    }

    @Test
    @DisplayName("同率の場合は辞書順で最初の組み合わせを返す(同じ構成のサイコロ2個なら [1])")
    void returnsFirstCombinationOnTieWithTwoDice() {
        int[][] dice = {{1, 2, 3, 4, 5, 6}, {1, 2, 3, 4, 5, 6}};

        assertThat(DiceGame.solve(dice)).containsExactly(1);
    }

    @Test
    @DisplayName("すべて引き分けになる場合は辞書順で最初の組み合わせ [1, 2] を返す")
    void returnsFirstCombinationWhenAllDraws() {
        int[][] dice = new int[4][6];
        for (int[] die : dice) {
            Arrays.fill(die, 7);
        }

        assertThat(DiceGame.solve(dice)).containsExactly(1, 2);
    }

    @Test
    @DisplayName("総当たりとの一致: n ≤ 6 のランダムな入力で、全出目を列挙する総当たりと同じ組み合わせを選ぶ")
    void matchesBruteForceOnSmallInputs() {
        Random random = new Random(20260928L);
        for (int trial = 0; trial < 200; trial++) {
            int n = 2 * (1 + random.nextInt(3));
            int[][] dice = new int[n][6];
            for (int[] die : dice) {
                for (int face = 0; face < 6; face++) {
                    die[face] = 1 + random.nextInt(6);
                }
            }

            assertThat(DiceGame.solve(dice))
                    .as("dice=%s", Arrays.deepToString(dice))
                    .containsExactly(bruteForce(dice));
        }
    }

    @Test
    @DisplayName("面の数が最小1・最大100を含む入力を正しく扱う")
    void handlesMinimumAndMaximumFaces() {
        int[][] dice = {{1, 1, 1, 1, 1, 1}, {100, 100, 100, 100, 100, 100}};

        assertThat(DiceGame.solve(dice)).containsExactly(2);
    }

    @Test
    @DisplayName("最大規模(n=10、面の数1〜100)でも1秒以内に長さ5の昇順配列を返す")
    void completesWithinTimeLimitOnMaximumInput() {
        Random random = new Random(42L);
        int[][] dice = new int[10][6];
        for (int[] die : dice) {
            for (int face = 0; face < 6; face++) {
                die[face] = 1 + random.nextInt(100);
            }
        }
        DiceGame.solve(dice); // JIT のウォームアップ

        int[] result = assertTimeout(Duration.ofSeconds(1), () -> DiceGame.solve(dice));

        assertThat(result).hasSize(5).isSorted();
        assertThat(Arrays.stream(result).boxed().toList()).allMatch(number -> 1 <= number && number <= 10);
    }

    @Test
    @DisplayName("dice が null の場合は IllegalArgumentException を送出する")
    void rejectsNullDice() {
        assertThatThrownBy(() -> DiceGame.solve(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "n={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, 3, 12})
    @DisplayName("n が範囲(2〜10)外、または奇数の場合は IllegalArgumentException を送出する")
    void rejectsInvalidDiceCount(int n) {
        int[][] dice = new int[n][];
        for (int i = 0; i < n; i++) {
            dice[i] = new int[] {1, 2, 3, 4, 5, 6};
        }

        assertThatThrownBy(() -> DiceGame.solve(dice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("要素が null、または面数が6でない場合は IllegalArgumentException を送出する")
    void rejectsInvalidDie() {
        int[] valid = {1, 2, 3, 4, 5, 6};
        List<int[]> invalidDice = new ArrayList<>();
        invalidDice.add(null);
        invalidDice.add(new int[] {1, 2, 3, 4, 5});
        invalidDice.add(new int[] {1, 2, 3, 4, 5, 6, 7});
        for (int[] invalid : invalidDice) {
            assertThatThrownBy(() -> DiceGame.solve(new int[][] {valid, invalid}))
                    .as("die=%s", Arrays.toString(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @ParameterizedTest(name = "面の数 {0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1, 101})
    @DisplayName("面の数が範囲(1〜100)を外れる場合は IllegalArgumentException を送出する")
    void rejectsFaceOutOfRange(int face) {
        int[][] dice = {{1, 2, 3, 4, 5, 6}, {1, 2, 3, 4, 5, face}};

        assertThatThrownBy(() -> DiceGame.solve(dice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 選び方を辞書順に列挙し、全出目(6^n 通り)を数え上げて勝ち数が真に最大の組み合わせを返す(テスト用オラクル)。
     */
    private static int[] bruteForce(int[][] dice) {
        int n = dice.length;
        int[] best = null;
        long bestWins = -1;
        for (int[] chosen : combinations(n, n / 2)) {
            boolean[] isA = new boolean[n];
            for (int index : chosen) {
                isA[index] = true;
            }
            long wins = 0;
            int outcomes = (int) Math.pow(6, n);
            for (int code = 0; code < outcomes; code++) {
                int sumA = 0;
                int sumB = 0;
                int rest = code;
                for (int die = 0; die < n; die++) {
                    int value = dice[die][rest % 6];
                    rest /= 6;
                    if (isA[die]) {
                        sumA += value;
                    } else {
                        sumB += value;
                    }
                }
                if (sumA > sumB) {
                    wins++;
                }
            }
            if (wins > bestWins) {
                bestWins = wins;
                best = chosen;
            }
        }
        return Arrays.stream(best).map(index -> index + 1).toArray();
    }

    /** 0〜n-1 から k 個を選ぶ組み合わせを辞書順に返す。 */
    private static List<int[]> combinations(int n, int k) {
        List<int[]> result = new ArrayList<>();
        addCombinations(n, k, 0, new int[k], 0, result);
        return result;
    }

    private static void addCombinations(int n, int k, int start, int[] current, int size, List<int[]> result) {
        if (size == k) {
            result.add(current.clone());
            return;
        }
        for (int i = start; i < n; i++) {
            current[size] = i;
            addCombinations(n, k, i + 1, current, size + 1, result);
        }
    }
}
