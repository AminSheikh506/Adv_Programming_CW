package client_server_model;

import controller.*;

import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.util.Set;
//import java.util.HashSet;
import java.util.concurrent.*;

class ServerListener extends Thread{
	private Scanner fromServer;
	
	public ServerListener(Scanner fromServer) {
		this.fromServer = fromServer;
	}
	
	@Override
	public void run() {
        while (fromServer.hasNextLine()) {
            System.out.println(fromServer.nextLine());
        }
    }
}
class ClientThread implements Runnable{
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
	private String username;
	private boolean coordinator = false;
    private static ConcurrentHashMap<String, ClientThread> clients = new ConcurrentHashMap<>();
    

    public ClientThread (Socket socket) throws IOException{
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    @Override
    public void run(){
        try{
        	username = in.nextLine();
            clients.put(username,this);
            broadcast("system " + username + " has joined");

			if (clients.size() == 1) {
            	coordinator = true;
                broadcast("system " + username + " is the new coordinator");
                System.out.println("system " + username + " is the new coordinator");
            }
            else {
                broadcast("system " + getCoordinator().username + " is the coordinator");
            }
            
            while (in.hasNextLine()){
                String message = in.nextLine();
                if (message.startsWith("dm")) {
                    sendDirectMessage(message);
                }
                else if (message.startsWith("system")){
                    controller.Main_controller.system_message(message);
                }
                else if (message.startsWith("dox")) {
                    if (coordinator == false) {
                        broadcast("system only the coordinator (" + getCoordinator().username + ") can dox, please send them a request and they may give you the info");
                    }
                    else{
                        sendInfo(message);
                    }
                }
                else {
                    broadcast(username + " " + message); //Space is a NECESSITY here due to how regex handling works of the recieved messages.
                }
            }
        }
        finally{
            if (username != null) {
        		clients.remove(username);
        		broadcast("system " + username + " has left the chat");
				if (coordinator == true) {
        			assignNewCoordinator();
        		}
        	}
            socketClose();
        }
    }
	private ClientThread getCoordinator() {
    	for (ClientThread client : clients.values()) {
    		if (client.coordinator == true) {
    			return(client);
    		}
    	}
    	return(null);
    }

    private void broadcast(String message) {
        for (ClientThread client : clients.values()) {
            client.out.println(message);}
    }

	private void assignNewCoordinator() {
    	if (clients.size() == 0) {
    		System.out.println("system no viable members to become coordinator");
    	}
    	else {
	        for (ClientThread client : clients.values()) {
	            client.coordinator = true;
                broadcast("system " + client.username + " is the new coordinator");
	            break;
	        }
    	}
    }
	
	private void sendDirectMessage(String message) {
        System.out.println("[CLIENTSERVER] DM recieved: " + message);

    	String[] splitMsg = message.split(" ",3);
    	
    	if (splitMsg.length < 3) {
    		out.println("system use this format: dm username message");
    		return;
    	}
    	
    	String user = splitMsg[1];
    	String dm = splitMsg[2];
    	
    	ClientThread target = clients.get(user);
    	
    	if (target == null) {
    		out.println("system ok schizo, now type someone who exists");
    		return;
    	}
    	
    	target.out.println("DM: " + username + "- " + dm);
    	out.println("DM to "+ user + "- "+ dm);
    }
	private void sendInfo(String message) {
    	String[] splitMsg = message.split(" ",2);
    	
    	if (splitMsg.length < 2) {
    		out.println("system use this format: dox <username>");
    		return;
    	}
    	String receiverName = splitMsg[1];
    	ClientThread receiver = clients.get(receiverName);
    	
    	if (receiver == null) {
    		out.println("system you can't give info about someone who doesn't exist");
    		return;
    	}
    	String fullInfo = "";
    	String clientInfo = "";
    	for (ClientThread client : clients.values()) {
            clientInfo = client.username + "- \n" + "IP: " + client.socket.getInetAddress().getHostAddress() + "  Port: " + socket.getPort() + "\n \n";
            fullInfo = fullInfo + clientInfo;
        }
    	receiver.out.println(fullInfo);

	}
    private void socketClose() {
        try {
            socket.close();
        } 
        catch (IOException e) {
            System.err.println("Error closing socket");
        }
    }
}

class Server{
    
