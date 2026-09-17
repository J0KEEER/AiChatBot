import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Modern Swing GUI for real-time interaction with the AI Chatbot.
 */
public class ChatWindow extends JFrame {

    private final ChatEngine engine;
    private final JTextPane chatPane;
    private final JTextField inputField;
    private final StringBuilder chatHistoryHtml;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public ChatWindow(ChatEngine engine) {
        this.engine = engine;
        this.chatHistoryHtml = new StringBuilder();
        this.chatPane = new JTextPane();
        this.inputField = new JTextField();

        initLookAndFeel();

        setTitle("AI Chatbot Assistant");
        setSize(520, 680);
        setMinimumSize(new Dimension(420, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Chat Log with ScrollPane
        chatPane.setContentType("text/html");
        chatPane.setEditable(false);
        chatPane.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Panel (Chips + Input bar)
        add(createBottomPanel(), BorderLayout.SOUTH);

        // Initial welcome message
        appendBotMessage("Hello! I am your <b>AI Assistant</b>. How can I help you today?");
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep default Swing look if system look fails
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(28, 41, 61)); // Deep Indigo / Slate
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel titleContainer = new JPanel();
        titleContainer.setOpaque(false);
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("🤖 AI Chatbot Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("● Online | Java NLP Rule-Based Engine");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(74, 222, 128)); // Bright Green

        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(3));
        titleContainer.add(statusLabel);

        // Custom styled Clear Chat button with visible text & borders across all platforms
        StyledButton clearButton = new StyledButton(
                "Clear Chat",
                new Color(51, 65, 85),
                new Color(71, 85, 105),
                new Color(30, 41, 59),
                Color.WHITE,
                new Color(100, 116, 139),
                8
        );
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearButton.setPreferredSize(new Dimension(100, 32));
        clearButton.addActionListener(e -> clearChat());

        header.add(titleContainer, BorderLayout.WEST);
        header.add(clearButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createBottomPanel() {
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(new Color(245, 247, 250));

        // Input and Send row
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setBackground(new Color(245, 247, 250));
        inputRow.setBorder(new EmptyBorder(12, 14, 14, 14));

        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(9, 12, 9, 12)
        ));

        // Custom styled Send button ensuring high-contrast white text on blue background
        StyledButton sendButton = new StyledButton(
                "Send ➤",
                new Color(37, 99, 235),
                new Color(29, 78, 216),
                new Color(30, 64, 175),
                Color.WHITE,
                null,
                8
        );
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendButton.setPreferredSize(new Dimension(95, 40));

        ActionListener sendListener = e -> handleUserInput();
        sendButton.addActionListener(sendListener);
        inputField.addActionListener(sendListener);

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendButton, BorderLayout.EAST);

        bottomContainer.add(inputRow, BorderLayout.CENTER);

        return bottomContainer;
    }

    private void handleUserInput() {
        String rawText = inputField.getText().trim();
        if (rawText.isEmpty()) {
            return;
        }

        // Display user query
        appendUserMessage(escapeHtml(rawText));
        inputField.setText("");

        // Process with ChatEngine
        String botReply = engine.getResponse(rawText);
        appendBotMessage(escapeHtml(botReply));
    }

    private void appendUserMessage(String message) {
        String timestamp = timeFormat.format(new Date());
        String html = "<div style='text-align: right; margin: 6px 0;'>"
                + "<div style='display: inline-block; background-color: #2563eb; color: #ffffff; "
                + "padding: 8px 12px; border-radius: 12px 12px 2px 12px; font-family: sans-serif; font-size: 13px; max-width: 75%; text-align: left;'>"
                + message
                + "</div>"
                + "<div style='font-size: 10px; color: #9ca3af; margin-top: 2px;'>You • " + timestamp + "</div>"
                + "</div>";
        chatHistoryHtml.append(html);
        updateChatView();
    }

    private void appendBotMessage(String message) {
        String timestamp = timeFormat.format(new Date());
        String html = "<div style='text-align: left; margin: 6px 0;'>"
                + "<div style='display: inline-block; background-color: #f1f5f9; color: #1e293b; "
                + "padding: 8px 12px; border-radius: 12px 12px 12px 2px; font-family: sans-serif; font-size: 13px; max-width: 75%; text-align: left; border: 1px solid #e2e8f0;'>"
                + message
                + "</div>"
                + "<div style='font-size: 10px; color: #9ca3af; margin-top: 2px;'>Bot • " + timestamp + "</div>"
                + "</div>";
        chatHistoryHtml.append(html);
        updateChatView();
    }

    private void updateChatView() {
        chatPane.setText("<html><body style='padding: 10px; font-family: sans-serif; background-color: #ffffff;'>"
                + chatHistoryHtml.toString()
                + "</body></html>");

        // Auto-scroll to bottom on EDT
        SwingUtilities.invokeLater(() -> {
            chatPane.setCaretPosition(chatPane.getDocument().getLength());
        });
    }

    private void clearChat() {
        chatHistoryHtml.setLength(0);
        appendBotMessage("Chat history cleared. How can I help you today?");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;")
                   .replace("\n", "<br/>");
    }

    public ChatEngine getEngine() {
        return engine;
    }

    /**
     * Cross-platform custom styled button with explicit rendering to avoid
     * native OS Look and Feel color-washing issues (such as macOS Aqua).
     */
    private static class StyledButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color textColor;
        private final Color borderColor;
        private final int cornerRadius;

        public StyledButton(String text, Color normalBg, Color hoverBg, Color pressedBg,
                            Color textColor, Color borderColor, int cornerRadius) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.textColor = textColor;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(textColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background fill
            if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Border outline
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }

            // Draw text centered
            g2.setFont(getFont());
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }
}
