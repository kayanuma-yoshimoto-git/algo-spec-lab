package com.example.algospeclab.algo.runlengthwindowsum;

/**
 * specs/run-length-window-sum/README.md 「5. 設計のアプローチ」で採用した
 * 区間和の区分線形性 + 2ポインタ方式による実装。
 *
 * <p>{@code brr} は {@code arr[i]} を {@code arr[i]} 個並べた「ラン」の連結であり、実際には展開しない。
 * 窓の左端・右端がそれぞれ同じランに留まる間は窓の和が一次関数になるため、
 * その区間ごとに一次方程式 {@code f + x·d = K} の解の個数を数える。</p>
 */
public final class RunLengthWindowSum {

    private static final int MAX_LENGTH = 100_000;
    private static final int MAX_VALUE = 100_000;

    private RunLengthWindowSum() {
    }

    /**
     * {@code brr} の l〜r 番目の和 K と、同じ長さで和が K となる部分配列の個数 C を返す。
     *
     * @param arr 元の配列(1 ≤ arr[i] ≤ 100,000、長さ 1〜100,000)
     * @param l   区間の左端(1始まり)
     * @param r   区間の右端(1始まり、brr の長さ以下)
     * @return {@code [K, C]}
     * @throws IllegalArgumentException 入力が制約に違反する場合
     */
    public static long[] solve(int[] arr, long l, long r) {
        validateArray(arr);
        int n = arr.length;

        // runStart[i]: ラン i の brr 上の開始位置(0始まり)。runStart[n] は brr の長さ
        long[] runStart = new long[n + 1];
        // squareSum[i]: ラン 0〜i-1 の要素の総和
        long[] squareSum = new long[n + 1];
        for (int i = 0; i < n; i++) {
            runStart[i + 1] = runStart[i] + arr[i];
            squareSum[i + 1] = squareSum[i] + (long) arr[i] * arr[i];
        }
        long total = runStart[n];
        validateRange(l, r, total);

        long k = prefixSum(arr, runStart, squareSum, r) - prefixSum(arr, runStart, squareSum, l - 1);
        long windowLength = r - l + 1;
        long c = countWindows(arr, runStart, squareSum, windowLength, k);
        return new long[] {k, c};
    }

    /** 長さ {@code windowLength} の窓のうち、和が {@code target} となるものの個数を数える。 */
    private static long countWindows(
            int[] arr, long[] runStart, long[] squareSum, long windowLength, long target) {
        long lastStart = runStart[arr.length] - windowLength;
        long sum = prefixSum(arr, runStart, squareSum, windowLength);
        int left = 0;
        int right = findRun(runStart, windowLength - 1);
        long count = 0;

        long start = 0;
        while (true) {
            long end = start + windowLength - 1;
            // 左端・右端ともに現在のランに留まれる最大ステップ数(窓の最終位置も上限)
            long leftRoom = runStart[left + 1] - 1 - start;
            long rightRoom = runStart[right + 1] - 1 - end;
            long steps = Math.min(Math.min(leftRoom, rightRoom), lastStart - start);
            long slope = (long) arr[right] - arr[left];
            count += countSolutions(sum, slope, steps, target);

            start += steps;
            if (start == lastStart) {
                return count;
            }
            // 1つ右へずらす: 左端のラン値が抜け、右端の次の要素が入る
            sum += steps * slope - arr[left];
            if (start == runStart[left + 1] - 1) {
                left++;
            }
            if (start + windowLength - 1 == runStart[right + 1] - 1) {
                right++;
            }
            sum += arr[right];
            start++;
        }
    }

    /** {@code sum + x·slope = target}(0 ≤ x ≤ steps)を満たす整数 x の個数を返す。 */
    private static long countSolutions(long sum, long slope, long steps, long target) {
        long diff = target - sum;
        if (slope == 0) {
            return diff == 0 ? steps + 1 : 0;
        }
        if (diff % slope != 0) {
            return 0;
        }
        long x = diff / slope;
        return 0 <= x && x <= steps ? 1 : 0;
    }

    /** brr の先頭 {@code length} 要素の和を返す。 */
    private static long prefixSum(int[] arr, long[] runStart, long[] squareSum, long length) {
        if (length == 0) {
            return 0;
        }
        int run = findRun(runStart, length - 1);
        return squareSum[run] + (length - runStart[run]) * arr[run];
    }

    /** brr 上の位置 {@code position}(0始まり)を含むラン番号を二分探索で求める。 */
    private static int findRun(long[] runStart, long position) {
        int low = 0;
        int high = runStart.length - 2;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (runStart[mid] <= position) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    private static void validateArray(int[] arr) {
        if (arr == null || arr.length == 0 || arr.length > MAX_LENGTH) {
            throw new IllegalArgumentException("arr の長さは 1〜" + MAX_LENGTH + " で指定してください。");
        }
        for (int value : arr) {
            if (value < 1 || value > MAX_VALUE) {
                throw new IllegalArgumentException("arr の各要素は 1〜" + MAX_VALUE + " で指定してください。");
            }
        }
    }

    private static void validateRange(long l, long r, long total) {
        if (l < 1 || l > r || r > total) {
            throw new IllegalArgumentException(
                    "l, r は 1 ≤ l ≤ r ≤ " + total + " を満たすよう指定してください。");
        }
    }
}
