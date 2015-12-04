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

    public String type;

    public Autosave(String type) {
        this.type = type;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Server.auto_save_interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(Autosave.class.getName()).log(Level.SEVERE, null, ex);
            }
            switch (type) {
                case "chat":
                    Server.server.message.save();
                    break;
                case "auth":
                    Server.server.user.save();
                    break;
            }
            System.out.println("Save!!!");
        }
    }

}
