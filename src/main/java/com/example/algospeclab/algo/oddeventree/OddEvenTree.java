package com.example.algospeclab.algo.oddeventree;

/**
 * specs/odd-even-tree/README.md 「5. 設計のアプローチ」で採用した実装。
 *
 * <p>根の子の数は次数、根以外の子の数は「次数 - 1」なので、各ノードの
 * {@code same = (番号の偶奇 == 次数の偶奇)} だけで判定できる。
 * 木の中で same のノードがちょうど1個なら偶奇ツリー、!same のノードがちょうど1個なら逆偶奇ツリーになれる。
 * 木ごとの集計には Union-Find を使う。</p>
 */
public final class OddEvenTree {

    private static final int MAX_NODES = 400_000;
    private static final int MAX_EDGES = 1_000_000;
    private static final int MAX_LABEL = 1_000_000;

    private OddEvenTree() {
    }

    /**
     * 偶奇ツリーになれる木の数と、逆偶奇ツリーになれる木の数を返す。
     *
     * <p>入力はフォレストであることを前提とし、値の範囲と形式のみを検証する
     * (閉路や nodes に無い番号を指す辺を含む場合の結果は未定義)。</p>
     *
     * @param nodes フォレストのノード番号(1〜1,000,000、1〜400,000 件)
     * @param edges 無向辺 [a, b] の一覧(1〜1,000,000 件)
     * @return {@code [偶奇ツリーになれる木の数, 逆偶奇ツリーになれる木の数]}
     * @throws IllegalArgumentException 値の範囲または形式が不正な場合
     */
    public static int[] solve(int[] nodes, int[][] edges) {
        validate(nodes, edges);
        int size = maxLabel(nodes, edges) + 1;

        int[] parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
        int[] degree = new int[size];
        for (int[] edge : edges) {
            degree[edge[0]]++;
            degree[edge[1]]++;
            parent[find(parent, edge[0])] = find(parent, edge[1]);
        }

        // 木の代表ノードごとに、same のノード数と !same のノード数を数える
        int[] sameCount = new int[size];
        int[] mismatchCount = new int[size];
        for (int node : nodes) {
            int root = find(parent, node);
            if (node % 2 == degree[node] % 2) {
                sameCount[root]++;
            } else {
                mismatchCount[root]++;
            }
        }

        int oddEven = 0;
        int reverseOddEven = 0;
        for (int node : nodes) {
            if (find(parent, node) == node) {
                oddEven += sameCount[node] == 1 ? 1 : 0;
                reverseOddEven += mismatchCount[node] == 1 ? 1 : 0;
            }
        }
        return new int[] {oddEven, reverseOddEven};
    }

    /** x が属する集合の代表を返す(経路半減で木を平らにする)。 */
    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static int maxLabel(int[] nodes, int[][] edges) {
        int max = 0;
        for (int node : nodes) {
            max = Math.max(max, node);
        }
        for (int[] edge : edges) {
            max = Math.max(max, Math.max(edge[0], edge[1]));
        }
        return max;
    }

    private static void validate(int[] nodes, int[][] edges) {
        if (nodes == null || nodes.length == 0 || nodes.length > MAX_NODES) {
            throw new IllegalArgumentException("nodes は 1〜" + MAX_NODES + " 件で指定してください。");
        }
        for (int node : nodes) {
            requireLabelInRange(node, "nodes の要素");
        }
        if (edges == null || edges.length == 0 || edges.length > MAX_EDGES) {
            throw new IllegalArgumentException("edges は 1〜" + MAX_EDGES + " 件で指定してください。");
        }
        for (int[] edge : edges) {
            if (edge == null || edge.length != 2) {
                throw new IllegalArgumentException("edges の要素は [a, b] の形式で指定してください。");
            }
            requireLabelInRange(edge[0], "edges の端点");
            requireLabelInRange(edge[1], "edges の端点");
        }
    }

    private static void requireLabelInRange(int label, String name) {
        if (label < 1 || label > MAX_LABEL) {
            throw new IllegalArgumentException(name + "は 1〜" + MAX_LABEL + " で指定してください: " + label);
        }
    }
}
