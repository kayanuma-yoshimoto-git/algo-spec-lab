package com.example.algospeclab.algo.yellowlightsync;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * specs/yellow-light-sync/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link YellowLightSync#solve(int[][])}(区間交差方式)。
 */
class YellowLightSyncTest {

    @Test
    @DisplayName("問題例1: 信号機が2つのとき、13秒で初めて全て黄色になる")
    void example1() {
        int[][] signals = {{2, 1, 2}, {5, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(13);
    }

    @Test
    @DisplayName("問題例2: 信号機が3つのとき、11秒で全て黄色になる")
    void example2() {
        int[][] signals = {{2, 3, 2}, {3, 1, 3}, {2, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(11);
    }

    @Test
    @DisplayName("問題例3: LCMが大きく、193秒まで全て黄色にならないケース")
    void example3() {
        int[][] signals = {{3, 3, 3}, {5, 4, 2}, {2, 1, 2}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(193);
    }

    @Test
    @DisplayName("全ての信号機が同時に黄色になる時刻が存在しない場合は-1を返す")
    void returnsMinusOneWhenNeverAllYellow() {
        int[][] signals = {{1, 1, 4}, {2, 1, 3}, {3, 1, 2}, {4, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(-1);
    }

    @Test
    @DisplayName("信号機が5つ(最大個数)でも正しく計算できる")
    void handlesMaximumSignalCount() {
        int[][] signals = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(2);
    }

    @Test
    @DisplayName("信号機が2つ(最小個数)でも正しく計算できる")
    void handlesMinimumSignalCount() {
        int[][] signals = {{1, 1, 1}, {1, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(2);
    }

    @Test
    @DisplayName("G・Y・Rが最小値(1)の境界値でも正しく計算できる")
    void handlesMinimumDurationBoundary() {
        int[][] signals = {{1, 1, 1}, {1, 1, 4}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(2);
    }

    @Test
    @DisplayName("G・Y・Rが最大値(18)付近の境界値でも正しく計算できる")
    void handlesMaximumDurationBoundary() {
        int[][] signals = {{18, 1, 1}, {18, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(19);
    }

    @Test
    @DisplayName("全ての信号機の周期が同一でも、黄色区間の重なりを正しく計算できる")
    void handlesSamePeriodDifferentOffset() {
        int[][] signals = {{2, 1, 2}, {1, 2, 2}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(3);
    }

    @Test
    @DisplayName("周期がほぼ互いに素な組み合わせでも、LCM上限内で正しく計算できる(性能確認)")
    void handlesNearCoprimePeriodsWithinLcmBound() {
        int[][] signals = {{18, 1, 1}, {17, 1, 1}, {15, 1, 1}, {11, 1, 1}, {9, 1, 1}};

        assertThat(YellowLightSync.solve(signals)).isEqualTo(923_779);
    }
}
