
// All imports
import java.awt.*;            // For colors fonts graphics etc.
import java.awt.event.*;      // For events control like clicks, button action etc
import java.util.ArrayList;   // Arraylist is dynamic , helps to store message
import java.util.List;
import javax.swing.*;               // Helps with the GUI Components
import javax.swing.border.*;        // Tools for drawing borders around panels
import javax.swing.plaf.basic.*;    // Helps to redesign


public class HakedGui {


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
    // Using it for
    static final Font FONT_NORMAL = new Font("SANS_SERIF", Font.PLAIN, 13); // Reg Text
    static final Font FONT_BOLD = new Font("SANS_SERIF", Font.BOLD, 13);    // Buttons labels, Sender names
    static final Font FONT_SMALL = new Font("SANS_SERIF", Font.PLAIN, 11);  // Small taglines like usages and username, port etc
    static final Font FONT_TITLE = new Font("SANS_SERIF", Font.BOLD, 22);   // For logo likr


    // SECTION 3 - CONSTANTS
    // Bubbles grow to at most 65%
    // like as i fixed it for now 420px so if remove sideber then
    // and  after removng container approx 260 * 65% so helps prevent streaching edge to edge

    static final double BUBBLE_MAX_FRACTION = 0.65;


    //  SECTION 4 - APP STATE
    //  Variables that hold state

    // used to define which mode user choose
    // true = Create Server, false = Join Server
    boolean isHostMode = false;

    // What username did the user type? gets rplcd in screen 2 with username, shown in screen 3)
    String username = "TypedUsername";

    // GUI components we need to reference from multiple methods
    JFrame MainChatWindow;                     // the main chat window (screen 3)
    JPanel bubbleContainer;                    // panel that holds all chat bubbles
    JScrollPane chatScroll;                    // scroll pane around bubbleContainer
    DefaultListModel<String> onlineUsersModel; // For online user list
    JTextField MessageTypingBox;               // the message input box
    JButton sendBtn;                           // the send button in case we enable or disable for different func

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

    // SECTION 6 - THEME INJECTION
    // Our applyTheme() method takes "Themes color" from section 01 and then we
    // Apply it to Swing Default GUI toolkit to get advance theme magagement of Buttons, Label, text field

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

        // Sidebar list (usinf it for Online user list)
        UIManager.put("List.background", BG_SIDEBAR);
        UIManager.put("List.foreground", COL_TEXT);
        UIManager.put("List.font", FONT_NORMAL);
        UIManager.put("List.selectionBackground", COL_ACCENT);
        UIManager.put("List.selectionForeground", Color.WHITE);

