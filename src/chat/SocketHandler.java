package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import org.json.JSONObject;

public class SocketHandler extends Thread {

    Socket clientSocket;
    String message;
    String jsonMessage;
    OutputStream out;
    InputStream in;
    public BufferedWriter writer;
    int code = 200;
    String sessionid;
    SocketService socketService;

    SocketHandler(Socket clientSocket, SocketService socketService) {
        super("SocketHandler");
        this.clientSocket = clientSocket;
        this.socketService = socketService;
    }

    public void run() {
        try {
            System.out.println("Client Connected!");
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("welcome, press enter to begin...");
            writer.flush();
            reader.readLine();
            do {
                message = reader.readLine();
                JSONObject r = new JSONObject();
                JSONObject jsonObject = toJSON(message);
                long q;
                ArrayList<String> commandList = new ArrayList();
                for (String s : SCommand.COMMAND_NAME) {
                    commandList.add(s);
                }
                commandList.add("sendMessage");
                commandList.add("register");
                commandList.add("login");
                commandList.add("getMessage");
                commandList.add("getOnlineList");
                commandList.add("logout");
                commandList.add("registerServer");
                commandList.add("loginServer");
                commandList.add("logoutServer");
                try {
                    if (commandList.contains(jsonObject.get("command").toString())) {
                        q = Server.server.feedback.getQueueNumber();
                        int ttl = jsonObject.has("ttl") ? jsonObject.getInt("ttl") : 0;
                        Server.server.commandQueue.add(new Command(jsonObject.get("command").toString(), (JSONObject) jsonObject.get("params"), q, ttl));
                        while ((r = Server.server.feedback.getResponse(q)) == null) {
                            Thread.sleep(10);
                        }
                    } else if (jsonObject.get("command").toString().equals("ping")) {
                        r.put("status", "alive");
                    }

                    if (jsonObject.get("command").toString().equals("login")) {
                        if (this.sessionid != null) {
                            writer.write("Anda sudah login, silahkan logout terlebih dahulu \r\n");
                        } else {
                            writer.write(r.get("message").toString() + "\r\n");
                            if (r.getJSONObject("data").get("sessionid") != null) {
                                this.sessionid = r.getJSONObject("data").get("sessionid").toString();
                                writer.write("WELCOME " + r.getJSONObject("data").get("username") + "\r\n");
                            }
                        }
                        writer.flush();
                    }

                    if (jsonObject.get("command").toString().equals("sendMessage")) {
                        String text = r.getJSONObject("data").get("username").toString() + ">" + r.getJSONObject("data").get("message").toString();
                        socketService.sendToAll(text);
                    }

                    if (jsonObject.get("command").toString().equals("getOnlineList")) {
                        writer.write("User(s) currently online: \r\n");
                        for (int i = 0; i < r.getJSONObject("data").getJSONArray("users").length(); i++) {
                            writer.write(r.getJSONObject("data").getJSONArray("users").optJSONObject(i).get("username").toString() + "\r\n");
                        }
                        writer.flush();
                    }

                    if (jsonObject.get("command").toString().equals("getMessage")) {
                        writer.write(r.toString() + "\r\n");
                        writer.flush();
                        for (int i = 0; i < r.getJSONObject("data").getJSONArray("messages").length(); i++) {
                            String text = r.getJSONObject("data").getJSONArray("messages").optJSONObject(i).get("username").toString() + ">" + r.getJSONObject("data").getJSONArray("messages").optJSONObject(i).get("message").toString() + "\r\n";
                            writer.write(text);
                        }
                        writer.flush();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } while (!message.equals("logout"));
            in.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private JSONObject toJSON(String message) {
        String[] temp = message.split(" ");
        StringBuilder build = new StringBuilder();
        build.append("{ ");
        switch (temp[0]) {
            case "register":
                build.append("\"command\" : \"register\",");
                build.append("\"params\" : {");
                build.append("\"username\": ");
                build.append("\"" + temp[1] + "\",");
                build.append("\"password\": ");
                build.append("\"" + temp[2] + "\"}");
                break;
            case "login":
                build.append("\"command\" : \"login\",");
                build.append("\"params\" : {");
                build.append("\"username\": ");
                build.append("\"" + temp[1] + "\",");
                build.append("\"password\": ");
                build.append("\"" + temp[2] + "\"}");
                break;
            case "sendMessage":
                build.append("\"command\" : \"sendMessage\",");
                build.append("\"params\" : {");
                build.append("\"sessionid\": ");
                build.append("\"" + sessionid + "\",");
                build.append("\"message\": ");
                build.append("\"" + temp[1] + "\"}");
                break;
            case "getOnlineList":
                build.append("\"command\" : \"getOnlineList\",");
                build.append("\"params\" : {");
                build.append("\"sessionid\": ");
                build.append("\"" + sessionid + "\"}");
                break;
            case "logout":
                build.append("\"command\" : \"logout\",");
                build.append("\"params\" : {");
                build.append("\"sessionid\": ");
                build.append("\"" + sessionid + "\"}");
                break;
            case "getMessage":
                build.append("\"command\" : \"getMessage\",");
                build.append("\"params\" : {");
                build.append("\"sessionid\": ");
                build.append("\"" + sessionid + "\",");
                build.append("\"timestamp\": ");
                build.append("\"" + temp[1] + "\"}");
                break;
        }
        build.append(" }");
        return new JSONObject(build.toString());
    }
}
