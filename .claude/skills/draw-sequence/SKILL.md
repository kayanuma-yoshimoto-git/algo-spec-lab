---
name: draw-sequence
description: 課題の実装コードを解析し Mermaid ベースのシーケンス図を設計書に追加するスキル
---

# /draw-sequence 指示書

対象アルゴリズム課題の実装 Java ロジックとテストコードをリバースエンジニアリングし、
Mermaid.js ベースのシーケンス図を生成して、その課題の設計書に追加してください。

## 対象課題の特定
- 引数で `<slug>` が渡された場合はそれを対象にする（例: `/draw-sequence two-sum`）。
- 渡されない場合は `specs/` 配下で最後に更新された `README.md` の課題を対象にし、対象 slug を明示してから進める。
- `<pkg>` は設計書に記載の実装パッケージ、無ければ `<slug>` からハイフンを除去した形。

## 実行手順
1. `src/main/java/com/example/algospeclab/algo/<pkg>/` 配下の実装コードの呼び出しフローを分析します。
2. オブジェクト間の呼び出し順序、データフロー、例外処理パイプラインを Mermaid の `sequenceDiagram` 形式に変換します。
3. `specs/<slug>/README.md` の最下部に `## 🔄 シーケンス図 (Sequence Diagram)` セクションを新設（既にあれば置き換え）し、生成した Mermaid ブロックを挿入します。
4. メッセージのラベルは日本語で記述します（設計書の原文が日本語以外でも、そのまま転記しない）。

## Mermaid 出力フォーマット例

~~~mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller
    participant Service
    participant Repository

    Client->>Controller: リクエスト送信
    Controller->>Service: ビジネスロジック呼び出し
    Service->>Repository: DB問い合わせ
    Repository-->>Service: データ返却
    Service-->>Controller: 処理結果返却
    Controller-->>Client: レスポンス (200 OK)
~~~
