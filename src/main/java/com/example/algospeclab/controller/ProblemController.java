package com.example.algospeclab.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.algospeclab.problem.ProblemListResponse;
import com.example.algospeclab.problem.ProblemService;

/** 課題マスタを REST API として公開するコントローラー。処理は {@link ProblemService} に委譲する。 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /** 登録済みの課題をすべて返す。 */
    @GetMapping
    public ProblemListResponse findAll() {
        return new ProblemListResponse(problemService.findAll());
    }
}
