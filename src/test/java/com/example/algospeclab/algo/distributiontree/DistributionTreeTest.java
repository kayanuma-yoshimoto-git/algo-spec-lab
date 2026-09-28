package com.example.algospeclab.algo.distributiontree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/distribution-tree/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link DistributionTree#solve(int, int)}(子の数の並びの列挙 + 1段だけ満杯でない配置の解析的評価)。
 */
class DistributionTreeTest {

    private static final int MAX_VALUE = 1_000_000_000;

    @ParameterizedTest(name = "distLimit={0}, splitLimit={1} → {2}")
    @CsvSource({"3, 6, 6", "0, 10, 1", "3, 100, 7", "5, 16, 9"})
    @DisplayName("問題例1〜4: リーフノード数の最大値を返す")
    void returnsMaximumLeavesInExamples(int distLimit, int splitLimit, int expected) {
        assertThat(DistributionTree.solve(distLimit, splitLimit)).isEqualTo(expected);
    }

    @Test
    @DisplayName("「3 の深さを先に置く」貪欲法では5個になる例で、2 → 3 の順の6個を返す")
    void prefersTwoBeforeThree() {
        assertThat(DistributionTree.solve(3, 6)).isEqualTo(6);
    }

    @Test
    @DisplayName("「上の深さから満杯に埋める」貪欲法では18個になる例で、満杯にしない段を持つ19個を返す")
    void leavesOneLevelUnfilledWhenBeneficial() {
        // 子の数 2, 2, 2, 3 の深さに分配ノードを 1, 2, 3, 6 個置く(3段目を満杯の4個にすると18個)
        assertThat(DistributionTree.solve(12, 24)).isEqualTo(19);
    }

    @Test
    @DisplayName("総当たりとの一致: distLimit ≤ 40、splitLimit ≤ 300 の全組み合わせで総当たりの結果と一致する")
    void matchesBruteForceOnSmallInputs() {
        BruteForce bruteForce = new BruteForce();
        for (int distLimit = 0; distLimit <= 40; distLimit++) {
            for (int splitLimit = 1; splitLimit <= 300; splitLimit++) {
                assertThat(DistributionTree.solve(distLimit, splitLimit))
                        .as("distLimit=%d, splitLimit=%d", distLimit, splitLimit)
                        .isEqualTo(bruteForce.maxLeaves(distLimit, splitLimit));
            }
        }
    }

    @ParameterizedTest(name = "distLimit={0}, splitLimit=1 → 1")
    @ValueSource(ints = {0, 1, 10, MAX_VALUE})
    @DisplayName("splitLimit=1 なら、分配ノードを置けないためどの distLimit でも1を返す")
    void returnsOneWhenSplitLimitIsOne(int distLimit) {
        assertThat(DistributionTree.solve(distLimit, 1)).isEqualTo(1);
    }

    @ParameterizedTest(name = "distLimit=1, splitLimit={0} → {1}")
    @CsvSource({"2, 2", "3, 3"})
    @DisplayName("分配ノード1個なら、子の数(2 または 3)がそのままリーフ数になる")
    void handlesSingleDistributionNode(int splitLimit, int expected) {
        assertThat(DistributionTree.solve(1, splitLimit)).isEqualTo(expected);
    }

    @Test
    @DisplayName("最大値の入力(10^9, 10^9)で、設計時の計算値 967458816 を返す(回帰テスト)")
    void handlesMaximumInput() {
        assertThat(DistributionTree.solve(MAX_VALUE, MAX_VALUE)).isEqualTo(967_458_816);
    }

    @Test
    @DisplayName("distLimit が小さく splitLimit が十分大きいとき、すべて子3つの分配ノードにできる(1 + 2 × 10)")
    void usesOnlyThreeChildNodesWhenSplitLimitIsLarge() {
        assertThat(DistributionTree.solve(10, MAX_VALUE)).isEqualTo(21);
    }

    @Test
    @DisplayName("distLimit=0 なら、splitLimit が最大でも1を返す")
    void returnsOneWhenNoDistributionNodeAllowed() {
        assertThat(DistributionTree.solve(0, MAX_VALUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("最大値の入力でも100ms以内に完了する")
    void completesWithinTimeLimitOnMaximumInput() {
        DistributionTree.solve(MAX_VALUE, MAX_VALUE); // JIT のウォームアップ

        assertTimeout(Duration.ofMillis(100), () -> DistributionTree.solve(MAX_VALUE, MAX_VALUE - 1));
    }

    @ParameterizedTest(name = "distLimit={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {-1, MAX_VALUE + 1})
    @DisplayName("distLimit が範囲(0〜10^9)を外れる場合は IllegalArgumentException を送出する")
    void rejectsDistLimitOutOfRange(int distLimit) {
        assertThatThrownBy(() -> DistributionTree.solve(distLimit, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "splitLimit={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1, MAX_VALUE + 1})
    @DisplayName("splitLimit が範囲(1〜10^9)を外れる場合は IllegalArgumentException を送出する")
    void rejectsSplitLimitOutOfRange(int splitLimit) {
        assertThatThrownBy(() -> DistributionTree.solve(3, splitLimit))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 全配置をメモ化再帰で探索する総当たり(テスト用オラクル)。
     * 各深さで子の数 c(2 または 3)と分配ノード数 m(1〜min(ノード数, 残り予算))をすべて試す。
     */
    private static final class BruteForce {

        private final Map<Long, Integer> memo = new HashMap<>();

        int maxLeaves(int distLimit, int splitLimit) {
            return 1 + maxGain(1, distLimit, splitLimit);
        }

        /**
         * @param nodes  現在の深さで分配ノードにできるノード数
         * @param budget 残りの分配ノード数
         * @param split  子の数の積の残り予算(splitLimit を上の段の子の数で割った値)
         * @return 現在の深さ以降で増やせるリーフ数の最大値
         */
        private int maxGain(int nodes, int budget, int split) {
            int usable = Math.min(nodes, budget); // 予算を超えるノード数は結果に影響しない
            long key = ((long) usable * 64 + budget) * 1024 + split;
            Integer cached = memo.get(key);
            if (cached != null) {
                return cached;
            }
            int best = 0;
            for (int children = 2; children <= 3; children++) {
                if (children > split) {
                    continue;
                }
                for (int m = 1; m <= usable; m++) {
                    int gain = m * (children - 1) + maxGain(m * children, budget - m, split / children);
                    best = Math.max(best, gain);
                }
            }
            memo.put(key, best);
            return best;
        }
    }
}
