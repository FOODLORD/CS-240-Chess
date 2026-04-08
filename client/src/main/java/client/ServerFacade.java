package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    //register
    public RegisterResponse register(RegisterRequest request) throws ResponseException {
        var httpRequest = buildRequest("POST", "/user", request, null);
        var response = sendRequest(httpRequest);
        return handleResponse(response, RegisterResponse.class);
    }

    //login
    public LoginResponse login(LoginRequest request) throws ResponseException {
        var httpRequest = buildRequest("POST", "/session", request, null);
        var response = sendRequest(httpRequest);
        return handleResponse(response, LoginResponse.class);
    }

    //clear
    public void clear() throws ResponseException {
        var httpRequest = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    //logout
    public void logout(String authToken) throws ResponseException {
        var httpRequest = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }

    //creategame
    public CreateGameResponse createGame(String authToken, CreateGameRequest request) throws ResponseException {
        var httpRequest = buildRequest("POST", "/game", request, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, CreateGameResponse.class);
    }

    //listgames
    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var httpRequest = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(httpRequest);
        return handleResponse(response, ListGamesResponse.class);
    }

    //joingame
    public void joinGame(String authToken, JoinGameRequest request) throws ResponseException {
        var httpRequest = buildRequest("PUT", "/game", request, authToken);
        var response = sendRequest(httpRequest);
        handleResponse(response, null);
    }


    //helper func

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeBody(body));

        if (body != null) {
            builder.setHeader("Content-Type", "application/json");
        }

        if (authToken != null) {
            builder.setHeader("authorization", authToken);
        }

        return builder.build();
    }

    private BodyPublisher makeBody(Object body) {
        if (body == null) {
            return BodyPublishers.noBody();
        }
        return BodyPublishers.ofString(gson.toJson(body));
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception error) {
            throw new ResponseException(ResponseException.Code.ServerError, error.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();

        if (status / 100 != 2) {
            var body = response.body();

            String message = ResponseException.fromJson(body).getMessage();
            ResponseException.Code code = ResponseException.fromHttpStatusCode(status);

            throw new ResponseException(code, message);
        }

        if (responseClass != null) {
            return gson.fromJson(response.body(), responseClass);
        }

        return null;
    }

    public String getServerUrl() {
        return serverUrl;
    }
}