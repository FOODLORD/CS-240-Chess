package client;
import java.util.*;
import java.util.Arrays;
import java.util.Scanner;
import chess.*;
import exception.ResponseException;
import model.*;
import websocket.commands.*;
import websocket.messages.*;
import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken = null;
    private ChessGame currentGame = new ChessGame();
    private ChessGame.TeamColor playerColor = ChessGame.TeamColor.WHITE;
    private List<GameData> listGames = new ArrayList<>();
    private WebSocketManager webSocket;
    private int currentGameID;
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
                if (!result.isEmpty()) {
                    System.out.println(result + RESET_TEXT_COLOR);
                }
            }
            catch (ResponseException error){
                printError(error);
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
    public String eval(String input) throws ResponseException {
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
            else if (state.equals(State.SIGNEDIN)) {
                return switch (cmd) {
                    case "create" -> createGame(params);
                    case "list" -> listGames();
                    case "join" -> joinGame(params);
                    case "observe" -> observeGame(params);
                    case "logout" -> logout();
                    case "help" -> help();
                    case "quit" -> "quit";
                    case "clearDatabase" -> clear();
                    default -> help();
                };
            }
            else if (state == State.INGAME) {
                if (webSocket == null) {
                    return "Not connected to a game";
                }
                return switch (cmd) {
                    case "move" -> makeMove(params);
                    case "leave" -> leaveGame();
                    case "resign" -> resignGame();
                    case "redraw" -> redraw();
                    case "help" -> inGameHelp();
                    case "highlight" -> highlight(params);
                    default -> "Invalid command. Type help.";
                };
            }
        return "";
    }
    public String register(String... params) throws ResponseException {
        if (params.length != 3) {
            throw new ResponseException(ResponseException.Code.ClientError,"Expected: register <username> <password> <email>");
        }
        RegisterResponse response = server.register(new RegisterRequest(params[0], params[1], params[2]));
        authToken = response.authToken();
        state = State.SIGNEDIN;
        return "Logged in as " + response.username();
    }
    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(ResponseException.Code.ClientError,"Expected: login <username> <password>");
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
        return SET_TEXT_COLOR_MAGENTA + "Logged out.";
    }
    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length != 1) {
            throw new ResponseException(ResponseException.Code.ClientError, "Expected: create <name>");
        }
        server.createGame(authToken, new CreateGameRequest(params[0]));
        return "Game created: " + params[0];
    }
    public String listGames() throws ResponseException {
        assertSignedIn();
        ListGamesResponse response = server.listGames(authToken);
        listGames = new ArrayList<>(response.games());
        if (listGames.isEmpty()) {
            return "No games found.";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < listGames.size(); i++) {
            GameData game = listGames.get(i);

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
        checkList();
        if (params.length != 2) {
            throw new ResponseException(ResponseException.Code.ClientError, "Expected: join <ID> <WHITE|BLACK>");
        }
        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        }
        catch (NumberFormatException error) {
            throw new ResponseException(ResponseException.Code.ClientError,"Game number must be a number");
        }
        if (index < 0 || index >= listGames.size()) {
            throw new ResponseException(ResponseException.Code.ClientError,"Invalid game ID");
        }
        String color = params[1].toUpperCase();
        if (color.equals("WHITE")) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else {
            playerColor = ChessGame.TeamColor.BLACK;
        }
        int gameID = listGames.get(index).gameID();
        currentGameID = gameID;
        server.joinGame(authToken, new JoinGameRequest(color, gameID));
        state = State.INGAME;
        try {
            webSocket = new WebSocketManager();
            webSocket.connect("ws://localhost:8080/ws", this::serverMessage);
            webSocket.send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
        }
        catch (Exception error) {
            System.out.println("WebSocket error: " + error.getMessage());
        }
        ListGamesResponse response = server.listGames(authToken);
        listGames = new ArrayList<>(response.games());
        GameData game = listGames.get(index);
        currentGame= game.game();
        if (currentGame == null) {
            currentGame = new ChessGame();
        }
        drawBoard(currentGame, playerColor);
        return SET_TEXT_COLOR_GREEN + "Joined game as " + color + RESET_TEXT_COLOR;
    }
    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        checkList();
        playerColor = ChessGame.TeamColor.WHITE;
        if (params.length != 1) {
            throw new ResponseException(ResponseException.Code.ClientError, "Expected: observe <ID>");
        }
        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        }
        catch (NumberFormatException e) {
            throw new ResponseException(ResponseException.Code.ClientError, "Game number must be a number");
        }
        if (index < 0 || index >= listGames.size()) {
            throw new ResponseException(ResponseException.Code.ClientError, "Invalid game ID: " + index);
        }
        int gameID = listGames.get(index).gameID();
        currentGameID = gameID;
        GameData game = listGames.get(index);
        currentGame = game.game();
        if (currentGame == null) {
            currentGame = new ChessGame();
        }
        state = State.INGAME;
        try {
            webSocket = new WebSocketManager();
            String serverUrl = server.getServerUrl().replace("http", "ws") + "/ws";
            webSocket.connect(serverUrl, this::serverMessage);
            webSocket.send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
        }
        catch (Exception error) {
            System.out.println("WebSocket error: " + error.getMessage());
        }
        drawBoard(currentGame, playerColor);
        return SET_TEXT_COLOR_GREEN + "Observing game " + gameID + RESET_TEXT_COLOR;
    }
    public String help() {
        if (state == State.SIGNEDOUT) {
            return SET_TEXT_COLOR_BLUE +
                    "Options: \n" + "register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                    "login <USERNAME> <PASSWORD> - to play chess\n" + "help - with possible commands\n" +
                    "quit - playing chess\n" + RESET_TEXT_COLOR;
        }
        return SET_TEXT_COLOR_BLUE +
                "Options: \n" + "create <NAME> - a game\n" + "list - games\n" +
                "join <ID> <WHITE|BLACK> - a game\n" + "observe <ID> - a game\n" +
                "logout - when you are done\n" + "help - with possible commands\n" +
                "quit - playing chess\n" + RESET_TEXT_COLOR;
    }
    public String clear() throws ResponseException {
        server.clear();
        return SET_TEXT_COLOR_MAGENTA + "Database cleared." + RESET_TEXT_COLOR;
    }
    private void printError(ResponseException error) {
        String color;
        if (error.code() == ResponseException.Code.ServerError) {
            color = SET_TEXT_COLOR_RED;
        } else {
            color = SET_TEXT_COLOR_YELLOW;
        }
        System.out.println(color + error.getMessage() + RESET_TEXT_COLOR);
    }
    private void checkList() throws  ResponseException{
        if (listGames == null || listGames.isEmpty()) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must list games first"
            );
        }
    }
    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }
    public String makeMove(String... params) {
        if (params.length != 2) {
            return "Usage: move <start> <end>";
        }
        try {
            ChessPosition start = parsePosition(params[0]);
            ChessPosition end = parsePosition(params[1]);

            ChessMove move = new ChessMove(start, end, null);

            webSocket.send(new MakeMoveCommand(authToken, currentGameID, move));

        } catch (Exception error) {
            return "Invalid move";
        }
        return "";
    }
    private ChessPosition parsePosition(String input) throws Exception {
        if (input.length() != 2) {
            throw new Exception("Invalid position. Use example like e2");
        }
        char colChar = input.charAt(0);
        char rowChar = input.charAt(1);
        int col = colChar - 'a' + 1;
        int row = rowChar - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new Exception("Invalid board position");
        }
        return new ChessPosition(row, col);
    }
    public String leaveGame() {
        try {
            webSocket.send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID));
            webSocket.close();
            webSocket = null;
        } catch (Exception error) {
            return error.getMessage();
        }
        state = State.SIGNEDIN;
        return "Left game";
    }
    public String resignGame() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine();
        if (!confirm.equalsIgnoreCase("yes")) {
            return "Resign cancelled";
        }
        try {
            webSocket.send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID));
        }
        catch (Exception error) {
            return error.getMessage();
        }

        return "";
    }
    public String redraw() {
        drawBoard(currentGame, playerColor);
        return "";
    }
    public String inGameHelp() {
        return """
        Commands:
        move <start> <end>
        leave
        resign
        redraw
        help
        highlight <tile>
        """;
    }
    public String highlight(String... params) {
        if (params.length != 1) {
            return "Usage: highlight <position>";
        }
        try {
            ChessPosition pos = parsePosition(params[0]);
            ChessPiece piece = currentGame.getBoard().getPiece(pos);
            if (piece == null) {
                return "No piece at that position";
            }
            Collection<ChessMove> moves;
            try {
                moves = currentGame.validMoves(pos);
            } catch (Exception error) {
                return "No moves available";
            }
            Collection<ChessPosition> highlights = new ArrayList<>();
            highlights.add(pos);
            for (ChessMove move : moves) {
                highlights.add(move.getEndPosition());
            }
            drawBoardWithHighlights(currentGame, playerColor, highlights);
        } catch (Exception error) {
            return "Invalid highlight";
        }
        return "";
    }
    //========================= drawBoard functions
    private void drawBoard(ChessGame game, ChessGame.TeamColor color) {
        System.out.println();
        boolean isWhite;
        isWhite = color == ChessGame.TeamColor.WHITE;
        printColumnLabels(isWhite);
        for (int row = (isWhite ? 8 : 1);
             isWhite ? row >= 1 : row <= 8;
             row += (isWhite ? -1 : 1)) {
            printRow(row, isWhite, game);
        }
        printColumnLabels(isWhite);
        System.out.println();
    }
    private void printRow(int row, boolean isWhite, ChessGame game) {
        // left row number
        System.out.print(" " + row + " ");
        for (int col = (isWhite ? 1 : 8);
             isWhite ? col <= 8 : col >= 1;
             col += (isWhite ? 1 : -1)) {
            boolean lightSquare = (row + col) % 2 == 0;
            // set background color
            if (lightSquare) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(SET_BG_COLOR_MAGENTA);
            }
            ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, col));
            if (piece != null) {
                System.out.print(getPieceSymbol(piece));
            } else {
                System.out.print(EMPTY);
            }
            System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        }
        // right row number
        System.out.println(" " + row);
    }
    private void printColumnLabels(boolean isWhite) {
        System.out.print("   ");
        if (isWhite) {
            for (char letter = 'a'; letter <= 'h'; letter++) {
                System.out.print(" "+ letter + "  ");
            }
        } else {
            for (char letter = 'h'; letter >= 'a'; letter--) {
                System.out.print(" " + letter + "  ");
            }
        }
        System.out.println();
    }
    private void drawBoardWithHighlights(ChessGame game, ChessGame.TeamColor color, Collection<ChessPosition> highlights) {
        System.out.println();
        boolean isWhite = color == ChessGame.TeamColor.WHITE;
        printColumnLabels(isWhite);
        for (int row = (isWhite ? 8 : 1);
             isWhite ? row >= 1 : row <= 8;
             row += (isWhite ? -1 : 1)) {
            printRowWithHighlights(row, isWhite, game, highlights);
        }
        printColumnLabels(isWhite);
        System.out.println();
    }
    private void printRowWithHighlights(int row, boolean isWhite, ChessGame game, Collection<ChessPosition> highlights) {
        System.out.print(" " + row + " ");
        for (int col = (isWhite ? 1 : 8);
             isWhite ? col <= 8 : col >= 1;
             col += (isWhite ? 1 : -1)) {
            ChessPosition pos = new ChessPosition(row, col);
            boolean lightSquare = (row + col) % 2 == 0;
            boolean isHighlight = highlights != null && highlights.contains(pos);
            if (isHighlight) {
                System.out.print(SET_BG_COLOR_GREEN);
            }
            else if (lightSquare) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(SET_BG_COLOR_MAGENTA);
            }
            ChessPiece piece = game.getBoard().getPiece(pos);
            if (piece != null) {
                System.out.print(getPieceSymbol(piece));
            } else {
                System.out.print(EMPTY);
            }
            System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
        }
        System.out.println(" " + row);
    }
    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_KING : SET_TEXT_COLOR_BLACK + BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_QUEEN : SET_TEXT_COLOR_BLACK + BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_BISHOP : SET_TEXT_COLOR_BLACK + BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_KNIGHT : SET_TEXT_COLOR_BLACK + BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_ROOK : SET_TEXT_COLOR_BLACK + BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? SET_TEXT_COLOR_WHITE + WHITE_PAWN : SET_TEXT_COLOR_BLACK + BLACK_PAWN;
        };
    }
    private void serverMessage(ServerMessage msg) {
        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {
                ChessGame newGame = ((LoadGameMessage) msg).getGame();
                if (newGame != null) {
                    currentGame = newGame;
                    drawBoard(currentGame, playerColor);
                }
            }
            case NOTIFICATION -> {
                String message = ((NotificationMessage) msg).getMessage();
                System.out.println(message);
                if (message.toLowerCase().contains("game over")) {
                    state = State.SIGNEDIN;
                    try {
                        webSocket.close();
                    } catch (Exception ignored) {}
                }
            }case ERROR -> {
                System.out.println(((ErrorMessage) msg).getErrorMessage());
            }
        }
    }
}
