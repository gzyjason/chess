package client;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class  WebSocketFacade extends Endpoint  {

    private final  Session session;
    private final Gson gson =  new Gson ();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws FacadeException {
        try {
            url = url.replace("http",  "ws");
            URI socketURI  = new URI(url +  "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>()  {


                @Override

                public void onMessage(String message) {

                    try {
                        ServerMessage serverMessage = deserialize(message);
                            observer.notify(serverMessage);
                    } catch (Exception exception) {
                        System.out.println("WebSocket Error: " + exception.getMessage());
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException exception) {
            throw new FacadeException(500, exception.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig  endpointConfig) {
    }
    private ServerMessage deserialize (String json) {
        JsonObject jsonObject =JsonParser.parseString(json).getAsJsonObject();
        String typeString = jsonObject.get("serverMessageType").getAsString();
        ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(typeString);
        return switch (type) {
            case LOAD_GAME -> {
                JsonObject gameObject = jsonObject.getAsJsonObject("game");
                if (gameObject != null && gameObject.has("game")) {
                    jsonObject.add("game", gameObject.get("game"));
                }
                yield gson.fromJson(jsonObject, LoadGameMessage.class);
            }



            case ERROR -> gson.fromJson(json, ErrorMessage.class);

            case NOTIFICATION -> gson.fromJson(json, NotificationMessage.class);
        };
    }

    public void sendCommand(UserGameCommand command ) throws  FacadeException {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException exception) {
            throw new FacadeException(500, exception.getMessage());
        }
    }

    public void close( ) throws FacadeException {

            try {
            if (this.session != null && this.session.isOpen()) {
                this.session.close();
            }


        } catch (IOException exception) {
            throw new FacadeException(500, exception.getMessage());
        }
    }
}