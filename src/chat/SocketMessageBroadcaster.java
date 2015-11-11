/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Surya Wono
 */
public class SocketMessageBroadcaster implements Runnable {

    public String sessionid;
    public BufferedWriter writer;
    public long lastbroadcast;
    public int broacastDelay = 1000;

    public SocketMessageBroadcaster(String sessionid, BufferedWriter writer, long lastbroadcast) {
        this.sessionid = sessionid;
        this.writer = writer;
        this.lastbroadcast = lastbroadcast;
    }

    @Override
    public void run() {
        while (true) {
            try {
                long q = Server.server.feedback.getQueueNumber();
                JSONObject request = new JSONObject();
                request.put("sessionid", sessionid).put("timestamp", Long.toString(lastbroadcast));
                Server.server.commandQueue.add(new Command("getMessage", request, q, 0));
                JSONObject r;
                while ((r = Server.server.feedback.getResponse(q)) == null) {
                    Thread.sleep(10);
                }
                if (r.getInt("status") == 10) {
                    JSONArray messages = r.getJSONObject("data").optJSONArray("messages");
                    for (Object o : messages) {
                        JSONObject jo = (JSONObject) o;
                        Server.server.socketservice.sendToAll(jo.getString("username") + ">" + jo.getString("message"));
                    }
                }
                lastbroadcast = new Date().getTime();
                Thread.sleep(broacastDelay);
            } catch (InterruptedException ex) {
                Logger.getLogger(SocketMessageBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SocketMessageBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
