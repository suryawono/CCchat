package chat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class HTTPservice implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        OutputStream responseBody = exchange.getResponseBody();
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "text/json");
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        int code = 200;
        JSONObject r = new JSONObject();
        if (requestMethod.equalsIgnoreCase("POST")) {
            InputStream is = exchange.getRequestBody();
            String requestData = "";
            int n;
            while ((n = is.read()) != -1) {
                requestData += (char) n;
            }
            JSONObject jsonObject = new JSONObject(requestData);
            long q;
            ArrayList<String> commandList = new ArrayList();
            for (String s : SCommand.COMMAND_NAME) {
                commandList.add(s);
            }
            commandList.add("sendMessage");
            commandList.add("register");
            commandList.add("login");
            commandList.add("getMessage");
            commandList.add("getOnlineList");
            commandList.add("logout");
            commandList.add("registerServer");
            commandList.add("loginServer");
            commandList.add("logoutServer");
            try {
                if (commandList.contains(jsonObject.get("command").toString())) {
                    q = Server.server.feedback.getQueueNumber();
                    int ttl = jsonObject.has("ttl") ? jsonObject.getInt("ttl") : 0;
                    Server.server.commandQueue.add(new Command(jsonObject.get("command").toString(), (JSONObject) jsonObject.get("params"), q, ttl));
                    while ((r = Server.server.feedback.getResponse(q)) == null) {
                        Thread.sleep(10);
                    }
                } else if (jsonObject.get("command").toString().equals("ping")) {
                    r.put("status", "alive");
                } else {
                    code = 400;
                }
            } catch (JSONException j) {
                code = 400;
            } catch (InterruptedException ex) {
                code = 408;
                Logger.getLogger(HTTPservice.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            code = 405;
        }
        exchange.sendResponseHeaders(code, 0);
        responseBody.write(r.toString().getBytes());
        responseBody.close();
    }

}
