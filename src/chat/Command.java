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
    private String queueNumber;

    public String getQueueNumber() {
        return queueNumber;
    }

    public Command(String name, JSONObject jsonobject, String queueNumber) {
        this.name = name;
        this.jsonobject = jsonobject;
        this.queueNumber = queueNumber;
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

    public String seril(){
        JSONObject jo=new JSONObject().put("name", this.name).put("data", this.jsonobject).put("queueNumber", this.queueNumber);
        return jo.toString();
    }
    
    @Override
    public String toString() {
        return this.jsonobject.toString();
    }
}
