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

    }
    
    private static void initialise_server(String serverIpAddress, Integer serverPort) throws IOException{
        //THIS IS WHERE THE CODE TO CREATE THE SERVER WOULD BE WRITTEN.
         System.out.println("SERVER LAUNCHING ON IP ADDRESS: " + serverIpAddress + " USING PORT " + serverPort);

        //Stores the different usernames in a set so we can reject users who attempt to join the chat using the same name as someone else.
        //private static Set<String> usernames = new HashSet<>(); IN COMMENTS BECAUSE UNUSED RN

        try (ServerSocket coordinator = new ServerSocket(serverPort)){
            while (true){
                //coordinator.accept(); COMMENTED OUT BECAUSE CURRENTLY INCOMPLETE
            }
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
