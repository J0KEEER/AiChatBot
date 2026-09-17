import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to fetch real-time live weather information using wttr.in weather API.
 */
public class WeatherService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Pattern CITY_PATTERN = Pattern.compile(
            "\\b(?:in|for|at|of)\\s+([a-zA-Z\\s]{2,30}?)(?:\\s+\\b(?:right now|today|tomorrow|now|currently|please|pls)\\b|[?,.!]|$)",
            Pattern.CASE_INSENSITIVE
    );

    private WeatherService() {}

    /**
     * Fetches current weather for a specified location or default local area.
     *
     * @param location city or country name, or null/empty for auto IP location
     * @return formatted weather string
     */
    public static String getWeather(String location) {
        try {
            String baseUrl = "https://wttr.in/";
            String queryParam = "?format=3";

            String requestUrl;
            if (location != null && !location.trim().isEmpty()) {
                String cleanLoc = location.trim().replaceAll("[^a-zA-Z\\s-]", "");
                String encodedLoc = URLEncoder.encode(cleanLoc, StandardCharsets.UTF_8);
                requestUrl = baseUrl + encodedLoc + queryParam;
            } else {
                requestUrl = baseUrl + queryParam;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(4))
                    .header("User-Agent", "curl/8.0")
                    .header("Accept", "*/*")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                String rawBody = response.body().replaceAll("<[^>]*>", "").trim();
                String body = rawBody.replaceAll("\\s+", " ");
                // Ensure output is clean
                if (!body.contains("Unknown location") && !body.contains("404") && !body.isEmpty()) {
                    return "🌤️ Live Weather: " + body;
                }
            }

            // If specific city failed, retry with default IP location
            if (location != null && !location.isEmpty()) {
                return "I couldn't find live weather for '" + location + "'. Try checking the city name spelling (e.g. 'weather in London' or 'weather in Tokyo').";
            }
        } catch (Exception e) {
            System.err.println("[WeatherService] Weather fetch failed: " + e.getMessage());
        }

        return "I'm currently unable to reach the live weather service. Please ensure you are connected to the internet and try again!";
    }

    /**
     * Extracts a city name from natural language query like 'weather in London' or 'what is the weather for Tokyo'.
     *
     * @param userInput raw user message
     * @return extracted city name, or null if no specific city was mentioned
     */
    public static String extractCity(String userInput) {
        if (userInput == null) {
            return null;
        }

        Matcher matcher = CITY_PATTERN.matcher(userInput);
        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            candidate = candidate.replaceAll("(?i)\\b(?:right now|today|tomorrow|now|rn|please|pls|currently|and|the)\\b", "").trim();
            if (candidate.length() < 2) {
                return null;
            }
            String lower = candidate.toLowerCase();
            if (lower.equals("celsius") || lower.equals("fahrenheit") || lower.equals("morning") || lower.equals("evening") || lower.equals("night")) {
                return null;
            }
            return candidate;
        }

        return null;
    }
}
