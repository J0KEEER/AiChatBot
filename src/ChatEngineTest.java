import java.util.List;

/**
 * Automated test suite for NLP TextPreprocessor, KnowledgeBase, and ChatEngine.
 * Can be run in headless/CI environments without requiring a GUI.
 */
public class ChatEngineTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("Running ChatEngine & NLP Unit Tests...\n");

        testTextPreprocessorTokenization();
        testTextPreprocessorStopWords();
        testKnowledgeBaseLoading();
        testChatEngineMatching();
        testMultiPartAndBrokenQueries();
        testChatEngineFallback();
        testChatEngineEmptyInput();

        System.out.println("\n-------------------------------------------------");
        System.out.println("Test Results: " + testsPassed + " passed, " + testsFailed + " failed.");
        System.out.println("-------------------------------------------------");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testTextPreprocessorTokenization() {
        System.out.println("[Test 1] TextPreprocessor Tokenization and Punctuation Stripping");
        String input = "Hello, world! What are your working hours???";
        List<String> tokens = TextPreprocessor.tokenize(input);

        // "are" and "your" are stop words, punctuation is stripped
        assertTrue(tokens.contains("hello"), "Tokens should contain 'hello'");
        assertTrue(tokens.contains("world"), "Tokens should contain 'world'");
        assertTrue(tokens.contains("working"), "Tokens should contain 'working'");
        assertTrue(tokens.contains("hours"), "Tokens should contain 'hours'");
        assertFalse(tokens.contains("are"), "Tokens should filter out stop word 'are'");
    }

    private static void testTextPreprocessorStopWords() {
        System.out.println("[Test 2] TextPreprocessor Stop-word Filtering");
        String input = "can you please do this for me";
        List<String> tokens = TextPreprocessor.tokenize(input);

        // All of these should be filtered out
        assertTrue(tokens.size() <= 1, "Stop words should be heavily filtered");
    }

    private static void testKnowledgeBaseLoading() {
        System.out.println("[Test 3] KnowledgeBase Loading from knowledge_base.txt");
        KnowledgeBase kb = new KnowledgeBase("knowledge_base.txt");
        List<Intent> intents = kb.getIntents();

        assertTrue(!intents.isEmpty(), "Knowledge base should have loaded intents");
        assertTrue(intents.stream().anyMatch(i -> "greeting".equals(i.getTag())), "Should contain 'greeting' intent");
        assertTrue(intents.stream().anyMatch(i -> "hours".equals(i.getTag())), "Should contain 'hours' intent");
        assertTrue(intents.stream().anyMatch(i -> "contact".equals(i.getTag())), "Should contain 'contact' intent");
    }

    private static void testChatEngineMatching() {
        System.out.println("[Test 4] ChatEngine Intent Matching & Responses");
        KnowledgeBase kb = new KnowledgeBase("knowledge_base.txt");
        ChatEngine engine = new ChatEngine(kb);

        // Test greetings
        String greetResp = engine.getResponse("Hey there, good morning!");
        assertTrue(greetResp.contains("help") || greetResp.contains("Hello") || greetResp.contains("Hi") || greetResp.contains("assist"),
                "Greeting response should be friendly and helpful: " + greetResp);

        // Test working hours
        String hoursResp = engine.getResponse("What are your office hours and timing?");
        assertTrue(hoursResp.toLowerCase().contains("9:00 am") || hoursResp.toLowerCase().contains("saturday"),
                "Hours response should state timings: " + hoursResp);

        // Test contact
        String contactResp = engine.getResponse("Can I get your contact email or helpline?");
        assertTrue(contactResp.toLowerCase().contains("support@assistant.ai"),
                "Contact response should contain support email: " + contactResp);

        // Test creator intent
        String creatorResp = engine.getResponse("Who created you?");
        assertTrue(creatorResp.toLowerCase().contains("mridul"),
                "Creator response should mention creator Mridul: " + creatorResp);

        // Test user identity intent
        String identityResp = engine.getResponse("Who am I?");
        assertTrue(identityResp.toLowerCase().contains("user"),
                "Identity response should acknowledge user: " + identityResp);

        // Test 'About' button prompt
        String aboutResp = engine.getResponse("About");
        assertTrue(aboutResp.toLowerCase().contains("ai faq chatbot") || aboutResp.toLowerCase().contains("assistant"),
                "About response should explain chatbot purpose: " + aboutResp);

        // Test 'how are you' intent
        String howAreYouResp = engine.getResponse("how are you");
        assertTrue(howAreYouResp.toLowerCase().contains("doing") || howAreYouResp.toLowerCase().contains("great") || howAreYouResp.toLowerCase().contains("fantastic") || howAreYouResp.toLowerCase().contains("running smoothly"),
                "How are you response should be polite and conversational: " + howAreYouResp);

        // Test dynamic time and hour
        String timeResp = engine.getResponse("What time is it right now?");
        assertTrue(timeResp.toLowerCase().contains("current time is") || timeResp.contains(":"),
                "Time response should return current formatted time: " + timeResp);

        String hourResp = engine.getResponse("What hour is it?");
        assertTrue(hourResp.toLowerCase().contains("hour") || hourResp.contains(":"),
                "Hour response should return current hour: " + hourResp);

        // Test dynamic day
        String dayResp = engine.getResponse("What day is today?");
        assertTrue(dayResp.toLowerCase().contains("today is"),
                "Day response should return current day: " + dayResp);

        // Test dynamic date
        String dateResp = engine.getResponse("What is the date today?");
        assertTrue(dateResp.toLowerCase().contains("today's date is"),
                "Date response should return current date: " + dateResp);

        // Test dynamic year
        String yearResp = engine.getResponse("What year is it?");
        assertTrue(yearResp.contains("202"),
                "Year response should return current year: " + yearResp);

        // Test weather response
        String weatherResp = engine.getResponse("What is the weather today?");
        assertTrue(weatherResp.toLowerCase().contains("weather") || weatherResp.toLowerCase().contains("temperature"),
                "Weather response should return weather status: " + weatherResp);
    }

    private static void testMultiPartAndBrokenQueries() {
        System.out.println("[Test 4.1] Multi-Part, Compound & Broken Language Handling");
        KnowledgeBase kb = new KnowledgeBase("knowledge_base.txt");
        ChatEngine engine = new ChatEngine(kb);

        // 1. Compound Weather, Date, and Time query with specific city
        String compoundResp = engine.getResponse("whats the weather,date and time right now in Greater Noida");
        assertTrue(compoundResp.contains("Weather") && compoundResp.contains("Greater Noida"),
                "Should include Greater Noida weather: " + compoundResp);
        assertTrue(compoundResp.contains("Date:"), "Should include Date in compound reply: " + compoundResp);
        assertTrue(compoundResp.contains("Time:"), "Should include Time in compound reply: " + compoundResp);

        // 2. Broken/Slang weather query with typo
        String slangWeatherResp = engine.getResponse("wat is d weathr in London rn");
        assertTrue(slangWeatherResp.toLowerCase().contains("london"),
                "Should understand slang/broken weather query: " + slangWeatherResp);

        // 3. Slang identity query
        String slangIdentityResp = engine.getResponse("hu r u");
        assertTrue(slangIdentityResp.toLowerCase().contains("faq chatbot") || slangIdentityResp.toLowerCase().contains("assistant"),
                "Should understand 'hu r u' as 'who are you': " + slangIdentityResp);

        // 4. Slang compound time and date
        String slangTimeDate = engine.getResponse("plz tel me tym n date");
        assertTrue(slangTimeDate.contains("Time:") && slangTimeDate.contains("Date:"),
                "Should understand 'plz tel me tym n date' as multi-dynamic query: " + slangTimeDate);

        // 5. Compound FAQ intent query
        String compoundFaq = engine.getResponse("what are your hours and who made you");
        assertTrue(compoundFaq.contains("Hours:") && compoundFaq.contains("Creator:"),
                "Should answer both hours and creator in compound FAQ: " + compoundFaq);
    }

    private static void testChatEngineFallback() {
        System.out.println("[Test 5] ChatEngine Unknown Query Fallback");
        KnowledgeBase kb = new KnowledgeBase("knowledge_base.txt");
        ChatEngine engine = new ChatEngine(kb);

        String unknownResp = engine.getResponse("Quantum entanglement interstellar teleportation hyperdrive");
        assertTrue(unknownResp.toLowerCase().contains("didn't understand") || unknownResp.toLowerCase().contains("rephrase"),
                "Unknown queries should trigger polite fallback: " + unknownResp);
    }

    private static void testChatEngineEmptyInput() {
        System.out.println("[Test 6] ChatEngine Empty and Whitespace Input Handling");
        KnowledgeBase kb = new KnowledgeBase("knowledge_base.txt");
        ChatEngine engine = new ChatEngine(kb);

        String emptyResp = engine.getResponse("   ");
        assertTrue(emptyResp.toLowerCase().contains("please type a message"),
                "Blank input should prompt user to type: " + emptyResp);

        String nullResp = engine.getResponse(null);
        assertTrue(nullResp.toLowerCase().contains("please type a message"),
                "Null input should prompt user to type: " + nullResp);
    }

    private static void assertTrue(boolean condition, String message) {
        if (condition) {
            System.out.println("  PASS: " + message);
            testsPassed++;
        } else {
            System.err.println("  FAIL: " + message);
            testsFailed++;
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
