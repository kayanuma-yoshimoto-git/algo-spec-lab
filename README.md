# 🚀 AI-Assisted Backend Development Laboratory

本プロジェクトは、**Spring Boot / PostgreSQL** をベースとしたバックエンド環境の構築と併せ、**Claude Code** を活用した効率的な AI 支援開発プロセス（AI-Driven Workflow）の実証および標準化を目的としたリポジトリです。

単なるコード生成にとどまらず、**【設計 → テスト → 実装 → 可視化】** に至るモジュール化された開発ワークフローを構築し、これを再利用可能な **Claude Code Custom Skills** として管理・運用します。

---

## 🎯 主要目標と評価指標

- **Claude Code ベースの開発プロセスの標準化**: 共通ルール（`CLAUDE.md`）および段階別 Custom Skill の設計
- **開発生産性と品質の向上**: TDD（テスト駆動開発）の適用、Edge Case の自動検証、ドキュメント化（Mermaid）の自動化
- **ナレッジ共有**: チーム内で即座に導入・展開可能な AI 活用ガイドおよびパイプラインの構築

---

## 🛠 技術スタック (Tech Stack)

### 現在の構成

| 区分 | 技術スタック                                         | 備考 |
| :--- |:-----------------------------------------------------| :--- |
| **Language** | Java 24 (ソース互換) / 実行 JVM: Amazon Corretto 24  | `build.gradle.kts` の `sourceCompatibility = 21` |
| **Framework** | Spring Boot 3.5.6                                    | `spring-boot-starter-web`（Spring MVC + 内蔵 Tomcat） |
| **Validation** | Jakarta Bean Validation (Hibernate Validator)        | `spring-boot-starter-validation` |
| **Build** | Gradle 8.14.3 (Kotlin DSL)                           | Wrapper 同梱（`./gradlew`）。`gradle` の別途インストール不要 |
| **Dependency 管理** | io.spring.dependency-management 1.1.7                | Spring Boot BOM によるバージョン統一 |
| **Test** | JUnit 5 + Spring Boot Test (`@WebMvcTest` / MockMvc) | `spring-boot-starter-test` |
| **API Doc** | springdoc-openapi 2.8.13 (Swagger UI)                | `springdoc-openapi-starter-webmvc-ui`。`/swagger-ui.html` で閲覧 |
| **AI Tooling** | Claude Code CLI                                      | 共通ルール `CLAUDE.md` + `.claude/skills/`（整備中） |

### 導入予定

| 区分 | 技術スタック | 用途 |
| :--- | :--- | :--- |
| **Database** | PostgreSQL + Spring Data JPA | 課題データ・実行結果の永続化 |
| **CI** | GitHub Actions | `./gradlew test` の自動実行 |

### Swagger UI の見かた

1. アプリを起動: `./gradlew bootRun`（ポート 8080）
2. ブラウザで **http://localhost:8080/swagger-ui.html** を開く（`/swagger-ui/index.html` に転送される）
3. OpenAPI 定義（JSON）そのものは **http://localhost:8080/v3/api-docs**

各エンドポイントの "Try it out" からブラウザ上でリクエストを送信できる。
タイトル等のメタ情報は `config/OpenApiConfig.java`、パスは `application.properties` の `springdoc.*` で変更可能。

---

## 🏗 Claude Code Skill アーキテクチャ

AI プロンプトの精度向上と再利用性の確保のため、**共通エンジン（Global Rules）** と **段階別スキル（Individual Skills）** を分離・モジュール化して管理しています。

```text
.claude/
├── CLAUDE.md                   # 🌐 [Global Engine] 全作業に自動適用される共通ルール
└── skills/                     # 🛠 [Individual Skills] 各フェーズ用カスタムコマンド
    ├── design-spec.md          # 1️⃣ /design-spec     : 課題分析および README 仕様書の作成
    ├── generate-tests.md       # 2️⃣ /generate-tests  : 仕様書に基づく JUnit5 テストコード作成
    ├── implement-solution.md    # 3️⃣ /implement-code  : テストをパスする実装コードの作成
    └── draw-sequence.md        # 4️⃣ /draw-sequence   : Mermaid ベースのシーケンス図生成