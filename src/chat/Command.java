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
public class Command {

    private String name;
    private JSONObject jsonobject;
    private long queueNumber;
    private int ttl;

    public long getQueueNumber() {
        return queueNumber;
    }

    public Command(String name, JSONObject jsonobject, long queueNumber, int ttl) {
        this.name = name;
        this.jsonobject = jsonobject;
        this.queueNumber = queueNumber;
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
