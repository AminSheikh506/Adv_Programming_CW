package gui;

import controller.*;

// All imports
import java.awt.*;            // For colors fonts graphics etc.
import java.awt.event.*;      // For events control like clicks, button action etc
import java.util.ArrayList;   // Arraylist is dynamic , helps to store message
import java.util.List;
import javax.swing.*;               // Helps with the GUI Components
import javax.swing.border.*;        // Tools for drawing borders around panels
import javax.swing.plaf.basic.*;    // Helps to redesign

public class Gui {

    //static reference to the GUI object
    private static Gui instance;

    public Gui() {
        instance = this;
    }

    //Returns the current GUI instance, or null if the GUI has not yet been created.  The controller uses this to deliver messages.
    public static Gui getInstance() {
        return instance;
    }

    // SECTION 1 - THEME COLOURS
    // Change colour here to change the whole application Color theme

    // Background colors
    static final Color BG_MAIN = new Color(0x0E1117);        // main window
    static final Color BG_CHAT = new Color(0x161B26);       // chat area
    static final Color BG_INPUT = new Color(0x1C2333);     // input bar
    static final Color BG_SIDEBAR = new Color(0x111827);  // sidebar
    static final Color BG_NAV = new Color(0x0A0D14);     // top nav bar
    // For Borders and accent
    static final Color COL_BORDER = new Color(0x2A3347);     // panel borders
    static final Color COL_ACCENT = new Color(0x00BCD4);    // teal accent colour
    // For Text
    static final Color COL_TEXT = new Color(0xE2E8F0);       // normal text
    static final Color COL_HINT = new Color(0x64748B);      // grey hint / label text
    // For System Notifications
    static final Color COL_ONLINE = new Color(0x22C55E);   // green online dot
    static final Color COL_EXIT = new Color(0xEF4444);    // red (leave / close)
    static final Color COL_INFO = new Color(0xFBBF24);   // yellow system messages
    // For Chat bubbles
    static final Color BUBBLE_IN = new Color(0x1E2D40);   // incoming bubble (left)
    static final Color BUBBLE_OUT = new Color(0x0D3640); // outgoing bubble (right)


    // SECTION 2 - Fixed FONTS
    static final Font FONT_NORMAL = new Font("SANS_SERIF", Font.PLAIN, 13); // Reg Text
    static final Font FONT_BOLD = new Font("SANS_SERIF", Font.BOLD, 13);    // Buttons labels, Sender names
    static final Font FONT_SMALL = new Font("SANS_SERIF", Font.PLAIN, 11);  // Small taglines like usages and username, port etc
    static final Font FONT_TITLE = new Font("SANS_SERIF", Font.BOLD, 22);   // For logo


    // SECTION 3 - CONSTANTS
    // Bubbles grow to at most 65%
    static final double BUBBLE_MAX_FRACTION = 0.65;


    //  SECTION 4 - APP STATE
    //  Variables that hold state

    // used to define which mode user choose
    // true = Create Server, false = Join Server
    boolean isHostMode = false;

    // What username did the user type?
    String username = "TypedUsername";

    //Default server port if a user creates a server without entering a port.
    int serverPort = 7000;

    // GUI components we need to reference from multiple methods
    JFrame MainChatWindow;                     // the main chat window (screen 3)
    JFrame serverWindow;                       // The server GUI window
    JDialog connectDialog;                     // The 'connect to server' window.
    JPanel bubbleContainer;                    // panel that holds all chat bubbles
    JScrollPane chatScroll;                    // scroll pane around bubbleContainer
    DefaultListModel<String> onlineUsersModel; // For online user list
    JTextField MessageTypingBox;               // the message input box
    JButton sendBtn;                           // the send button

    //Used to open each window where the last window was positioned.
    Point lastWindowPosition = null;

    //Used to determine if we're at the main menu.
    boolean goToMainMenuAfterClose = false;

    // === USER INFO FEATURE ===
    // Per-user info lookup map — populated when users join
    java.util.Map<String, UserInfo> userInfoMap = new java.util.HashMap<>();

    // The coordinator's username
    public String coordinatorUsername = "";

    // Port and IP captured from the connect screen
    String sessionPort = "7000";
    String sessionIp   = "127.0.0.1";
    
    // Every message that has been sent, we store it for future use
    final List<Message> messageHistory = new ArrayList<>();


    //  SECTION 5 - MESSAGE CLASS
    //  One Message object created for every chat bubble or system notice.

    static class Message {

        String text;      // what text
        boolean outgoing; // true = outgoing (right side), false = incoming (left side)
        String sender;    // who sent it (username) (null for system messages)
        boolean isSystem; // true = yellow system notice for notification broadcast

        Message(String text, boolean outgoing, String sender, boolean isSystem) {
            this.text = text;
            this.outgoing = outgoing;
            this.sender = sender;
            this.isSystem = isSystem;
        }
    }

