# CLAUDE.md

## 言語 / Language

- ユーザーとのやり取り(説明・回答・進捗報告など)は必ず**日本語**で行うこと。
- ドキュメント・コメント・README はすべて**日本語**で記述する。
- コード内の識別子、コミットメッセージは既存コードのスタイルに合わせる。

# Global Rules for AI-Assisted Backend Project

## プロジェクト概要

`algo-spec-lab` は Spring Boot / PostgreSQL ベースのバックエンド環境を土台に、Claude Code を活用した
**【設計 → テスト → 実装 → 可視化】** の AI 支援開発ワークフローを実証・標準化するためのリポジトリ。
アルゴリズム課題の Spec を題材に、再利用可能な Custom Skill としてプロセスを整備していく。

## Tech Stack & Environment

### 現在の構成

- Language: Java 21（ソース互換 `sourceCompatibility = 21`） / 実行 JVM: Amazon Corretto 24
- Framework: Spring Boot 3.5.6（`spring-boot-starter-web` = Spring MVC + 内蔵 Tomcat）
- Validation: Jakarta Bean Validation / Hibernate Validator（`spring-boot-starter-validation`）
- API Doc: springdoc-openapi 2.8.13（Swagger UI: `/swagger-ui.html`、OpenAPI JSON: `/v3/api-docs`）
- Test: JUnit 5 + Spring Boot Test（`@WebMvcTest` / `MockMvc`）（`spring-boot-starter-test`）
- Build: Gradle 8.14.3（Kotlin DSL）。Wrapper 同梱のため `./gradlew` を使用（`gradle` の別途インストール不要）
- Dependency 管理: io.spring.dependency-management 1.1.7（Spring Boot BOM）
- AI Tooling: Claude Code CLI（共通ルール `CLAUDE.md` + `.claude/skills/`（整備中））

### 導入予定

- Database: PostgreSQL + Spring Data JPA（課題データ・実行結果の永続化）
- CI: GitHub Actions（`./gradlew test` の自動実行）

## プロジェクト構成

```text
src/main/java/com/example/algospeclab/
├── AlgoSpecLabApplication.java        # エントリポイント
├── config/OpenApiConfig.java          # springdoc / OpenAPI メタ情報
├── controller/AlgorithmController.java # /api/algorithms 配下のサンプル API（sort, fibonacci）
└── web/GlobalExceptionHandler.java    # バリデーション例外を ProblemDetail に変換
src/main/resources/application.properties # ポート 8080 / springdoc パス設定
src/test/java/com/example/algospeclab/    # @WebMvcTest ベースのコントローラーテスト
```

- ロジックは現状コントローラーに直書き。検証が進んだら service 層へ切り出す想定。
- リクエスト/レスポンスは record で表現し、`ProblemDetail`（RFC 7807）でエラーを返す。

## よく使うコマンド

- ビルド: `./gradlew build`
- テスト: `./gradlew test`
- 起動: `./gradlew bootRun`（http://localhost:8080 、Swagger UI は `/swagger-ui.html`）

## Coding & Design Conventions

- Google Java Style Guide に従う。
- コードは clean・readable・self-documenting であること。
- ドキュメント・コメント・README は日本語で記述する。
- Security: 認証情報・秘密鍵・本番 DB 接続情報をコードに含めない。テストにはダミーデータを使う。
