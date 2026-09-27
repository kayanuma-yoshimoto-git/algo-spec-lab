package com.example.algospeclab.algo.numberbaseball;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * specs/number-baseball/README.md 「5. 設計のアプローチ」で採用した
 * 候補集合の絞り込み + ミニマックス(最大分割サイズ最小化)による実装。
 *
 * <p>状態は solve 呼び出しごとに生成し、呼び出し間で共有しない(毎回オンラインで提出値を計算する)。</p>
 */
public final class NumberBaseball {

    private static final int MIN_N = 6;
    private static final int MAX_N = 3024;
    /** 暗証番号になりうる全3,024通り(昇順)。 */
    private static final int[] ALL_SECRETS = IntStream
            .rangeClosed(Judge.MIN_NUMBER, Judge.MAX_NUMBER)
            .filter(Judge::isValidSecret)
            .toArray();

    private NumberBaseball() {
    }

    /**
     * submit を呼び出して暗証番号を特定し、返す。
     *
     * @param n         提出可能な最大回数(6〜3,024)
     * @param submitter 数を提出する関数
     * @return 暗証番号
     * @throws IllegalArgumentException n が範囲外、または submitter が null の場合(1回も提出しない)
     * @throws IllegalStateException    手がかりが不正・矛盾している、または n 回以内に特定できない場合
     */
    public static int solve(int n, Submitter submitter) {
        if (n < MIN_N || n > MAX_N) {
            throw new IllegalArgumentException("n は " + MIN_N + "〜" + MAX_N + " で指定してください: " + n);
        }
        if (submitter == null) {
            throw new IllegalArgumentException("submitter を指定してください。");
        }

        int[] candidates = ALL_SECRETS.clone();
        int size = candidates.length;
        int submitCount = 0;
        while (size > 1) {
            if (submitCount == n) {
                throw new IllegalStateException(n + " 回の提出で暗証番号を1つに特定できませんでした。");
            }
            int guess = chooseGuess(candidates, size);
            int clue = Judge.parse(submitter.submit(guess));
            submitCount++;
            size = retainConsistent(candidates, size, guess, clue);
        }
        if (size == 0) {
            throw new IllegalStateException("手がかりが矛盾しており、暗証番号の候補が残っていません。");
        }
        return candidates[0];
    }

    /**
     * 残り候補を手がかりごとに分類したときの最大グループが最小になる提出値を選ぶ。
     * 同値の場合は「グループサイズの二乗和が小さい → 残り候補に含まれる → 小さい数」の順で決める。
     */
    private static int chooseGuess(int[] candidates, int size) {
        int[] masks = new int[size];
        boolean[] isCandidate = new boolean[Judge.MAX_NUMBER + 1];
        for (int i = 0; i < size; i++) {
            masks[i] = Judge.digitMask(candidates[i]);
            isCandidate[candidates[i]] = true;
        }

        int bestGuess = -1;
        int bestMax = Integer.MAX_VALUE;
        long bestSquareSum = Long.MAX_VALUE;
        boolean bestIsCandidate = false;
        int[] counts = new int[Judge.CLUE_KINDS];
        for (int guess = Judge.MIN_NUMBER; guess <= Judge.MAX_NUMBER; guess++) {
            Arrays.fill(counts, 0);
            int max = 0;
            for (int i = 0; i < size && max <= bestMax; i++) {
                max = Math.max(max, ++counts[Judge.judge(guess, candidates[i], masks[i])]);
            }
            if (max > bestMax) {
                continue; // 最大グループが現在の最良より大きい時点で打ち切る
            }
            long squareSum = 0;
            for (int count : counts) {
                squareSum += (long) count * count;
            }
            if (max < bestMax
                    || squareSum < bestSquareSum
                    || (squareSum == bestSquareSum && isCandidate[guess] && !bestIsCandidate)) {
                bestGuess = guess;
                bestMax = max;
                bestSquareSum = squareSum;
                bestIsCandidate = isCandidate[guess];
            }
        }
        return bestGuess;
    }

    /** guess に対する手がかりが clue と一致する候補だけを配列の先頭に詰め、残った個数を返す。 */
    private static int retainConsistent(int[] candidates, int size, int guess, int clue) {
        int kept = 0;
        for (int i = 0; i < size; i++) {
            int secret = candidates[i];
            if (Judge.judge(guess, secret, Judge.digitMask(secret)) == clue) {
                candidates[kept++] = secret;
            }
        }
        return kept;
    }
}
