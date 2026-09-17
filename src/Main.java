import javax.swing.SwingUtilities;
import java.io.File;
import java.util.Scanner;

/**
 * Entry point for the AI Chatbot application.
 * Supports both Swing GUI mode (default) and CLI mode (--cli / -c).
 */
public class Main {

    private static final String DEFAULT_KB_PATH = "knowledge_base.txt";

    public static void main(String[] args) {
        boolean cliMode = false;
        String kbPath = DEFAULT_KB_PATH;

        for (int i = 0; i < args.length; i++) {
            if ("--cli".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i])) {
                cliMode = true;
            } else if ("--kb".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                kbPath = args[++i];
            }
        }

        System.out.println("=================================================");
        System.out.println("             AI Chatbot Assistant");
        System.out.println("=================================================");

        // Verify KB file exists or notify fallback
        File kbFile = new File(kbPath);
        if (kbFile.exists()) {
            System.out.println("[Init] Using knowledge base: " + kbFile.getAbsolutePath());
        } else {
            System.out.println("[Init] '" + kbPath + "' not found. Initializing built-in default knowledge base.");
        }

        KnowledgeBase kb = new KnowledgeBase(kbPath);
        ChatEngine engine = new ChatEngine(kb);

        if (cliMode) {
            runCli(engine);
        } else {
            System.out.println("[Init] Launching Swing GUI interface...");
            SwingUtilities.invokeLater(() -> {
                try {
                    ChatWindow window = new ChatWindow(engine);
                    window.setVisible(true);
                } catch (Exception e) {
                    System.err.println("[Error] Failed to launch GUI: " + e.getMessage());
                    System.out.println("[Fallback] Starting in CLI console mode...");
                    runCli(engine);
                }
            });
        }
    }

    private static void runCli(ChatEngine engine) {
        System.out.println("\n--- Interactive CLI Mode (type 'exit' or 'bye' to quit) ---");
        System.out.println("Bot: Hello! I am your AI Virtual Assistant. Ask me anything.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("You: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                String response = engine.getResponse(input);
                System.out.println("Bot: " + response + "\n");

                if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                    break;
                }
            }
        }
        System.out.println("Session ended. Goodbye!");
    }
}
