package com.example.algospeclab.algo.numberbaseball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 暗証番号が固定された {@link Submitter} のシミュレーター。
 * REST API とテストで使用し、提出履歴を記録する。
 */
public final class FixedSecretSubmitter implements Submitter {

    private final int secret;
    private final int secretMask;
    private final List<Attempt> history = new ArrayList<>();

    /**
     * @param secret 暗証番号
     * @throws IllegalArgumentException secret が 1〜9 の互いに異なる4桁でない場合
     */
    public FixedSecretSubmitter(int secret) {
        if (!Judge.isValidSecret(secret)) {
            throw new IllegalArgumentException("secret は 1〜9 の互いに異なる4桁で指定してください: " + secret);
        }
        this.secret = secret;
        this.secretMask = Judge.digitMask(secret);
    }

    @Override
    public String submit(int guess) {
        if (!Judge.isInRange(guess)) {
            throw new IllegalArgumentException(
                    "提出値は " + Judge.MIN_NUMBER + "〜" + Judge.MAX_NUMBER + " で指定してください: " + guess);
        }
        String clue = Judge.format(Judge.judge(guess, secret, secretMask));
        history.add(new Attempt(guess, clue));
        return clue;
    }

    /** 提出履歴を提出順に返す。 */
    public List<Attempt> history() {
        return Collections.unmodifiableList(history);
    }

    /** これまでの提出回数を返す。 */
    public int submitCount() {
        return history.size();
    }
}