    // === USER INFO CLASS ===
    // Holds connection details for each user shown in the sidebar.
    // Populated by the controller when users join/leave.
    public static class UserInfo {
        String username;
        String ip;
        String port;
        boolean isCoordinator;

        public UserInfo(String username, String ip, String port, boolean isCoordinator) {
            this.username      = username;
            this.ip            = ip;
            this.port          = port;
            this.isCoordinator = isCoordinator;
        }
    }

    // SECTION 6 - THEME INJECTION
    // Our applyTheme() method takes "Themes color" from section 01 and then we
    // Apply it to Swing Default GUI toolkit to get advance theme management of Buttons, Label, text field

    static void applyTheme() {

        // Dialog and panel backgrounds (we change from default to dark navy)
        UIManager.put("OptionPane.background", BG_CHAT);
        UIManager.put("Panel.background", BG_CHAT);
        UIManager.put("OptionPane.messageForeground", COL_TEXT);

        // Labels
        UIManager.put("Label.foreground", COL_TEXT);
        UIManager.put("Label.font", FONT_NORMAL);

        // Text fields
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", COL_TEXT);
        UIManager.put("TextField.caretForeground", COL_ACCENT);
        UIManager.put("TextField.font", FONT_NORMAL);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        // Buttons
        UIManager.put("Button.background", BG_INPUT);
        UIManager.put("Button.foreground", COL_TEXT);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Button.border", new LineBorder(COL_BORDER, 1, true));

        // Text areas (used inside chat bubbles)
        UIManager.put("TextArea.background", BG_CHAT);
        UIManager.put("TextArea.foreground", COL_TEXT);
        UIManager.put("TextArea.font", FONT_NORMAL);

        // Sidebar list (using it for Online user list)
        UIManager.put("List.background", BG_SIDEBAR);
        UIManager.put("List.foreground", COL_TEXT);
        UIManager.put("List.font", FONT_NORMAL);
        UIManager.put("List.selectionBackground", COL_ACCENT);
        UIManager.put("List.selectionForeground", Color.WHITE);

        // Scroll panes
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }


    // SECTION 7 - REUSABLE COMPONENTS
    // For Button, Icon, Scrollbar, Card, LabelField etc.

