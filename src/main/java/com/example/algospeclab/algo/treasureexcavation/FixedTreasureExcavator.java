package com.example.algospeclab.algo.treasureexcavation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 宝の位置が固定された {@link Excavator} のシミュレーター。
 * REST API とテストで使用し、掘った列の順序と累計コストを記録する。
 */
public final class FixedTreasureExcavator implements Excavator {

    private final int[] depth;
    private final int treasureCol;
    private final List<Integer> excavatedColumns = new ArrayList<>();
    private long totalCost;

    /**
     * @param depth       各列の掘削コスト(depth[i] は i+1 列目)
     * @param treasureCol 宝がある列(1始まり)
     * @throws IllegalArgumentException depth が null、または treasureCol が 1〜depth.length の範囲外の場合
     */
    public FixedTreasureExcavator(int[] depth, int treasureCol) {
        if (depth == null) {
            throw new IllegalArgumentException("depth を指定してください。");
        }
        this.depth = depth.clone();
        requireColumnInRange(treasureCol, "treasureCol");
        this.treasureCol = treasureCol;
    }

    @Override
    public int excavate(int col) {
        requireColumnInRange(col, "col");
        excavatedColumns.add(col);
        totalCost += depth[col - 1];
        if (col == treasureCol) {
            return FOUND;
        }
        return treasureCol < col ? LEFT : RIGHT;
    }

    /** 掘った列(1始まり)を掘った順に返す。 */
    public List<Integer> excavatedColumns() {
        return Collections.unmodifiableList(excavatedColumns);
    }

    /** これまでに掘った列の深さの合計を返す。 */
    public long totalCost() {
        return totalCost;
    }

    private void requireColumnInRange(int col, String name) {
        if (col < 1 || col > depth.length) {
            throw new IllegalArgumentException(name + " は 1〜" + depth.length + " で指定してください。");
        }
    }
}
