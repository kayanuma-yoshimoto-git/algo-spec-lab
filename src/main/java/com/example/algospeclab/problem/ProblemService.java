package com.example.algospeclab.problem;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 課題マスタ（problem テーブル）を参照するサービス。 */
@Service
public class ProblemService {

    private final ProblemRepository repository;

    public ProblemService(ProblemRepository repository) {
        this.repository = repository;
    }

    /** 登録済みの課題をすべて id の昇順で返す。0 件なら空リスト。 */
    @Transactional(readOnly = true)
    public List<ProblemResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(ProblemResponse::from)
                .toList();
    }
}
