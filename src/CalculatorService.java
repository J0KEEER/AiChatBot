import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service providing arithmetic calculations, percentage evaluation,
 * mathematical functions (sqrt, cbrt, abs, round), exponentiation, and
 * natural-language math query translation.
 */
public class CalculatorService {

    private static final Pattern PERCENT_PATTERN = Pattern.compile(
            "(?i)\\b([0-9]+(?:\\.[0-9]+)?)\\s*(?:%|percent)\\s+of\\s+([0-9]+(?:\\.[0-9]+)?)\\b"
    );

    private static final Pattern ROOT_PATTERN = Pattern.compile(
            "(?i)\\b(?:square\\s+root|cube\\s+root|sqrt|cbrt)\\s*(?:of)?\\s*\\(?([0-9]+(?:\\.[0-9]+)?)\\)?"
    );

    private static final Pattern EXPLICIT_CALC_PREFIX = Pattern.compile(
            "(?i)^(?:please\\s+|pls\\s+|can\\s+you\\s+|tell\\s+me\\s+)?(?:calculate|calc|compute|evaluate|solve)\\b"
    );

    private static final Pattern WHAT_IS_PREFIX = Pattern.compile(
            "(?i)^(?:please\\s+|pls\\s+)?(?:what\\s+is\\s+the|what\\s+is|whats\\s+the|whats|how\\s+much\\s+is)\\b"
    );

    private CalculatorService() {}

