
import java.awt.*;
import javax.swing.*;

public class ChatGui {
    private static boolean isHosting = false;
    public static void main (String [] args){

        // Localhost define
        String Localhost = "127.0.0.1";
        // First initial window message and input
        String [] options = {"Create Server","Join server"};
        // lets  use 7000-7010 to avoid conflicts with other programs for now
        // can change later with dynamic porting
        int minPort = 7000;
        int maxPort = 7010;

        // the initial Dialogue window setup
        int choice = JOptionPane.showOptionDialog(
                null,                   //show in center of screen
                "Welcome! Please Select an option:",   // The message to display
                "Chat Launcher Window",                // Title of the window
                JOptionPane.YES_NO_OPTION,      // Type of dialog (Yes/No style)
                JOptionPane.QUESTION_MESSAGE,   // Icon to show (question mark)
                null,                           // No custom icon
                options,                        // The button labels
                options[0]                      // Default selection (Create Server) gives the blue pointer nice
                                                // Do 1 to select server side
        );

        // close program if user quit
        if (choice == -1) System.exit(0);

        // Data input validation
        // Variables to store the user's input
        String theUser = "", thePort = "", theIp = "";

        // Infinite loop - we'll break out when we have valid data
        // this to prevent wrong info , future use as well
        while (true) {

            // User typing box
            JTextField userBox = new JTextField(10);             // For typing username
            JTextField portBox = new JTextField(10);             // For typing port number
            JTextField ipBox   = new JTextField(Localhost, 10);  // Pre-filled with 127.0.0.1 for now

            // Panel organizer
            JPanel inputPanel = new JPanel(new GridLayout(0, 1, 5, 5));


            // input validation for second  Dialogue window
            // If user chose "Join Server" (choice == 1), they need to enter an IP
            if (choice == 1) {
                inputPanel.add(new JLabel("Input Server IP:"));  // Label
                inputPanel.add(ipBox);                                // Input field for IP
            } else {
                // If hosting, show them the IP they'll be using
                inputPanel.add(new JLabel("Hosting on Private IP: " + Localhost));
                isHosting = true;  // Mark that this client is the host
            }
            // Add username and port fields for both types user
            inputPanel.add(new JLabel("Please provide your Username:"));
            inputPanel.add(userBox);
            inputPanel.add(new JLabel("Select a Port No. (" + minPort + "-" + maxPort + "):")); // can make dynamic later
            inputPanel.add(portBox);

            // Added  OK and Cancel buttons in the Dialogue box
            int result = JOptionPane.showConfirmDialog(
                    null,            // No parent window
                    inputPanel,                     // The panel we created above
                    options[choice],                // Title (either "Create Server" or "Join Server")
                    JOptionPane.OK_CANCEL_OPTION    // Show OK and Cancel buttons
            );

            // If user clicks Cancel or closes the window, exit the program
            if (result != JOptionPane.OK_OPTION) System.exit(0);

            // if okay then Get the text from each field and remove extra spaces
            theUser = userBox.getText().trim();  // .trim() removes spaces at start/end
            thePort = portBox.getText().trim();
            theIp = ipBox.getText().trim();

            // Check if username is provided
            if (!theUser.isEmpty()) break;  // Valid! Exit the loop // for advance version use

            // If username is empty loop will continue
            JOptionPane.showMessageDialog(null, "Username is required!");

        }


        // Make veriabvle final for future use
        final String userValue = theUser;  // The username
        final String portValue = thePort;  // The port number
        final String ipValue   = theIp;    // The server IP

        // Lets create the main window  -> Chat window
        JFrame frame = new JFrame("Chat Window");

        // JTextArea to get a box area
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        // input fields
        JTextField inputField = new JTextField();              // get user typed messages
        JButton sendBtn = new JButton("Send");           // Button to send message
        JButton leaveBtn = new JButton("Leave Room");    // Button to exit chat

        // Settings for the window
        // JList displays the items from the model
        // DefaultListModel is the data structure that holds the list items
        // This is for the advance chat box. i have a hash map where im tracking
        // name and ip details for tap on name and kick and show details
        // advance one is lot complex as coordinator has different functionallity than other
        // working on it and it will be finish soon
        DefaultListModel<String> listModel = new DefaultListModel<>(); //  for the list section
        JList<String> userList = new JList<>(listModel);
        // Create a panel for the sidebar
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(new JLabel(" Online Users (Tap for details):"), BorderLayout.NORTH);
        sidePanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        // JScrollPane adds a scrollbar if the list gets too long

        // For Split Pane
        // this JSplitPane divides the window into two resizable sections for better access of the chat
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,      // Split left-right
                new JScrollPane(chatArea),        // Left: chat area with scrollbar
                sidePanel                         // Right: user list
        );
        split.setDividerLocation(300);  // Initial position of the divider -> change accordingly later

        // Navigation bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navBar.add(leaveBtn);  // this Put Leave button on the right side

        // Bottom input bar
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);  // Input field

        JPanel btnGrp = new JPanel(new FlowLayout());
        btnGrp.add(sendBtn);                          // fix Send button on the right
        bottom.add(btnGrp, BorderLayout.EAST);

        // --- ASSEMBLE THE MAIN WINDOW ---
        // this setting assemble to organize main window components can change later
        frame.setLayout(new BorderLayout());
        frame.add(navBar, BorderLayout.NORTH);    // Top: Leave button
        frame.add(split, BorderLayout.CENTER);    // Middle: Chat + User list
        frame.add(bottom, BorderLayout.SOUTH);    // Bottom: Input field + Send button


        // This calls the main window 
        frame.setSize(400, 400);// Set window size (width, height)
        frame.setLocationRelativeTo(null);  // Center the window on screen
        frame.setVisible(true);             // Make the window visible!
        
    }

}