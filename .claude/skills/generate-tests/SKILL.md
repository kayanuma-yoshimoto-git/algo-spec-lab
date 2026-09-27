---
name: generate-tests
description: 課題ディレクトリの設計書に基づき JUnit5 テストコードを作成するスキル
---

# /generate-tests 指示書

対象アルゴリズム課題の設計書 `specs/<slug>/README.md` を読み込み、TDD（テスト駆動開発）の原則に従って、
実装コードより先に JUnit 5 ベースのテストコードを作成してください。

## 対象課題の特定
- 引数で `<slug>` が渡された場合はそれを対象にする（例: `/generate-tests two-sum`）。
- 渡されない場合は `specs/` 配下で最後に更新された `README.md` の課題を対象にし、対象 slug を明示してから進める。
- `<pkg>` は設計書に記載の実装パッケージ、無ければ `<slug>` からハイフンを除去した形。

## 実行手順
1. `specs/<slug>/README.md` の「4. 正常系と異常系の定義」項目を詳細に分析します。
2. 定義されたすべての正常系および異常系ケースを検証するテストクラスを
   `src/test/java/com/example/algospeclab/algo/<pkg>/` 配下に作成します。

## テストコード作成規約
- **フレームワーク:** JUnit 5, AssertJ（いずれも `spring-boot-starter-test` に含まれるため依存追加は不要）
- **パッケージ:** `com.example.algospeclab.algo.<pkg>`
- **テスト対象の切り分け:**
  - 純粋なロジック／アルゴリズム関数が対象なら Spring に依存しない素の JUnit テスト
  - Web層（コントローラー）が対象なら `@WebMvcTest` + `MockMvc`
- **表示名:** すべてのテストメソッドに `@DisplayName("日本語でのテスト内容の説明")` を付与し、何を検証しているか明確にする
- **言語:** 設計書の原文が日本語以外でも、`@DisplayName` やコード内コメントは日本語で記述する（原文をそのまま転記しない）
- **TDD原則:** 実装クラスがまだ存在しない（または未完成の）ため、最初はコンパイルエラーまたはテストが失敗する状態が正常です。
