package com.example.algospeclab.algo.distributiontree;

/**
 * specs/distribution-tree/README.md 「5. 設計のアプローチ」で採用した実装。
 *
 * <p>深さごとの子の数の並び(2 を a 個 → 3 を b 個)を列挙し、各並びについて
 * 「1段だけ満杯でない」分配ノードの配置を解析的に評価して、リーフ数の最大値を求める。</p>
 *
 * <p>深さ i の分配ノード数を m_i、子の数を c_i とすると、リーフ数 = 1 + Σ m_i × (c_i - 1)、
 * 制約は m_{i+1} ≤ c_i × m_i(m_1 ≤ 1)、Σ m_i ≤ distLimit、Π c_i ≤ splitLimit となる。</p>
 */
public final class DistributionTree {

    private static final int MAX_LIMIT = 1_000_000_000;

    private DistributionTree() {
    }

    /**
     * 条件を満たすツリーで作れるリーフノード数の最大値を返す。
     *
     * @param distLimit  置ける分配ノード数の上限(0〜10^9)
     * @param splitLimit 分配度の上限(1〜10^9)
     * @return リーフノード数の最大値(splitLimit 以下)
     * @throws IllegalArgumentException 引数が範囲外の場合
     */
    public static int solve(int distLimit, int splitLimit) {
        if (distLimit < 0 || distLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("distLimit は 0〜" + MAX_LIMIT + " で指定してください: " + distLimit);
        }
        if (splitLimit < 1 || splitLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("splitLimit は 1〜" + MAX_LIMIT + " で指定してください: " + splitLimit);
        }

        long best = 1; // 分配ノードを置かない場合: ルートの子1つだけがリーフ
        for (int twos = 0; power(2, twos) <= splitLimit; twos++) {
            for (int threes = 0; power(2, twos) * power(3, threes) <= splitLimit; threes++) {
                if (twos + threes > 0) {
                    best = Math.max(best, maxLeaves(childrenSequence(twos, threes), distLimit));
                }
            }
        }
        return (int) best;
    }

    /** 上から 2 を twos 個、続けて 3 を threes 個並べた、深さごとの子の数を返す。 */
    private static int[] childrenSequence(int twos, int threes) {
        int[] children = new int[twos + threes];
        for (int i = 0; i < children.length; i++) {
            children[i] = i < twos ? 2 : 3;
        }
        return children;
    }

    /**
     * 深さごとの子の数が children のとき、満杯でない段 j を1つ選ぶ配置の中でリーフ数の最大値を返す。
     *
     * <p>段 0..j-1 は満杯、段 j に y 個、段 j+1..k-2 は y を起点に満杯、最後の段 k-1 は残り予算分だけ置く。
     * リーフ数は y について折れ線なので、端点と折れ目の前後だけを評価する。</p>
     */
    private static long maxLeaves(int[] children, long budget) {
        int last = children.length - 1;
        long best = 1;
        long fullCost = 0; // 段 0..j-1 を満杯にする分配ノード数
        long capacity = 1; // 段 j に置ける分配ノード数の上限(= 段 0..j-1 の子の数の積)
        for (int j = 0; j <= last && fullCost <= budget; j++) {
            long rest = budget - fullCost;
            if (j == last) {
                long placed = Math.min(capacity, rest);
                best = Math.max(best, capacity + placed * (children[last] - 1));
            } else {
                long costPerY = 0; // 段 j..k-2 の分配ノード数は y × costPerY
                long lastPerY = 1; // 最後の段の容量は y × lastPerY
                for (int i = j; i < last; i++) {
                    costPerY += lastPerY;
                    lastPerY *= children[i];
                }
                long yMax = Math.min(capacity, rest / costPerY);
                long yKink = rest / (costPerY + lastPerY);
                for (long y : new long[] {1, yMax, yKink, yKink + 1}) {
                    if (1 <= y && y <= yMax) {
                        long lastPlaced = Math.min(y * lastPerY, rest - y * costPerY);
                        long leaves = capacity + y * (lastPerY - 1) + lastPlaced * (children[last] - 1);
                        best = Math.max(best, leaves);
                    }
                }
            }
            fullCost += capacity;
            capacity *= children[j];
        }
        return best;
    }

    private static long power(int base, int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }
}
