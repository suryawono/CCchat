/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class UserRipple implements Runnable {

    @Override
    public void run() {
        while (true) {

            try {
                JSONArray users = new JSONArray();
                String queueNumber = Server.generateTicketId();
                for (Map.Entry<String, String> pair : Server.server.users.entrySet()) {
                    System.out.println(pair);
                    users.put(new JSONObject().put("username", pair.getValue()));
                }
                Date d = new Date();
                long lat = d.getTime();
                Server.server.queueAuthPublisher.add(new JSONObject().put("command", "updatelat").put("data", new JSONObject().put("users", users).put("lat", lat)).put("queueNumber", queueNumber).toString());
                Thread.sleep(5000);
            } catch (Exception ex) {
                Logger.getLogger(UserRipple.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
