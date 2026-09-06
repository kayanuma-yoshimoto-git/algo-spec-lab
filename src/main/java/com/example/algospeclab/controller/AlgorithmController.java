package com.example.algospeclab.controller;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * アルゴリズムの Spec を試すためのサンプルコントローラー。
 * ロジックは今のところコントローラー内に直書きしているが、
 * 検証が進んだら service 層へ切り出す想定。
 */
@RestController
@RequestMapping("/api/algorithms")
@Validated
public class AlgorithmController {

    /** 整数列を昇順ソートして返す。 */
    @PostMapping("/sort")
    public SortResponse sort(@Valid @RequestBody SortRequest request) {
        int[] sorted = request.values().stream().mapToInt(Integer::intValue).sorted().toArray();
        return new SortResponse(Arrays.stream(sorted).boxed().toList());
    }

    /** n 番目のフィボナッチ数を返す（0 始まり）。 */
    @GetMapping("/fibonacci/{n}")
    public FibonacciResponse fibonacci(@PathVariable @Min(0) int n) {
        long prev = 0;
        long curr = 1;
        for (int i = 0; i < n; i++) {
            long next = prev + curr;
            prev = curr;
            curr = next;
        }
        return new FibonacciResponse(n, prev);
    }

    public record SortRequest(@NotNull List<@NotNull Integer> values) {
    }

    public record SortResponse(List<Integer> values) {
    }

    public record FibonacciResponse(int n, long value) {
    }
}
