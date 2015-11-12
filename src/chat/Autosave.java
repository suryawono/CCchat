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
public class Autosave implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Server.auto_save_interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(Autosave.class.getName()).log(Level.SEVERE, null, ex);
            }
            Server.server.user.save();
            Server.server.message.save();
            Server.server.save();
            System.out.println("Save!!!");
        }
    }

}