    // [ makePillButton() - METHOD {01} FOR BUTTON ]
    static JButton makePillButton(String label, Color color, int w, int h) {

        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(getModel().isRollover() ? color.brighter() : color);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                graphics.setColor(Color.WHITE);
                graphics.setFont(FONT_BOLD);
                FontMetrics fm = graphics.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(label)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                graphics.drawString(label, textX, textY);
                graphics.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // [ makeIconButton() - METHOD {02} For TopNav Button Customization ]
    static JButton makeIconButton(String symbol, Color color, ActionListener action) {

        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    graphics.setColor(new Color(color.getRed(), color.getGreen(),
                            color.getBlue(), 50));
                    graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                }
                graphics.setColor(color);
                graphics.setFont(FONT_BOLD);
                FontMetrics fm = graphics.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(symbol)) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                graphics.drawString(symbol, textX, textY);
                graphics.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // [ makeScrollBar() - METHOD {03} TO CREATE THE VERTICAL READING SCROLLBAR ]
    static void makeScrollBar(JScrollPane sp) {

        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(4, 0));
        bar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0x3A4A60);
                trackColor = BG_CHAT;
            }

            private JButton noButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createDecreaseButton(int o) { return noButton(); }

            @Override
            protected JButton createIncreaseButton(int o) { return noButton(); }
        });
    }


    // [ makeLabelledField() - METHOD {04} TO CREATE INPUT LABEL ]
    static JPanel makeLabelledField(String caption, JTextField field) {

        JLabel label = new JLabel(caption);
        label.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        label.setForeground(COL_HINT);

        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        row.add(label, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // [ makeModeCard() - METHOD {05} THAT CREATES C OR J SERVER CARD ]
    static JPanel makeModeCard(String icon, String title,
                               String smallTextDetails, Runnable onClick) {

        JPanel card = new JPanel(new GridLayout(3, 1, 0, 6)) {

            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }

                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }

                    @Override
                    public void mouseClicked(MouseEvent e) { onClick.run(); }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(hovered ? new Color(0x232D42) : BG_INPUT);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                graphics.setColor(hovered ? COL_ACCENT : COL_BORDER);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                graphics.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 28));
        iconLabel.setForeground(COL_ACCENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_BOLD);
        titleLabel.setForeground(COL_TEXT);

        JLabel STDOnCard = new JLabel(smallTextDetails, SwingConstants.CENTER);
        STDOnCard.setFont(FONT_SMALL);
        STDOnCard.setForeground(COL_HINT);

        card.add(iconLabel);
        card.add(titleLabel);
        card.add(STDOnCard);
        return card;
    }


    //  SECTION 8 - WINDOW 1 LAUNCH DIALOG
    int showLaunchScreen() {

        int[] choice = {-1};

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);
        dialog.setSize(480, 380);
        if (lastWindowPosition != null) dialog.setLocation(lastWindowPosition);
        else dialog.setLocationRelativeTo(null);

        JLabel logo = new JLabel("JAVACHAT", SwingConstants.CENTER);
        logo.setFont(FONT_TITLE);
        logo.setForeground(COL_ACCENT);

        JLabel tagline1 = new JLabel("A Custom Client-Server Chat Application",
                SwingConstants.CENTER);
        tagline1.setFont(FONT_SMALL);
        tagline1.setForeground(COL_HINT);

        JLabel tagline2 = new JLabel("Made by: Hugo Amin Kipp & Emon",
                SwingConstants.CENTER);
        tagline2.setFont(FONT_SMALL);
        tagline2.setForeground(COL_HINT);

        JPanel taglines = new JPanel(new GridLayout(2, 1, 0, 3));
        taglines.setOpaque(false);
        taglines.add(tagline1);
        taglines.add(tagline2);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(logo);
        header.add(taglines);

        JPanel createCard = makeModeCard(
                "\u2338",
                "Create Server",
                "Host a new room",
                () -> { choice[0] = 0; dialog.dispose(); }
        );

        JPanel joinCard = makeModeCard(
                "\u2192",
                "Join Server",
                "Enter a room",
                () -> { choice[0] = 1; dialog.dispose(); }
        );

        JPanel cards = new JPanel(new GridLayout(1, 2, 14, 0));
        cards.setOpaque(false);
        cards.add(createCard);
        cards.add(joinCard);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(28, 32, 28, 32)));
        root.add(header, BorderLayout.NORTH);
        root.add(cards,  BorderLayout.CENTER);

        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.setVisible(true);
        lastWindowPosition = dialog.getLocation();
        return choice[0];
    }


    //  SECTION 9 - SCREEN 2: CONNECT DIALOG
    boolean showConnectScreen(int mode) {

        boolean isCreating = (mode == 0);
        isHostMode = isCreating;

        connectDialog = new JDialog((Frame) null, true);
        JDialog dialog = connectDialog;
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        JTextField usernameField = new JTextField(16);
        JTextField portField     = new JTextField("7000", 16);
        JTextField ipField       = new JTextField("127.0.0.1", 16);

        JLabel iconLabel = new JLabel(
                isCreating ? "\u2338" : "\u2192",
                SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 32));
        iconLabel.setForeground(COL_ACCENT);
        iconLabel.setAlignmentX(0.5f);

        JLabel titleLabel = new JLabel(
                isCreating ? "Create Server" : "Join Server",
                SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COL_TEXT);
        titleLabel.setAlignmentX(0.5f);

        JLabel subtitleLabel = new JLabel(
                isCreating ? "Configure your server" : "Connect to a server",
                SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(COL_HINT);
        subtitleLabel.setAlignmentX(0.5f);

        JLabel greenDot = new JLabel("\u25CF");
        greenDot.setForeground(COL_ONLINE);
        greenDot.setFont(FONT_SMALL);

        JLabel hostIp = new JLabel("Hosting on: " + getLocalIPv4());
        hostIp.setForeground(COL_ACCENT);
        hostIp.setFont(FONT_BOLD);

        JPanel hostBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        hostBadge.setBackground(new Color(0x0D2A2E));
        hostBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x1A4A50), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        hostBadge.add(greenDot);
        hostBadge.add(hostIp);
        hostBadge.setAlignmentX(0.5f);
        hostBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                hostBadge.getPreferredSize().height));

        int numRows = isCreating ? 1 : 3;
        JPanel form = new JPanel(new GridLayout(numRows, 1, 0, 14));
        form.setBackground(BG_INPUT);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        if (!isCreating) {
            form.add(makeLabelledField("SERVER IP", ipField));
            form.add(makeLabelledField("USERNAME", usernameField));
        }
        form.add(makeLabelledField("PORT  (1 to 65535)", portField));
        form.setAlignmentX(0.5f);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                form.getPreferredSize().height));

        JButton backBtn = makePillButton("BACK", new Color(0x374151), 100, 38);

        JButton proceedBtn = makePillButton(
                isCreating ? "CREATE  \u25B6" : "JOIN  \u25B6",
                COL_ACCENT, 130, 38);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(backBtn);
        buttonRow.add(proceedBtn);
        buttonRow.setAlignmentX(0.5f);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(iconLabel);
        root.add(Box.createVerticalStrut(8));
        root.add(titleLabel);
        root.add(Box.createVerticalStrut(6));
        root.add(subtitleLabel);
        root.add(Box.createVerticalStrut(16));
        if (isCreating) {
            root.add(hostBadge);
            root.add(Box.createVerticalStrut(12));
        }
        root.add(form);
        root.add(Box.createVerticalStrut(20));
        root.add(buttonRow);

        boolean[] submitted = {false};
        boolean[] wentBack  = {false};

        proceedBtn.addActionListener(e -> {
            String typedIP = ipField.getText().trim();
            String typedUsername = isCreating ? "Host" : usernameField.getText().trim();
            String typedPort = portField.getText().trim();

            // Validate username
            if (!isCreating && typedUsername.isEmpty()) {
                showErrorPopup("Please enter a username.");
                return;
            }

            // Validate port is a number
            int intPort;
            try {
                intPort = Integer.parseInt(typedPort);
            } catch (NumberFormatException exc) {
                showErrorPopup("Port must be a number.");
                return;
            }

            // Validate port range
            if (intPort < 1 || intPort > 65535) {
                showErrorPopup("Port must be between 1 and 65535.");
                return;
            }

            // All valid — proceed
            username    = typedUsername;
            sessionPort = typedPort;
            sessionIp   = isCreating ? getLocalIPv4() : typedIP;
            serverPort  = intPort;
            submitted[0] = true;
            dialog.dispose();

            if (isCreating) {
                controller.Main_controller.createServerButtonPressed(intPort);
            } else {
                controller.Main_controller.joinServerButtonPressed(typedIP, intPort, typedUsername);
            }
        });

        ActionListener pressEnter = e -> proceedBtn.doClick();
        usernameField.addActionListener(pressEnter);
        portField.addActionListener(pressEnter);
        ipField.addActionListener(pressEnter);

        backBtn.addActionListener(e -> {
            wentBack[0] = true;
            dialog.dispose();
        });

        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, dialog.getHeight()));
        if (lastWindowPosition != null) dialog.setLocation(lastWindowPosition);
        else dialog.setLocationRelativeTo(null);

        dialog.setVisible(true); //Blocks here until dialog is closed

        if (wentBack[0] || goToMainMenuAfterClose) {
            goToMainMenuAfterClose = false;
            int newMode = showLaunchScreen();
            if (newMode == -1) { exitApp(); return false; }
            boolean proceed = showConnectScreen(newMode);
            if (proceed) {
                if (isHostMode) showServerWindow();
                else showChatWindow();
            }
            return false;
        }

        if (!submitted[0]) return false;
        return true;
    }


    //  SECTION 10A - SCREEN 3 MAIN CHAT WINDOW
    void showChatWindow() {

        // SUBSECT {01} - CREATES WINDOW
        MainChatWindow = new JFrame();
        MainChatWindow.setUndecorated(true);
        MainChatWindow.setBackground(new Color(0, 0, 0, 0));
        MainChatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        MainChatWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                lastWindowPosition = MainChatWindow.getLocation();
                exitApp();
            }
        });

        // SUBSECT {02} - FOR NAV BAR
        JLabel appLogo = new JLabel("\u23FF");
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("JAVACHAT");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel userLabel = new JLabel("@" + username);
        userLabel.setFont(FONT_NORMAL);
        userLabel.setForeground(COL_TEXT);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(userLabel);

        JButton minimiseBtn = makeIconButton("\u2212", COL_HINT,
                e -> MainChatWindow.setState(Frame.ICONIFIED));

        JButton closeBtn = makeIconButton("\u2715", COL_EXIT,
                e -> exitApp());

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minimiseBtn);
        navRight.add(closeBtn);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper, BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        Point[] dragStart = {null};
        navBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        navBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pos = MainChatWindow.getLocation();
                MainChatWindow.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });


        // SUBSECT {03} - CREATES CHAT BUBBLE AREA
        bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.Y_AXIS));
        bubbleContainer.setBackground(BG_CHAT);
        bubbleContainer.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel bubbleWrapper = new JPanel(new BorderLayout());
        bubbleWrapper.setBackground(BG_CHAT);
        bubbleWrapper.add(bubbleContainer, BorderLayout.NORTH);

        chatScroll = new JScrollPane(bubbleWrapper);
        chatScroll.setBackground(BG_CHAT);
        chatScroll.getViewport().setBackground(BG_CHAT);
        chatScroll.setBorder(new MatteBorder(1, 0, 0, 0, COL_BORDER));
        makeScrollBar(chatScroll);

        // SUBSECT {04} - USERLIST
        onlineUsersModel = new DefaultListModel<>();

        JList<String> userList = new JList<>(onlineUsersModel);
        userList.setFixedCellHeight(28);
        userList.setBorder(new EmptyBorder(4, 10, 4, 8));

        // === USER INFO FEATURE: hover highlight tracking ===
        final int[] hoverIndex = {-1};
        userList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = userList.locationToIndex(e.getPoint());
                Rectangle cellBounds = (idx >= 0) ? userList.getCellBounds(idx, idx) : null;
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) {
                    idx = -1;
                }
                if (idx != hoverIndex[0]) {
                    hoverIndex[0] = idx;
                    userList.repaint();
                }
            }
        });
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverIndex[0] = -1;
                userList.repaint();
            }
        });

        // Custom renderer for green dot + username + hover border
        userList.setCellRenderer((list, value, index, selected, focused) -> {
            JLabel dot = new JLabel("\u25CF");
            dot.setForeground(COL_ONLINE);
            dot.setFont(FONT_SMALL);

            JLabel name = new JLabel(value.toString());
            name.setFont(FONT_NORMAL);
            name.setForeground(COL_TEXT);

            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            cell.setOpaque(false);

            // === USER INFO FEATURE: teal border highlight on hover ===
            if (index == hoverIndex[0]) {
                cell.setBorder(new LineBorder(COL_ACCENT, 1, true));
            }
            cell.add(dot);
            cell.add(name);
            return cell;
        });

        // === USER INFO FEATURE: single-click opens user info popup ===
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String clicked = onlineUsersModel.getElementAt(index);
                    showUserInfoPopup(clicked);
                }
            }
        });

        // Sidebar header
        JLabel onlineHeader = new JLabel("ONLINE");
        onlineHeader.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        onlineHeader.setForeground(COL_HINT);
        onlineHeader.setBackground(BG_SIDEBAR);
        onlineHeader.setOpaque(true);
        onlineHeader.setBorder(new EmptyBorder(12, 14, 8, 8));

        JScrollPane sideScroll = new JScrollPane(userList);
        sideScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        makeScrollBar(sideScroll);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 1, 0, 0, COL_BORDER));
        sidebar.setPreferredSize(new Dimension(130, 0));
        sidebar.add(onlineHeader, BorderLayout.NORTH);
        sidebar.add(sideScroll,   BorderLayout.CENTER);

        JPanel chatAndSidebar = new JPanel(new BorderLayout());
        chatAndSidebar.setOpaque(false);
        chatAndSidebar.add(chatScroll, BorderLayout.CENTER);
        chatAndSidebar.add(sidebar,    BorderLayout.EAST);


        // SUBSECT {05} - MESSAGE INPUT BAR
        MessageTypingBox = new JTextField();
        MessageTypingBox.setBackground(BG_INPUT);
        MessageTypingBox.setForeground(COL_TEXT);
        MessageTypingBox.setCaretColor(COL_ACCENT);
        MessageTypingBox.setFont(FONT_NORMAL);
        MessageTypingBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x3A4A60), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        sendBtn = makePillButton("SEND \u25B6", COL_ACCENT, 100, 38);

        JButton leaveBtn = makePillButton("LEAVE", COL_EXIT, 80, 38);

        ActionListener sendAction = e -> {
            String text = MessageTypingBox.getText().trim();
            if (!text.isEmpty()) {
                addMessage(text, true, username);
                controller.Main_controller.message_sent_from_user(text);
                MessageTypingBox.setText("");
            }
        };
        sendBtn.addActionListener(sendAction);
        MessageTypingBox.addActionListener(sendAction);

        leaveBtn.addActionListener(e -> exitApp());

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(sendBtn);
        btnGroup.add(leaveBtn);

        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(BG_INPUT);
        inputBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        inputBar.add(MessageTypingBox, BorderLayout.CENTER);
        inputBar.add(btnGroup,         BorderLayout.EAST);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(inputBar, BorderLayout.CENTER);

        // SUBSECT {06} - ROOT ASSEMBLY
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(BG_MAIN);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                graphics.setColor(COL_ACCENT);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                graphics.dispose();
            }
        };
        rootPanel.setOpaque(false);
        rootPanel.add(navBar,          BorderLayout.NORTH);
        rootPanel.add(chatAndSidebar,  BorderLayout.CENTER);
        rootPanel.add(bottomBar,       BorderLayout.SOUTH);

        MainChatWindow.setContentPane(rootPanel);
        MainChatWindow.setSize(420, 420);
        MainChatWindow.setResizable(false);
        if (lastWindowPosition != null) MainChatWindow.setLocation(lastWindowPosition);
        else MainChatWindow.setLocationRelativeTo(null);
        MainChatWindow.setVisible(true);
        lastWindowPosition = MainChatWindow.getLocation();

        // Add the logged-in user to the sidebar
        onlineUsersModel.addElement(username);

        // Adds the users name to a map
        userInfoMap.put(username, new UserInfo(username, sessionIp, sessionPort, isHostMode));


        // === USER INFO FEATURE: seed own UserInfo so popup works immediately ===
        coordinatorUsername = isHostMode ? username : "";
        userInfoMap.put(username, new UserInfo(
                username,
                sessionIp,
                sessionPort,
                isHostMode
        ));

        // Give focus to the input field right away
        MessageTypingBox.requestFocusInWindow();
    }


    //  SECTION 10B - SERVER MONITOR WINDOW
    void showServerWindow() {

        serverWindow = new JFrame();
        serverWindow.setUndecorated(true);
        serverWindow.setBackground(new Color(0, 0, 0, 0));
        serverWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        serverWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitApp(); }
        });

        // --- NAV BAR ---
        JLabel appLogo = new JLabel("\u23FF");
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("JAVACHAT");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel modeLabel = new JLabel("SERVER MODE");
        modeLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        modeLabel.setForeground(COL_HINT);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(modeLabel);

        JButton minimiseBtn = makeIconButton("\u2212", COL_HINT,
                e -> serverWindow.setState(Frame.ICONIFIED));
        JButton closeBtn    = makeIconButton("\u2715", COL_EXIT,
                e -> exitApp());

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minimiseBtn);
        navRight.add(closeBtn);

        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper,  BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        Point[] dragStart = {null};
        navBar.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        navBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point pos = serverWindow.getLocation();
                serverWindow.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        // --- STATUS BADGE (IP + PORT) ---
        JLabel dotLabel = new JLabel("\u25CF");
        dotLabel.setForeground(COL_ONLINE);
        dotLabel.setFont(FONT_SMALL);

        JLabel ipPortLabel = new JLabel("IP: " + getLocalIPv4() + "                PORT: " + serverPort);
        ipPortLabel.setForeground(COL_ACCENT);
        ipPortLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        badge.setBackground(new Color(0x0D2A2E));
        badge.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(6, 14, 6, 14)));
        badge.add(dotLabel);
        badge.add(ipPortLabel);

        // --- LOG AREA ---
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(BG_CHAT);
        logArea.setForeground(new Color(0x9DFFB0));
        logArea.setCaretColor(COL_ACCENT);
        logArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBackground(BG_CHAT);
        logScroll.getViewport().setBackground(BG_CHAT);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        makeScrollBar(logScroll);

        java.io.OutputStream logStream = new java.io.OutputStream() {
            @Override
            public void write(int b) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append(String.valueOf((char) b));
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
            @Override
            public void write(byte[] buf, int off, int len) {
                String text = new String(buf, off, len);
                SwingUtilities.invokeLater(() -> {
                    logArea.append(text);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
        };
        System.setOut(new java.io.PrintStream(logStream, true));

        JLabel logHeader = new JLabel("  CONSOLE OUTPUT");
        logHeader.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        logHeader.setForeground(COL_HINT);
        logHeader.setBackground(BG_SIDEBAR);
        logHeader.setOpaque(true);
        logHeader.setBorder(new EmptyBorder(8, 14, 8, 8));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setOpaque(false);
        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        // --- ROOT ASSEMBLY ---
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(BG_MAIN);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                graphics.setColor(COL_ACCENT);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                graphics.dispose();
            }
        };
        rootPanel.setOpaque(false);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(navBar, BorderLayout.NORTH);
        topSection.add(badge,  BorderLayout.CENTER);

        rootPanel.add(topSection, BorderLayout.NORTH);
        rootPanel.add(logPanel,   BorderLayout.CENTER);

        serverWindow.setContentPane(rootPanel);
        serverWindow.setSize(420, 420);
        serverWindow.setResizable(false);
        if (lastWindowPosition != null) serverWindow.setLocation(lastWindowPosition);
        else serverWindow.setLocationRelativeTo(null);
        serverWindow.setVisible(true);
        lastWindowPosition = serverWindow.getLocation();
    }


    // SECTION 11 - CHAT BUBBLE RENDERING

    int getBubbleWidth() {
        int panelWidth = (bubbleContainer != null) ? bubbleContainer.getWidth() : 260;
        int usable = Math.max(panelWidth - 52, 80);
        return Math.min((int)(usable * BUBBLE_MAX_FRACTION), 500);
    }

    void addMessage(String text, boolean outgoing, String sender) {
        messageHistory.add(new Message(text, outgoing, sender, false));
        drawBubble(text, outgoing, sender);
    }

    void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = chatScroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        });
    }

    void drawBubble(String text, boolean outgoing, String sender) {

        int bubblewidth = getBubbleWidth();

        JLabel senderLabel = new JLabel("@" + sender);
        senderLabel.setFont(FONT_BOLD.deriveFont(11f));
        senderLabel.setForeground(COL_ACCENT);
        senderLabel.setBorder(new EmptyBorder(0, 4, 2, 4));

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(FONT_NORMAL);
        textArea.setOpaque(false);
        textArea.setForeground(COL_TEXT);
        textArea.setBorder(new EmptyBorder(9, 12, 9, 12));

        textArea.setSize(bubblewidth - 2, 9999);
        int textHeight = textArea.getPreferredSize().height;

        textArea.setPreferredSize(new Dimension(bubblewidth - 2, textHeight));
        textArea.setMaximumSize(new Dimension(bubblewidth - 2, textHeight));
        textArea.setMinimumSize(new Dimension(10,      textHeight));

        Color bgCol    = outgoing ? BUBBLE_OUT : BUBBLE_IN;
        Color borderCol = outgoing ? COL_ACCENT : COL_BORDER;
        int bh = textHeight + 2;

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(bgCol);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                graphics.setColor(borderCol);
                graphics.setStroke(new BasicStroke(1f));
                graphics.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                graphics.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.add(textArea, BorderLayout.CENTER);
        bubble.setPreferredSize(new Dimension(bubblewidth, bh));
        bubble.setMinimumSize(new Dimension(bubblewidth, bh));
        bubble.setMaximumSize(new Dimension(bubblewidth, bh));

        float align = outgoing ? 1.0f : 0.0f;
        senderLabel.setAlignmentX(align);
        bubble.setAlignmentX(align);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(senderLabel);
        stack.add(bubble);
        stack.setMaximumSize(new Dimension(bubblewidth, Short.MAX_VALUE));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(0f);
        row.add(stack, outgoing ? BorderLayout.EAST : BorderLayout.WEST);


        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }

    void drawSystemRow(String text) {

        if (bubbleContainer == null) {
            System.out.println("[SYSTEM]  " + text);
            return;
        }

        JLabel label = new JLabel("\u2022 " + text, SwingConstants.CENTER);
        label.setFont(FONT_SMALL.deriveFont(Font.ITALIC));
        label.setForeground(COL_INFO);
        label.setAlignmentX(0.5f);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(1, 0, 1, 0));
        row.add(label, BorderLayout.CENTER);
        row.setAlignmentX(0f);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }

    public void addSystemMessage(String text) {
        messageHistory.add(new Message(text, false, null, true));
        drawSystemRow(text);
    }

    public void updateOnlineUsers(java.util.List<UserInfo> users) {
        SwingUtilities.invokeLater(() -> {
            if (onlineUsersModel == null) return;
            onlineUsersModel.clear();
            userInfoMap.clear();
            for (UserInfo u : users) {
                onlineUsersModel.addElement(u.username);
                userInfoMap.put(u.username, u);
            }
        });
    }


    // SECTION 12 - USER INFO POPUP
    // Shown when the user clicks a name in the Online sidebar.
    // Displays the clicked user's IP, port, and whether they are the coordinator.

    void showUserInfoPopup(String clickedUsername) {

        // Look up info; fall back to "Unknown" if not yet populated by the controller
        UserInfo info = userInfoMap.get(clickedUsername);
        if (info == null) {
            info = new UserInfo(clickedUsername, "Unknown", "Unknown", false);
        }

        boolean isCoord = info.isCoordinator;

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        // Role colours — teal for coordinator, grey for member
        Color roleColor  = isCoord ? COL_ACCENT : COL_HINT;
        Color roleBg     = isCoord ? new Color(0x0D2A2E) : new Color(0x1C2333);
        Color roleBorder = isCoord ? new Color(0x1A4A50) : COL_BORDER;
        String roleText  = isCoord ? "COORDINATOR" : "MEMBER";

        JLabel roleBadgeLabel = new JLabel(roleText, SwingConstants.CENTER);
        roleBadgeLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        roleBadgeLabel.setForeground(roleColor);

        JPanel roleBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        roleBadge.setBackground(roleBg);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(roleBorder, 1, true),
                new EmptyBorder(4, 14, 4, 14)));
        roleBadge.add(roleBadgeLabel);
        roleBadge.setAlignmentX(0.5f);
        roleBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                roleBadge.getPreferredSize().height));

        JLabel atLabel = new JLabel("@" + info.username, SwingConstants.CENTER);
        atLabel.setFont(FONT_TITLE);
        atLabel.setForeground(COL_TEXT);
        atLabel.setAlignmentX(0.5f);

        // Info card showing IP and Port
        JPanel infoCard = new JPanel(new GridLayout(2, 1, 0, 10));
        infoCard.setBackground(BG_INPUT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        infoCard.setAlignmentX(0.5f);
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                infoCard.getPreferredSize().height + 60));
        infoCard.add(makeInfoRow("\uD83C\uDF10  IP ADDRESS", info.ip));
        infoCard.add(makeInfoRow("\u26A1  PORT",             info.port));

        JButton closePopupBtn = makePillButton("CLOSE  \u2715", new Color(0x374151), 120, 38);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(closePopupBtn);
        btnRow.setAlignmentX(0.5f);
        closePopupBtn.addActionListener(e -> dialog.dispose());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(roleColor, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(roleBadge);
        root.add(Box.createVerticalStrut(12));
        root.add(atLabel);
        root.add(Box.createVerticalStrut(18));
        root.add(infoCard);
        root.add(Box.createVerticalStrut(20));
        root.add(btnRow);

        // Drag to move
        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(320, dialog.getHeight()));
        dialog.setLocationRelativeTo(MainChatWindow);
        dialog.setVisible(true);
    }

    // Helper — two-line label row (caption above, value below) used inside the user info card
    private JPanel makeInfoRow(String caption, String value) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        captionLabel.setForeground(COL_HINT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_BOLD);
        valueLabel.setForeground(COL_TEXT);

        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);
        row.add(captionLabel, BorderLayout.NORTH);
        row.add(valueLabel,   BorderLayout.CENTER);
        return row;
    }

     //SECTION 13 - Obtain the users IPv4 address so it is displayed when creating a server.
    static String getLocalIPv4() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                // Skip loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) continue;
                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    // IPv4 only, skip loopback 127.x.x.x
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not determine local IP: " + e);
        }
        return "127.0.0.1"; // fallback if nothing found
    }

    //SECTION 14 - Show error pop-up message
    public void showErrorPopup(String errorMessage) {

        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        JLabel iconLabel = new JLabel("⚠", SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 32));
        iconLabel.setForeground(COL_EXIT);
        iconLabel.setAlignmentX(0.5f);

        JLabel messageLabel = new JLabel("<html><div style='text-align:center'>" + errorMessage + "</div></html>",
                SwingConstants.CENTER);
        messageLabel.setFont(FONT_NORMAL);
        messageLabel.setForeground(COL_TEXT);
        messageLabel.setAlignmentX(0.5f);

        JButton closeBtn = makePillButton("OK", COL_EXIT, 80, 38);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(closeBtn);
        btnRow.setAlignmentX(0.5f);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_EXIT, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(iconLabel);
        root.add(Box.createVerticalStrut(12));
        root.add(messageLabel);
        root.add(Box.createVerticalStrut(20));
        root.add(btnRow);

        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point pos = dialog.getLocation();
                dialog.setLocation(pos.x + e.getX() - dragStart[0].x,
                                pos.y + e.getY() - dragStart[0].y);
            }
        });

        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(300, dialog.getHeight()));
        dialog.setLocationRelativeTo(MainChatWindow);
        dialog.setVisible(true);
    }

    //  SECTION 15 — EXIT METHOD
    void exitApp() {
        System.exit(0);
    }

    // SECTION 16 - RETURNS TO CHOSEN GUI WINDOW.
    public void returnToJoinScreen() {
        SwingUtilities.invokeLater(() -> {
            // Dispose chat window if it somehow opened
            if (MainChatWindow != null) {
                MainChatWindow.dispose();
                MainChatWindow = null;
            }
            // Clear state
            onlineUsersModel = null;
            bubbleContainer  = null;
            chatScroll       = null;
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";

            // Go straight back to the join screen (mode 1 = join)
            boolean proceed = showConnectScreen(1);
            if (!proceed) exitApp();
        });
    }

    public void returnToCreateScreen() {
        SwingUtilities.invokeLater(() -> {
            if (serverWindow != null) {
                serverWindow.dispose();
                serverWindow = null;
            }
            if (MainChatWindow != null) {
                MainChatWindow.dispose();
                MainChatWindow = null;
            }
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";

            // Go straight back to the create screen (mode 0 = create)
            boolean proceed = showConnectScreen(0);
            if (!proceed) exitApp();
            else if (isHostMode) showServerWindow();
            else showChatWindow();
        });
    }

    public void returnToMainMenu() {
        SwingUtilities.invokeLater(() -> {
            //If the connect screen is currently open, set the flag and let showConnectScreen handle the navigation when it unblocks
            if (connectDialog != null && connectDialog.isShowing()) {
                goToMainMenuAfterClose = true;
                connectDialog.dispose();
                connectDialog = null;
                return;
            }

            // Otherwise handle navigation directly (e.g. called from chat window)
            if (serverWindow != null) { serverWindow.dispose(); serverWindow = null; }
            if (MainChatWindow != null) { MainChatWindow.dispose(); MainChatWindow = null; }

            onlineUsersModel = null;
            bubbleContainer = null;
            chatScroll = null;
            userInfoMap.clear();
            messageHistory.clear();
            coordinatorUsername = "";
            username = "TypedUsername";
            isHostMode = false;
            sessionPort = "7000";
            sessionIp = "127.0.0.1";

            int mode = showLaunchScreen();
            if (mode == -1) { exitApp(); return; }
            boolean proceed = showConnectScreen(mode);
            if (proceed) {
                if (isHostMode) showServerWindow();
                else showChatWindow();
            }
        });
    }

    public void displayRecievedMessage(String recievedMessage, String senderName) {
        // Displays received messages from the controller into the users GUI.
        addMessage(recievedMessage, false, senderName);
    }

    //  SECTION 16 - Main method
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        applyTheme();

        Gui app = new Gui();

        int mode = app.showLaunchScreen();
        if (mode == -1) System.exit(0);

        boolean proceed = app.showConnectScreen(mode);
        //If !proceed, showConnectScreen() already handled gui navigation internally
        if (proceed) {
            if (app.isHostMode) SwingUtilities.invokeLater(app::showServerWindow);
            else SwingUtilities.invokeLater(app::showChatWindow);
        }
    }
}
