package chat;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    boolean isConnectedtoServer;
    int server_socket;

    public Client() {
        isConnectedtoServer = false;
    }

    void run() {
        try {
            Scanner sc = new Scanner(System.in);
            if (!isConnectedtoServer) {
                System.out.println("Insert server port");
                server_socket = sc.nextInt();
                requestSocket = new Socket("localhost", server_socket);
            }

            System.out.println("Connected to localhost in port" + server_socket);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            do {
                try {
                    message = (String) in.readObject();
                    System.out.println("server> " + message);
                    sendMessage("Hi server");
                    message = "test";
                    sendMessage(message);
                } catch (ClassNotFoundException classNot) {
                    System.err.println("data received in unknown format");
                }
            } while (!message.equals("logout"));
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Client client = new Client();
        client.run();
    }
}
