package chat;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Checker implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                if (!Server.server.network.isAlive()) {
                    Thread temp = new Thread(new Network());
                    temp.start();
                    Server.server.network = temp;
                }
                if (!Server.server.autosave.isAlive()) {
                    Thread temp = new Thread(new Autosave());
                    temp.start();
                    Server.server.autosave = temp;
                }
                if (!Server.server.network.isAlive()) {
                    Thread temp = new Thread(new Network());
                    temp.start();
                    Server.server.network = temp;
                }
                if (!Server.server.socketServiceThread.isAlive()) {
                    Thread temp = new Thread(new SocketService(Server.server.socketservice.socket_port));
                    temp.start();
                    Server.server.socketServiceThread = temp;
                }
                if (!Server.server.handshaker.isAlive()) {
                    Thread temp = new Thread(new HandShaker());
                    temp.start();
                    Server.server.handshaker = temp;
                }
                for (int i = 0; i < Server.server.tExecutor.length; i++) {
                    if (!Server.server.tExecutor[i].isAlive()) {
                        Thread temp = new Thread(Server.server.executor[i]);
                        temp.start();
                        Server.server.tExecutor[i] = temp;
                    }

                }
            } catch (RuntimeException e) {
                Server.server.restartChecker();
                Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
            } catch (Exception e) {
                Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
