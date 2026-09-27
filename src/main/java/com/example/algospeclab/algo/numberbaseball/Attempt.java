package com.example.algospeclab.algo.numberbaseball;

/**
 * 1回分の提出記録。
 *
 * @param guess 提出した数
 * @param clue  返ってきた手がかり("xS yB" 形式)
 */
public record Attempt(int guess, String clue) {
}
