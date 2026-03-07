// Main controller script to allow the GUI and server/client code to communicate between each other.

package controller;

import gui.*;
import client_server_model.*;

class Gui implements Runnable {

    public void run() {
        System.out.println("Starting GUI...");
        gui.Gui.main(null);   // start GUI
    }
}

class JoinServer implements Runnable {
    private String serverIp;
    private Integer serverPort;
    private String userUsername;

    public JoinServer(String serverIp, Integer serverPort, String userUsername) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.userUsername = userUsername;
    }

    public void run() {
        System.out.println("[CONTROLLER] Starting the client/server script");

        try {
            ClientServer cs = new ClientServer();
            cs.start("join", serverIp, serverPort, userUsername );
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Failed to launch ClientServer.java script.");
            e.printStackTrace(); //Prints what went wrong with the object, used to debug.
        }
    }

    static void send_server_details(String serverIP, Integer serverPort, String userUsername){
        //Used to send the IP, USERNAME & PORT from the GUI to the server code so the user can connect to a server of their choice.
        JoinServer server = new JoinServer(serverIP, serverPort, userUsername);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}

class CreateServer implements Runnable {
    private Integer serverPort = 7000;

    public CreateServer(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        System.out.println("[CONTROLLER] Starting server on port " + serverPort + "...");
        try {
            ClientServer cs = new ClientServer();
            cs.start("create", null, serverPort, "Host");
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Failed to launch server.");
            e.printStackTrace();
        }
    }

    static void create_new_server(Integer serverPort) {
        CreateServer newServer = new CreateServer(serverPort);
        Thread newServerThread = new Thread(newServer);
        newServerThread.start();
    }
}

public class Main_controller {

    private static String message;
    private static boolean messageAvailable = false;
    private static final Object messageLock = new Object();

    public static void joinServerButtonPressed(String serverIP, Integer serverPort, String userUsername ){
        System.out.println("[CONTROLLER] User pressed the 'join' button named 'proceedBtn' with the following attributes:\nSERVER IP: " + serverIP
        + "\nSERVER PORT: " + serverPort
        + "\nUSERNAME: " + userUsername);

        JoinServer.send_server_details(serverIP, serverPort, userUsername);
    }


    public static void message_sent_from_user(String message){
        //Sends the message from the GUI to the ClientServer
        System.out.println("[CONTROLLER] The user sent the message: " + message);
        synchronized(messageLock) {
            Main_controller.message = message;
            messageAvailable = true;
        }
    }

    public static String getMessage(){
        //The ClientServer script calls this method repeatedly. If a message exists, it is parsed to the ClientServer script.
        synchronized(messageLock) { //Synchronized threads because sometimes the ClientServer script would be out of sync with the controller script and wouldnt see any messages.
            if (!messageAvailable) {
                return null;
            }
            String temp = message;
            message = null;
            messageAvailable = false;
            return temp;
        }
    }

    public static void displayMessage(String recievedMessage, String senderName){
        gui.Gui guiInstance = gui.Gui.getInstance();                // ask the GUI for itself
        if (guiInstance != null) {
            guiInstance.displayRecievedMessage(recievedMessage, senderName);
        } else {
            System.err.println("[CONTROLLER] displayMessage(): GUI not initialised, message ignored");
        }
    }

    public static void system_message(String notification){
        //Notifies when users join, coordinator changes or user leaves.
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance != null) {
            guiInstance.addSystemMessage(notification); //Adds a system/notification message into the GUI.
        } else {
            System.err.println("[CONTROLLER] notify_user(): GUI not initialised, notification ignored");
        }
    }

    public static void createServerButtonPressed(Integer serverPort){
        //This method is called from the GUI. It passes the users port so we can create a server with the details.
        CreateServer.create_new_server(serverPort);
    }
    public static void main(String[] args) {

        //Create a GUI object and start the GUI thread.
        Thread guiThread = new Thread(new Gui());
        guiThread.start();

        //Wait for the GUI to launch. Don't assume it is running instantly as some machines are slow.
        while (gui.Gui.getInstance() == null) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {} 
        }
    }
}