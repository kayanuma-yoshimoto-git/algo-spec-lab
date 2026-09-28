package com.example.algospeclab.controller;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

import com.example.algospeclab.algo.distributiontree.DistributionTree;
import com.example.algospeclab.algo.numberbaseball.Attempt;
import com.example.algospeclab.algo.numberbaseball.FixedSecretSubmitter;
import com.example.algospeclab.algo.nthspell.NthSpell;
import com.example.algospeclab.algo.numberbaseball.NumberBaseball;
import com.example.algospeclab.algo.runlengthwindowsum.RunLengthWindowSum;
import com.example.algospeclab.algo.serverscaleout.ServerScaleOut;
import com.example.algospeclab.algo.treasureexcavation.FixedTreasureExcavator;
import com.example.algospeclab.algo.treasureexcavation.TreasureExcavation;
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

    /**
     * brr の l〜r 番目の和 K と、同じ長さで和が K となる部分配列の個数 C を返す。
     * l ≤ r ≤ arr の総和 の違反は algo 層が IllegalArgumentException で検出し、このエンドポイントでのみ 400 に変換する。
     */
    @PostMapping("/run-length-window-sum")
    public RunLengthWindowSumResponse runLengthWindowSum(@Valid @RequestBody RunLengthWindowSumRequest request) {
        int[] arr = request.arr().stream().mapToInt(Integer::intValue).toArray();
        long[] result;
        try {
            result = RunLengthWindowSum.solve(arr, request.l(), request.r());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return new RunLengthWindowSumResponse(result[0], result[1]);
    }

    /**
     * 宝の位置を固定したシミュレーションで宝を発掘し、見つけた列・総コスト・掘削順序を返す。
     * treasureCol ≤ w や最悪ケース最小コスト ≤ money の違反は algo 層が IllegalArgumentException で検出し、
     * このエンドポイントでのみ 400 に変換する。
     */
    @PostMapping("/treasure-excavation")
    public TreasureExcavationResponse treasureExcavation(@Valid @RequestBody TreasureExcavationRequest request) {
        int[] depth = request.depth().stream().mapToInt(Integer::intValue).toArray();
        try {
            FixedTreasureExcavator excavator = new FixedTreasureExcavator(depth, request.treasureCol());
            int column = TreasureExcavation.solve(depth, request.money(), excavator);
            return new TreasureExcavationResponse(column, excavator.totalCost(), excavator.excavatedColumns());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * 暗証番号を固定したシミュレーションで数字野球を解き、特定した暗証番号・提出回数・提出履歴を返す。
     * 「secret が 1〜9 の互いに異なる4桁」の違反はシミュレーター生成時の IllegalArgumentException を 400 に変換する。
     */
    @PostMapping("/number-baseball")
    public NumberBaseballResponse numberBaseball(@Valid @RequestBody NumberBaseballRequest request) {
        FixedSecretSubmitter submitter;
        try {
            submitter = new FixedSecretSubmitter(request.secret());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        int answer = NumberBaseball.solve(request.n(), submitter);
        return new NumberBaseballResponse(answer, submitter.submitCount(), submitter.history());
    }

    /** 分配ノード数と分配度の上限のもとで作れるツリーのリーフノード数の最大値を返す。 */
    @PostMapping("/distribution-tree")
    public DistributionTreeResponse distributionTree(@Valid @RequestBody DistributionTreeRequest request) {
        return new DistributionTreeResponse(DistributionTree.solve(request.distLimit(), request.splitLimit()));
    }

    /**
     * 削除後の呪文書で n 番目の呪文を返す。
     * bans の重複は algo 層が IllegalArgumentException で検出し、このエンドポイントでのみ 400 に変換する。
     */
    @PostMapping("/nth-spell")
    public NthSpellResponse nthSpell(@Valid @RequestBody NthSpellRequest request) {
        String[] bans = request.bans().toArray(new String[0]);
        try {
            return new NthSpellResponse(NthSpell.solve(request.n(), bans));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
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

    public record RunLengthWindowSumRequest(
            @NotNull @Size(min = 1, max = 100_000)
            List<@NotNull @Min(1) @Max(100_000) Integer> arr,
            @Min(1) long l,
            @Min(1) long r) {
    }

    public record RunLengthWindowSumResponse(long k, long c) {
    }

    public record TreasureExcavationRequest(
            @NotNull @Size(min = 2, max = 200)
            List<@NotNull @Min(1) @Max(100_000) Integer> depth,
            @Min(1) int money,
            @Min(1) int treasureCol) {
    }

    public record TreasureExcavationResponse(int column, long totalCost, List<Integer> excavatedColumns) {
    }

    public record NumberBaseballRequest(
            @Min(6) @Max(3024) int n,
            @Min(1000) @Max(9999) int secret) {
    }

    public record NumberBaseballResponse(int answer, int submitCount, List<Attempt> history) {
    }

    public record DistributionTreeRequest(
            @Min(0) @Max(1_000_000_000) int distLimit,
            @Min(1) @Max(1_000_000_000) int splitLimit) {
    }

    public record DistributionTreeResponse(int leaves) {
    }

    public record NthSpellRequest(
            @Min(1) @Max(1_000_000_000_000_000L) long n,
            @NotNull @Size(min = 1, max = 300_000)
            List<@NotNull @Pattern(regexp = "[a-z]{1,11}") String> bans) {
    }

    public record NthSpellResponse(String spell) {
    }
}
