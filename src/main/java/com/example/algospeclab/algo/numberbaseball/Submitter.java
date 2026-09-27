package com.example.algospeclab.algo.numberbaseball;

/** 数字野球で数を提出する関数型インターフェース。 */
@FunctionalInterface
public interface Submitter {

    /**
     * guess(1000〜9999)を提出する。
     *
     * @return 手がかり("xS yB" 形式。x は STRIKE の個数、y は BALL の個数)
     */
    String submit(int guess);
}
