/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CIA
 */
public class AuthRipple implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
                Server.server.topicUserstatusPublisher.add(Server.server.user.getOnlineList().toString());
            } catch (InterruptedException ex) {
                Logger.getLogger(AuthRipple.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
