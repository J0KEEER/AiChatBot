import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NLP Text Preprocessor with slang expansion, typo tolerance (Levenshtein distance),
 * normalization, tokenization, and stop-word filtering.
 */
public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "i", "you", "he", "she", "it",
            "they", "we", "do", "does", "did", "please", "can", "could", "would", "should",
            "of", "in", "at", "to", "for", "with", "me", "my", "your"
    )));

    // Common words that should never be mutated by fuzzy matching
    private static final Set<String> PRESERVED_WORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "your", "you", "four", "our", "are", "here", "there", "where", "hear", "year",
            "more", "some", "come", "done", "fine", "line", "mine", "name", "good", "have",
            "hours", "hour", "time", "date", "day", "days"
    )));

    // Common chat slang, abbreviations, and broken spelling normalization
    private static final Map<String, String> SLANG_MAP = new HashMap<>();

    static {
        SLANG_MAP.put("u", "you");
        SLANG_MAP.put("ur", "your");
        SLANG_MAP.put("r", "are");
        SLANG_MAP.put("wat", "what");
        SLANG_MAP.put("wht", "what");
        SLANG_MAP.put("wats", "what is");
        SLANG_MAP.put("whats", "what is");
        SLANG_MAP.put("wts", "what is");
        SLANG_MAP.put("hu", "who");
        SLANG_MAP.put("hw", "how");
        SLANG_MAP.put("hwo", "how");
        SLANG_MAP.put("hwz", "how is");
        SLANG_MAP.put("tym", "time");
        SLANG_MAP.put("tiem", "time");
        SLANG_MAP.put("tm", "time");
        SLANG_MAP.put("rn", "now");
        SLANG_MAP.put("weathr", "weather");
        SLANG_MAP.put("wether", "weather");
        SLANG_MAP.put("wethr", "weather");
        SLANG_MAP.put("clg", "college");
        SLANG_MAP.put("plz", "please");
        SLANG_MAP.put("pls", "please");
        SLANG_MAP.put("thx", "thanks");
        SLANG_MAP.put("thnx", "thanks");
        SLANG_MAP.put("ty", "thanks");
        SLANG_MAP.put("abt", "about");
        SLANG_MAP.put("dat", "date");
        SLANG_MAP.put("daet", "date");
        SLANG_MAP.put("yr", "year");
        SLANG_MAP.put("hrs", "hours");
        SLANG_MAP.put("hlp", "help");
        SLANG_MAP.put("loc", "location");
        SLANG_MAP.put("addr", "address");
        SLANG_MAP.put("n", "and");
    }

    // Domain keywords for fuzzy typo matching
    private static final String[] DOMAIN_KEYWORDS = {
            "weather", "temperature", "forecast", "services", "contact", "creator", "goodbye"
    };

    private TextPreprocessor() {}

    /**
     * Normalizes text by expanding slang, correcting minor typos, converting to lowercase,
     * and separating punctuation cleanly.
     *
     * @param input raw user input
     * @return cleaned alphanumeric string with whitespace
     */
    public static String cleanText(String input) {
        if (input == null) {
            return "";
        }

        // Expand common contractions
        String text = input.toLowerCase()
                .replaceAll("what's", "what is ")
                .replaceAll("how's", "how is ")
                .replaceAll("who's", "who is ")
                .replaceAll("it's", "it is ")
                .replaceAll("can't", "cannot ")
                .replaceAll("won't", "will not ")
                .replaceAll("n't", " not ");

        // Replace punctuation with whitespace so words aren't merged
        text = text.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        if (text.isEmpty()) {
            return "";
        }

        // Slang expansion and fuzzy spelling correction per token
        String[] words = text.split("\\s+");
        StringBuilder normalized = new StringBuilder();
        for (String word : words) {
            String mapped = SLANG_MAP.get(word);
            if (mapped != null) {
                normalized.append(mapped).append(" ");
            } else {
                String corrected = correctFuzzy(word);
                normalized.append(corrected).append(" ");
            }
        }

        return normalized.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Tokenizes input text into a list of normalized words, filtering out stop words.
     *
     * @param input user or pattern input
     * @return filtered list of significant word tokens
     */
    public static List<String> tokenize(String input) {
        String cleaned = cleanText(input);
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        String[] rawTokens = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String word : rawTokens) {
            if (!word.isBlank() && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    /**
     * Performs fuzzy correction if a word is very close to a key domain keyword.
     * Only applies to words of length >= 6 that are not standard common English words.
     */
    private static String correctFuzzy(String word) {
        if (word.length() < 6 || PRESERVED_WORDS.contains(word)) {
            return word;
        }

        for (String keyword : DOMAIN_KEYWORDS) {
            if (word.equalsIgnoreCase(keyword)) {
                return word;
            }
            int dist = levenshteinDistance(word, keyword);
            if (dist <= 2) {
                return keyword;
            }
        }
        return word;
    }

    /**
     * Calculates Levenshtein edit distance between two strings.
     */
    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    public static Set<String> getStopWords() {
        return STOP_WORDS;
    }
}
