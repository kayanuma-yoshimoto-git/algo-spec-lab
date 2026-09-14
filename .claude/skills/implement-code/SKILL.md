---
name: implement-code
description: 課題ディレクトリのテストをパスする最小限の実装コードを作成するスキル
---

# /implement-code 指示書

`generate-tests` で作成された失敗するテストをすべてパスさせるための、最小限のプロダクションコードを実装してください。

## 対象課題の特定
- 引数で `<slug>` が渡された場合はそれを対象にする（例: `/implement-code two-sum`）。
- 渡されない場合は `specs/` 配下で最後に更新された `README.md` の課題を対象にし、対象 slug を明示してから進める。
- `<pkg>` は設計書に記載の実装パッケージ、無ければ `<slug>` からハイフンを除去した形。

## 実行手順
1. `src/test/java/com/example/algospeclab/algo/<pkg>/` のテストコードを確認します。
2. テストを通過させるためのロジックを `src/main/java/com/example/algospeclab/algo/<pkg>/` 配下に実装します。
3. `./gradlew test` を実行し、すべてのテストが Success（100% 通過）することを確認します。

## 実装規約
- **パッケージ:** `com.example.algospeclab.algo.<pkg>`
- **クリーンコード:** SOLID原則に従い、可読性が高く保守しやすいコードを記述します。
- **オーバーエンジニアリングの回避:** 設計書およびテストケースで要求されていない過剰な機能は実装しません。
- **リファクタリング:** テストが通過した後、コードの重複を排除し構造を整えます。
- 他の課題（`algo/` 配下の別パッケージ）のコードには手を入れません。
