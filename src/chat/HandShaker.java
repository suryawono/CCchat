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
public class HandShaker implements Runnable {

    private boolean isReady = false;
    private int sleep=10;

    @Override
    public void run() {
        while (true) {
            try {
                if (Server.server.serverList != null && !isReady) {
                    System.out.println("Server ready...");
                    this.isReady=true;
                    this.sleep=Server.auto_connect_interval;
                }
                for (Map.Entry pair : Server.server.serverList.entrySet()) {
                    JSONObject o = (JSONObject) pair.getValue();
                    //login
                    if (o.getString("sessionid").equals("")) {
                        JSONObject login_r = this.doLogin(o.getString("ip"), o.getString("http_port"), o.getString("password"));
                        if (login_r != null) {
                            o.put("sessionid", login_r.getJSONObject("data").getString("sessionid"));
                        }
                    } else {
                        this.doRegister(o.getString("sessionid"), o.getString("ip"), o.getString("http_port"), Server.ip, Integer.toString(Server.http_port), Integer.toString(Server.socket_port), Server.password);
                    }
                    //register
                }
            } catch (NullPointerException e) {
            }
            try {
                Thread.sleep(this.sleep);
            } catch (InterruptedException ex) {
                Logger.getLogger(HandShaker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public JSONObject doLogin(String ip, String port, String password) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http", ip, Integer.parseInt(port), "/");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");
            JSONObject body = new JSONObject();
            body.put("command", "loginServer");
            body.put("params", new JSONObject().put("ipid", ip + ":" + port).put("password", password));
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
            return responsejson;
        } catch (Exception ex) {
            System.out.println("Cannot connect to " + ip + ":" + port);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public JSONObject doRegister(String sessionid, String ip_target, String port_target, String ip, String http_port, String socket_port, String password) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http", ip_target, Integer.parseInt(port_target), "/");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");
            JSONObject body = new JSONObject();
            body.put("command", "registerServer");
            body.put("params", new JSONObject().put("ip", ip).put("password", password).put("http_port", http_port).put("socket_port", socket_port).put("sessionid", sessionid));
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
            System.out.println("Connected to " + ip_target + ":" + port_target);
            return responsejson;
        } catch (Exception ex) {
            System.out.println("Cannot connect to " + ip_target + ":" + port_target);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
