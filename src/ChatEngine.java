import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Advanced ChatEngine handling NLP-based intent matching, multi-part compound queries,
 * broken/slang language understanding, and dynamic information synthesis (weather, date, time).
 */
public class ChatEngine {

    private final KnowledgeBase kb;
    private final Random random;

    public ChatEngine(KnowledgeBase kb) {
        this(kb, new Random());
    }

    public ChatEngine(KnowledgeBase kb, Random random) {
        this.kb = Objects.requireNonNull(kb, "KnowledgeBase cannot be null");
        this.random = Objects.requireNonNull(random, "Random generator cannot be null");
    }

    /**
     * Determines the most appropriate response for the given user input.
     * Supports single intents, multi-part questions, and dynamic queries.
     *
     * @param userInput the raw text input from the user
     * @return the chatbot's response message
     */
    public String getResponse(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return "Please type a message so I can help you!";
        }

        String cleaned = TextPreprocessor.cleanText(userInput);
        if (cleaned.isEmpty()) {
            return "Could you please rephrase that using standard words or characters?";
        }

        // 1. Check for compound / multi-part dynamic queries (e.g. weather + date + time in Greater Noida)
        String multiDynamic = handleMultiDynamicQuery(userInput, cleaned);
        if (multiDynamic != null) {
            return multiDynamic;
        }

        // 2. Standalone calculation query (e.g. "25 + 75", "what is 15 * 4", "calc 100 / 4", "15% of 200", "sqrt(144)")
        if (CalculatorService.isCalculationQuery(userInput, cleaned)) {
            return CalculatorService.process(userInput);
        }

        // 3. Multi-part intent queries (e.g. "what are your hours and who made you")
        String multiIntent = handleMultiIntentQuery(userInput);
        if (multiIntent != null) {
            return multiIntent;
        }

        // 3. Single dynamic handlers
        if (isWeatherQuery(cleaned)) {
            String city = WeatherService.extractCity(userInput);
            return WeatherService.getWeather(city);
        }
        if (isHourQuery(cleaned)) {
            return DateTimeService.getCurrentHour();
        }
        if (isTimeQuery(cleaned)) {
            return DateTimeService.getCurrentTime();
        }
        if (isDayQuery(cleaned)) {
            return DateTimeService.getCurrentDay();
        }
        if (isDateQuery(cleaned)) {
            return DateTimeService.getCurrentDate();
        }
        if (isYearQuery(cleaned)) {
            return DateTimeService.getCurrentYear();
        }

        // 4. Standard single intent match
        List<String> userTokens = TextPreprocessor.tokenize(userInput);
        Intent bestMatch = matchIntent(userInput, userTokens);

        if (bestMatch != null) {
            String tag = bestMatch.getTag();
            if ("weather".equalsIgnoreCase(tag)) {
                String city = WeatherService.extractCity(userInput);
                return WeatherService.getWeather(city);
            } else if ("time".equalsIgnoreCase(tag)) {
                return cleaned.contains("hour") ? DateTimeService.getCurrentHour() : DateTimeService.getCurrentTime();
            } else if ("date".equalsIgnoreCase(tag)) {
                return DateTimeService.getCurrentDate();
            } else if ("day".equalsIgnoreCase(tag)) {
                return DateTimeService.getCurrentDay();
            } else if ("year".equalsIgnoreCase(tag)) {
                return DateTimeService.getCurrentYear();
            }

            List<String> responses = bestMatch.getResponses();
            if (!responses.isEmpty()) {
                return responses.get(random.nextInt(responses.size()));
            }
        }

        if (userTokens.isEmpty()) {
            return "Could you please rephrase that with more details, or try asking about our hours, services, live weather, or current time?";
        }

