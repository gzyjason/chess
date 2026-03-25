package client;
import model.GameData;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.List;


public class ChessClient {
    ServerFacade serverFacade;
    String authToken;
    private List<GameData> cachedGames;

    public ChessClient (String serverURL){
        this.serverFacade = new ServerFacade(serverURL);
    }

    public enum State {
        SIGNED_OUT,
        SIGNED_IN
    }

    private State state = State.SIGNED_OUT;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equalsIgnoreCase("quit")) {
            System.out.print("[" + state + "] >>> ");
            input = scanner.nextLine();
            String[] splitInput = input.split("\\s+");
            try {
                eval(splitInput);
            } catch(FacadeException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
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

        if (tokens.length == 0 || tokens[0].isEmpty()) {return;}

        if (state == State.SIGNED_OUT) {
            switch (tokens[0].toLowerCase()) {
                case "help":
                    System.out.println(helpMenuOut);
                    break;
                case "login":
                    if (tokens.length != 3) {
                        System.out.println("Expected 2 arguments: login <USERNAME> <PASSWORD>");
                        break;
                    }
                    var retrievedLogin = serverFacade.login(tokens[1], tokens[2]);
                    authToken = retrievedLogin.authToken();
                    state = State.SIGNED_IN;
                    break;
                case "register":
                    if (tokens.length != 4) {
                        System.out.println("Expected 3 arguments: register <USERNAME> <PASSWORD> <EMAIL>");
                        break;
                    }
                    var retrievedRegister = serverFacade.register(tokens[1], tokens[2], tokens[3]);
                    authToken = retrievedRegister.authToken();
                    state = State.SIGNED_IN;
                    break;
            }
        }

        if (state == State.SIGNED_IN){
            switch (tokens[0].toLowerCase()) {
                case "help":
                    System.out.println(helpMenuIn);
                    break;
                case "create":
                    if (tokens.length != 2){
                        System.out.println("Expected 1 argument: create <NAME>");
                        break;
                    }
                    serverFacade.createGame(authToken, tokens[1]);
                    System.out.println("Created " + tokens[1]);
                    break;
                case "list":
                    if (tokens.length != 1){
                        System.out.println("No argument accepted for list");
                        break;
                    }
                    var gameList = serverFacade.listGame(authToken);
                    this.cachedGames = new ArrayList<>(gameList.games());
                    if (cachedGames.isEmpty()) {
                        System.out.println("No games found. Use 'create <NAME>' to start one");
                    }

                    System.out.println("Current Games:");
                    for (int i = 0; i < cachedGames.size(); i++){
                        var currentGame = cachedGames.get(i);
                        int displayIndex = i + 1;
                        System.out.println(displayIndex + ". " + currentGame.gameName());
                    }
                case "join":
                    if (tokens.length != 3) {
                        System.out.println("Expected 2 arguments: join <ID> [WHITE|BLACK]");
                        break;
                    }
                    if (cachedGames == null || cachedGames.isEmpty()) {
                        System.out.println("Please 'list' games before attempting to join or observe");
                        break;
                    }
                    if (!Objects.equals(tokens[2], "WHITE") && !Objects.equals(tokens[2], "BLACK")) {
                        System.out.println("Second argument must be either 'WHITE' or 'BLACK'");
                        break;
                    }
                    try {
                        int listNumber = Integer.parseInt(tokens[1]);
                        int index = listNumber - 1;
                        if (index < 0 || index > cachedGames.size()){
                            System.out.println("Invalid game number");
                            break;
                        }
                        int retrievedGameID = cachedGames.get(index).gameID();
                        serverFacade.joinGame(authToken, tokens[2].toUpperCase(), retrievedGameID);
                    } catch (NumberFormatException exception) {
                        System.out.println("ID must be an integer");
                    } catch (FacadeException exception){
                        System.out.println("Error: " + exception.getMessage());
                    }
                    break;
            }
        }
    }
}
