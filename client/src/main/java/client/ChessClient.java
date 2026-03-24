package client;

import java.util.*;
import java.util.Arrays;
import java.util.Scanner;

import exception.ResponseException;
import model.*;


import static ui.EscapeSequences.*;

public class ChessClient {

    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;

    private List<GameData> ListGames = new ArrayList<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess. Type help to get started. ♕");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        String result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result + RESET_TEXT_COLOR);
            }

            catch (Exception error) {
                System.out.println(SET_TEXT_COLOR_RED + error.getMessage() + RESET_TEXT_COLOR);
            }
        }

        System.out.println(SET_TEXT_COLOR_YELLOW + "Goodbye");
    }

    private void printPrompt() {
        System.out.print("\n[" + state + "] >>> " + RESET_TEXT_COLOR);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state.equals(State.SIGNEDOUT)) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "help" -> help();
                    case "quit" -> "quit";
                    default -> help();
                };
            }

            else {
                return switch (cmd) {
                    case "create" -> createGame(params);
                    case "list" -> listGames();
                    case "join" -> joinGame(params);
                    case "observe" -> observeGame(params);
                    case "logout" -> logout();
                    case "help" -> help();
                    case "quit" -> "quit";
                    case "clear" -> clear();
                    default -> help();
                };
            }

        }

        catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length != 3) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Expected: register <username> <password> <email>");
        }

        RegisterResponse response = server.register(new RegisterRequest(params[0], params[1], params[2]));

        authToken = response.authToken();
        state = State.SIGNEDIN;

        return "Logged in as " + response.username();
    }

    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Expected: login <username> <password>");
        }

        LoginResponse response = server.login(new LoginRequest(params[0], params[1]));

        authToken = response.authToken();
        state = State.SIGNEDIN;

        return "Logged in as "+ SET_TEXT_COLOR_GREEN + response.username() + RESET_TEXT_COLOR + "\n\n" + help();
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        state = State.SIGNEDOUT;
        return "Logged out.";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Expected: create <name>");
        }

        server.createGame(authToken, new CreateGameRequest(params[0]));
        return "Game created: " + params[0];
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        ListGamesResponse response = server.listGames(authToken);

        ListGames = new ArrayList<>(response.games());

        if (ListGames.isEmpty()) {
            return "No games found.";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < ListGames.size(); i++) {
            GameData game = ListGames.get(i);

            String whiteUser = (game.whiteUsername() == null) ? "-" : game.whiteUsername();
            String blackUser = (game.blackUsername() == null) ? "-" : game.blackUsername();

            result.append(SET_TEXT_COLOR_BLUE)
                    .append((i + 1))
                    .append(". ")
                    .append("Game name: ").append(game.gameName())
                    .append(" | White: ").append(whiteUser)
                    .append(" | Black: ").append(blackUser)
                    .append(RESET_TEXT_COLOR)
                    .append("\n");
        }

        return result.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 2) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Expected: join <ID> <WHITE|BLACK>");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException e) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Game number must be a number");
        }

        if (index < 0 || index >= ListGames.size()) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Invalid game ID");
        }

        String color = params[1].toUpperCase();
        int gameID = ListGames.get(index).gameID();

        server.joinGame(authToken, new JoinGameRequest(color, gameID));

        return "Joined game as " + color;
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Expected: observe <ID>");
        }

        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException e) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Game number must be a number");
        }

        if (index < 0 || index >= ListGames.size()) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "Invalid game ID");
        }

        int gameID = ListGames.get(index).gameID();

        server.joinGame(authToken, new JoinGameRequest(null, gameID));

        return "Observing game";
    }


    public String help() {
        if (state == State.SIGNEDOUT) {
            return SET_TEXT_COLOR_BLUE +
                    "Options: \n" +
                    "register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                    "login <USERNAME> <PASSWORD> - to play chess\n" +
                    "help - with possible commands\n" +
                    "quit - playing chess\n" +
                    RESET_TEXT_COLOR;
        }

        return SET_TEXT_COLOR_BLUE +
                "Options: \n" +
                "create <NAME> - a game\n" +
                "list - games\n" +
                "join <ID> <WHITE|BLACK> - a game\n" +
                "observe <ID> - a game\n" +
                "logout - when you are done\n" +
                "help - with possible commands\n" +
                "quit - playing chess\n" +
                RESET_TEXT_COLOR;
    }

    public String clear() throws ResponseException {
        server.clear();

        return SET_TEXT_COLOR_MAGENTA + "Database cleared." + RESET_TEXT_COLOR;
    }



    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(ResponseException.Code.ClientError, SET_TEXT_COLOR_RED + "You must sign in");
        }
    }

}
