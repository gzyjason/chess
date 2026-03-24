package client;
import java.util.Scanner;


public class ChessClient {
    ServerFacade serverFacade;
    String authToken;

    public ChessClient (String serverURL){
        serverURL = "http://localhost:8080";
        this.serverFacade = new ServerFacade(serverURL);
    }

    public enum State {
        SIGNED_OUT,
        SIGNED_IN
    }

    private State state = State.SIGNED_OUT;

    public void run() throws FacadeException {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equalsIgnoreCase("quit")) {
            System.out.print("[" + state + "] >>> ");
            input = scanner.nextLine();
            String[] splitInput = input.split("\\s+");
            eval(splitInput);
        }

        scanner.close();
    }

    public void eval(String[] tokens) throws FacadeException {
        String helpMenuOut = """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;

        String helpMenuIn = """
                create <NAME>: to create a game.
                list: to list games.
                join <ID> [WHITE|BLACK]: to join a game.
                observe <ID>: to watch a game.
                logout: to log out.
                quit: to exit the program.
                help: to see this menu again.
                """;

        switch (tokens[0].toLowerCase()){
            case "help":
                if (state == State.SIGNED_OUT) {
                    System.out.println(helpMenuOut);
                }
                if (state == State.SIGNED_IN){
                    System.out.println(helpMenuIn);
                }
                break;
            case  "login":
                if (tokens.length != 3){
                    System.out.println("Expected 2 arguments: login <USERNAME> <PASSWORD>");
                    break;
                }
                var retrievedLogin = serverFacade.login(tokens[1], tokens[2]);
                authToken = retrievedLogin.authToken();
                state = State.SIGNED_IN;
                break;
            case "register":
                if (tokens.length != 4){
                    System.out.println("Expected 3 arguments: register <USERNAME> <PASSWORD> <EMAIL>");
                    break;
                }
                var retrievedRegister = serverFacade.register(tokens[1], tokens[2], tokens[3]);
                authToken = retrievedRegister.authToken();
                state = State.SIGNED_IN;
                break;
            }
    }
}
