import java.util.Scanner;

class prompt_homepage_GUI{
    static int run(){
        
        int user_selection = 0;
        Scanner input_reader = new Scanner(System.in);

        System.out.print("\nWELCOME TO OUR SOCKET SERVER CHATROOM\n\nPlease choose a selection by entering its number:\n1) Create server\n2) Join server\n\n>>>");

        try {
            user_selection = input_reader.nextInt();
            
            if (user_selection < 1 || user_selection > 2){
                System.err.println("The number chosen by the user wasn't specified in the selection menu. EXITING (in the future, 'please try again' will be used.");
                System.exit(user_selection);
            }

        } catch (Exception e) {
            System.err.println("Incorrect value entered. Input must be a number.");
            
        }

        input_reader.close();

        return user_selection;
    }
}


class Server{
    
    static void create(){
        System.out.println("\nCreate server mode selected\n");
    }

    static void join(){
        System.out.println("\nJoin server mode selected\n");
    }
}


public class Main {
    public static void main(String[] args) throws Exception {

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
