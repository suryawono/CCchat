/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class Network implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
                SCommand sc = Server.server.scommandQueue.poll();
                if (sc != null) {
                    JSONObject body = new JSONObject();
                    body.put("command", SCommand.COMMAND_NAME[sc.getType()]);
                    body.put("params", sc.getJsonobject());
                    body.put("ttl", sc.getTtl());
                    this.send(body);
                }

            } catch (RuntimeException e) {
                Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
                Server.server.restartNetworkThread();
            } catch (Exception e) {
                Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void send(JSONObject body) {
        for (Map.Entry pair : Server.server.serverList.entrySet()) {
            JSONObject o = (JSONObject) pair.getValue();
            if (o.getString("sessionid").equals("")) {
            } else {
                HttpURLConnection connection = null;
                try {
                    System.out.println("Propate " + body.getString("command") + " to " + o.getString("ip") + ":" + o.getString("http_port"));
                    URL url = new URL("http", o.getString("ip"), Integer.parseInt(o.getString("http_port")), "/");

                    body.getJSONObject("params").put("ssessionid", o.getString("sessionid"));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type",
                            "application/json");
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.write(body.toString().getBytes());
                    wr.close();
                    //Get Response  
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                    rd.close();
                    JSONObject responsejson = new JSONObject(response.toString());
                } catch (Exception e) {
                    Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
            //register
        }

    }
}
