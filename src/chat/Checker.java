package chat;

public class Checker implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
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
            } catch (Exception e) {
                Server.server.restartChecker();
            }
        }
    }
}
