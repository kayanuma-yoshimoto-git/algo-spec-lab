package com.example.algospeclab.problem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.algospeclab.controller.AlgorithmController;

/**
 * 起動時に {@link AlgorithmController} が公開している課題をすべて problem テーブルへ登録・更新する。
 * 課題名・課題説明・引数はクラスパス上の設計書 (specs/&lt;slug&gt;/README.md) から取得する。
 * 設計書が無い・形式が不正な課題は警告ログを出してスキップし、起動は止めない。
 */
@Component
public class ProblemCatalogInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProblemCatalogInitializer.class);

    private final ProblemRepository repository;

    public ProblemCatalogInitializer(ProblemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String slug : controllerSlugs()) {
            SpecDocument spec;
            try {
                spec = SpecDocument.parse(readSpec(slug));
            } catch (IOException | IllegalArgumentException e) {
                log.warn("課題 {} の設計書を読み込めないため登録をスキップします: {}", slug, e.getMessage());
                continue;
            }
            Problem problem = repository.findBySlug(slug).orElseGet(() -> new Problem(slug));
            problem.apply(spec);
            repository.save(problem);
        }
        log.info("課題マスタを同期しました（{} 件）", repository.count());
    }

    /** {@link AlgorithmController} の POST エンドポイントのパス末尾（= slug）を名前順で返す。 */
    static List<String> controllerSlugs() {
        return Arrays.stream(AlgorithmController.class.getDeclaredMethods())
                .map(method -> AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class))
                .filter(Objects::nonNull)
                .flatMap(mapping -> Arrays.stream(mapping.path()))
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .sorted()
                .toList();
    }

    static String readSpec(String slug) throws IOException {
        try (InputStream in = new ClassPathResource("specs/" + slug + "/README.md").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
