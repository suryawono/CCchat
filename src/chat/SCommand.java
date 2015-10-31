/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class SCommand {

    public static final int REGISTRASI_CLIENT = 1;
    public static final int LOGIN_CLIENT = 2;
    public static final int SEND_CHAT_CLIENT = 4;
    public static final int LOGOUT_CLIENT = 8;
    public static final String[] COMMAND_NAME = new String[64];

    static {
        COMMAND_NAME[REGISTRASI_CLIENT] = "registerClient";
        COMMAND_NAME[LOGIN_CLIENT] = "loginClient";
        COMMAND_NAME[SEND_CHAT_CLIENT] = "sendClientChat";
        COMMAND_NAME[LOGOUT_CLIENT] = "logoutClient";
    }

    private int type;
    private JSONObject jsonobject;
    private int ttl;

    public SCommand(int type, JSONObject jsonobject, int ttl) {
        this.type = type;
        this.jsonobject = jsonobject;
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getType() {
        return type;
    }

    public JSONObject getJsonobject() {
        return jsonobject;
    }

    public void setJsonobject(JSONObject jsonobject) {
        this.jsonobject = jsonobject;
    }

    @Override
    public String toString() {
        return this.jsonobject.toString();
    }
}
