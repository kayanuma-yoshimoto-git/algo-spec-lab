package com.example.algospeclab.controller;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.algospeclab.algo.serverscaleout.ServerScaleOut;
import com.example.algospeclab.algo.yellowlightsync.YellowLightSync;

/**
 * アルゴリズム課題を REST API として公開するコントローラー。
 * 各エンドポイントは入力検証を行った上で {@code algo} 層の関数に処理を委譲し、
 * ロジック自体はコントローラーに持たせない。
 */
@RestController
@RequestMapping("/api/algorithms")
@Validated
public class AlgorithmController {

    /** 全ての信号機が同時に黄色になる最も早い時刻（秒）を返す。存在しない場合は -1。 */
    @PostMapping("/yellow-light-sync")
    public YellowLightSyncResponse yellowLightSync(@Valid @RequestBody YellowLightSyncRequest request) {
        int[][] signals = toSignalArray(request.signals());
        for (int[] signal : signals) {
            int sum = signal[0] + signal[1] + signal[2];
            if (sum < 3 || sum > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "各信号機の G+Y+R の合計は 3〜20 の範囲で指定してください。");
            }
        }
        return new YellowLightSyncResponse(YellowLightSync.solve(signals));
    }

    private static int[][] toSignalArray(List<List<Integer>> signals) {
        int[][] result = new int[signals.size()][3];
        for (int i = 0; i < signals.size(); i++) {
            List<Integer> signal = signals.get(i);
            result[i][0] = signal.get(0);
            result[i][1] = signal.get(1);
            result[i][2] = signal.get(2);
        }
        return result;
    }

    /** 1日を通して全ての時間帯の要件を満たすための、サーバー増設回数の合計の最小値を返す。 */
    @PostMapping("/server-scale-out")
    public ServerScaleOutResponse serverScaleOut(@Valid @RequestBody ServerScaleOutRequest request) {
        int[] players = request.players().stream().mapToInt(Integer::intValue).toArray();
        return new ServerScaleOutResponse(ServerScaleOut.solve(players, request.m(), request.k()));
    }

    public record YellowLightSyncRequest(
            @NotNull @Size(min = 2, max = 5)
            List<@NotNull @Size(min = 3, max = 3) List<@NotNull @Min(1) @Max(18) Integer>> signals) {
    }

    public record YellowLightSyncResponse(int time) {
    }

    public record ServerScaleOutRequest(
            @NotNull @Size(min = 24, max = 24)
            List<@NotNull @Min(0) @Max(1000) Integer> players,
            @Min(1) @Max(1000) int m,
            @Min(1) @Max(24) int k) {
    }

    public record ServerScaleOutResponse(int additions) {
    }
}
