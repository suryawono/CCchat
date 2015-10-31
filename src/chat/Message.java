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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 *
 * @author CIA
 */
public class Message {

    private PriorityQueue<MessageEntity> messages;

    public Message() throws IOException {
        this.messages = new PriorityQueue();
        this.readDB();
    }

    private void readDB() throws FileNotFoundException, IOException {
        FileReader fileReader = null;
        File file = new File(Server.message_filename);
        if (file.exists()) {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader
                    = new BufferedReader(fileReader);
            String jsonString = "";
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString += line;
            }
            JSONArray ja = (JSONArray) new JSONObject(jsonString).optJSONArray("messages");
            for (Iterator<Object> it = ja.iterator(); it.hasNext();) {
                JSONObject o = (JSONObject) it.next();
                this.messages.offer(new MessageEntity(Long.parseLong(o.get("timestamp").toString()), o.getString("username"), o.getString("message")));
            }
        }
    }

    private void writeDB() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(Server.message_filename);
        new JSONObject().put("messages", this.messages).write(pw);
        pw.close();
    }

    public JSONObject putMessage(String username, String message) {
        Date date = new Date();
        long timestamp = date.getTime();
        this.messages.offer(new MessageEntity(timestamp, username, message));
        return new JSONObject().put("username", username).put("message", message).put("timestamp", timestamp);
    }

    @Override
    public String toString() {
        return this.messages.toString();
    }

    public void save() {
        try {
            this.writeDB();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONArray getMessage(long timestamp) {
        JSONArray messageList = new JSONArray();
        this.messages.stream().filter((me) -> (Long.compare(me.getTimestamp(), timestamp) > 0)).forEach((MessageEntity me) -> {
            messageList.put(new JSONObject().put("username", me.getUsername()).put("timestamp", Long.toString(me.getTimestamp())).put(("message"), me.getMessage()));
        });
        return messageList;
    }

    public void appendMessage(MessageEntity me) {
        for (MessageEntity e : this.messages) {
            if (me.isIdentic(e)) {
                return;
            }
        }
        this.messages.offer(me);
    }
}
