import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a conversational intent containing:
 * - A unique tag/label (e.g. 'greeting', 'hours')
 * - Sample training patterns/phrases
 * - Possible bot response options
 */
public class Intent {
    private final String tag;
    private final List<String> patterns;
    private final List<String> responses;

    public Intent(String tag, List<String> patterns, List<String> responses) {
        this.tag = Objects.requireNonNull(tag, "Intent tag cannot be null").trim();
        this.patterns = new ArrayList<>(Objects.requireNonNull(patterns, "Patterns list cannot be null"));
        this.responses = new ArrayList<>(Objects.requireNonNull(responses, "Responses list cannot be null"));
    }

    public String getTag() {
        return tag;
    }

    public List<String> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    public List<String> getResponses() {
        return Collections.unmodifiableList(responses);
    }

    @Override
    public String toString() {
        return "Intent{" +
                "tag='" + tag + '\'' +
                ", patternsCount=" + patterns.size() +
                ", responsesCount=" + responses.size() +
                '}';
    }
}
