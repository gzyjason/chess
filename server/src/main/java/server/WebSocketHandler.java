package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;

public class WebSocketHandler {
    private final Gson gson = new Gson();

    public void onConnect(WsConnectContext ctx){

    }

    public void onMessage (WsMessageContext ctx){

    }

    public void onClose(WsCloseContext ctx){

    }
}