    private static void attempt_to_join_server(String ip_Address, int serverPort, String userUsername){
        System.out.println("ATTEMPTING TO JOIN SERVER AT IP: " + ip_Address + " ON PORT " + serverPort);

        try (Socket socket = new Socket(ip_Address, serverPort)) {
            Scanner fromServer = new Scanner(socket.getInputStream());
            PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);

            // Start listening thread to handle incoming messages
            Thread listenerThread = new Thread(() -> {
                while (fromServer.hasNextLine()) {
                    String message = fromServer.nextLine();
                    System.out.println("New message recieved: " + message);
                    if (message.startsWith("system")) {
                        // Strip the "system" prefix and pass the content to the system message display method
                        String systemContent = message.substring("system".length()).trim();
                        controller.Main_controller.system_message(systemContent);
                    } 

                    else if (message.startsWith(userUsername)){
                        //Do nothing because we don't want to see our own messages.
                    }
                    else {
                        String[] sliced_message = message.split(" ", 2); //Splits the message from format name:message to [name] [message]
                        controller.Main_controller.displayMessage(sliced_message[1], sliced_message[0]); //splits the message between message and username.
                    }
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            //Used to set the users username in chat.
            toServer.println(userUsername);

            System.out.println("[CLIENTSERVER] You can now start chatting:");

            //Responsible for actually sending the message via the socket
            while (true) {
                String message = controller.Main_controller.getMessage();
                if (message != null && !message.isEmpty()) {
                    System.out.println("[CLIENTSERVER] Sending message '" + message + "'");
                    toServer.println(message);
                }
            }

        } catch (Exception e) {
            System.err.println("[CLIENTSERVER] Could not connect to the server. Check your internet connection, IP & port.");
            System.exit(7);
        }
    }
    
    private static void initialise_server(String serverIpAddress, Integer serverPort) throws IOException {

        System.out.println("SERVER LAUNCHING ON IP ADDRESS: " + serverIpAddress + " USING PORT " + serverPort);

        ServerSocket coordinator = new ServerSocket(serverPort);

        while (true) {
            Socket clientSocket = coordinator.accept();

            System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

            ClientThread client = new ClientThread(clientSocket);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }


    public static String get_ip_address() throws UnknownHostException {
        //Returns the private IP address of the user as a string. A seperate method was used to keep code clean.
        InetAddress localhost = InetAddress.getLocalHost();
        String private_ip = localhost.getHostAddress().trim();

        return private_ip;
    }

    public static void create(Integer serverPort) throws Exception{

        String serverIpAddress = get_ip_address(); 

        try {
            System.out.println("The users port is: " + serverPort);
            if (serverPort >= 1 && serverPort <= 65534){
                //Attempts to create the server with provided PORT
                initialise_server(serverIpAddress, serverPort);
            } else {
                System.err.println("Port is either too small or too large. Must be within 1 - 65534. EXITING");
                System.exit(3);
            }

        } catch(Exception e) {
            System.err.println("The user did not enter a valid integer OR the port is already in use. EXITING");
            System.exit(2);
        }
    }

    public static void join(String serverIP, Integer serverPort, String userUsername){
        
        System.out.println("The user chose to join server with attributes:" + serverIP + ", " + serverPort + ", " + userUsername);

        try {
            if (serverPort >= 1 && serverPort <= 65534){
                Server.attempt_to_join_server(serverIP, serverPort, userUsername);
            } else {
                System.err.println("Invalid PORT. Must be between 1 and 65534. EXITING");
                System.exit(5);
            }
        } catch (Exception e) {
            System.err.println("Error joining server. EXITING");
            System.exit(5);
        }
    }
}


public class ClientServer {
    public void start(String user_selection, String serverIp, Integer serverPort, String userUsername) throws Exception {

        //Currently runs CLI interface where users can 1)create server 2)Join server
        System.out.println("The user chose the choice: " + user_selection);
        
        if (user_selection == "create"){
            Server.create(serverPort);
        }
        else if (user_selection == "join"){
            Server.join(serverIp, serverPort, userUsername);
        }
        
        assert (user_selection != "create" || user_selection != "join"): "The GUI did not catch incorrect input. The program should have never reached here.";
    }
}