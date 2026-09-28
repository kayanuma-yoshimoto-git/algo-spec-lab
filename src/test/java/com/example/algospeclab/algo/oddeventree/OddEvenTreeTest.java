package com.example.algospeclab.algo.oddeventree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/odd-even-tree/README.md の「4. 正常系と異常系の定義」に対応するテスト。
 * 実装は {@link OddEvenTree#solve(int[], int[][])}(番号と次数の偶奇が一致するノード数を木ごとに数える方式)。
 */
class OddEvenTreeTest {

    private static final int MAX_NODES = 400_000;
    private static final int MAX_EDGES = 1_000_000;
    private static final int MAX_LABEL = 1_000_000;

    @Test
    @DisplayName("問題例1: 偶奇ツリーになれる木が1つ、逆偶奇ツリーになれる木は0")
    void example1() {
        int[] nodes = {11, 9, 3, 2, 4, 6};
        int[][] edges = {{9, 11}, {2, 3}, {6, 3}, {3, 4}};

        assertThat(OddEvenTree.solve(nodes, edges)).containsExactly(1, 0);
    }

    @Test
    @DisplayName("問題例2: 偶奇ツリーになれる木が2つ、逆偶奇ツリーになれる木が1つ")
    void example2() {
        int[] nodes = {9, 15, 14, 7, 6, 1, 2, 4, 5, 11, 8, 10};
        int[][] edges = {{5, 14}, {1, 4}, {9, 11}, {2, 15}, {2, 5}, {9, 7}, {8, 1}, {6, 4}};

        assertThat(OddEvenTree.solve(nodes, edges)).containsExactly(2, 1);
    }

    @Test
    @DisplayName("根の選び方次第で、1つの木が偶奇ツリーにも逆偶奇ツリーにもなれる")
    void countsTreeThatCanBeBoth() {
        // 5 を根にすると偶奇ツリー、6 を根にすると逆偶奇ツリー
        assertThat(OddEvenTree.solve(new int[] {5, 6}, new int[][] {{5, 6}})).containsExactly(1, 1);
    }

    @Test
    @DisplayName("どの根を選んでも偶奇ツリーにも逆偶奇ツリーにもなれない木は数えない")
    void ignoresTreeThatCanBeNeither() {
        assertThat(OddEvenTree.solve(new int[] {9, 11}, new int[][] {{9, 11}})).containsExactly(0, 0);
    }

    @Test
    @DisplayName("孤立ノードは、偶数番号なら偶奇ツリー、奇数番号なら逆偶奇ツリーとして数える")
    void countsIsolatedNodes() {
        assertThat(OddEvenTree.solve(new int[] {2, 5, 6}, new int[][] {{5, 6}})).containsExactly(2, 1);
        assertThat(OddEvenTree.solve(new int[] {3, 5, 6}, new int[][] {{5, 6}})).containsExactly(1, 2);
    }

    @Test
    @DisplayName("総当たりとの一致: 小さいランダムなフォレストで、全ノードを根に試す総当たりと一致する")
    void matchesBruteForceOnSmallForests() {
        Random random = new Random(20260928L);
        for (int trial = 0; trial < 3000; trial++) {
            int[] nodes = randomLabels(random, 2 + random.nextInt(11));
            List<int[]> edgeList = new ArrayList<>();
            edgeList.add(new int[] {nodes[1], nodes[0]}); // 辺を最低1本含める
            for (int i = 2; i < nodes.length; i++) {
                if (random.nextInt(4) != 0) {
                    edgeList.add(new int[] {nodes[i], nodes[random.nextInt(i)]});
                }
            }
            int[][] edges = edgeList.toArray(new int[0][]);

            assertThat(OddEvenTree.solve(nodes, edges))
                    .as("nodes=%s, edges=%s", Arrays.toString(nodes), Arrays.deepToString(edges))
                    .containsExactly(bruteForce(nodes, edges));
        }
    }

    @Test
    @DisplayName("ノード番号の最小値1と最大値1000000を含む入力を正しく扱う")
    void handlesMinimumAndMaximumLabels() {
        // 1(奇数・次数1)は same、1000000(偶数・次数1)は !same なので両方になれる
        assertThat(OddEvenTree.solve(new int[] {1, MAX_LABEL}, new int[][] {{1, MAX_LABEL}}))
                .containsExactly(1, 1);
    }

    @Test
    @DisplayName("最大規模(40万ノードの1本のパス)でも1秒以内に完了する")
    void completesWithinTimeLimitOnMaximumPath() {
        int[] nodes = new int[MAX_NODES];
        int[][] edges = new int[MAX_NODES - 1][];
        for (int i = 0; i < MAX_NODES; i++) {
            nodes[i] = 1 + 2 * i; // すべて奇数: 両端は same、途中は !same のため、どちらにもなれない
            if (i > 0) {
                edges[i - 1] = new int[] {nodes[i - 1], nodes[i]};
            }
        }
        OddEvenTree.solve(nodes, edges); // JIT のウォームアップ

        assertTimeout(Duration.ofSeconds(1),
                () -> assertThat(OddEvenTree.solve(nodes, edges)).containsExactly(0, 0));
    }

    @Test
    @DisplayName("nodes が null・空配列・400,001件の場合は IllegalArgumentException を送出する")
    void rejectsInvalidNodesSize() {
        int[][] edges = {{1, 2}};
        assertThatThrownBy(() -> OddEvenTree.solve(null, edges))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OddEvenTree.solve(new int[0], edges))
                .isInstanceOf(IllegalArgumentException.class);
        int[] tooMany = new int[MAX_NODES + 1];
        for (int i = 0; i < tooMany.length; i++) {
            tooMany[i] = i + 1;
        }
        assertThatThrownBy(() -> OddEvenTree.solve(tooMany, edges))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "nodes の要素 {0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, -1, MAX_LABEL + 1})
    @DisplayName("nodes の要素が範囲(1〜1000000)を外れる場合は IllegalArgumentException を送出する")
    void rejectsNodeLabelOutOfRange(int invalid) {
        assertThatThrownBy(() -> OddEvenTree.solve(new int[] {1, invalid}, new int[][] {{1, 2}}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("edges が null・空配列・1,000,001件の場合は IllegalArgumentException を送出する")
    void rejectsInvalidEdgesSize() {
        int[] nodes = {1, 2};
        assertThatThrownBy(() -> OddEvenTree.solve(nodes, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OddEvenTree.solve(nodes, new int[0][]))
                .isInstanceOf(IllegalArgumentException.class);
        int[][] tooMany = new int[MAX_EDGES + 1][];
        Arrays.fill(tooMany, new int[] {1, 2});
        assertThatThrownBy(() -> OddEvenTree.solve(nodes, tooMany))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("edges の要素が null、長さが2でない、端点が範囲外の場合は IllegalArgumentException を送出する")
    void rejectsInvalidEdgeElement() {
        int[] nodes = {1, 2};
        List<int[][]> invalidEdges = List.of(
                new int[][] {null},
                new int[][] {{1}},
                new int[][] {{1, 2, 3}},
                new int[][] {{0, 1}},
                new int[][] {{1, MAX_LABEL + 1}});
        for (int[][] edges : invalidEdges) {
            assertThatThrownBy(() -> OddEvenTree.solve(nodes, edges))
                    .as("edges=%s", Arrays.deepToString(edges))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    /** 1〜30 から互いに異なる番号を count 個選ぶ。 */
    private static int[] randomLabels(Random random, int count) {
        List<Integer> labels = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            labels.add(i);
        }
        Collections.shuffle(labels, random);
        return labels.subList(0, count).stream().mapToInt(Integer::intValue).toArray();
    }

    /** 各木で全ノードを根に試し、子の数を数えて定義どおりに判定する総当たり(テスト用オラクル)。 */
    private static int[] bruteForce(int[] nodes, int[][] edges) {
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        for (int node : nodes) {
            adjacency.put(node, new ArrayList<>());
        }
        for (int[] edge : edges) {
            adjacency.get(edge[0]).add(edge[1]);
            adjacency.get(edge[1]).add(edge[0]);
        }
        Set<Integer> visited = new HashSet<>();
        int oddEven = 0;
        int reverse = 0;
        for (int start : nodes) {
            if (!visited.add(start)) {
                continue;
            }
            List<Integer> tree = new ArrayList<>();
            Deque<Integer> queue = new ArrayDeque<>(List.of(start));
            while (!queue.isEmpty()) {
                int node = queue.poll();
                tree.add(node);
                for (int next : adjacency.get(node)) {
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }
            boolean canBeOddEven = false;
            boolean canBeReverse = false;
            for (int root : tree) {
                boolean allMatch = true;
                boolean allMismatch = true;
                for (int node : tree) {
                    int children = adjacency.get(node).size() - (node == root ? 0 : 1);
                    if (node % 2 == children % 2) {
                        allMismatch = false;
                    } else {
                        allMatch = false;
                    }
                }
                canBeOddEven |= allMatch;
                canBeReverse |= allMismatch;
            }
            oddEven += canBeOddEven ? 1 : 0;
            reverse += canBeReverse ? 1 : 0;
        }
        return new int[] {oddEven, reverse};
    }
}
