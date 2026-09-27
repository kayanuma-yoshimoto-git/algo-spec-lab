package com.example.algospeclab.algo.runlengthwindowsum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/run-length-window-sum/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link RunLengthWindowSum#solve(int[], long, long)}(区分線形性 + 2ポインタ方式)。
 */
class RunLengthWindowSumTest {

    private static final int MAX_N = 100_000;
    private static final int MAX_VALUE = 100_000;

    @Test
    @DisplayName("問題例1: 5〜7番目の区間和は8で、同じ長さ・同じ和の部分配列は2個")
    void example1() {
        assertThat(RunLengthWindowSum.solve(new int[] {3, 2, 3, 1, 1}, 5, 7))
                .containsExactly(8L, 2L);
    }

    @Test
    @DisplayName("問題例2: 全要素が2の配列で長さ1の区間和は2で、該当する部分配列は6個")
    void example2() {
        assertThat(RunLengthWindowSum.solve(new int[] {2, 2, 2}, 2, 2))
                .containsExactly(2L, 6L);
    }

    @Test
    @DisplayName("問題例3: 25〜27番目の区間和は15で、該当する部分配列は3個")
    void example3() {
        assertThat(RunLengthWindowSum.solve(new int[] {8, 8, 6, 5, 2, 9, 8, 4, 3, 10}, 25, 27))
                .containsExactly(15L, 3L);
    }

    @Test
    @DisplayName("問題例4: 大きな値で区間和が int を超え、該当する部分配列は1個")
    void example4() {
        int[] arr = {70195, 25471, 7389, 58187, 18454, 90532, 97667, 17148, 91636, 2810};

        assertThat(RunLengthWindowSum.solve(arr, 126058, 462933))
                .containsExactly(27554327568L, 1L);
    }

    @Test
    @DisplayName("問題例5: 該当する部分配列が9190個ある")
    void example5() {
        int[] arr = {16952, 70276, 16771, 37992, 87549, 54906, 36718, 20478, 57088, 27916,
                51509, 83422, 51707, 18807, 80859, 2673, 37734, 93380};

        assertThat(RunLengthWindowSum.solve(arr, 149845, 228204))
                .containsExactly(6860339640L, 9190L);
    }

    @Test
    @DisplayName("問題例6: 該当する部分配列が59513個ある")
    void example6() {
        int[] arr = {49134, 86806, 94548, 88849, 95022, 28334, 16637, 79487, 23773, 7314,
                47370, 50269, 36573, 9415, 44674, 28096};

        assertThat(RunLengthWindowSum.solve(arr, 61242, 88535))
                .containsExactly(2369282964L, 59513L);
    }

    @Test
    @DisplayName("全要素が同じ値のとき、同じ長さの別区間もすべて同じ和になる")
    void countsAllWindowsWhenValuesAreUniform() {
        assertThat(RunLengthWindowSum.solve(new int[] {2, 2, 2}, 2, 3))
                .containsExactly(4L, 5L);
    }

    @Test
    @DisplayName("窓の和が変化する場合、和が一致する部分配列だけを数える")
    void countsOnlyMatchingWindowWhenSumChanges() {
        // brr = [1, 2, 2]。[1, 2] は和3、[2, 2] は和4
        assertThat(RunLengthWindowSum.solve(new int[] {1, 2}, 1, 2))
                .containsExactly(3L, 1L);
    }

