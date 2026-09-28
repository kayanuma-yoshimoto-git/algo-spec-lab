package com.example.algospeclab.algo.ancientbase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * specs/ancient-base/README.md 「5. 設計のアプローチ」で採用した実装。
 *
 * <p>2〜9 進法を総当たりで検証して進法の候補を絞り、結果が消えた式を候補ごとに計算する。
 * 全候補で表記が一致すればその値、一致しなければ {@code ?} で埋める。</p>
 */
public final class AncientBase {

    private static final int MIN_BASE = 2;
    private static final int MAX_BASE = 9;
    private static final int MIN_EXPRESSIONS = 2;
    private static final int MAX_EXPRESSIONS = 100;
    private static final String ERASED = "X";
    private static final String UNCERTAIN = "?";
    private static final Pattern EXPRESSION_PATTERN =
            Pattern.compile("(\\d{1,2}) ([+-]) (\\d{1,2}) = (\\d{1,3}|X)");

    private AncientBase() {
    }

    /**
     * 結果が消えた式の結果を埋め、入力順に返す。
     *
     * @param expressions "A + B = C" または "A - B = C" 形式の式(C は X または整数、2〜100 件)
     * @return 結果が消えた式を "A op B = 結果" の形にしたもの(結果が不確定なら "?")
     * @throws IllegalArgumentException 形式が不正、結果が消えた式が無い、候補の進法が無い、または結果が負になる場合
     */
    public static String[] solve(String[] expressions) {
        List<Expression> parsed = parse(expressions);
        List<Integer> bases = candidateBases(parsed);
        if (bases.isEmpty()) {
            throw new IllegalArgumentException("式どうしが矛盾しており、2〜9 進法のいずれとも合いません。");
        }

        List<String> results = new ArrayList<>();
        for (Expression expression : parsed) {
            if (expression.isErased()) {
                results.add(expression.left() + " " + expression.operator() + " " + expression.right()
                        + " = " + fillResult(expression, bases));
            }
        }
        if (results.isEmpty()) {
            throw new IllegalArgumentException("結果が消えた式(= X)がありません。");
        }
        return results.toArray(new String[0]);
    }

    /** 全候補の進法で計算し、表記が一致すればその値、一致しなければ "?" を返す。 */
    private static String fillResult(Expression expression, List<Integer> bases) {
        String common = null;
        for (int base : bases) {
            int value = expression.evaluate(base);
            if (value < 0) {
                throw new IllegalArgumentException("結果が負になる式があります: " + expression.left()
                        + " " + expression.operator() + " " + expression.right());
            }
            String written = Integer.toString(value, base);
            if (common == null) {
                common = written;
            } else if (!common.equals(written)) {
                return UNCERTAIN;
            }
        }
        return common;
    }

    /** すべての式の数字が書けて、結果が既知の式がすべて成り立つ進法を返す。 */
    private static List<Integer> candidateBases(List<Expression> expressions) {
        List<Integer> bases = new ArrayList<>();
        for (int base = MIN_BASE; base <= MAX_BASE; base++) {
            if (isConsistent(expressions, base)) {
                bases.add(base);
            }
        }
        return bases;
    }

    private static boolean isConsistent(List<Expression> expressions, int base) {
        for (Expression expression : expressions) {
            if (!expression.isWritableIn(base)) {
                return false;
            }
            if (!expression.isErased()
                    && expression.evaluate(base) != Integer.parseInt(expression.result(), base)) {
                return false;
            }
        }
        return true;
    }

    private static List<Expression> parse(String[] expressions) {
        if (expressions == null || expressions.length < MIN_EXPRESSIONS || expressions.length > MAX_EXPRESSIONS) {
            throw new IllegalArgumentException(
                    "expressions は " + MIN_EXPRESSIONS + "〜" + MAX_EXPRESSIONS + " 件で指定してください。");
        }
        List<Expression> parsed = new ArrayList<>();
        for (String text : expressions) {
            Matcher matcher = text == null ? null : EXPRESSION_PATTERN.matcher(text);
            if (matcher == null || !matcher.matches()) {
                throw new IllegalArgumentException("式の形式が不正です: " + text);
            }
            parsed.add(new Expression(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)));
        }
        return parsed;
    }

    /** 1つの式。数字列は文字列のまま保持し、進法ごとに解釈する。 */
    private record Expression(String left, String operator, String right, String result) {

        boolean isErased() {
            return ERASED.equals(result);
        }

        /** 式に現れる数字(結果が消えた式は A と B のみ)がすべて base 未満か。 */
        boolean isWritableIn(int base) {
            return maxDigit(left) < base && maxDigit(right) < base && (isErased() || maxDigit(result) < base);
        }

        /** base 進法で解釈した A op B の値(10 進の int)を返す。 */
        int evaluate(int base) {
            int a = Integer.parseInt(left, base);
            int b = Integer.parseInt(right, base);
            return "+".equals(operator) ? a + b : a - b;
        }

        private static int maxDigit(String number) {
            int max = 0;
            for (int i = 0; i < number.length(); i++) {
                max = Math.max(max, number.charAt(i) - '0');
            }
            return max;
        }
    }
}
