package com.example.algospeclab.algo.nthspell;

import java.util.Arrays;

/**
 * specs/nth-spell/README.md 「5. 設計のアプローチ」で採用した実装。
 *
 * <p>呪文を全単射 26 進数の通し番号("a"=1, …, "z"=26, "aa"=27, …)に変換すると、
 * 番号順が呪文書の並び(文字数の少ない順 → 辞書順)と一致する。
 * 削除呪文の番号を昇順に見ながら n を補正し、補正後の番号を呪文に戻す。</p>
 */
public final class NthSpell {

    private static final long MAX_N = 1_000_000_000_000_000L;
    private static final int MAX_BANS = 300_000;
    private static final int MAX_SPELL_LENGTH = 11;
    private static final int ALPHABET_SIZE = 26;

    private NthSpell() {
    }

    /**
     * 削除後の呪文書で n 番目の呪文を返す。
     *
     * @param n    求める順位(1〜10^15)
     * @param bans 削除された呪文(英小文字 1〜11 文字、重複なし、1〜300,000 件)
     * @return 削除後の呪文書で n 番目の呪文
     * @throws IllegalArgumentException 引数が制約に違反する場合(bans の重複を含む)
     */
    public static String solve(long n, String[] bans) {
        if (n < 1 || n > MAX_N) {
            throw new IllegalArgumentException("n は 1〜" + MAX_N + " で指定してください: " + n);
        }
        if (bans == null || bans.length == 0 || bans.length > MAX_BANS) {
            throw new IllegalArgumentException("bans は 1〜" + MAX_BANS + " 件で指定してください。");
        }

        long[] banIndexes = new long[bans.length];
        for (int i = 0; i < bans.length; i++) {
            banIndexes[i] = toIndex(bans[i]);
        }
        Arrays.sort(banIndexes);

        long target = n;
        for (int i = 0; i < banIndexes.length; i++) {
            if (i > 0 && banIndexes[i] == banIndexes[i - 1]) {
                throw new IllegalArgumentException("bans に重複した呪文があります: " + fromIndex(banIndexes[i]));
            }
            if (banIndexes[i] <= target) {
                target++; // 答え以前の呪文が1つ削除されたので、答えは1つ後ろにずれる
            }
        }
        return fromIndex(target);
    }

    /** 呪文を全単射 26 進数の通し番号に変換する。 */
    private static long toIndex(String spell) {
        if (spell == null || spell.isEmpty() || spell.length() > MAX_SPELL_LENGTH) {
            throw new IllegalArgumentException("bans の要素は 1〜" + MAX_SPELL_LENGTH + " 文字で指定してください: " + spell);
        }
        long index = 0;
        for (int i = 0; i < spell.length(); i++) {
            char ch = spell.charAt(i);
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("bans の要素は英小文字のみで指定してください: " + spell);
            }
            index = index * ALPHABET_SIZE + (ch - 'a' + 1);
        }
        return index;
    }

    /** 全単射 26 進数の通し番号を呪文に変換する。 */
    private static String fromIndex(long index) {
        StringBuilder spell = new StringBuilder();
        long rest = index;
        while (rest > 0) {
            rest--;
            spell.append((char) ('a' + rest % ALPHABET_SIZE));
            rest /= ALPHABET_SIZE;
        }
        return spell.reverse().toString();
    }
}
