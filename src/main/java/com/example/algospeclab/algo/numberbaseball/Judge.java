package com.example.algospeclab.algo.numberbaseball;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数字野球の判定規則と、手がかりの符号化・文字列変換をまとめたユーティリティ。
 *
 * <p>手がかり (STRIKE 数 x, BALL 数 y) は {@code x * 5 + y}(0〜24)の整数に符号化して扱う。</p>
 */
final class Judge {

    static final int MIN_NUMBER = 1000;
    static final int MAX_NUMBER = 9999;
    /** 符号化した手がかりが取りうる値の個数。 */
    static final int CLUE_KINDS = 25;

    private static final Pattern CLUE_PATTERN = Pattern.compile("([0-4])S ([0-4])B");
    /** DIGITS[v - MIN_NUMBER] は v の各桁(千の位から順)。 */
    private static final int[][] DIGITS = new int[MAX_NUMBER - MIN_NUMBER + 1][];

    static {
        for (int v = MIN_NUMBER; v <= MAX_NUMBER; v++) {
            DIGITS[v - MIN_NUMBER] = new int[] {v / 1000, v / 100 % 10, v / 10 % 10, v % 10};
        }
    }

    private Judge() {
    }

    /** v が 1000〜9999 の範囲内かを返す。 */
    static boolean isInRange(int v) {
        return MIN_NUMBER <= v && v <= MAX_NUMBER;
    }

    /** v が暗証番号になりうる数(1〜9 の互いに異なる4桁)かを返す。 */
    static boolean isValidSecret(int v) {
        if (!isInRange(v)) {
            return false;
        }
        int seen = 0;
        for (int digit : DIGITS[v - MIN_NUMBER]) {
            if (digit == 0 || (seen & (1 << digit)) != 0) {
                return false;
            }
            seen |= 1 << digit;
        }
        return true;
    }

    /** 暗証番号 secret に含まれる数字のビットマスクを返す(判定の高速化用)。 */
    static int digitMask(int secret) {
        int mask = 0;
        for (int digit : DIGITS[secret - MIN_NUMBER]) {
            mask |= 1 << digit;
        }
        return mask;
    }

    /**
     * 暗証番号 secret に guess を提出したときの手がかりを符号化して返す。
     *
     * @param secretMask {@link #digitMask(int)} で求めた secret の数字のビットマスク
     */
    static int judge(int guess, int secret, int secretMask) {
        int[] g = DIGITS[guess - MIN_NUMBER];
        int[] s = DIGITS[secret - MIN_NUMBER];
        int strikes = 0;
        int balls = 0;
        for (int i = 0; i < 4; i++) {
            if (g[i] == s[i]) {
                strikes++;
            } else if ((secretMask & (1 << g[i])) != 0) {
                balls++;
            }
        }
        return strikes * 5 + balls;
    }

    /** 符号化した手がかりを "xS yB" 形式の文字列にする。 */
    static String format(int clue) {
        return (clue / 5) + "S " + (clue % 5) + "B";
    }

    /**
     * "xS yB" 形式の文字列を符号化した手がかりに変換する。
     *
     * @throws IllegalStateException 形式が不正、または x + y > 4 の場合
     */
    static int parse(String clue) {
        Matcher matcher = clue == null ? null : CLUE_PATTERN.matcher(clue);
        if (matcher == null || !matcher.matches()) {
            throw new IllegalStateException("手がかりの形式が不正です: " + clue);
        }
        int strikes = Integer.parseInt(matcher.group(1));
        int balls = Integer.parseInt(matcher.group(2));
        if (strikes + balls > 4) {
            throw new IllegalStateException("手がかりの STRIKE と BALL の合計が4を超えています: " + clue);
        }
        return strikes * 5 + balls;
    }
}
