import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages conversational intents loaded from an external knowledge base file
 * or built-in defaults.
 */
public class KnowledgeBase {

    private final List<Intent> intents = new ArrayList<>();
    private final String filePath;

    public KnowledgeBase(String filePath) {
        this.filePath = filePath;
        loadFromFile(filePath);
    }

    /**
     * Loads intents from a pipe-delimited file.
     * Line format: tag|pattern1;pattern2;...|response1;response2;...
     */
    private void loadFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("[KnowledgeBase] File not found at '" + path + "'. Loading built-in default intents.");
            loadDefaults();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int loadedCount = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    String tag = parts[0].trim();
                    List<String> patterns = parseList(parts[1]);
                    List<String> responses = parseList(parts[2]);

                    if (!tag.isEmpty() && !patterns.isEmpty() && !responses.isEmpty()) {
                        intents.add(new Intent(tag, patterns, responses));
                        loadedCount++;
                    }
                } else {
                    System.err.println("[KnowledgeBase] Skipping malformed line: " + line);
                }
            }

            if (loadedCount == 0) {
                System.err.println("[KnowledgeBase] Knowledge base was empty. Loading defaults.");
                loadDefaults();
            } else {
                System.out.println("[KnowledgeBase] Successfully loaded " + loadedCount + " intents from " + path);
            }
        } catch (IOException e) {
            System.err.println("[KnowledgeBase] Error reading '" + path + "': " + e.getMessage() + ". Falling back to defaults.");
            loadDefaults();
        }
    }

    private List<String> parseList(String raw) {
        String[] items = raw.split(";");
        List<String> list = new ArrayList<>();
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    /**
     * Populates knowledge base with default fallback FAQs.
     */
    public void loadDefaults() {
        intents.clear();
        intents.add(new Intent("greeting",
                Arrays.asList("hi", "hello", "hey", "good morning", "good evening", "greetings"),
                Arrays.asList(
                        "Hello! How can I help you today?",
                        "Hi there! What can I do for you?",
                        "Greetings! How may I assist you?"
                )));

        intents.add(new Intent("goodbye",
                Arrays.asList("bye", "goodbye", "see you", "exit", "quit", "cya"),
                Arrays.asList(
                        "Goodbye! Have a great day.",
                        "See you soon! Feel free to return if you have more questions.",
                        "Have a wonderful time ahead! Farewell."
                )));

        intents.add(new Intent("thanks",
                Arrays.asList("thanks", "thank you", "appreciate it", "much appreciated", "thx"),
                Arrays.asList(
                        "You're very welcome!",
                        "Glad I could help!",
                        "Anytime! Let me know if you need anything else."
                )));

        intents.add(new Intent("about",
                Arrays.asList("who are you", "what are you", "your name", "tell me about yourself", "introduce yourself", "about"),
                Arrays.asList(
                        "I am an intelligent AI FAQ Chatbot built in Java using NLP text processing and rule-based intent matching.",
                        "I am a virtual desktop assistant designed to answer questions, guide you with information, and help with common queries."
                )));

        intents.add(new Intent("creator",
                Arrays.asList("who made you", "who created you", "who is your developer", "who is the author", "creator", "developer"),
                Arrays.asList(
                        "I was created by Mridul as a Java programming project featuring rule-based NLP and Swing GUI.",
                        "I was built by Mridul in Java to demonstrate NLP-based text processing and interactive chatbot design."
                )));

        intents.add(new Intent("user_identity",
                Arrays.asList("who am i", "who i am", "my identity", "do you know me"),
                Arrays.asList(
                        "You are the user, and I am here to assist you with any questions or tasks you have!",
                        "You are my user! Feel free to ask me anything about our services, hours, or general questions."
                )));

        intents.add(new Intent("hours",
                Arrays.asList("hours", "what are your hours", "timing", "working hours", "open hours", "when are you open", "office hours", "schedule"),
                Arrays.asList(
                        "Our operating hours are Monday through Saturday, 9:00 AM to 6:00 PM.",
                        "We are available Monday to Saturday, between 9:00 AM and 6:00 PM."
                )));

        intents.add(new Intent("services",
                Arrays.asList("services", "what are your services", "what do you do", "what can you help with", "capabilities", "features"),
                Arrays.asList(
                        "I can answer frequently asked questions, provide support assistance, guide you with service schedules, and chat in real-time.",
                        "My capabilities include interactive conversation, text preprocessing, keyword matching, and answering common queries."
                )));

        intents.add(new Intent("contact",
                Arrays.asList("contact", "how to contact", "email", "phone", "reach you", "call", "support email", "helpline", "contact support"),
                Arrays.asList(
                        "You can reach out via email at support@assistant.ai or submit your inquiry through the help portal.",
                        "Feel free to contact support at support@assistant.ai for any assistance."
                )));

        intents.add(new Intent("how_are_you",
                Arrays.asList("how are you", "how are you doing", "how are things", "hows it going", "how is it going", "how do you do", "how are you feeling", "whats up", "hows life"),
                Arrays.asList(
                        "I'm doing great, thank you for asking! How are you doing today?",
                        "I'm feeling fantastic and ready to help! How can I assist you?",
                        "All systems running smoothly and feeling great! How is your day going?"
                )));

        intents.add(new Intent("help",
                Arrays.asList("help", "options", "what can i ask", "commands", "menu", "assistance"),
                Arrays.asList(
                        "You can ask me about our working hours, live weather, current time/day/year, do math calculations (e.g. 'calc 25 * 4'), who created me, or simply say hello!",
                        "Need assistance? Try asking: 'What are your hours?', 'What is the weather?', 'Calculate 15% of 200', 'What time is it?', or 'Who created you?'"
                )));

        intents.add(new Intent("calculator",
                Arrays.asList("calculator", "math", "how to calculate", "math help", "math commands"),
                Arrays.asList(
                        "I can perform calculations! Try: 'calc 25 * 4', '15% of 200', 'sqrt(144)', '2^8', '(10 + 5) / 3', or simply '45 + 55'.",
                        "Need math assistance? You can enter expressions like '100 / 4', 'calculate 50 * 3', '15 percent of 80', or 'square root of 144'."
                )));

        intents.add(new Intent("time",
                Arrays.asList("time", "what time is it", "current time", "what hour is it", "what is the time", "time now", "tell me the time", "what hour", "current hour", "clock"),
                Collections.singletonList("[DYNAMIC_TIME]")));

        intents.add(new Intent("date",
                Arrays.asList("date", "what date is it", "what is the date", "todays date", "current date", "what is todays date"),
                Collections.singletonList("[DYNAMIC_DATE]")));

        intents.add(new Intent("day",
                Arrays.asList("day", "what day is it", "what day is today", "current day", "which day is today", "what day of week", "what day of the week"),
                Collections.singletonList("[DYNAMIC_DAY]")));

        intents.add(new Intent("year",
                Arrays.asList("year", "what year is it", "current year", "what is the year", "which year", "what year are we in"),
                Collections.singletonList("[DYNAMIC_YEAR]")));

        intents.add(new Intent("weather",
                Arrays.asList("weather", "what is the weather", "how is the weather", "weather today", "current weather", "temperature", "weather forecast", "is it raining", "how hot is it", "how cold is it"),
                Collections.singletonList("[DYNAMIC_WEATHER]")));
    }

    public List<Intent> getIntents() {
        return Collections.unmodifiableList(intents);
    }

    public String getFilePath() {
        return filePath;
    }
}