    /**
     * Determines whether the given user input represents a calculation request.
     *
     * @param rawInput raw string input from user
     * @param cleaned  preprocessed alphanumeric text
     * @return true if the input should be handled by CalculatorService
     */
    public static boolean isCalculationQuery(String rawInput, String cleaned) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return false;
        }

        String trimmed = rawInput.trim();
        String lower = trimmed.toLowerCase(Locale.ENGLISH);

        // 1. Standalone calculator / math inquiry
        if (lower.equals("calc") || lower.equals("calculate") || lower.equals("calculator")
                || lower.equals("math") || lower.equals("how to calculate") || lower.equals("math help")) {
            return true;
        }

        // 2. Explicit calculation verbs (calc, calculate, compute, evaluate, solve)
        if (EXPLICIT_CALC_PREFIX.matcher(trimmed).find()) {
            return true;
        }

        // 3. Percentage queries (e.g. "15% of 200" or "what is 20 percent of 500")
        if (PERCENT_PATTERN.matcher(trimmed).find()) {
            return true;
        }

        // 4. Square root or cube root queries
        if (ROOT_PATTERN.matcher(trimmed).find()) {
            return true;
        }

        // 5. "What is / How much is" math queries
        if (WHAT_IS_PREFIX.matcher(trimmed).find()) {
            boolean hasMathSymbol = trimmed.contains("+") || trimmed.contains("-")
                    || trimmed.contains("*") || trimmed.contains("/") || trimmed.contains("%")
                    || trimmed.contains("^") || trimmed.contains("×") || trimmed.contains("÷");
            boolean hasMathWord = lower.contains("plus") || lower.contains("minus")
                    || lower.contains("times") || lower.contains("multiplied")
                    || lower.contains("divided") || lower.contains("modulo")
                    || lower.contains("power") || lower.contains("sqrt");
            if (hasMathSymbol || hasMathWord) {
                return true;
            }
        }

        // 6. Direct arithmetic expression (e.g. "25 + 75", "100 / 4", "(10 + 5) * 2", "2 ^ 8")
        if (isPureArithmeticExpression(trimmed)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if a string contains numbers and math operators without unrelated conversational words.
     */
    private static boolean isPureArithmeticExpression(String text) {
        String s = text.trim();
        if (s.endsWith("?") || s.endsWith("=")) {
            s = s.substring(0, s.length() - 1).trim();
        }

        // Must contain at least one digit
        if (!s.matches(".*\\d.*")) {
            return false;
        }

        // Must contain at least one math operator or math word
        boolean hasOperator = s.matches(".*[+\\-*/%^×÷].*")
                || s.matches("(?i).*\\b(?:plus|minus|times|divided|over|mod)\\b.*");
        if (!hasOperator) {
            return false;
        }

        // Check that remaining characters are allowed in math expressions
        String normalized = s.replaceAll("(?i)\\b(?:plus|minus|times|divided|by|over|mod|sqrt|cbrt|abs|round|pi|e)\\b", "")
                             .replaceAll("[0-9\\s.+\\-*/%^()×÷]", "");

        return normalized.isEmpty();
    }

    /**
     * Evaluates the calculation query and returns a formatted response string.
     *
     * @param rawInput user input
     * @return formatted answer or error description
     */
    public static String process(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return getHelpMessage();
        }

        String trimmed = rawInput.trim();
        String lower = trimmed.toLowerCase(Locale.ENGLISH);

        // Standalone help
        if (lower.equals("calc") || lower.equals("calculate") || lower.equals("calculator")
                || lower.equals("math") || lower.equals("how to calculate") || lower.equals("math help")) {
            return getHelpMessage();
        }

        // Percentage query evaluation: "X% of Y"
        Matcher pctMatcher = PERCENT_PATTERN.matcher(trimmed);
        if (pctMatcher.find()) {
            try {
                double pct = Double.parseDouble(pctMatcher.group(1));
                double base = Double.parseDouble(pctMatcher.group(2));
                double result = (pct / 100.0) * base;
                return "🧮 " + formatNumber(pct) + "% of " + formatNumber(base) + " = " + formatNumber(result);
            } catch (Exception e) {
                return "⚠️ Math Error: Could not evaluate percentage: " + e.getMessage();
            }
        }

        // Normalize natural language expression
        String mathExpr = normalizeExpression(trimmed);
        if (mathExpr.isEmpty()) {
            return getHelpMessage();
        }

        try {
            double result = evaluate(mathExpr);
            return "🧮 Calculation: " + mathExpr + " = " + formatNumber(result);
        } catch (ArithmeticException e) {
            return "⚠️ Math Error: " + e.getMessage();
        } catch (Exception e) {
            return "⚠️ Math Error: Could not evaluate expression '" + mathExpr + "'. Please check the syntax (e.g. 'calc (15 + 5) * 4').";
        }
    }

    /**
     * Translates conversational phrasing into a standard evaluable arithmetic expression.
     */
    public static String normalizeExpression(String input) {
        String expr = input.trim();

        // Strip prefixes
        expr = EXPLICIT_CALC_PREFIX.matcher(expr).replaceFirst("");
        expr = WHAT_IS_PREFIX.matcher(expr).replaceFirst("");

        // Strip trailing punctuation
        expr = expr.replaceAll("[?!.]+$", "").trim();

        // Convert word operations to standard operators
        expr = expr.replaceAll("(?i)\\bmultiplied\\s+by\\b", "*")
                   .replaceAll("(?i)\\btimes\\b", "*")
                   .replaceAll("(?i)\\bdivided\\s+by\\b", "/")
                   .replaceAll("(?i)\\bdivide\\s+by\\b", "/")
                   .replaceAll("(?i)\\bover\\b", "/")
                   .replaceAll("(?i)\\bplus\\b", "+")
                   .replaceAll("(?i)\\badd\\b", "+")
                   .replaceAll("(?i)\\bminus\\b", "-")
                   .replaceAll("(?i)\\bsubtract\\b", "-")
                   .replaceAll("(?i)\\bto\\s+the\\s+power\\s+of\\b", "^")
                   .replaceAll("(?i)\\braised\\s+to\\b", "^")
                   .replaceAll("(?i)\\bpower\\s+of\\b", "^")
                   .replaceAll("(?i)\\bpower\\b", "^")
                   .replaceAll("(?i)\\bmodulo\\b", "%")
                   .replaceAll("(?i)\\bmod\\b", "%")
                   .replaceAll("(?i)\\bremainder\\s+of\\b", "%")
                   .replaceAll("×", "*")
                   .replaceAll("÷", "/");

        // Convert root phrasing: "square root of 144" -> "sqrt(144)"
        expr = expr.replaceAll("(?i)\\bsquare\\s+root\\s+of\\s+([0-9]+(?:\\.[0-9]+)?)", "sqrt($1)")
                   .replaceAll("(?i)\\bsquare\\s+root\\s*\\(", "sqrt(")
                   .replaceAll("(?i)\\bcube\\s+root\\s+of\\s+([0-9]+(?:\\.[0-9]+)?)", "cbrt($1)")
                   .replaceAll("(?i)\\bcube\\s+root\\s*\\(", "cbrt(");

        // Normalize solitary x as multiplication when between numbers/spaces
        expr = expr.replaceAll("(?<=\\d)\\s*[xX]\\s*(?=\\d)", " * ");

        return expr.trim();
    }

    /**
     * Evaluates an arithmetic expression string with standard precedence.
     */
    public static double evaluate(String expression) {
        ExpressionParser parser = new ExpressionParser(expression);
        return parser.parse();
    }

    /**
     * Formats a double value cleanly, stripping superfluous trailing decimal zeros.
     */
    public static String formatNumber(double val) {
        if (Double.isNaN(val)) {
            return "NaN";
        }
        if (Double.isInfinite(val)) {
            return val > 0 ? "Infinity" : "-Infinity";
        }
        if (Math.abs(val - Math.round(val)) < 1e-9 && Math.abs(val) < 1e15) {
            return String.valueOf(Math.round(val));
        }
        BigDecimal bd = BigDecimal.valueOf(val).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString();
    }

    private static String getHelpMessage() {
        return "🧮 I can perform calculations for you! Try asking:\n"
                + "• calc 25 * 4\n"
                + "• 15% of 200\n"
                + "• sqrt(144)\n"
                + "• 2 ^ 8\n"
                + "• (10 + 5) / 3\n"
                + "• 45 + 55";
    }

    /**
     * Recursive descent expression parser supporting +, -, *, /, %, ^, parentheses,
     * constants (pi, e), and functions (sqrt, cbrt, abs, round).
     */
    private static class ExpressionParser {
        private final String text;
        private int pos = -1;
        private int ch;

        public ExpressionParser(String text) {
            this.text = text;
            nextChar();
        }

        private void nextChar() {
            ch = (++pos < text.length()) ? text.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                nextChar();
            }
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        public double parse() {
            double x = parseExpression();
            while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                nextChar();
            }
            if (pos < text.length()) {
                throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            }
            return x;
        }

        private double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+')) {
                    x += parseTerm();
                } else if (eat('-')) {
                    x -= parseTerm();
                } else {
                    return x;
                }
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*')) {
                    x *= parseFactor();
                } else if (eat('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0.0) {
                        throw new ArithmeticException("Cannot divide by zero (undefined)");
                    }
                    x /= divisor;
                } else if (eat('%')) {
                    double divisor = parseFactor();
                    if (divisor == 0.0) {
                        throw new ArithmeticException("Cannot divide by zero (undefined)");
                    }
                    x %= divisor;
                } else {
                    return x;
                }
            }
        }

        private double parseFactor() {
            double x = parseUnary();
            if (eat('^')) {
                double exponent = parseFactor();
                x = Math.pow(x, exponent);
            }
            return x;
        }

        private double parseUnary() {
            if (eat('+')) return +parseUnary();
            if (eat('-')) return -parseUnary();
            return parsePrimary();
        }

        private double parsePrimary() {
            while (ch == ' ' || ch == '\t') {
                nextChar();
            }

            int startPos = this.pos;
            if (eat('(')) {
                double x = parseExpression();
                if (!eat(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis ')'");
                }
                return x;
            }

            if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') {
                    nextChar();
                }
                String numStr = text.substring(startPos, this.pos);
                try {
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format: " + numStr);
                }
            }

            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                    nextChar();
                }
                String func = text.substring(startPos, this.pos).toLowerCase(Locale.ENGLISH);

                if (func.equals("pi")) return Math.PI;
                if (func.equals("e")) return Math.E;

                if (!eat('(')) {
                    throw new IllegalArgumentException("Expected '(' after function: " + func);
                }
                double x = parseExpression();
                if (!eat(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis ')' for function: " + func);
                }

                switch (func) {
                    case "sqrt":
                        if (x < 0) {
                            throw new ArithmeticException("Square root of a negative number is not a real number");
                        }
                        return Math.sqrt(x);
                    case "cbrt":
                        return Math.cbrt(x);
                    case "abs":
                        return Math.abs(x);
                    case "round":
                        return Math.round(x);
                    default:
                        throw new IllegalArgumentException("Unknown math function: " + func);
                }
            }

            throw new IllegalArgumentException("Unexpected character: " + (ch == -1 ? "End of input" : String.valueOf((char) ch)));
        }
    }
}
