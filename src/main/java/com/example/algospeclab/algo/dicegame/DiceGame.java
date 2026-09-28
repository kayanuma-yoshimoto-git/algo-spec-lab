package com.example.algospeclab.algo.dicegame;

import java.util.Arrays;

/**
 * specs/dice-game/README.md 「5. 設計のアプローチ」で採用した実装。
 *
 * <p>A の選び方(n/2 個)を辞書順に全列挙し、A・B それぞれの合計値の分布を DP で求めて勝ち数を数える。
 * 勝ち数が真に大きいときだけ最良を更新するため、同率の場合は辞書順で最初の組み合わせが残る。</p>
 */
public final class DiceGame {

    private static final int MIN_DICE = 2;
    private static final int MAX_DICE = 10;
    private static final int FACES = 6;
    private static final int MIN_FACE_VALUE = 1;
    private static final int MAX_FACE_VALUE = 100;

    private DiceGame() {
    }

    /**
     * A が勝つ確率が最も高くなるサイコロの選び方を返す。
     *
     * @param dice dice[i] は i+1 番のサイコロの6面の数(サイコロの数は 2〜10 の偶数、各数は 1〜100)
     * @return A が選ぶべきサイコロの番号(1 始まり、昇順)
     * @throws IllegalArgumentException 入力が制約に違反する場合
     */
    public static int[] solve(int[][] dice) {
        validate(dice);
        int n = dice.length;
        int[] chosen = new int[n / 2];
        int[] best = new int[n / 2];
        long[] bestWins = {-1};
        search(dice, 0, 0, chosen, best, bestWins);
        return Arrays.stream(best).map(index -> index + 1).toArray();
    }

    /** 組み合わせを辞書順に列挙し、勝ち数が真に大きいものを best に残す。 */
    private static void search(int[][] dice, int start, int size, int[] chosen, int[] best, long[] bestWins) {
        if (size == chosen.length) {
            long wins = countWins(dice, chosen);
            if (wins > bestWins[0]) {
                bestWins[0] = wins;
                System.arraycopy(chosen, 0, best, 0, chosen.length);
            }
            return;
        }
        for (int i = start; i < dice.length; i++) {
            chosen[size] = i;
            search(dice, i + 1, size + 1, chosen, best, bestWins);
        }
    }

    /** A が chosen のサイコロ、B が残りを振ったとき、A の合計が B の合計より大きくなる出目の数を返す。 */
    private static long countWins(int[][] dice, int[] chosen) {
        boolean[] isA = new boolean[dice.length];
        for (int index : chosen) {
            isA[index] = true;
        }
        long[] countA = sumDistribution(dice, isA, true);
        long[] countB = sumDistribution(dice, isA, false);

        long wins = 0;
        long lessB = 0; // B の合計が s 未満になる出目の数
        for (int s = 0; s < countA.length; s++) {
            wins += countA[s] * lessB;
            lessB += countB[s];
        }
        return wins;
    }

    /** isA[i] == side のサイコロを全部振ったときの合計値の分布(合計が s になる出目の数)を返す。 */
    private static long[] sumDistribution(int[][] dice, boolean[] isA, boolean side) {
        long[] count = new long[MAX_FACE_VALUE * (dice.length / 2) + 1];
        count[0] = 1;
        for (int i = 0; i < dice.length; i++) {
            if (isA[i] != side) {
                continue;
            }
            long[] next = new long[count.length];
            for (int s = 0; s < count.length; s++) {
                if (count[s] == 0) {
                    continue;
                }
                for (int face : dice[i]) {
                    next[s + face] += count[s];
                }
            }
            count = next;
        }
        return count;
    }

    private static void validate(int[][] dice) {
        if (dice == null || dice.length < MIN_DICE || dice.length > MAX_DICE || dice.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "サイコロの数は " + MIN_DICE + "〜" + MAX_DICE + " の偶数で指定してください。");
        }
        for (int[] die : dice) {
            if (die == null || die.length != FACES) {
                throw new IllegalArgumentException("各サイコロは " + FACES + " 面で指定してください。");
            }
            for (int face : die) {
                if (face < MIN_FACE_VALUE || face > MAX_FACE_VALUE) {
                    throw new IllegalArgumentException(
                            "面の数は " + MIN_FACE_VALUE + "〜" + MAX_FACE_VALUE + " で指定してください: " + face);
                }
            }
        }
    }
}
