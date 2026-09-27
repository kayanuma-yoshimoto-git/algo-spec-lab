package com.example.algospeclab.algo.treasureexcavation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/treasure-excavation/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link TreasureExcavation#solve(int[], int, Excavator)}(区間 DP によるミニマックス戦略)。
 */
class TreasureExcavationTest {

    private static final int MAX_WIDTH = 200;
    private static final int MAX_DEPTH = 100_000;

    /** 問題例1〜6: depth, money, 宝の列。 */
    static Stream<Arguments> examples() {
        return Stream.of(
                Arguments.of(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 55, 3),
                Arguments.of(new int[] {1, 1, 1, 1, 1}, 3, 5),
                Arguments.of(new int[] {2, 100, 1, 100, 3, 100, 1}, 200, 6),
                Arguments.of(new int[] {2, 100, 1, 100, 3, 100, 1}, 200, 5),
                Arguments.of(new int[] {3, 2, 1, 2, 3, 2, 1, 2}, 8, 5),
                Arguments.of(new int[] {1, 1000, 1, 1, 1, 10, 15, 1}, 1002, 2));
    }

    @ParameterizedTest(name = "depth={0}, money={1} で宝が{2}列にあるとき、{2}を返す")
    @MethodSource("examples")
    @DisplayName("問題例1〜6: 宝の列を掘り当て、総コストが money 以内に収まる")
    void findsTreasureInExamples(int[] depth, int money, int treasureCol) {
        assertFound(depth, money, treasureCol);
    }

    @ParameterizedTest(name = "depth={0}, money={1} のすべての宝の位置で正解する")
    @MethodSource("examples")
    @DisplayName("全位置網羅: 問題例の各 depth で、宝をどの列に置いても money 以内で見つける")
    void findsTreasureAtEveryPositionInExamples(int[] depth, int money, int ignoredTreasureCol) {
        for (int treasureCol = 1; treasureCol <= depth.length; treasureCol++) {
            assertFound(depth, money, treasureCol);
        }
    }

    @Test
    @DisplayName("最適性: 小さいランダム入力で、全位置での最大コストが総当たりのミニマックス値と一致する")
    void worstCaseCostMatchesBruteForceMinimax() {
        Random random = new Random(20260928L);
        for (int trial = 0; trial < 300; trial++) {
            int[] depth = new int[2 + random.nextInt(6)];
            for (int i = 0; i < depth.length; i++) {
                depth[i] = 1 + random.nextInt(5);
            }
            long optimal = bruteForceMinimax(depth, 1, depth.length);
            int money = (int) optimal;

            long worst = 0;
            for (int treasureCol = 1; treasureCol <= depth.length; treasureCol++) {
                worst = Math.max(worst, assertFound(depth, money, treasureCol));
            }
            assertThat(worst)
                    .as("depth=%s", Arrays.toString(depth))
                    .isEqualTo(optimal);
        }
    }

    @Test
    @DisplayName("決定性: 同じ入力・同じ宝の位置なら、掘削順序は毎回同じになる")
    void excavationOrderIsDeterministic() {
        int[] depth = {3, 2, 1, 2, 3, 2, 1, 2};
        FixedTreasureExcavator first = new FixedTreasureExcavator(depth, 5);
        FixedTreasureExcavator second = new FixedTreasureExcavator(depth, 5);

        TreasureExcavation.solve(depth, 8, first);
        TreasureExcavation.solve(depth, 8, second);

        assertThat(first.excavatedColumns()).isEqualTo(second.excavatedColumns());
    }

    @Test
    @DisplayName("最小幅: depth=[1,1], money=2 で、宝が1列・2列のどちらでも見つける")
    void handlesMinimumWidth() {
        int[] depth = {1, 1};

        assertFound(depth, 2, 1);
        assertFound(depth, 2, 2);
    }

    @Test
    @DisplayName("全要素が同じ最大値・最大幅のとき、money=800000 ですべての宝の位置で見つける")
    void handlesUniformMaximumDepth() {
        int[] depth = new int[MAX_WIDTH];
        Arrays.fill(depth, MAX_DEPTH);

        for (int treasureCol = 1; treasureCol <= MAX_WIDTH; treasureCol++) {
            assertFound(depth, 800_000, treasureCol);
        }
    }

