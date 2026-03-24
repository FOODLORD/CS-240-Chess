package client;


public class ClientMain {
    public static void main(String[] args) {

        String serverUrl = "http://localhost:8080";

        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            ChessClient client = new ChessClient(serverUrl);
            client.run();

        }

        catch (Throwable ex) {
            System.out.printf("Unable to start client: %s%n", ex.getMessage());
        }

    }
}
