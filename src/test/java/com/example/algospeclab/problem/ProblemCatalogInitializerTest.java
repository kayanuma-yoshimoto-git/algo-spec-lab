package com.example.algospeclab.problem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** コントローラーの全エンドポイントに、課題マスタへ登録できる設計書が揃っていることを確認する（DB 不要）。 */
class ProblemCatalogInitializerTest {

    @Test
    @DisplayName("AlgorithmController の全 POST エンドポイントの slug を取得する")
    void collectsAllControllerSlugs() {
        assertThat(ProblemCatalogInitializer.controllerSlugs()).containsExactly(
                "ancient-base", "dice-game", "distribution-tree", "nth-spell", "number-baseball",
                "odd-even-tree", "run-length-window-sum", "server-scale-out", "treasure-excavation",
                "yellow-light-sync");
    }

    @Test
    @DisplayName("全エンドポイントの設計書がクラスパスにあり、課題名と引数を取り出せる")
    void everyEndpointHasParsableSpec() throws Exception {
        List<String> slugs = ProblemCatalogInitializer.controllerSlugs();
        for (String slug : slugs) {
            SpecDocument spec = SpecDocument.parse(ProblemCatalogInitializer.readSpec(slug));
            assertThat(spec.title()).as(slug).isNotBlank();
            assertThat(spec.arguments()).as(slug).isNotBlank();
        }
    }
}