    @Test
    @DisplayName("最大幅のランダムな depth でも1秒以内に宝を見つける")
    void completesWithinTimeLimitOnMaximumWidth() {
        Random random = new Random(42L);
        int[] depth = new int[MAX_WIDTH];
        for (int i = 0; i < MAX_WIDTH; i++) {
            depth[i] = 1 + random.nextInt(MAX_DEPTH);
        }
        int money = Arrays.stream(depth).sum();

        assertTimeout(Duration.ofSeconds(1), () -> assertFound(depth, money, 137));
    }

    @Test
    @DisplayName("money が最悪ケース最小コスト未満なら、1回も掘らずに IllegalArgumentException を送出する")
    void rejectsInsufficientMoneyWithoutExcavating() {
        AtomicInteger calls = new AtomicInteger();
        Excavator counting = col -> {
            calls.incrementAndGet();
            return 0;
        };

        assertThatThrownBy(() -> TreasureExcavation.solve(new int[] {1, 1, 1, 1, 1}, 2, counting))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(calls).hasValue(0);
    }

    @Test
    @DisplayName("depth が null の場合は IllegalArgumentException を送出する")
    void rejectsNullDepth() {
        assertThatThrownBy(() -> TreasureExcavation.solve(null, 10, col -> 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "depth の長さが {0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, 1, MAX_WIDTH + 1})
    @DisplayName("depth の長さが範囲(2〜200)を外れる場合は IllegalArgumentException を送出する")
    void rejectsWidthOutOfRange(int width) {
        int[] depth = new int[width];
        Arrays.fill(depth, 1);

        assertThatThrownBy(() -> TreasureExcavation.solve(depth, Math.max(width, 1), col -> 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "depth[1]={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1, MAX_DEPTH + 1})
    @DisplayName("depth[i] が範囲(1〜100000)を外れる場合は IllegalArgumentException を送出する")
    void rejectsDepthValueOutOfRange(int invalidValue) {
        int[] depth = {1, invalidValue, 1};

        assertThatThrownBy(() -> TreasureExcavation.solve(depth, 1_000_000, col -> 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "money={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1})
    @DisplayName("money が0以下の場合は IllegalArgumentException を送出する")
    void rejectsNonPositiveMoney(int money) {
        assertThatThrownBy(() -> TreasureExcavation.solve(new int[] {1, 1}, money, col -> 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("excavator が null の場合は IllegalArgumentException を送出する")
    void rejectsNullExcavator() {
        assertThatThrownBy(() -> TreasureExcavation.solve(new int[] {1, 1}, 2, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("excavate が -1/0/1 以外を返した場合は IllegalStateException を送出する")
    void rejectsUnknownExcavationResult() {
        assertThatThrownBy(() -> TreasureExcavation.solve(new int[] {1, 1, 1}, 3, col -> 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("excavate が常に「右側」を返すなど、候補範囲の外を指す矛盾した応答なら IllegalStateException を送出する")
    void rejectsContradictoryExcavationResult() {
        assertThatThrownBy(() -> TreasureExcavation.solve(new int[] {1, 1, 1, 1, 1}, 5, col -> 1))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * 宝の位置を固定して solve を実行し、正解条件(0 を受け取った・返り値が宝の列・総コスト ≤ money)を検証する。
     *
     * @return 実際にかかった総コスト
     */
    private static long assertFound(int[] depth, int money, int treasureCol) {
        FixedTreasureExcavator excavator = new FixedTreasureExcavator(depth, treasureCol);

        int result = TreasureExcavation.solve(depth, money, excavator);

        String context = "depth=" + Arrays.toString(depth) + ", money=" + money + ", 宝=" + treasureCol;
        assertThat(result).as(context).isEqualTo(treasureCol);
        // 最後に掘った列が宝の列 = excavate から 0 を受け取っている
        assertThat(excavator.excavatedColumns()).as(context).last().isEqualTo(treasureCol);
        assertThat(excavator.totalCost()).as(context).isLessThanOrEqualTo(money);
        return excavator.totalCost();
    }

    /** 区間 [l, r] の宝を確実に掘り当てる最悪ケース最小コストを、メモ化なしの総当たりで求める(テスト用オラクル)。 */
    private static long bruteForceMinimax(int[] depth, int l, int r) {
        if (l > r) {
            return 0;
        }
        long best = Long.MAX_VALUE;
        for (int k = l; k <= r; k++) {
            long worstAfter = Math.max(bruteForceMinimax(depth, l, k - 1), bruteForceMinimax(depth, k + 1, r));
            best = Math.min(best, depth[k - 1] + worstAfter);
        }
        return best;
    }
}
