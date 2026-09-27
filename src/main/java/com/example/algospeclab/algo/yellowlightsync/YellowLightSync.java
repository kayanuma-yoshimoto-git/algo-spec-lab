package com.example.algospeclab.algo.yellowlightsync;

import java.util.ArrayList;
import java.util.List;

/**
 * specs/yellow-light-sync/README.md 「5. 設計のアプローチ」で採用した
 * 黄色区間の交差（インターバルマージ）方式による実装。
 *
 * <p>信号機を1つずつ取り込みながら、「ここまでの信号機が全て黄色になる区間」を
 * two-pointer で交差計算し、最終的に残った区間の先頭時刻を返す。</p>
 */
public final class YellowLightSync {

    private YellowLightSync() {
    }

    /**
     * 全ての信号機が同時に黄色になる最も早い時刻（秒）を返す。存在しない場合は -1。
     *
     * @param signals signals[i] = [G, Y, R]（緑・黄・赤の持続時間）
     */
    public static int solve(int[][] signals) {
        long period = signalPeriod(signals[0]);
        List<long[]> windows = new ArrayList<>();
        windows.add(yellowWindow(signals[0]));

        for (int i = 1; i < signals.length; i++) {
            long nextPeriod = signalPeriod(signals[i]);
            long mergedPeriod = lcm(period, nextPeriod);

            List<long[]> expandedWindows = expand(windows, period, mergedPeriod);
            List<long[]> nextWindows = expand(List.of(yellowWindow(signals[i])), nextPeriod, mergedPeriod);

            windows = intersect(expandedWindows, nextWindows);
            period = mergedPeriod;

            if (windows.isEmpty()) {
                return -1;
            }
        }

        return (int) (windows.get(0)[0] + 1);
    }

    private static long signalPeriod(int[] signal) {
        return signal[0] + signal[1] + signal[2];
    }

    /** 信号機の黄色区間を offset の半開区間 [G, G+Y) として返す。 */
    private static long[] yellowWindow(int[] signal) {
        int g = signal[0];
        int y = signal[1];
        return new long[] {g, g + y};
    }

    /** 周期 {@code from} を単位とする区間リストを [0, to) 全体に複製して展開する。 */
    private static List<long[]> expand(List<long[]> baseWindows, long from, long to) {
        List<long[]> result = new ArrayList<>();
        for (long offset = 0; offset < to; offset += from) {
            for (long[] window : baseWindows) {
                result.add(new long[] {window[0] + offset, window[1] + offset});
            }
        }
        return result;
    }

    /** ソート済み・互いに非重複な2つの区間リストの交差を two-pointer で求める。 */
    private static List<long[]> intersect(List<long[]> a, List<long[]> b) {
        List<long[]> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            long start = Math.max(a.get(i)[0], b.get(j)[0]);
            long end = Math.min(a.get(i)[1], b.get(j)[1]);
            if (start < end) {
                result.add(new long[] {start, end});
            }
            if (a.get(i)[1] < b.get(j)[1]) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }
}
