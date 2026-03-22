package client;
import com.google.gson.Gson;
import results.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {
    String serverUrl;

    public ServerFacade(String serverUrl){
        this.serverUrl = serverUrl;
    }

    public RegisterResult register(String username, String password, String email) throws FacadeException {
        return null;
    }

    public LoginResult login(String username, String password) throws FacadeException{
        return null;
    }

    public void logout(String authToken) throws FacadeException {}

    public ListGamesResult listGame(String authToken) throws FacadeException {
        return null;
    }

    public CreateGameResult createGame(String authToken, String gameName) throws FacadeException {
        return null;
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws FacadeException{}

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
                    Map responseMap = new Gson().fromJson(reader, Map.class);
                    throw new FacadeException(responseMap.get("message").toString());
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
