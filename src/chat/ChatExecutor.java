/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class ChatExecutor implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
                Command c = Server.server.commandQueue.poll();
                JSONObject result = new JSONObject();
                if (c != null) {
                    try {
                        switch (c.getName()) {
                            case "register":
                                this.register(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"), c.getQueueNumber());
                                break;
                            case "login":
                                this.login(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"), c.getQueueNumber());
                                break;
                            case "sendMessage":
                                JSONObject sendmessage_r = this.sendMessage(c.getJsonobject().getString("sessionid"), c.getJsonobject().getString("message"));
                                if (sendmessage_r != null) {
                                    result.put("status", Feedback.MESSAGE_SENT);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.MESSAGE_SENT]);
                                    result.put("data", sendmessage_r);
//                                    this.addSCommand(SCommand.SEND_CHAT_CLIENT, sendmessage_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                }
                                Server.server.feedback.putResult(c.getQueueNumber(), result);
                                break;
                            case "getOnlineList":
                                JSONObject user_list = this.getOnlineList(c.getJsonobject().getString("sessionid"));
                                if (user_list != null) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", user_list);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                }
                                Server.server.feedback.putResult(c.getQueueNumber(), result);
                                break;
                            case "logout":
                                JSONObject logout_r;
                                this.logout(c.getJsonobject().getString("sessionid"), c.getQueueNumber());
                                break;
                            case "getMessage":
                                JSONArray messages;
                                if ((messages = this.getMessage(c.getJsonobject().getString("sessionid"), Long.parseLong(c.getJsonobject().getString("timestamp")))) != null) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject().put("messages", messages));
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                }
                                Server.server.feedback.putResult(c.getQueueNumber(), result);
                                break;
                        }
                    } catch (JSONException j) {
                        String e = j.toString();
                        String m = e.substring(e.indexOf((int) '\"') + 1, e.indexOf((int) '\"', e.indexOf((int) '\"') + 1));
                        result.put("status", Feedback.INVALID_PARAMS);
                        result.put("message", Feedback.STATUS_MESSAGE[Feedback.INVALID_PARAMS]);
                        result.put("data", new JSONObject().put("missing", m));
                        Server.server.feedback.putResult(c.getQueueNumber(), result);
                    }
                }
            } catch (InterruptedException | NullPointerException e) {
                Logger.getLogger(ChatExecutor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void register(String username, String password, String queueNumber) {
        Server.server.queueAuthPublisher.add(new JSONObject().put("command", "register").put("data", new JSONObject().put("username", username).put("password", password)).put("queueNumber", queueNumber).toString());
    }

    public String hashPassword(String password) {
        return password;
    }

    public void login(String username, String password, String queueNumber) {
        Server.server.queueAuthPublisher.add(new JSONObject().put("command", "login").put("data", new JSONObject().put("username", username).put("password", password)).put("queueNumber", queueNumber).toString());
    }

    public JSONObject sendMessage(String sessionid, String message) {
        String username = Server.server.users.get(sessionid);
        Date date = new Date();
        long timestamp = date.getTime();
        JSONObject msgs = new JSONObject().put("username", username).put("message", message).put("timestamp", timestamp);
        if (username == null) {
            return null;
        } else {
            Server.server.topicChatPublisher.add(msgs.toString());
//            return Server.server.message.putMessage(username, message);
            return msgs;
        }
    }

    public void logout(String sessionid, String queueNumber) {
        Server.server.queueAuthPublisher.add(new JSONObject().put("command", "logout").put("data", new JSONObject().put("sessionid", sessionid)).put("queueNumber", queueNumber).toString());
    }

    public JSONArray getMessage(String sessionid, long timestamp) {
        String username = Server.server.users.get(sessionid);
        if (username == null) {
            return null;
        } else {
            return Server.server.message.getMessage(timestamp);
        }
    }

    public JSONObject getOnlineList(String sessionid) {
        return Server.server.userstatus;
    }

}
