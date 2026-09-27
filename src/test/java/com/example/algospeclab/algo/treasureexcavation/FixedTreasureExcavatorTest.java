package com.example.algospeclab.algo.treasureexcavation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * specs/treasure-excavation/README.md の「2.2 固定位置シミュレーター」に対応するテスト。
 */
class FixedTreasureExcavatorTest {

    private static final int[] DEPTH = {1, 2, 3, 4, 5};

    @Test
    @DisplayName("宝の列を掘ると0、宝より右の列では-1、左の列では1を返す")
    void returnsDirectionTowardTreasure() {
        FixedTreasureExcavator excavator = new FixedTreasureExcavator(DEPTH, 3);

        assertThat(excavator.excavate(3)).isZero();
        assertThat(excavator.excavate(5)).isEqualTo(-1);
        assertThat(excavator.excavate(1)).isEqualTo(1);
    }

    @Test
    @DisplayName("掘った列の順序と、掘った列の深さの累計コストを記録する")
    void recordsExcavatedColumnsAndTotalCost() {
        FixedTreasureExcavator excavator = new FixedTreasureExcavator(DEPTH, 3);

        excavator.excavate(5);
        excavator.excavate(1);
        excavator.excavate(3);

        assertThat(excavator.excavatedColumns()).containsExactly(5, 1, 3);
        assertThat(excavator.totalCost()).isEqualTo(5L + 1L + 3L);
    }

    @ParameterizedTest(name = "treasureCol={0} の場合は IllegalArgumentException を送出する")
    @ValueSource(ints = {0, 6})
    @DisplayName("treasureCol が範囲(1〜w)を外れる場合は IllegalArgumentException を送出する")
    void rejectsTreasureColumnOutOfRange(int treasureCol) {
        assertThatThrownBy(() -> new FixedTreasureExcavator(DEPTH, treasureCol))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "col={0} を掘ると IllegalArgumentException を送出する")
    @ValueSource(ints = {0, 6})
    @DisplayName("掘る列が範囲(1〜w)を外れる場合は IllegalArgumentException を送出する")
    void rejectsExcavationColumnOutOfRange(int col) {
        FixedTreasureExcavator excavator = new FixedTreasureExcavator(DEPTH, 3);

        assertThatThrownBy(() -> excavator.excavate(col))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
