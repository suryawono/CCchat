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
    public static final int CLIENT_LIST = 16;
    public static final int MESSAGE_LIST = 32;
    public static final int CLIENT_ACTIVITY_TIME = 64;
    public static final String[] COMMAND_NAME = new String[255];

    static {
        COMMAND_NAME[REGISTRASI_CLIENT] = "registerClient";
        COMMAND_NAME[LOGIN_CLIENT] = "loginClient";
        COMMAND_NAME[SEND_CHAT_CLIENT] = "sendClientChat";
        COMMAND_NAME[LOGOUT_CLIENT] = "logoutClient";
        COMMAND_NAME[CLIENT_LIST] = "getClientList";
        COMMAND_NAME[MESSAGE_LIST] = "getMessageList";
        COMMAND_NAME[CLIENT_ACTIVITY_TIME] = "updateCAT";
    }

    private int type;
    private JSONObject jsonobject;

    public SCommand(int type, JSONObject jsonobject) {
        this.type = type;
        this.jsonobject = jsonobject;
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

    public String seril(){
        return new JSONObject().put("type", type).put("data", this.jsonobject).toString();
    }
    
    @Override
    public String toString() {
        return this.jsonobject.toString();
    }
}
