---
name: design-spec
description: 課題ごとに専用ディレクトリを作成し、設計書 (README.md) を作成するスキル
---

# /design-spec 指示書

ユーザーから入力された課題内容（問題文、要求仕様、またはURL）を分析し、
**アルゴリズム課題ごとに専用ディレクトリを作成**して、その中に標準仕様となる `README.md` を作成してください。

## ディレクトリ規約

- 課題ごとに `specs/<slug>/` を作成する。
- `<slug>` は課題内容から決めた小文字英数字とハイフンの識別子（kebab-case）。例: `two-sum`, `binary-search`, `lru-cache`
- 実装・テストのコードはこのディレクトリには置かず、`src` 配下にミラー配置する（後続スキルが使用）。
  - 実装: `src/main/java/com/example/algospeclab/algo/<pkg>/`
  - テスト: `src/test/java/com/example/algospeclab/algo/<pkg>/`
  - `<pkg>` は `<slug>` からハイフンを除去した形。例: `two-sum` → `twosum`
- 1課題 = 1ディレクトリ構成。既存のプロジェクト直下 `README.md`（プロジェクト全体の説明）は上書きしない。

```text
specs/
└── <slug>/
    └── README.md          # この課題の設計書（本スキルの成果物）
src/main/java/com/example/algospeclab/algo/<pkg>/   # 実装（/implement-code）
src/test/java/com/example/algospeclab/algo/<pkg>/   # テスト（/generate-tests）
```

## 実行手順
1. 入力された要件を分析し、問題のコアとなるドメイン・アルゴリズムロジックを把握します。
2. `<slug>` を決定し、既に `specs/<slug>/` が存在する場合はユーザーに更新か別名かを確認します。
3. `specs/<slug>/README.md` を作成（または更新）します。
4. 決定した `<slug>` と `<pkg>`、対象パッケージパスを最後に明示して報告します（後続スキルが参照するため）。

## `specs/<slug>/README.md` 必須出力フォーマット

~~~markdown
# [課題名/問題タイトル]

- **slug:** <slug>
- **実装パッケージ:** com.example.algospeclab.algo.<pkg>

## 1. 問題の概要
- 解決すべき課題やアルゴリズムの目的を簡潔に記述

## 2. 入出力の定義
- **入力 (Input):** 型、データ構造、パラメータの説明
- **出力 (Output):** 戻り値の型、期待されるレスポンス形式

## 3. 制約条件
- 時間複雑度 / 空間複雑度の目標値
- データサイズの境界値（Min/Max）

## 4. 正常系と異常系の定義 (Test Cases)
### 正常系 (Normal Cases)
- [ ] 標準的な入力値に対する期待動作
### 異常系・限界値 (Edge Cases)
- [ ] Null、空文字、配列の要素数0
- [ ] 境界値（最大値・最小値）
- [ ] 不正なフォーマットや例外発生ケース

## 5. 設計のアプローチ
- 採用するアルゴリズムやデータ構造の選定理由
- 処理の大まかな流れ
~~~
