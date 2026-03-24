package exception;

import com.google.gson.Gson;


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



    private static class ErrorResponse {
        String message;
    }

    public static ResponseException fromJson(String json) {
        try {
            ErrorResponse error = new Gson().fromJson(json, ErrorResponse.class);

            if (error != null && error.message != null) {
                return new ResponseException(Code.ClientError, error.message);
            }

        }

        catch (Exception ignored) {
        }

        return new ResponseException(Code.ServerError, "Error: server error");
    }


    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400, 401, 403 -> Code.ClientError;
            default -> Code.ServerError;
        };
    }


}