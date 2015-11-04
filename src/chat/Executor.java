/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class Executor implements Runnable {

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
                                JSONObject register_r = this.register(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"));
                                if (register_r != null) {
                                    result.put("status", Feedback.PENDAFTARAN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.PENDAFTARAN_BERHASIL]);
                                    result.put("data", new JSONObject().put("username", c.getJsonobject().getString("username")));
                                    this.addSCommand(SCommand.REGISTRASI_CLIENT, register_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.USERNAME_TERDAFTAR);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.USERNAME_TERDAFTAR]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "login":
                                JSONObject login_r = this.login(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"));
                                if (login_r == null) {
                                    result.put("status", Feedback.LOGIN_GAGAL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_GAGAL]);
                                    result.put("data", new JSONObject());
                                } else {
                                    result.put("status", Feedback.LOGIN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_BERHASIL]);
                                    result.put("data", login_r);
                                    this.addSCommand(SCommand.LOGIN_CLIENT, login_r, Server.ttl);
                                }
                                break;
                            case "sendMessage":
                                JSONObject sendmessage_r = this.sendMessage(c.getJsonobject().getString("sessionid"), c.getJsonobject().getString("message"));
                                if (sendmessage_r != null) {
                                    result.put("status", Feedback.MESSAGE_SENT);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.MESSAGE_SENT]);
                                    result.put("data", sendmessage_r);
                                    this.addSCommand(SCommand.SEND_CHAT_CLIENT, sendmessage_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "logout":
                                JSONObject logout_r;
                                if ((logout_r = this.logout(c.getJsonobject().getString("sessionid"))) != null) {
                                    result.put("status", Feedback.LOGOUT_SUCCESS);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGOUT_SUCCESS]);
                                    result.put("data", logout_r);
                                    this.addSCommand(SCommand.LOGOUT_CLIENT, logout_r, Server.ttl);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
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
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "registerServer":
                                if (this.registerServer(c.getJsonobject().getString("sessionid"), c.getJsonobject().getString("ip"), Integer.parseInt(c.getJsonobject().getString("http_port")), Integer.parseInt(c.getJsonobject().getString("socket_port")), c.getJsonobject().getString("password"))) {
                                    result.put("status", Feedback.PENDAFTARAN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.PENDAFTARAN_BERHASIL]);
                                    result.put("data", new JSONObject());
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "loginServer":
                                String ssid2 = this.loginServer(c.getJsonobject().getString("ipid"), c.getJsonobject().getString("password"));
                                if (ssid2 != null) {
                                    result.put("status", Feedback.LOGIN_BERHASIL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_BERHASIL]);
                                    result.put("data", new JSONObject().put("sessionid", ssid2));
                                } else {
                                    result.put("status", Feedback.LOGIN_GAGAL);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGIN_GAGAL]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "logoutServer":
                                String ipid;
                                if ((ipid = this.logoutServer(c.getJsonobject().getString("sessionid"))) != null) {
                                    result.put("status", Feedback.LOGOUT_SUCCESS);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.LOGOUT_SUCCESS]);
                                    result.put("data", new JSONObject().put("ipid", ipid));
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "registerClient":
                                if (this.registerClient(c.getJsonobject().getString("ssessionid"), c.getJsonobject().getString("username"), c.getJsonobject().getString("password"), c.getJsonobject().getString("salt"), Long.parseLong(c.getJsonobject().getString("register_time")))) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject());
                                    this.addSCommand(SCommand.REGISTRASI_CLIENT, c.getJsonobject(), c.getTtl() - 1);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "loginClient":
                                if (this.loginClient(c.getJsonobject().getString("ssessionid"), c.getJsonobject().getString("username"), c.getJsonobject().getString("sessionid"), Long.parseLong(c.getJsonobject().getString("login_time")))) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject());
                                    this.addSCommand(SCommand.LOGIN_CLIENT, c.getJsonobject(), c.getTtl() - 1);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "logoutClient":
                                if (this.logoutClient(c.getJsonobject().getString("ssessionid"), c.getJsonobject().getString("username"))) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject());
                                    this.addSCommand(SCommand.LOGOUT_CLIENT, c.getJsonobject(), c.getTtl() - 1);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "sendClientChat":
                                if (this.sendClientChat(c.getJsonobject().getString("ssessionid"), c.getJsonobject().getString("username"), c.getJsonobject().getString("message"), c.getJsonobject().getLong("timestamp"))) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject());
                                    this.addSCommand(SCommand.SEND_CHAT_CLIENT, c.getJsonobject(), c.getTtl() - 1);
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                            case "getClientList":
                                JSONArray clients;
                                if ((clients = this.getClientList()) != null) {
                                    result.put("status", Feedback.OK);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.OK]);
                                    result.put("data", new JSONObject().put("clients", clients));
                                } else {
                                    result.put("status", Feedback.NEED_AUTH);
                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.NEED_AUTH]);
                                    result.put("data", new JSONObject());
                                }
                                break;
                        }
                    } catch (JSONException j) {
                        String e = j.toString();
                        String m = e.substring(e.indexOf((int) '\"') + 1, e.indexOf((int) '\"', e.indexOf((int) '\"') + 1));
                        result.put("status", Feedback.INVALID_PARAMS);
                        result.put("message", Feedback.STATUS_MESSAGE[Feedback.INVALID_PARAMS]);
                        result.put("data", new JSONObject().put("missing", m));
                    }
                    Server.server.feedback.putResult(c.getQueueNumber(), result);
                }
            } catch (InterruptedException | NullPointerException e) {
                Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public JSONObject register(String username, String password) {
        try {
            return Server.server.user.register(username, password);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JSONObject sendMessage(String sessionid, String message) {
        String username = Server.server.user.isValidSessionid(sessionid);
        if (username == null) {
            return null;
        } else {
            return Server.server.message.putMessage(username, message);
        }
    }

    public JSONObject logout(String sessionid) {
        return Server.server.user.logout(sessionid);
    }

    public JSONArray getMessage(String sessionid, long timestamp) {
        String username = Server.server.user.isValidSessionid(sessionid);
        if (username == null) {
            return null;
        } else {
            return Server.server.message.getMessage(timestamp);
        }
    }

    public boolean registerServer(String sessionid, String ip, int http_port, int socket_port, String password) {
        if (Server.server.isValidSession(sessionid) != null) {
            Server.server.registerServer(ip, http_port, socket_port, password);
            return true;
        } else {
            return false;
        }
    }

    public String loginServer(String ipID, String password) {
        return Server.server.login(ipID, password);
    }

    public String logoutServer(String sessionid) {
        return Server.server.logout(sessionid);
    }

    public boolean registerClient(String sessionid, String username, String hashedPassword, String salt, long registeredTime) {
        if (Server.server.isValidSession(sessionid) != null) {
            Server.server.user.appendUser(username, hashedPassword, salt, registeredTime);
            return true;
        } else {
            return false;
        }
    }

    public boolean loginClient(String sessionid, String username, String token, long loginTime) {
        if (Server.server.isValidSession(sessionid) != null) {
            Server.server.user.appendLogin(username, token, loginTime);
            return true;
        } else {
            return false;
        }
    }

    public boolean logoutClient(String sessionid, String username) {
        if (Server.server.isValidSession(sessionid) != null) {
            Server.server.user.appendLogout(username);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendClientChat(String sessionid, String username, String message, long timestamp) {
        if (Server.server.isValidSession(sessionid) != null) {
            Server.server.message.appendMessage(new MessageEntity(timestamp, username, message));
            return true;
        } else {
            return false;
        }
    }

    public void addSCommand(int type, JSONObject o, int ttl) {
        if (ttl > 0) {
            Server.server.scommandQueue.offer(new SCommand(type, o, ttl));
        }
    }

    public JSONArray getClientList() {
        return null;
    }
}
