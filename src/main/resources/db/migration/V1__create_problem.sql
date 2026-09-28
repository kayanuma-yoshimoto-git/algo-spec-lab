-- アルゴリズム課題マスタ
-- 起動時に AlgorithmController のエンドポイントと設計書 (specs/<slug>/README.md) から登録・更新される
CREATE TABLE problem (
    id          BIGSERIAL    PRIMARY KEY,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    title       VARCHAR(200) NOT NULL,
    description TEXT         NOT NULL,
    arguments   TEXT         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE problem IS 'アルゴリズム課題';
COMMENT ON COLUMN problem.slug IS '課題の slug（specs/<slug> のディレクトリ名、API パスの末尾）';
COMMENT ON COLUMN problem.title IS '課題名（設計書の見出し 1）';
COMMENT ON COLUMN problem.description IS '課題説明（設計書 README.md の Markdown 全文）';
COMMENT ON COLUMN problem.arguments IS '引数（設計書のリクエスト DTO の引数リスト）';
