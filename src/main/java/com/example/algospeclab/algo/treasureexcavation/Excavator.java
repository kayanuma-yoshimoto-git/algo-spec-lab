package com.example.algospeclab.algo.treasureexcavation;

/** 掘削ロボットへの命令を表す関数型インターフェース。 */
@FunctionalInterface
public interface Excavator {

    /** 宝を見つけた場合の応答。 */
    int FOUND = 0;
    /** 宝が掘った列より左側にある場合の応答。 */
    int LEFT = -1;
    /** 宝が掘った列より右側にある場合の応答。 */
    int RIGHT = 1;

    /**
     * col 列(1始まり)を掘る。
     *
     * @return 宝を見つけたら {@link #FOUND}、宝が左側なら {@link #LEFT}、右側なら {@link #RIGHT}
     */
    int excavate(int col);
}
