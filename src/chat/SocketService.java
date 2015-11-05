package chat;

import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketService implements Runnable {

    ServerSocket providerSocket;
    Socket connection = null;
    OutputStream out;
    InputStream in;
    String message;

    public SocketService(int socket_port) throws IOException {
        providerSocket = new ServerSocket(socket_port);
        connection = null;

    }

    @Override
    public void run() {
        while (true) {
            try {
                connection = providerSocket.accept();
                System.out.println("Client Connected!");
                out = connection.getOutputStream();
                in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                do{
                message = reader.readLine();
                System.out.println(message);
                }while(!message.equals("logout"));
                in.close();
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
