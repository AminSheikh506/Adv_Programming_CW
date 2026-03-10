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
	private boolean active = true;
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
                broadcast("coord " + username);
                System.out.println("[SERVER] " + username + " is the coordinator");
            } else {
                ClientThread currentCoord = getCoordinator();
                if (currentCoord != null) {
                    out.println("system The current coordinator is " + currentCoord.username);
                }
            }
			if (coordinator) {
                Ping();
            }

            //Server sends the Username, IP and PORT when a new user joins to the coordinator directly.
            sendToCoordinator("peerinfo " + username + " " + socket.getInetAddress().getHostAddress() + " " + socket.getPort());
            

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
                else if (message.startsWith("userlist ")) {
                    broadcast(message);
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
				if (coordinator) {
        			assignNewCoordinator();
        		}
                //Tells the coordinator that someone has left and to update the 'online' list.
                sendToCoordinator("depart " + username);
        	}
            socketClose();
        }
    }
	private void Ping() {
    	
        try {
        	Thread.sleep(20000); // 20 seconds
        } catch (InterruptedException e) {
            return;
        }
        
        for (ClientThread client : clients.values()) {
        	client.active = false;
        	client.out.println("you have been made inactive. message to become active");
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
		this.active = true;
        for (ClientThread client : clients.values()) {
            client.out.println(message);}
    }

    private void sendToCoordinator (String message) {
        //Sends a message from any member to the coordinator. Used for requesting IP, Username and PORT.
        ClientThread coord = getCoordinator();
        if (coord != null){
            coord.out.println(message);
        }
    }

	private void assignNewCoordinator() {
    	if (clients.size() == 0) {
    		System.out.println("system no viable members to become coordinator");
    	}
    	else {
	        for (ClientThread client : clients.values()) {
	            client.coordinator = true;
                broadcast("coord " + client.username);
                System.out.println("[SERVER] " + client.username + " is the new coordinator");
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
    		out.println("system that isn't a valid target");
    		return;
    	}
		
		this.active = true;
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
    
    private static void attempt_to_join_server(String ip_Address, int serverPort, String userUsername) {
        System.out.println("ATTEMPTING TO JOIN SERVER AT IP: " + ip_Address + " ON PORT " + serverPort);

        try (Socket socket = new Socket(ip_Address, serverPort)) {
            Scanner fromServer    = new Scanner(socket.getInputStream());
            PrintWriter toServer  = new PrintWriter(socket.getOutputStream(), true);

            java.util.concurrent.atomic.AtomicBoolean isCoordinator =
                    new java.util.concurrent.atomic.AtomicBoolean(false);

            java.util.concurrent.atomic.AtomicReference<String> lastUserlistData =
                    new java.util.concurrent.atomic.AtomicReference<>("");

            java.util.concurrent.ConcurrentHashMap<String, String[]> peerMap =
                    new java.util.concurrent.ConcurrentHashMap<>();

            Thread listenerThread = new Thread(() -> {
                while (fromServer.hasNextLine()) {
                    String message = fromServer.nextLine();
                    System.out.println("[CLIENTSERVER] Received: " + message);

                    if (message.startsWith("coord ")) {
                        String coordName = message.substring(6).trim();
                        boolean isSelf   = coordName.equals(userUsername);
                        isCoordinator.set(isSelf);
                        controller.Main_controller.set_coordinator(coordName);
                        controller.Main_controller.system_message(coordName + " is the coordinator");

                        if (isSelf) {
                            peerMap.clear();
                            String lastData = lastUserlistData.get();
                            if (!lastData.isEmpty()) {
                                for (String entry : lastData.split(",")) {
                                    String[] parts = entry.split(":", 3);
                                    if (parts.length >= 3) {
                                        peerMap.put(parts[0].trim(),
                                                    new String[]{parts[1].trim(), parts[2].trim()});
                                    }
                                }
                            }
                        }
                    }

                    else if (message.startsWith("peerinfo ") && isCoordinator.get()) {
                        String[] parts = message.split(" ", 4);
                        if (parts.length >= 4) {
                            peerMap.put(parts[1], new String[]{parts[2], parts[3]});
                            broadcastUserList(toServer, peerMap, userUsername);
                        }
                    }

                    else if (message.startsWith("depart ") && isCoordinator.get()) {
                        String departedUser = message.substring(7).trim();
                        peerMap.remove(departedUser);
                        broadcastUserList(toServer, peerMap, userUsername);
                    }

                    else if (message.startsWith("userlist ")) {
                        String data = message.substring(9).trim();
                        lastUserlistData.set(data);
                        controller.Main_controller.update_user_list(data);
                    }

                    else if (message.startsWith("system ")) {
                        String content = message.substring(7).trim();
                        controller.Main_controller.system_message(content);
                    }

                    else if (message.startsWith(userUsername + " ")) {
                        // Own message echoed back — ignore
                    }

                    else {
                        String[] sliced = message.split(" ", 2);
                        if (sliced.length >= 2) {
                            controller.Main_controller.displayMessage(sliced[1], sliced[0]);
                        }
                    }
                }
                //If the code reaches this stage, the server has been shut down.
                controller.Main_controller.system_message("The server has been shut down.");
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            synchronized (toServer) { toServer.println(userUsername); }
            System.out.println("[CLIENTSERVER] Connected as: " + userUsername);

            while (true) {
                String msg = controller.Main_controller.getMessage();
                if (msg != null && !msg.isEmpty()) {
                    System.out.println("[CLIENTSERVER] Sending: " + msg);
                    synchronized (toServer) { toServer.println(msg); }
                }
            }

        } catch (Exception e) {
            System.err.println("[CLIENTSERVER] Could not connect to the server.");
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

    private static void broadcastUserList(PrintWriter toServer, java.util.concurrent.ConcurrentHashMap<String, String[]> peerMap, String coordinatorName) {
        StringBuilder sb = new StringBuilder("userlist ");
        sb.append(coordinatorName).append("|");
        boolean first = true;
        for (java.util.Map.Entry<String, String[]> entry : peerMap.entrySet()) {
            if (!first) sb.append(",");
            // Data format: "coordName|user1:ip1:port1,user2:ip2:port2,..."
            sb.append(entry.getKey()).append(":").append(entry.getValue()[0]).append(":").append(entry.getValue()[1]);
            first = false;
        }
        synchronized (toServer) { toServer.println(sb.toString()); }
        System.out.println("[CLIENTSERVER] Coordinator broadcast: " + sb.toString());
    }
}

public class ClientServer {
    public void start(String user_selection, String serverIp, Integer serverPort, String userUsername) throws Exception {

        //Currently runs CLI interface where users can 1)create server 2)Join server
        System.out.println("The user chose the choice: " + user_selection);
        
        if (user_selection.equals("create")){
            Server.create(serverPort);
        }
        else if (user_selection.equals("join")){
            Server.join(serverIp, serverPort, userUsername);
        }
        
        assert (user_selection != "create" || user_selection != "join"): "The GUI did not catch incorrect input. The program should have never reached here.";
    }
}

