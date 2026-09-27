package com.example.algospeclab.algo.treasureexcavation;

/**
 * specs/treasure-excavation/README.md 「5. 設計のアプローチ」で採用した
 * 区間 DP(ミニマックス)による実装。
 *
 * <p>候補区間 [l, r] ごとに「確実に宝を掘り当てるための最悪ケース最小コスト」と
 * その最小値を与える掘削列を前計算し、excavate の応答に従って候補区間を絞り込む。</p>
 */
public final class TreasureExcavation {

    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_DEPTH = 100_000;

    private TreasureExcavation() {
    }

    /**
     * excavate を呼び出して宝を掘り当て、宝があった列を返す。
     *
     * @param depth     depth[i] は i+1 列目の掘削コスト
     * @param money     使用可能な総コスト
     * @param excavator 掘削ロボット
     * @return 宝があった列(1始まり)
     * @throws IllegalArgumentException 入力が制約に違反する、または最悪ケース最小コストが money を超える場合(1回も掘らない)
     * @throws IllegalStateException    excavate が不正・矛盾した応答を返した場合
     */
    public static int solve(int[] depth, int money, Excavator excavator) {
        validate(depth, money, excavator);
        int w = depth.length;
        int[][] choice = new int[w + 2][w + 2];
        long worstCaseCost = buildStrategy(depth, choice);
        if (worstCaseCost > money) {
            throw new IllegalArgumentException(
                    "確実に宝を見つけるには最悪 " + worstCaseCost + " のコストが必要です(money=" + money + ")。");
        }

        int l = 1;
        int r = w;
        while (l <= r) {
            int col = choice[l][r];
            int result = excavator.excavate(col);
            switch (result) {
                case Excavator.FOUND -> {
                    return col;
                }
                case Excavator.LEFT -> r = col - 1;
                case Excavator.RIGHT -> l = col + 1;
                default -> throw new IllegalStateException("excavate が不正な値を返しました: " + result);
            }
        }
        throw new IllegalStateException("excavate の応答が矛盾しており、宝の候補となる列が残っていません。");
    }

    /**
     * 区間 DP で各区間 [l, r] の最適な掘削列を {@code choice[l][r]} に格納し、全体 [1, w] の最悪ケース最小コストを返す。
     * 同値の場合は最も左の列を選び、決定性を保つ。
     */
    private static long buildStrategy(int[] depth, int[][] choice) {
        int w = depth.length;
        // cost[l][r]: 区間 [l, r] の最悪ケース最小コスト。l > r(空区間)は 0
        long[][] cost = new long[w + 2][w + 2];
        for (int length = 1; length <= w; length++) {
            for (int l = 1; l + length - 1 <= w; l++) {
                int r = l + length - 1;
                long best = Long.MAX_VALUE;
                for (int k = l; k <= r; k++) {
                    long candidate = depth[k - 1] + Math.max(cost[l][k - 1], cost[k + 1][r]);
                    if (candidate < best) {
                        best = candidate;
                        choice[l][r] = k;
                    }
                }
                cost[l][r] = best;
            }
        }
        return cost[1][w];
    }

    private static void validate(int[] depth, int money, Excavator excavator) {
        if (depth == null || depth.length < MIN_WIDTH || depth.length > MAX_WIDTH) {
            throw new IllegalArgumentException("depth の長さは " + MIN_WIDTH + "〜" + MAX_WIDTH + " で指定してください。");
        }
        for (int value : depth) {
            if (value < 1 || value > MAX_DEPTH) {
                throw new IllegalArgumentException("depth の各要素は 1〜" + MAX_DEPTH + " で指定してください。");
            }
        }
        if (money < 1) {
            throw new IllegalArgumentException("money は1以上で指定してください。");
        }
        if (excavator == null) {
            throw new IllegalArgumentException("excavator を指定してください。");
        }
    }
}
