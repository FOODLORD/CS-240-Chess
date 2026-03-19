package exception;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        ClientError,
    }

    private final Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code code() {
        return code;
    }


    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }


    public static ResponseException fromJson(String json) {
        try {
            var map = new Gson().fromJson(json, HashMap.class);

            String status = map.get("status").toString();
            String message = map.get("message").toString();

            return new ResponseException(Code.valueOf(status), message);

        } catch (Exception error) {

            return new ResponseException(Code.ServerError, json);
        }
    }


    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400, 401, 403 -> Code.ClientError;
            default -> Code.ServerError;
        };
    }


    public int toHttpStatusCode() {
        return switch (code) {
            case ServerError -> 500;
            case ClientError -> 400;
        };
    }
}