    @Test
    @DisplayName("小さいランダム入力で、brr を展開して数える愚直解と結果が一致する")
    void matchesBruteForceOnRandomSmallInputs() {
        Random random = new Random(20260927L);
        for (int trial = 0; trial < 3000; trial++) {
            int[] arr = new int[1 + random.nextInt(8)];
            int total = 0;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = 1 + random.nextInt(6);
                total += arr[i];
            }
            int l = 1 + random.nextInt(total);
            int r = l + random.nextInt(total - l + 1);

            assertThat(RunLengthWindowSum.solve(arr, l, r))
                    .as("arr=%s, l=%d, r=%d", Arrays.toString(arr), l, r)
                    .containsExactly(bruteForce(arr, l, r));
        }
    }

    @Test
    @DisplayName("最小入力: arr=[1], l=r=1 のとき [1, 1] を返す")
    void handlesMinimumInput() {
        assertThat(RunLengthWindowSum.solve(new int[] {1}, 1, 1))
                .containsExactly(1L, 1L);
    }

    @Test
    @DisplayName("l=r のとき、区間和はその要素の値で、同じ値の要素数が個数になる")
    void handlesSingleElementRange() {
        // brr = [3, 3, 3, 2, 2, 3, 3, 3, 1, 1]。4番目は2で、値2の要素は2個
        assertThat(RunLengthWindowSum.solve(new int[] {3, 2, 3, 1, 1}, 4, 4))
                .containsExactly(2L, 2L);
    }

    @Test
    @DisplayName("全区間(l=1, r=brrの長さ)のとき、区間和は全要素の和で個数は1")
    void handlesWholeRange() {
        assertThat(RunLengthWindowSum.solve(new int[] {3, 2, 3, 1, 1}, 1, 10))
                .containsExactly(24L, 1L);
    }

    @Test
    @DisplayName("最大規模の全区間で、区間和が上限の10^15になる")
    void handlesMaximumSum() {
        int[] arr = maxSizeArray();

        assertThat(RunLengthWindowSum.solve(arr, 1, 10_000_000_000L))
                .containsExactly(1_000_000_000_000_000L, 1L);
    }

    @Test
    @DisplayName("最大規模で l=r=1 のとき、個数が int の範囲を超える10^10になる")
    void handlesCountExceedingIntRange() {
        int[] arr = maxSizeArray();

        assertThat(RunLengthWindowSum.solve(arr, 1, 1))
                .containsExactly(100_000L, 10_000_000_000L);
    }

    @Test
    @DisplayName("最大規模の入力でも1秒以内に計算が完了する")
    void completesWithinTimeLimitOnMaximumInput() {
        int[] arr = new int[MAX_N];
        Random random = new Random(42L);
        for (int i = 0; i < MAX_N; i++) {
            arr[i] = 1 + random.nextInt(MAX_VALUE);
        }
        long total = Arrays.stream(arr).asLongStream().sum();

        assertTimeout(Duration.ofSeconds(1), () -> RunLengthWindowSum.solve(arr, total / 3, total / 3 * 2));
    }

    @Test
    @DisplayName("arr が null の場合は IllegalArgumentException を送出する")
    void rejectsNullArray() {
        assertThatThrownBy(() -> RunLengthWindowSum.solve(null, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("arr が空配列の場合は IllegalArgumentException を送出する")
    void rejectsEmptyArray() {
        assertThatThrownBy(() -> RunLengthWindowSum.solve(new int[0], 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "arr[1]={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1, MAX_VALUE + 1})
    @DisplayName("arr[i] が範囲(1〜100000)を外れる場合は IllegalArgumentException を送出する")
    void rejectsElementOutOfRange(int invalidValue) {
        int[] arr = {3, invalidValue, 3};

        assertThatThrownBy(() -> RunLengthWindowSum.solve(arr, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("arr の長さが100000を超える場合は IllegalArgumentException を送出する")
    void rejectsTooLongArray() {
        int[] arr = new int[MAX_N + 1];
        Arrays.fill(arr, 1);

        assertThatThrownBy(() -> RunLengthWindowSum.solve(arr, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "l={0}, r={1} の場合は IllegalArgumentException を送出する")
    @CsvSource({"0, 3", "-1, 3", "5, 4"})
    @DisplayName("l が1未満、または l > r の場合は IllegalArgumentException を送出する")
    void rejectsInvalidLeftBound(long l, long r) {
        assertThatThrownBy(() -> RunLengthWindowSum.solve(new int[] {3, 2, 3, 1, 1}, l, r))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("r が brr の長さ(arr の総和)を超える場合は IllegalArgumentException を送出する")
    void rejectsRightBoundBeyondLength() {
        assertThatThrownBy(() -> RunLengthWindowSum.solve(new int[] {3, 2, 3, 1, 1}, 1, 11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** 長さ・各要素ともに上限値の配列を返す。 */
    private static int[] maxSizeArray() {
        int[] arr = new int[MAX_N];
        Arrays.fill(arr, MAX_VALUE);
        return arr;
    }

    /** brr を実際に展開し、全ての窓を数え上げる愚直解(テスト用オラクル)。 */
    private static long[] bruteForce(int[] arr, int l, int r) {
        int[] brr = Arrays.stream(arr).flatMap(v -> Arrays.stream(new int[v]).map(x -> v)).toArray();
        int length = r - l + 1;
        long k = 0;
        for (int i = l - 1; i < r; i++) {
            k += brr[i];
        }
        long c = 0;
        for (int start = 0; start + length <= brr.length; start++) {
            long sum = 0;
            for (int i = start; i < start + length; i++) {
                sum += brr[i];
            }
            if (sum == k) {
                c++;
            }
        }
        return new long[] {k, c};
    }
}
