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
public class RegistryExecutor implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
                Command c = Server.server.commandQueue.poll();
                JSONObject result = new JSONObject();
                if (c != null) {
                    System.out.println(c);
                    try {
                        switch (c.getName()) {
                            case "registerServer":
//                                JSONObject register_r = this.register(c.getJsonobject().getString("username"), c.getJsonobject().getString("password"));
//                                if (register_r != null) {
//                                    result.put("status", Feedback.PENDAFTARAN_BERHASIL);
//                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.PENDAFTARAN_BERHASIL]);
//                                    result.put("data", new JSONObject().put("username", c.getJsonobject().getString("username")));
////                                    this.addSCommand(SCommand.REGISTRASI_CLIENT, register_r, Server.ttl);
//                                } else {
//                                    result.put("status", Feedback.USERNAME_TERDAFTAR);
//                                    result.put("message", Feedback.STATUS_MESSAGE[Feedback.USERNAME_TERDAFTAR]);
//                                    result.put("data", new JSONObject().put("commandrequest", c.getName()));
//                                }
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
//                    Server.server.topicFeedbackPublisher.add(new JSONObject().put("queueNumber", c.getQueueNumber()).put("data", result).toString());
//                    Server.server.queueFeedbackPublisherList.get(c.getJsonobject().getString("ipid")).add(result.put("queueNumber", c.getQueueNumber()).toString());
                }
            } catch (InterruptedException | NullPointerException e) {
                Logger.getLogger(RegistryExecutor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

}