        return "Sorry, I didn't understand that. Could you rephrase, or ask about our hours, services, live weather, time, or type 'help' for options.";
    }

    /**
     * Handles compound dynamic requests combining weather, date, day, time, hour, or year.
     */
    private String handleMultiDynamicQuery(String rawInput, String cleaned) {
        boolean hasWeather = isWeatherQuery(cleaned);
        boolean hasTime = isTimeQuery(cleaned);
        boolean hasHour = isHourQuery(cleaned);
        boolean hasDate = isDateQuery(cleaned);
        boolean hasDay = isDayQuery(cleaned);
        boolean hasYear = isYearQuery(cleaned);

        int count = (hasWeather ? 1 : 0) + (hasTime ? 1 : 0) + (hasHour ? 1 : 0)
                + (hasDate ? 1 : 0) + (hasDay ? 1 : 0) + (hasYear ? 1 : 0);

        if (count < 2) {
            return null;
        }

        StringBuilder sb = new StringBuilder("Here is the information you requested:\n");

        if (hasWeather) {
            String city = WeatherService.extractCity(rawInput);
            sb.append("• ").append(WeatherService.getWeather(city)).append("\n");
        }

        if (hasDate) {
            sb.append("• 📅 Date: ").append(DateTimeService.getCurrentDate());
            if (!hasDay) {
                String dayName = LocalDate.now().getDayOfWeek().name();
                String dayCap = dayName.charAt(0) + dayName.substring(1).toLowerCase(Locale.ENGLISH);
                sb.append(" (").append(dayCap).append(")");
            }
            sb.append("\n");
        }

        if (hasDay && !hasDate) {
            sb.append("• 📅 Day: ").append(DateTimeService.getCurrentDay()).append("\n");
        }

        if (hasTime) {
            sb.append("• 🕒 Time: ").append(DateTimeService.getCurrentTime()).append("\n");
        } else if (hasHour) {
            sb.append("• 🕒 Hour: ").append(DateTimeService.getCurrentHour()).append("\n");
        }

        if (hasYear && !hasDate) {
            sb.append("• 🗓️ Year: ").append(DateTimeService.getCurrentYear()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Handles compound multi-part intent queries separated by 'and', commas, or conjunctions.
     */
    private String handleMultiIntentQuery(String rawInput) {
        String[] clauses = rawInput.split("\\b(?:and|also|plus|as well as)\\b|,");
        if (clauses.length < 2) {
            return null;
        }

        List<String> results = new ArrayList<>();
        Set<String> seenTags = new HashSet<>();

        for (String clause : clauses) {
            String trimmed = clause.trim();
            if (trimmed.length() < 2) continue;

            // 1. Check if clause is a calculation query
            String cleanedClause = TextPreprocessor.cleanText(trimmed);
            if (CalculatorService.isCalculationQuery(trimmed, cleanedClause)) {
                if (!seenTags.contains("calculation")) {
                    seenTags.add("calculation");
                    String calcResult = CalculatorService.process(trimmed).replace("🧮 ", "");
                    if (!calcResult.startsWith("Calculation:")) {
                        calcResult = "Calculation: " + calcResult;
                    }
                    results.add("• " + calcResult);
                }
                continue;
            }

            // 2. Match standard conversational intent
            List<String> tokens = TextPreprocessor.tokenize(trimmed);
            Intent match = matchIntent(trimmed, tokens);
            if (match != null && !seenTags.contains(match.getTag())) {
                seenTags.add(match.getTag());
                String tag = match.getTag();
                String label = tag.substring(0, 1).toUpperCase() + tag.substring(1).replace("_", " ");
                List<String> resps = match.getResponses();
                String reply = resps.get(random.nextInt(resps.size()));
                results.add("• " + label + ": " + reply);
            }
        }

        if (results.size() >= 2) {
            StringBuilder sb = new StringBuilder("Here is what you wanted to know:\n");
            for (String item : results) {
                sb.append(item).append("\n\n");
            }
            return sb.toString().trim();
        }

        return null;
    }

    private boolean isWeatherQuery(String cleaned) {
        return cleaned.contains("weather") || cleaned.contains("temperature") || cleaned.contains("forecast")
                || cleaned.contains("is it raining") || cleaned.contains("how hot") || cleaned.contains("how cold")
                || cleaned.contains("climate");
    }

    private boolean isTimeQuery(String cleaned) {
        if (isBusinessHoursQuery(cleaned)) {
            return false;
        }
        return cleaned.equals("time") || cleaned.contains("what time") || cleaned.contains("current time")
                || cleaned.contains("time now") || cleaned.contains("clock")
                || (cleaned.contains("time") && !cleaned.contains("timing") && !cleaned.contains("schedule"));
    }

    private boolean isHourQuery(String cleaned) {
        if (isBusinessHoursQuery(cleaned)) {
            return false;
        }
        return cleaned.equals("hour") || cleaned.contains("what hour")
                || cleaned.contains("which hour") || cleaned.contains("current hour");
    }

    private boolean isDateQuery(String cleaned) {
        return cleaned.contains("date") || cleaned.contains("calendar");
    }

    private boolean isDayQuery(String cleaned) {
        return cleaned.contains("what day") || cleaned.contains("which day")
                || cleaned.contains("day is today") || cleaned.contains("day of week")
                || cleaned.contains("day of the week")
                || (cleaned.contains("day") && !cleaned.contains("day to day") && !cleaned.contains("have a great day") && !cleaned.contains("good day") && !isBusinessHoursQuery(cleaned));
    }

    private boolean isYearQuery(String cleaned) {
        return cleaned.contains("year") || cleaned.contains("which year") || cleaned.contains("what year");
    }

    private boolean isBusinessHoursQuery(String cleaned) {
        return cleaned.contains("working hours") || cleaned.contains("office hours")
                || cleaned.contains("open hours") || cleaned.contains("timing")
                || cleaned.contains("operating schedule") || cleaned.contains("when are you open")
                || cleaned.contains("your hours") || cleaned.contains("business hours")
                || cleaned.contains("support hours");
    }

    /**
     * Finds the intent with the highest overlap and pattern match score.
     *
     * @param rawInput   the raw or cleaned input string
     * @param userTokens tokenized input words
     * @return best matching Intent, or null if no pattern meets threshold
     */
    public Intent matchIntent(String rawInput, List<String> userTokens) {
        String cleanedInput = TextPreprocessor.cleanText(rawInput);
        Intent bestMatch = null;
        int bestScore = 0;

        for (Intent intent : kb.getIntents()) {
            for (String pattern : intent.getPatterns()) {
                String cleanedPattern = TextPreprocessor.cleanText(pattern);
                List<String> patternTokens = TextPreprocessor.tokenize(pattern);

                int score = calculateScore(cleanedInput, userTokens, cleanedPattern, patternTokens);
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = intent;
                }
            }
        }

        return (bestScore > 0) ? bestMatch : null;
    }

    /**
     * Calculates matching score based on token overlap with whole-word phrase bonuses.
     */
    private int calculateScore(String cleanedInput, List<String> userTokens,
                               String cleanedPattern, List<String> patternTokens) {
        int score = 0;

        String paddedInput = " " + cleanedInput + " ";
        String paddedPattern = " " + cleanedPattern + " ";

        // 1. Exact full sentence match
        if (!cleanedPattern.isEmpty() && cleanedInput.equals(cleanedPattern)) {
            score += 25;
        } else if (!cleanedPattern.isEmpty() && paddedInput.contains(paddedPattern)) {
            // Whole-word phrase containment bonus scaled by phrase length
            int phraseWords = cleanedPattern.split("\\s+").length;
            score += 10 + (phraseWords * 3);
        }

        // 2. Token overlap
        int matchedTokens = 0;
        for (String token : userTokens) {
            if (patternTokens.contains(token)) {
                matchedTokens++;
            }
        }

        if (matchedTokens > 0) {
            score += matchedTokens * 3;
            // Bonus if all tokens in the training pattern were found in user query
            if (!patternTokens.isEmpty() && matchedTokens == patternTokens.size()) {
                score += 4;
            }
        }

        return score;
    }

    public KnowledgeBase getKnowledgeBase() {
        return kb;
    }
}
