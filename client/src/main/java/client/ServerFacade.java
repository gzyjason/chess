package client;
import com.google.gson.Gson;
import request.CreateGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import results.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    String serverUrl;

    public ServerFacade(String serverUrl){
        this.serverUrl = serverUrl;
    }

    public RegisterResult register(String username, String password, String email) throws FacadeException {
        RegisterRequest request = new RegisterRequest(username, password, email);
        return httpRequests("POST", "/user", request, RegisterResult.class, null);
    }

    public LoginResult login(String username, String password) throws FacadeException{
        LoginRequest request = new LoginRequest(username, password);
        return httpRequests("POST", "/session", request, LoginResult.class, null);
    }

    public void logout(String authToken) throws FacadeException {
        httpRequests("DELETE", "/session", null, null, authToken);
    }

    public ListGamesResult listGame(String authToken) throws FacadeException {
        return httpRequests("GET", "/game", null, ListGamesResult.class, authToken);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws FacadeException {
        CreateGameRequest request = new CreateGameRequest(gameName);
        return httpRequests("POST", "/game", request, CreateGameResult.class, authToken);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws FacadeException{
        httpRequests("PUT", "/game", null, null, authToken);
    }

    public void clear() throws FacadeException{
        httpRequests("DELETE", "/db", null, null, null);
    }

    private <T> T httpRequests(String method, String path, Object requestData, Class<T> responseClass,
                               String headerToken) throws FacadeException {
        try {
            URI uri = new URI(serverUrl + path);
            URL url = uri.toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);

            if (headerToken != null) {
                http.addRequestProperty("authorization", headerToken);
            }

            if (requestData != null){
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                try (OutputStream output = http.getOutputStream()){
                    String json = new Gson().toJson(requestData);
                    output.write(json.getBytes());
                }
            }
            http.connect();

            int responseCode = http.getResponseCode();

            if (responseCode >= 300) {
                try (InputStream errors = http.getErrorStream()){
                    InputStreamReader reader = new InputStreamReader(errors);
                    ErrorResult errorResult = new Gson().fromJson(reader, ErrorResult.class);
                    throw new FacadeException(errorResult.getMessage());
                }
            }

            if (responseClass != null && responseClass != void.class) {
                try (InputStream inputStream = http.getInputStream()){
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    return new Gson().fromJson(reader, responseClass);
                }
            }

            return null;
        } catch (Exception ex) {
            throw new FacadeException(ex.getMessage());
        }



    }

}
