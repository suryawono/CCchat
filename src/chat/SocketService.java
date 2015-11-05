package chat;

import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketService implements Runnable {

    ServerSocket providerSocket;
    Socket connection = null;
    int socket_port;
     private ArrayList<SocketHandler> clientList;

    public SocketService(int socket_port) throws IOException {
        this.socket_port = socket_port;
        clientList = new ArrayList();
    }

    @Override
    public void run() {
        while (true) {
            boolean listeningSocket = true;
            try {
                providerSocket = new ServerSocket(socket_port);
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + socket_port);
            }

            connection = null;

            while (listeningSocket) {
                try {
                    Socket clientSocket = providerSocket.accept();
                    SocketHandler h = new SocketHandler(clientSocket);
                    clientList.add(h);
                    h.start();                    
                } catch (IOException ex) {
                    Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                providerSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
