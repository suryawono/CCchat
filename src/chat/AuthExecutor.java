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
public class AuthExecutor implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
                Command c = Server.server.commandQueue.poll();
                JSONObject result = new JSONObject();
                if (c != null) {
                    boolean skip = false;
                    try {
                        switch (c.getName()) {
                            case "register":
                                JSONObject register_r = this.register(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"));
                                if (register_r != null) {
                                    result.put("status", Feedback.PENDAFTARAN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.PENDAFTARAN_BERHASIL]);
                                    result.put("data", new JSONObject().put("username", c.getJsonobject().getString("username")));
//                                    this.addSCommand(SCommand.REGISTRASI_CLIENT, register_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.USERNAME_TERDAFTAR);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.USERNAME_TERDAFTAR]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                }
                                break;
                            case "login":
                                JSONObject login_r = this.login(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"));
                                if (login_r == null) {
                                    result.put("status", Feedback.LOGIN_GAGAL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_GAGAL]);
                                    result.put("data", new JSONObject());
                                } else if (login_r.has("status")) {
                                    result.put("status", Feedback.SUDAH_LOGIN);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.SUDAH_LOGIN]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                } else {
                                    result.put("status", Feedback.LOGIN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_BERHASIL]);
                                    result.put("data", login_r);
//                                    this.addSCommand(SCommand.LOGIN_CLIENT, login_r, Server.ttl);
                                }
                                break;
                            case "logout":
                                JSONObject logout_r;
                                if ((logout_r = this.logout(c.getJsonobject().getString("sessionid"))) != null) {
                                    result.put("status", Feedback.LOGOUT_SUCCESS);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGOUT_SUCCESS]);
                                    result.put("data", logout_r);
//                                    this.addSCommand(SCommand.LOGOUT_CLIENT, logout_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
                                }
                                break;
                            case "updatelat":
                                skip = true;
                                this.updateCAT(c.getJsonobject().getJSONArray("users"), c.getJsonobject().getLong("lat"));
                                result.put("status", Feedback.OK);
                                result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                result.put("data", new JSONObject());
                                break;
                        }
                    } catch (JSONException j) {
                        String e = j.toString();
                        String m = e.substring(e.indexOf((int) '\"') + 1, e.indexOf((int) '\"', e.indexOf((int) '\"') + 1));
                        result.put("status", Feedback.INVALID_PARAMS);
                        result.put("message", Feedback.STATUS_MESSAGE[Feedback.INVALID_PARAMS]);
                        result.put("data", new JSONObject().put("missing", m));
                    }
//                    Server.server.topicFeedbackPublisher.add(new JSONObject().put("queueNumber", c.getQueueNumber()).put("data", result).toString());
                    if (!skip) {
                        Server.server.queueFeedbackPublisherList.get(c.getJsonobject().getString("ipid")).add(result.put("queueNumber", c.getQueueNumber()).toString());
                    }
                }
            } catch (InterruptedException | NullPointerException e) {
                Logger.getLogger(AuthExecutor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public JSONObject register(String username, String password) {
        try {
            return Server.server.user.register(username, password);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(AuthExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String hashPassword(String password) {
        return password;
    }

    public JSONObject login(String username, String password) {
        try {
            return Server.server.user.login(username, password);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(AuthExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JSONObject logout(String sessionid) {
        return Server.server.user.logout(sessionid);
    }

    public JSONObject getOnlineList() {
        return Server.server.user.getOnlineList();
    }

    public void updateCAT(JSONArray users, long lat) {
        System.out.println("update2");
        for (Object o : users) {
            JSONObject jo = (JSONObject) o;
            Server.server.user.updateCAT(jo.getString("username"), lat);
        }
    }
}
