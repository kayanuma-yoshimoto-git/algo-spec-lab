# CLAUDE.md

## 言語 / Language

- ユーザーとのチャット上のやり取り(説明・回答・進捗報告など)は**韓国語**で行うこと。
- ただし成果物(README・コメント・ドキュメント)は入力言語やチャット言語に関わらず必ず**日本語**で記述する。
  ユーザーからの入力(課題文・URL・貼り付けテキストなど)が韓国語や英語であっても、成果物は日本語に翻訳して記述し、
  原文の非日本語テキストをそのまま転記しない。
- ドキュメント・コメント・README はすべて**日本語**で記述する。
- コード内の識別子、コミットメッセージは既存コードのスタイルに合わせる。

# Global Rules for AI-Assisted Backend Project

## プロジェクト概要

`algo-spec-lab` は Spring Boot / PostgreSQL ベースのバックエンド環境を土台に、Claude Code を活用した
**【設計 → テスト → 実装 → 可視化】** の AI 支援開発ワークフローを実証・標準化するためのリポジトリ。
アルゴリズム課題の Spec を題材に、再利用可能な Custom Skill としてプロセスを整備していく。

## Tech Stack & Environment

### 現在の構成

- Language: Java 24（ソース互換 `sourceCompatibility = 24`） / 実行 JVM: Amazon Corretto 24
- Framework: Spring Boot 3.5.6（`spring-boot-starter-web` = Spring MVC + 内蔵 Tomcat）
- Validation: Jakarta Bean Validation / Hibernate Validator（`spring-boot-starter-validation`）
- API Doc: springdoc-openapi 2.8.13（Swagger UI: `/swagger-ui.html`、OpenAPI JSON: `/v3/api-docs`）
- Test: JUnit 5 + Spring Boot Test（`@WebMvcTest` / `MockMvc`）（`spring-boot-starter-test`）
- Build: Gradle 8.14.3（Kotlin DSL）。Wrapper 同梱のため `./gradlew` を使用（`gradle` の別途インストール不要）
- Dependency 管理: io.spring.dependency-management 1.1.7（Spring Boot BOM）
- AI Tooling: Claude Code CLI（共通ルール `CLAUDE.md` + `.claude/skills/`（整備中））
- Database: PostgreSQL + Spring Data JPA + Flyway（スキーマは `db/migration` で管理、JPA は `ddl-auto=validate`）
  - 接続情報は環境変数 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` で渡す
  - 起動時に `AlgorithmController` の全エンドポイントを設計書から `problem` テーブル（課題マスタ）へ登録・更新する

### 導入予定

- Database: 実行結果の永続化
- CI: GitHub Actions（`./gradlew test` の自動実行）

## プロジェクト構成

```text
specs/<slug>/README.md                    # アルゴリズム課題ごとの設計書（/design-spec）
src/main/java/com/example/algospeclab/
├── AlgoSpecLabApplication.java            # エントリポイント
├── config/OpenApiConfig.java              # springdoc / OpenAPI メタ情報
├── controller/AlgorithmController.java    # /api/algorithms 配下の課題別エンドポイント（algo 層に委譲）
├── controller/ProblemController.java      # GET /api/problems で課題マスタの一覧を返す（problem 層に委譲）
├── web/GlobalExceptionHandler.java        # バリデーション例外を ProblemDetail に変換
├── problem/                               # 課題マスタ（起動時に設計書から problem テーブルへ同期、一覧参照サービス）
└── algo/<pkg>/                            # 課題ごとの実装（/implement-code）
src/main/resources/application.properties  # ポート 8080 / springdoc パス / DB 接続設定
src/main/resources/db/migration/           # Flyway マイグレーション（V<番号>__<内容>.sql）
src/test/java/com/example/algospeclab/
├── controller/                            # @WebMvcTest ベースのコントローラーテスト
├── problem/                               # 課題マスタのテスト（DB 不要）
└── algo/<pkg>/                            # 課題ごとのテスト（/generate-tests）
```

- アルゴリズム課題は 1課題 = 1ディレクトリ。設計書は `specs/<slug>/`、コードは `algo/<pkg>/` にミラー配置する。
- `<slug>` は kebab-case（ディレクトリ名）、`<pkg>` はハイフンを除去した形（Java パッケージ名）。例: `two-sum` → `twosum`
- サンプル API のロジックは現状コントローラーに直書き。検証が進んだら service 層へ切り出す想定。
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
