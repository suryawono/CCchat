/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class User {

    private HashMap<String, JSONObject> users;

    public User() throws IOException {
        this.users = new HashMap();
        this.readDB();
    }

    private void readDB() throws FileNotFoundException, IOException {
        FileReader fileReader = null;
        File file = new File(Server.user_filename);
        if (file.exists()) {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader
                    = new BufferedReader(fileReader);
            String jsonString = "";
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString += line;
            }
            JSONObject jo = (JSONObject) new JSONObject(jsonString).get("users");
            for (String key : jo.keySet()) {
                this.users.put(key, jo.getJSONObject(key));
            }
        }
    }

    public JSONObject register(String username, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (!this.users.containsKey(username)) {
            Date date = new Date();
            long time = date.getTime();
            JSONObject o = new JSONObject();
            final Random r = new SecureRandom();
            byte[] salt = new byte[32];
            r.nextBytes(salt);
            String encodedSalt = String.format("%064x", new java.math.BigInteger(1, salt));
            o.put("register_time", Long.toString(time));
            o.put("password", this.hashPassword(password, encodedSalt));
            o.put("sessionid", "");
            o.put("login_time", "");
            o.put("salt", encodedSalt);
            this.users.put(username, o);
            return new JSONObject().put("username", username).put("password", this.hashPassword(password, encodedSalt)).put("salt", encodedSalt).put("register_time", Long.toString(time));
        }
        return null;
    }

    public JSONObject login(String username, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (this.users.containsKey(username)) {
            JSONObject o = this.users.get(username);
            String salt = o.getString("salt");
            String hashedPassword = o.getString("password");
            if (hashedPassword.equals(this.hashPassword(password, salt))) {
                final Random r = new SecureRandom();
                byte[] ssid = new byte[32];
                r.nextBytes(ssid);
                String encodedSSID = String.format("%064x", new java.math.BigInteger(1, ssid));
                this.users.get(username).put("sessionid", encodedSSID);
                Date date = new Date();
                long time = date.getTime();
                this.users.get(username).put("login_time", Long.toString(time));
                return new JSONObject().put("username", username).put("sessionid", encodedSSID).put("login_time", Long.toString(time));
            }
        }
        return null;
    }

    public String hashPassword(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((password + salt).getBytes("UTF-8"));
        return String.format("%064x", new java.math.BigInteger(1, md.digest()));
    }

    private void writeDB() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(Server.user_filename);
        new JSONObject().put("users", this.users).write(pw);
        pw.close();
    }

    public void save() {
        try {
            this.writeDB();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String isValidSessionid(String sessionid) {
        for (Map.Entry<String, JSONObject> entry : this.users.entrySet()) {
            if (entry.getValue().getString("sessionid").equals(sessionid)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public JSONObject logout(String sessionid) {
        String username = this.isValidSessionid(sessionid);
        if (username != null) {
            this.users.get(username).put("sessionid", "");
            return new JSONObject().put("username", username);
        } else {
            return null;
        }
    }

    public void appendUser(String username, String hashedPassword, String salt, long registeredTime) {
        if (!this.users.containsKey(username)) {
            JSONObject o = new JSONObject();
            o.put("register_time", Long.toString(registeredTime));
            o.put("password", hashedPassword);
            o.put("sessionid", "");
            o.put("login_time", "");
            o.put("salt", salt);
            this.users.put(username, o);
        }
    }

    public void appendLogin(String username, String token, long loginTime) {
        if (this.users.containsKey(username)) {
            this.users.get(username).put("sessionid", token);
            this.users.get(username).put("login_time", loginTime);
        }
    }

    public void appendLogout(String username) {
        if (this.users.containsKey(username)) {
            this.users.get(username).put("sessionid", "");
        }
    }

}
