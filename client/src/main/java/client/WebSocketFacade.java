package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private final Session session;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws FacadeException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage serverMessage = deserialize(message);
                observer.notify(serverMessage);
            });
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new FacadeException(500, e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    private ServerMessage deserialize(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);
        return switch (base.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(json, LoadGameMessage.class);
            case ERROR -> gson.fromJson(json, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(json, NotificationMessage.class);
        };
    }

    public void sendCommand(UserGameCommand command) throws FacadeException {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new FacadeException(500, e.getMessage());
        }
    }

    public void close() throws FacadeException {
        try {
            if (this.session != null && this.session.isOpen()) {
                this.session.close();
            }
        } catch (IOException e) {
            throw new FacadeException(500, e.getMessage());
        }
    }
}