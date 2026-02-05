import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.HashSet;
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
    private static Set<ClientHandler> clients =
            ConcurrentHashMap.newKeySet();

    public ClientThread (Socket socket) throws IOException{
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    @Override
    public void run(){
        try{
            clients.add(this);
            out.println("test,has joined");
            while (in.hasNextLine()){
                String message = in.nextLine();
                broadcast(message);
            }
        }
        finally{
            clients.remove(this);
            socketClose();
        }
    }
    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
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

class prompt_homepage_GUI{
    //Makes the CLI interface pop up for the user where they are presented with 2 options. 1) Create server. 2) Join server.
    static int run(){
        
        int user_selection = 0;
        Scanner input_reader = new Scanner(System.in);

        System.out.print("\nWELCOME TO OUR SOCKET SERVER CHATROOM\n\nPlease choose a selection by entering its number:\n1) Create server\n2) Join server\n\n>>>");

        try {
            user_selection = input_reader.nextInt();
            
            if (user_selection < 1 || user_selection > 2){
                System.err.println("The number chosen by the user wasn't specified in the selection menu. EXITING (in the future, 'please try again' will be used.)");
                input_reader.close();
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Incorrect value entered. Input must be a number.");   
        }
        return user_selection;
    }
}


class Server{
    

    private static void attempt_to_join_server(String ip_Address, int serverPort){
        System.out.println("ATTEMPTING TO JOIN SERVER AT IP: " + ip_Address + " ON PORT " + serverPort);

        try {
            Socket socket = new Socket(ip_Address, serverPort);

            Scanner fromServer = new Scanner(socket.getInputStream());
            PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
            Scanner userInput = new Scanner(System.in);

            // Start listening thread
            new ServerListener(fromServer).start();

            System.out.println("You can now start chatting:");

            // Main thread sends messages
            while (true) {
                String message = userInput.nextLine();
                toServer.println(message);
            

        } catch (Exception e) {
            System.err.println("Could not connect to the server. Check your internet connection, IP & port.");
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

    public static void create() throws Exception{

        String serverIpAddress = get_ip_address();
        int serverPort;
        Scanner input_reader = new Scanner(System.in);

        System.out.println("\nCreate server mode selected\n");
        System.out.println("The servers private IP is: " + serverIpAddress);
        System.out.print("\nPlease choose a valid PORT to run your server on:\n>>>");
        
        try {
            serverPort = input_reader.nextInt(); 

            if (serverPort >= 1 && serverPort <= 65534){
                //Attempts to create the server with provided PORT
                initialise_server(serverIpAddress, serverPort);

            } else {
                System.err.println("Port is either to small or too large. must be within 1 - 65534. EXITING (in the future, 'please try again' will be used.)");
                input_reader.close();
                System.exit(3);
            }

        } catch(Exception e) {
            System.err.println("The user did not enter a valid integer. EXITING (in the future, 'please try again' will be used.)");
            input_reader.close();
            System.exit(2);
        }
    }

    public static void join(){
        String joinIP = "";
        int joinPort;

        System.out.println("\nJoin server mode selected\n");
        System.out.print("\nPlease enter the servers IP address that you are trying to connect to:\n>>>");

        Scanner input_reader = new Scanner(System.in);
        
        try {
            joinIP = input_reader.next();
            System.out.println("The users chosen IP is:" + joinIP);
        
        } catch (Exception e) {
            System.err.println("Invalid IP entered. EXITING (in the future, 'please try again' will be used.)");
            input_reader.close();
            System.exit(4);
        }

        try {
            System.out.print("\nPlease enter the servers PORT that you are trying to connect to:\n>>>");
            joinPort = input_reader.nextInt();

            if (joinPort >= 1 || joinPort <= 65534){
                //Attempts to join the server provided IP and PORT are in the correct format.
                Server.attempt_to_join_server(joinIP, joinPort);

            } else {
                System.err.println("Invalid PORT. The port should always be a number between 1 and 65534. EXITING (in the future, 'please try again' will be used.)");
                System.exit(5);
            }
            
        } catch (Exception e) {
            System.err.println("PORT was not a vaid integer. Please enter a numerical value between 1 - 65534. EXITING (in the future, 'please try again' will be used.)");
            input_reader.close();
            System.exit(5);
        }

    }
}


public class Main {
    public static void main(String[] args) throws Exception {

        //Currently runs CLI interface where users can 1)create server 2)Join server
        int user_selection = prompt_homepage_GUI.run();
        System.out.println("The user chose the choice: " + user_selection);
        
        if (user_selection == 1){
            Server.create();
        }
        else if (user_selection == 2){
            Server.join();
        }
        
        assert (user_selection != 1 || user_selection != 2): "The prompt_homepage_GUI.run() method did not successfully catch the users input error. Program should have never reached here";
    }
}
