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

| 区分 | 技術スタック                                         | 備考                                                             |
| :--- |:-----------------------------------------------------|:-----------------------------------------------------------------|
| **Language** | Java 24 (ソース互換) / 実行 JVM: Amazon Corretto 24  | `build.gradle.kts` の `sourceCompatibility = 24`                 |
| **Framework** | Spring Boot 3.5.6                                    | `spring-boot-starter-web`（Spring MVC + 内蔵 Tomcat）            |
| **Validation** | Jakarta Bean Validation (Hibernate Validator)        | `spring-boot-starter-validation`                                 |
| **Build** | Gradle 8.14.3 (Kotlin DSL)                           | Wrapper 同梱（`./gradlew`）。`gradle` の別途インストール不要     |
| **Dependency 管理** | io.spring.dependency-management 1.1.7                | Spring Boot BOM によるバージョン統一                             |
| **Test** | JUnit 5 + Spring Boot Test (`@WebMvcTest` / MockMvc) | `spring-boot-starter-test`                                       |
| **API Doc** | springdoc-openapi 2.8.13 (Swagger UI)                | `springdoc-openapi-starter-webmvc-ui`。`/swagger-ui.html` で閲覧 |
| **AI Tooling** | Claude Code CLI                                      | 共通ルール `CLAUDE.md` + `.claude/skills/`（5スキル完成）        |

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

AI プロンプトの精度向上と再利用性の確保のため、**共通エンジン（Global Rules）** と **段階別スキル（Individual Skills）** を分離・モジュール化して管理しています。5つのスキルが完成済みです。

```text
CLAUDE.md                          # 🌐 [Global Engine] 全作業に自動適用される共通ルール（リポジトリ直下）
.claude/
└── skills/                        # 🛠 [Individual Skills] 各フェーズ用カスタムコマンド
    ├── design-spec/SKILL.md       # 1️⃣ /design-spec    : grill-me → save の2段階で specs/<slug>/ に設計書を作成
    ├── generate-tests/SKILL.md    # 2️⃣ /generate-tests : 設計書に基づく JUnit5 テストコード作成
    ├── implement-code/SKILL.md    # 3️⃣ /implement-code : テストをパスする実装コードの作成
    ├── draw-sequence/SKILL.md     # 4️⃣ /draw-sequence  : Mermaid シーケンス図を設計書に追加
    └── review-diff/SKILL.md       # 🔍 /review-diff    : 現在の git 差分を確認してコードレビュー
```

各スキルは `.claude/skills/<コマンド名>/SKILL.md` に配置し、`SKILL.md` の frontmatter に `name`（ディレクトリ名と一致）と `description` を持たせる。

### `/design-spec` の grill-me → save

設計書をいきなり書き始めず、次の2段階で進める。

1. **grill-me**: 要件を分析し、設計判断が分かれる論点（アルゴリズムの候補選択、未規定の例外処理、公開方式など）があればユーザーに確認する。
2. **save**: 確定した内容を反映して `specs/<slug>/README.md` を作成する。

論点が無ければ grill-me はスキップされ、そのまま save に進む。

### アルゴリズム課題ごとのディレクトリ構成

課題は 1つにつき 1ディレクトリで管理します。設計書は `specs/<slug>/` に分離し、実装・テストは Gradle の
標準レイアウトに合わせて `src` 配下へミラー配置します。

```text
specs/
└── <slug>/                                        # 例: two-sum, binary-search
    └── README.md                                  # 設計書（/design-spec が作成、/draw-sequence が図を追記）
src/main/java/com/example/algospeclab/algo/<pkg>/  # 実装（/implement-code）
src/test/java/com/example/algospeclab/algo/<pkg>/  # テスト（/generate-tests）
```

- `<slug>`: 小文字英数字とハイフンの識別子（kebab-case）。ディレクトリ名に使用。
- `<pkg>`: `<slug>` からハイフンを除去した形（`two-sum` → `twosum`）。Java パッケージ名に使用。
- 2番目以降のスキルは引数で `<slug>` を指定でき（例: `/generate-tests two-sum`）、省略時は `specs/` 配下で最後に更新された設計書を対象にします。
- プロジェクト直下の `README.md`（このファイル）は課題では上書きしません。

### ワークフロー例

```bash
/design-spec Two Sum: 配列から和が target になる 2 要素の添字を返す
/generate-tests two-sum
/implement-code two-sum
/draw-sequence two-sum
/review-diff
```

### 実装例: yellow-light-sync

上記ワークフローを一巡させた実例。信号機が全て同時に黄色になる最速時刻を求める課題。

| フェーズ | 成果物 |
| --- | --- |
| design-spec | [`specs/yellow-light-sync/README.md`](specs/yellow-light-sync/README.md)（設計・シーケンス図含む） |
| generate-tests | `src/test/java/.../algo/yellowlightsync/YellowLightSyncTest.java`、`src/test/java/.../controller/YellowLightSyncControllerTest.java` |
| implement-code | `src/main/java/.../algo/yellowlightsync/YellowLightSync.java`、`AlgorithmController` の `POST /api/algorithms/yellow-light-sync` |
| draw-sequence | 設計書内の `## 🔄 シーケンス図` セクション |