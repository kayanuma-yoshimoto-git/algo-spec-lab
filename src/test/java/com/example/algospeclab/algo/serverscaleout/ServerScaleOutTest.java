package com.example.algospeclab.algo.serverscaleout;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * specs/server-scale-out/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link ServerScaleOut#solve(int[], int, int)}(貪欲法 + 期限管理キュー方式)。
 */
class ServerScaleOutTest {

    @Test
    @DisplayName("問題例1: m=3, k=5 のとき、増設回数の合計は7回")
    void example1() {
        int[] players = {0, 2, 3, 3, 1, 2, 0, 0, 0, 0, 4, 2, 0, 6, 0, 4, 2, 13, 3, 5, 10, 0, 1, 5};

        assertThat(ServerScaleOut.solve(players, 3, 5)).isEqualTo(7);
    }

    @Test
    @DisplayName("問題例2: m=5, k=1 のとき、増設回数の合計は11回")
    void example2() {
        int[] players = {0, 0, 0, 10, 0, 12, 0, 15, 0, 1, 0, 1, 0, 0, 0, 5, 0, 0, 11, 0, 8, 0, 0, 0};

        assertThat(ServerScaleOut.solve(players, 5, 1)).isEqualTo(11);
    }

    @Test
    @DisplayName("問題例3: m=1, k=1 のとき、増設回数の合計は12回")
    void example3() {
        int[] players = {0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 5, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1};

        assertThat(ServerScaleOut.solve(players, 1, 1)).isEqualTo(12);
    }

    @Test
    @DisplayName("全時間帯の利用者が0なら、増設は不要")
    void returnsZeroWhenNoPlayersAllDay() {
        int[] players = new int[24];

        assertThat(ServerScaleOut.solve(players, 3, 5)).isEqualTo(0);
    }

    @Test
    @DisplayName("サーバーがちょうど期限切れになる時刻に再び必要になる場合、正しく再増設する")
    void reProvisionsExactlyWhenPreviousServerExpires() {
        int[] players = new int[24];
        players[0] = 1;
        players[1] = 1;
        players[2] = 1;

        assertThat(ServerScaleOut.solve(players, 1, 2)).isEqualTo(2);
    }

    @Test
    @DisplayName("k=24(最大)で全時間帯同じ台数が必要な場合、最初の1回だけで済む")
    void handlesMaximumOperatingHours() {
        int[] players = new int[24];
        java.util.Arrays.fill(players, 1000);

        assertThat(ServerScaleOut.solve(players, 1000, 24)).isEqualTo(1);
    }

    @Test
    @DisplayName("k=1(最小)で全時間帯同じ台数が必要な場合、毎時間分の増設が必要になる")
    void handlesMinimumOperatingHours() {
        int[] players = new int[24];
        java.util.Arrays.fill(players, 1000);

        assertThat(ServerScaleOut.solve(players, 1000, 1)).isEqualTo(24);
    }

    @Test
    @DisplayName("m=1(最小)かつ利用者数が最大値(1000)の時間帯があっても正しく計算できる")
    void handlesMinimumMWithMaximumPlayers() {
        int[] players = new int[24];
        players[0] = 1000;

        assertThat(ServerScaleOut.solve(players, 1, 1)).isEqualTo(1000);
    }
}