        // Scroll panes
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }


    // SECTION 7 - REUSABLE COMPONENTS
    // For Button, Icon, Scrollbar, Card, LabelFiels etc.

    // [ makePillButton() - METHOD {01} FOR BUTTON ]
    // Create rounded button like (Leave or Send)
    // label = text, color = fill, w/h = size in pixels
    static JButton makePillButton(String label, Color color, int w, int h) {

        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Hoovering effect
                // getModel().isRollover() check if mouse is hovering
                // with ternary operator we decide if hover then bright else false
                graphics.setColor(getModel().isRollover() ? color.brighter() : color);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Centred text
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
    // Using it for minimise (-) and exit (x) in the nav bar
    static JButton makeIconButton(String symbol, Color color, ActionListener action) {

        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // a little highlight on hover
                if (getModel().isRollover()) {
                    graphics.setColor(new Color(color.getRed(), color.getGreen(),
                            color.getBlue(), 50));
                    graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                }

                // Draw symbol centred
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


    // [ makeScrollBar() - METHOD {03) TO CREATE THE VERTICAL READING SCROLLBAR ]
    // Swing has a bad scroolbar icon and bar , this method overrides that
    static void makeScrollBar(JScrollPane sp) {

        // We disable the horizontal scrollbar as we dont need ti
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Instead we get a vertical scrollbar here 0 height means java manages height automatically
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(4, 0)); // very thin
        // Replaces tradiotional scrollbar with our custom version
        bar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0x3A4A60);  // draggable thumb
                trackColor = BG_CHAT;                   // track behind thumb
            }

            // Returns an invisible button it replaces the traditional arrows like button
            private JButton noButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return noButton();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return noButton();
            }
        });
    }


    // [ makeLabelledField() - METHOD {04} TO CREATE INPUT LABEL ]
    // This method heps us to create input field used in connect dialog
    // like where we would input username port etc
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
    // check for user click for create or join serverr
    // icon = unicode symbol, title = headings "Create server" std = small description, runableonClick = click listen
    static JPanel makeModeCard(String icon, String title,
                               String smallTextDetails, Runnable onClick) {

        // 3-row grid to place icon,logo / title / description
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 6)) {

            boolean hovered = false;

            {   // instance initializer - when mouse on card we repaint with hoover style
                // Detect hover and click
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onClick.run();
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Background - slightly lighter on hover
                graphics.setColor(hovered ? new Color(0x232D42) : BG_INPUT);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Border - accent on hover, normal otherwise
                graphics.setColor(hovered ? COL_ACCENT : COL_BORDER);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);

                graphics.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Build the three labels
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
    //  The very first window the user sees.
    //  Returns:  0  = "Create Server", 1  = "Join Server", -1  = exit.
    int showLaunchScreen() {

        // choice[0] will be set when the user clicks a card
        // using array cause we can only use final variables from outside to inside a lambda
        // here we dont reassign instead we change the single value inside that array
        int[] choice = {-1};

        // Create a modal dialog
        JDialog dialog = new JDialog((Frame) null, true);
        // setUndecorated removes the os title bar for -/x/<>
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(null); // Open it at the center of screen

        // App logo
        JLabel logo = new JLabel("HAKED", SwingConstants.CENTER);
        logo.setFont(FONT_TITLE);
        logo.setForeground(COL_ACCENT);

        // Tagline 1
        JLabel tagline1 = new JLabel("A Custom Server Client Chat Application",
                SwingConstants.CENTER);
        tagline1.setFont(FONT_SMALL);
        tagline1.setForeground(COL_HINT);

        // Tagline 2
        JLabel tagline2 = new JLabel("Made by Hugo Amin Kipp Emon DT",
                SwingConstants.CENTER);
        tagline2.setFont(FONT_SMALL);
        tagline2.setForeground(COL_HINT);

        // Stack the two taglines vertically together
        JPanel taglines = new JPanel(new GridLayout(2, 1, 0, 3));
        taglines.setOpaque(false);
        taglines.add(tagline1);
        taglines.add(tagline2);

        // Header logo on top, taglines below
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0)); // gap below header
        header.add(logo);
        header.add(taglines);

        // "Create Server" card choice points to 0
        JPanel createCard = makeModeCard(
                "\u2338",          // ⌸ server icon
                "Create Server",
                "Host a new room",
                () -> { choice[0] = 0; dialog.dispose(); }
        );

        // "Join Server" card → choice points to 1
        JPanel joinCard = makeModeCard(
                "\u2192",          // → arrow icon
                "Join Server",
                "Enter a room",
                () -> { choice[0] = 1; dialog.dispose(); }
        );

        // Main C J Mode cards side by side
        JPanel cards = new JPanel(new GridLayout(1, 2, 14, 0));
        cards.setOpaque(false);
        cards.add(createCard);
        cards.add(joinCard);

        // Root panel - here we organize headers, taglines, carcs
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),  // thin outer border
                new EmptyBorder(28, 32, 28, 32)));     // inner padding
        root.add(header, BorderLayout.NORTH);    // Header on north toop
        root.add(cards,  BorderLayout.CENTER);  // cards on center

        // Drag to move as no OS title bar so we handle it manually here
        Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            @Override
            // record exact mouse pressed pos
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // get where the winfow is rn
                Point pos = dialog.getLocation();
                dialog.setLocation(
                        // calc how far the mouse went and move window accordingly
                        pos.x + e.getX() - dragStart[0].x,
                        pos.y + e.getY() - dragStart[0].y);
            }
        });

        // Assemble and show
        dialog.add(root);
        dialog.setVisible(true); // pause until the dialog is closed

        return choice[0]; // 0, 1, or -1

    }
    //  SECTION 9 - SCREEN 2: CONNECT DIALOG
    //  Collects the username (and port, and IP for join mode).
    //  Returns true  = user submitted the form (go to Main chat window)
    //  Return  false = user hit BACK (go back to screen 1)

    boolean showConnectScreen(int mode) {

        boolean isCreating = (mode == 0); // true = Create Server, false = Join
        isHostMode = isCreating;

        // - Dialog setup
        JDialog dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(BG_CHAT);

        // - Input fields
        JTextField usernameField = new JTextField(16);
        JTextField portField     = new JTextField("7000", 16);
        JTextField ipField       = new JTextField("127.0.0.1", 16);

        // - Icon on the second window
        JLabel iconLabel = new JLabel(
                // This ternary operator picks icon for C or J
                isCreating ? "\u2338" : "\u2192",
                SwingConstants.CENTER);
        iconLabel.setFont(new Font("SANS_SERIF", Font.PLAIN, 32));
        iconLabel.setForeground(COL_ACCENT);
        iconLabel.setAlignmentX(0.5f);

        // - Title
        JLabel titleLabel = new JLabel(
                isCreating ? "Create Server" : "Join Server",
                SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COL_TEXT);
        titleLabel.setAlignmentX(0.5f);

        // - Subtitle
        JLabel subtitleLabel = new JLabel(
                isCreating ? "Configure your server" : "Connect to a server",
                SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(COL_HINT);
        subtitleLabel.setAlignmentX(0.5f);

        // - "Hosting on 127.0.0.1" badge for Create mode only
        JLabel greenDot = new JLabel("\u25CF"); // green dot icon
        greenDot.setForeground(COL_ONLINE);
        greenDot.setFont(FONT_SMALL);

        JLabel hostIp = new JLabel("Hosting on  127.0.0.1");
        hostIp.setForeground(COL_ACCENT);
        hostIp.setFont(FONT_SMALL);

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

        // Form input fields
        // Create, 2 rows (username + port)
        // Join, 3 rows (IP + username + port)
        int numRows = isCreating ? 2 : 3;
        JPanel form = new JPanel(new GridLayout(numRows, 1, 0, 14));
        form.setBackground(BG_INPUT);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        if (!isCreating) {
            form.add(makeLabelledField("SERVER IP", ipField));
        }
        form.add(makeLabelledField("USERNAME", usernameField));
        form.add(makeLabelledField("PORT  (7000 to 7010)", portField));
        form.setAlignmentX(0.5f);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                form.getPreferredSize().height));

        // - Buttons
        JButton backBtn = makePillButton("BACK", new Color(0x374151), 100, 38);

        JButton proceedBtn = makePillButton(
                isCreating ? "CREATE  \u25B6" : "JOIN  \u25B6",
                COL_ACCENT, 130, 38);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(backBtn);
        buttonRow.add(proceedBtn);
        buttonRow.setAlignmentX(0.5f);

        // - Assembling everythibg top to bottom
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG_CHAT);
        root.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(28, 32, 24, 32)));
        root.add(iconLabel);
        // An invisible spacing 8 px
        root.add(Box.createVerticalStrut(8));
        root.add(titleLabel);
        root.add(Box.createVerticalStrut(6));
        root.add(subtitleLabel);
        root.add(Box.createVerticalStrut(16));
        // Adds the green badge in create mode
        if (isCreating) {
            root.add(hostBadge);
            root.add(Box.createVerticalStrut(12));
        }
        root.add(form);
        root.add(Box.createVerticalStrut(20));
        root.add(buttonRow);

        // - Tracks what the user chose
        // true when they click create or join, false when they click back
        boolean[] submitted = {false};

        // - Proceed button action
        proceedBtn.addActionListener(e -> {
            String typedUsername = usernameField.getText().trim();
            username = typedUsername;
            submitted[0] = true;
            dialog.dispose();
        });

        // Pressing Enter in any field acts like clicking the button
        ActionListener pressEnter = e -> proceedBtn.doClick();
        usernameField.addActionListener(pressEnter);
        portField.addActionListener(pressEnter);
        ipField.addActionListener(pressEnter);

        // BACK button closes without submitting
        backBtn.addActionListener(e -> dialog.dispose());

        // - Drag to move
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

        // - Show dialog
        dialog.add(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, dialog.getHeight()));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true); // BLOCKS here

        // Return false if BACK was hit, true if form was submitted
        if (!submitted[0]) return false;
        return true;

    }


    //  SECTION 10 - SCREEN 3 MAIN CHAT WINDOW
    void showChatWindow() {

        // SUBSECT {01} - CREATES WINDOW
        // - Window setup
        MainChatWindow = new JFrame();
        MainChatWindow.setUndecorated(true);
        MainChatWindow.setBackground(new Color(0, 0, 0, 0));
        MainChatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Wire the OS close button  to our exitApp() method
        MainChatWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exitApp(); }
        });

        // SUBSECT {02} - FOR NAV BAR
        // - Nav bar
        // Draws a line along its bottom edge to separate it from the chat area
        JLabel appLogo = new JLabel("\u23FF"); //  icon for the application
        appLogo.setFont(FONT_BOLD.deriveFont(13f));
        appLogo.setForeground(COL_ACCENT);

        JLabel appName = new JLabel("HAKED");
        appName.setFont(FONT_BOLD.deriveFont(14f));
        appName.setForeground(COL_ACCENT);

        JLabel userLabel = new JLabel("@" + username);
        userLabel.setFont(FONT_NORMAL);
        userLabel.setForeground(COL_TEXT);

        // - Left side HAKED and @username
        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        navLeft.setOpaque(false);
        navLeft.add(appLogo);
        navLeft.add(appName);
        navLeft.add(userLabel);

        // Using unicode buttons
        JButton minimiseBtn = makeIconButton("\u2212", COL_HINT,
                e -> MainChatWindow.setState(Frame.ICONIFIED));

        JButton closeBtn = makeIconButton("\u2715", COL_EXIT,
                e -> exitApp());

        // Right side: − ✕
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        navRight.setOpaque(false);
        navRight.add(minimiseBtn);
        navRight.add(closeBtn);

        // Wrap in GridBagLayout panels to vertically centre them in the nav bar
        JPanel leftWrapper = new JPanel(new GridBagLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(navLeft);

        JPanel rightWrapper = new JPanel(new GridBagLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(navRight);

        JPanel navBar = new JPanel(new BorderLayout()) {
            @Override
            // We override anf draw a 1px line at the bottom of nav bar
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(COL_BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1); // bottom border line
            }
        };
        navBar.setBackground(BG_NAV);
        navBar.setPreferredSize(new Dimension(0, 46));
        navBar.setBorder(new EmptyBorder(0, 14, 0, 10));
        navBar.add(leftWrapper, BorderLayout.WEST);
        navBar.add(rightWrapper, BorderLayout.EAST);

        // Drag the window by the nav bar
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
        // - Chat bubble area
        // Bubbles are stacked vertically inside this panel
        bubbleContainer = new JPanel();
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.Y_AXIS));
        bubbleContainer.setBackground(BG_CHAT);
        bubbleContainer.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Wrap in a scroll pane
        // IF a message become bigger then chat area, it provides scrolling
        chatScroll = new JScrollPane(bubbleContainer);
        // Colors the scroll pane itself to prevent new white areas
        chatScroll.setBackground(BG_CHAT);
        chatScroll.getViewport().setBackground(BG_CHAT);
        chatScroll.setBorder(new MatteBorder(1, 0, 0, 0, COL_BORDER));
        makeScrollBar(chatScroll);

        // SUBSECT {04} - USERLIST
        // - Online users sidebar
        onlineUsersModel = new DefaultListModel<>();

        JList<String> userList = new JList<>(onlineUsersModel);
        userList.setFixedCellHeight(28);
        userList.setBorder(new EmptyBorder(4, 10, 4, 8));

        // Custom renderer for green dot + username
        userList.setCellRenderer((list, value, index, selected, focused) -> {
            JLabel dot = new JLabel("\u25CF"); // ● green dot icon
            dot.setForeground(COL_ONLINE);
            dot.setFont(FONT_SMALL);

            JLabel name = new JLabel(value.toString());
            name.setFont(FONT_NORMAL);
            name.setForeground(COL_TEXT);

            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            cell.setOpaque(false);
            cell.add(dot);
            cell.add(name);
            return cell;
        });

        // Sidebar header above the list (ONLINE)
        JLabel onlineHeader = new JLabel("ONLINE");
        onlineHeader.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        onlineHeader.setForeground(COL_HINT);
        onlineHeader.setBackground(BG_SIDEBAR);
        onlineHeader.setOpaque(true);
        onlineHeader.setBorder(new EmptyBorder(12, 14, 8, 8));

        // Scrollable
        JScrollPane sideScroll = new JScrollPane(userList);
        sideScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        makeScrollBar(sideScroll);

        // Assembling sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new MatteBorder(0, 1, 0, 0, COL_BORDER));
        sidebar.setPreferredSize(new Dimension(130, 0));
        sidebar.add(onlineHeader, BorderLayout.NORTH);
        sidebar.add(sideScroll,   BorderLayout.CENTER);

        // Chat area + sidebar side by side
        JPanel chatAndSidebar = new JPanel(new BorderLayout());
        chatAndSidebar.setOpaque(false);
        chatAndSidebar.add(chatScroll, BorderLayout.CENTER);
        chatAndSidebar.add(sidebar,    BorderLayout.EAST);


        // SUBSECT {05} - MESSAGE INPUT BAR
        // - Message input bar
        MessageTypingBox = new JTextField();
        MessageTypingBox.setBackground(BG_INPUT);
        MessageTypingBox.setForeground(COL_TEXT);
        // Changes typing cursor
        MessageTypingBox.setCaretColor(COL_ACCENT);
        MessageTypingBox.setFont(FONT_NORMAL);
        MessageTypingBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x3A4A60), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        sendBtn = makePillButton("SEND \u25B6", COL_ACCENT, 100, 38);

        JButton leaveBtn = makePillButton("LEAVE", COL_EXIT, 80, 38);

        // What happens when the user sends a message
        ActionListener sendAction = e -> {
            String text = MessageTypingBox.getText().trim();
            if (!text.isEmpty()) {
                addMessage(text, true, username); // render as outgoing bubble
                MessageTypingBox.setText("");
            }
        };
        sendBtn.addActionListener(sendAction);
        MessageTypingBox.addActionListener(sendAction); // Enter key also sends

        leaveBtn.addActionListener(e -> exitApp());

        // Group buttons together
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(sendBtn);
        btnGroup.add(leaveBtn);

        // Full input bar  text field + buttons
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(BG_INPUT);
        inputBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, COL_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        inputBar.add(MessageTypingBox, BorderLayout.CENTER);
        inputBar.add(btnGroup,         BorderLayout.EAST);

        // Thin outer wrapper for the input bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.add(inputBar, BorderLayout.CENTER);

        // SUBSECT {06} - ROOT ASSEMBLY
        // - Root panel (draws rounded border around everything)
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark rounded background
                graphics.setColor(BG_MAIN);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Accent colour border
                graphics.setColor(COL_ACCENT);
                graphics.setStroke(new BasicStroke(1.5f));
                graphics.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);

                graphics.dispose();
            }
        };
        rootPanel.setOpaque(false);

        // - Assemble the three zones
        rootPanel.add(navBar,          BorderLayout.NORTH);
        rootPanel.add(chatAndSidebar,  BorderLayout.CENTER);
        rootPanel.add(bottomBar,       BorderLayout.SOUTH);

        // - Size and show
        MainChatWindow.setContentPane(rootPanel);
        // Currently chat window is fixed
        // we can maek it dynamic but i think fixed is good
        MainChatWindow.setSize(420, 420);
        MainChatWindow.setResizable(false);
        MainChatWindow.setLocationRelativeTo(null);
        MainChatWindow.setVisible(true);

        // Add the logged-in user to the sidebar
        onlineUsersModel.addElement(username);
        // Broadcast "joined" system message
        addSystemMessage(username + " joined the chat.");
        // Give focus to the input field right away
        MessageTypingBox.requestFocusInWindow();

    }

    // SECTION 11 - CHAT BUBBLE RENDERING
    // getBubbleWidth()
    // METHOD {01} - RETURNS HOW WIDE A BUBBLE SHOULD BE
    int getBubbleWidth() {
        int panelWidth = (bubbleContainer != null) ? bubbleContainer.getWidth() : 260;
        int usable = Math.max(panelWidth - 32, 80); // subtract 16px padding each side
        return Math.min((int)(usable * BUBBLE_MAX_FRACTION), 500);
    }

    // addMessage()
    // METHOD {02} - TO CREATE MESSAGE OBJECT
    // Saves to history and calls drawBubble() to render it.
    void addMessage(String text, boolean outgoing, String sender) {
        // when isSystem = false then ist normal message not system and we save it
        messageHistory.add(new Message(text, outgoing, sender, false));
        drawBubble(text, outgoing, sender);
    }


    //  scrollToBottom()
    //  METHOD {03} - HELPER FOR SCROLLING
    //  Scrolls the chat to show the most recent message.
    void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }


    // drawBubble()
    // METHOD {04} - HANDLES POSITION OB CHAT BUBBLE
    // Draws one chat bubble and appends it to bubbleContainer.
    // Handles both outgoing and incoming message bubble position.
    void drawBubble(String text, boolean outgoing, String sender) {

        int bubblewidth = getBubbleWidth(); // bubble width in pixels

        // Sender name label "@Sender"
        JLabel senderLabel = new JLabel("@" + sender);
        senderLabel.setFont(FONT_BOLD.deriveFont(11f));
        senderLabel.setForeground(COL_ACCENT);
        senderLabel.setBorder(new EmptyBorder(0, 4, 2, 4));

        // Message text wrapping read-only text area
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(FONT_NORMAL);
        textArea.setOpaque(false);
        textArea.setForeground(COL_TEXT);
        textArea.setBorder(new EmptyBorder(9, 12, 9, 12));

        // Measure how tall the text needs to be at this width
        textArea.setSize(bubblewidth - 2, 9999);
        int textHeight = textArea.getPreferredSize().height;

        // Pin the text area to exactly that measured size
        textArea.setPreferredSize(new Dimension(bubblewidth - 2, textHeight));
        textArea.setMaximumSize(new Dimension(bubblewidth - 2, textHeight));
        textArea.setMinimumSize(new Dimension(10,      textHeight));

        // Bubble panel (rounded rectangle background)
        Color bgCol    = outgoing ? BUBBLE_OUT : BUBBLE_IN;
        Color borderCol = outgoing ? COL_ACCENT : COL_BORDER;
        int bh = textHeight + 2; // bubble height = text height + 2px

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded fill
                graphics.setColor(bgCol);
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Rounded border
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

        // Stack: name label above bubble
        // RIGHT-aligned for outgoing (1.0f), LEFT-aligned for incoming (0.0f)
        float align = outgoing ? 1.0f : 0.0f;
        senderLabel.setAlignmentX(align);
        bubble.setAlignmentX(align);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(senderLabel);
        stack.add(bubble);
        stack.setMaximumSize(new Dimension(bubblewidth, Short.MAX_VALUE));

        // Row puts the stack on the correct side
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0)); // vertical gap between bubbles
        row.setAlignmentX(0f);
        row.add(stack, outgoing ? BorderLayout.EAST : BorderLayout.WEST);

        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }


    //  drawSystemRow()
    //  METHOD {05} - SYSTEM BROADCAST MESSAGES
    //  Draws a centred italic yellow system notice.
    //  Broadcast when a user joins or leaves, or for important events.
    void drawSystemRow(String text) {

        JLabel label = new JLabel("\u2022 " + text, SwingConstants.CENTER);
        label.setFont(FONT_SMALL.deriveFont(Font.ITALIC));
        label.setForeground(COL_INFO);
        label.setAlignmentX(0.5f);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.add(label, BorderLayout.CENTER);
        row.setAlignmentX(0f);

        bubbleContainer.add(row);
        bubbleContainer.revalidate();
        scrollToBottom();
    }


    //  addSystemMessage()
    //  METHOD {06} - HELPER BROADCAST
    //  Public method to add a yellow system notice.
    //  Saves to history and calls drawSystemRow() to render it.
    //  here false means outgoing, when null so no username and its system then
    void addSystemMessage(String text) {
        messageHistory.add(new Message(text, false, null, true));
        drawSystemRow(text);
    }



    //  SECTION 12 — EXIT METHOD
    void exitApp() {
        System.exit(0);
    }


    //  SECTION 13 - Main method 
    public static void main(String[] args) {


        // Apply dark theme to all Swing components
        // Switch Swing to use Cross-Platform look and feel to prevent native os Swing GUI style
        // IF fails then we catch the error, ignore it anf continue with native setting anyway
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        applyTheme();

        // Create one instance of the first window
        HakedGui app = new HakedGui();

        // Screen 1 — Launch dialog (Create Server / Join Server)
        int mode = app.showLaunchScreen();
        if (mode == -1) System.exit(0); // user closed the window

        // showConnectScreen() - 2nd window
        boolean proceed = app.showConnectScreen(mode);
        if (!proceed) System.exit(0); // user hit BACK

        // Main chat window, shown on the Swing thread
        SwingUtilities.invokeLater(app::showChatWindow);


//        //  SECTION TEST AND DEBUG [ remove it later if we dont test ]
//        //______________________________________________________________//
//        // makePillButton() — test the pill button if working
//        // Start
//        JFrame testFrame = new JFrame("Button Test");
//        testFrame.setSize(300, 150);
//        testFrame.setLayout(new java.awt.FlowLayout());
//        testFrame.getContentPane().setBackground(new Color(0x161B26));
//        testFrame.add(makePillButton("SEND \u25B6", new Color(0x00BCD4), 100, 38));
//        testFrame.add(makePillButton("LEAVE", new Color(0xEF4444), 80, 38));
//        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        testFrame.setVisible(true);
//        // END

//        // makeIconButton() - test -/x button on nav bar
//        // START
//        testFrame.add(makeIconButton("\u2212", new Color(0x64748B), e -> System.out.println("min")));
//        testFrame.add(makeIconButton("\u2715", new Color(0xEF4444), e -> System.out.println("close")));
//        // END
//
//        // makeLabelledField() - test input field customization
//        // START
//        JTextField tf = new JTextField(16);
//        testFrame.add(makeLabelledField("USERNAME", tf));
//        // END
//
//        // makeModeCard() - test C or J description cards
//        // START
//        testFrame.setLayout(new java.awt.GridLayout(1, 2, 14, 0));
//        testFrame.setSize(400, 200);
//        testFrame.add(makeModeCard("\u2338", "Create Server", "Host a new room", () -> System.out.println("Create clicked!")));
//        testFrame.add(makeModeCard("\u2192", "Join Server",   "Enter a room",    () -> System.out.println("Join clicked!")));
//        //END
    }
}