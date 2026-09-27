package com.example.algospeclab.algo.serverscaleout;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * specs/server-scale-out/README.md 「5. 設計のアプローチ」で採用した
 * 貪欲法 + 期限管理キュー方式による実装。
 *
 * <p>時間帯を先頭から順に走査し、その時点で不足しているサーバー数だけを都度増設する。
 * 増設したサーバーは {@code k} 時間後に期限切れとしてキューから取り除く。</p>
 */
public final class ServerScaleOut {

    private ServerScaleOut() {
    }

    /**
     * 1日を通して全ての時間帯の要件を満たすための、サーバー増設回数の合計の最小値を返す。
     *
     * @param players players[i] は i時〜i+1時の利用者数(長さ24)
     * @param m       サーバー1台が支えられる最大利用者数
     * @param k       サーバー1台の稼働時間
     */
    public static int solve(int[] players, int m, int k) {
        Deque<int[]> expirations = new ArrayDeque<>(); // [期限切れ時刻, 台数]
        int current = 0;
        int totalAdditions = 0;

        for (int hour = 0; hour < players.length; hour++) {
            while (!expirations.isEmpty() && expirations.peekFirst()[0] <= hour) {
                current -= expirations.pollFirst()[1];
            }

            int required = players[hour] / m;
            if (current < required) {
                int need = required - current;
                current += need;
                expirations.addLast(new int[] {hour + k, need});
                totalAdditions += need;
            }
        }

        return totalAdditions;
    }
}
