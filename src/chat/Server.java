package chat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class Server {

    public static String configuration_filename;
    public volatile Queue<Command> commandQueue;
    public volatile Queue<SCommand> scommandQueue;
    public static Server server;
    public static int http_port;
    public static int socket_port;
    public static int ttl;
    public static int auto_save_interval;
    public static int auto_connect_interval;
    public static String ip;
    public static String password;
    public static String message_filename;
    public static String user_filename;
    public Executor[] executor;
    public HTTPservice httpservice;
    public JSONObject registeredUser;
    public Message message;
    public User user;
    public Feedback feedback;
    public HashMap<String, JSONObject> serverList;
    public JSONObject config;
    public HashMap<String, String> connectedFrom;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Server.configuration_filename = args[0];
        Server.server = new Server(1);

    }

    Server(int executorNum) throws IOException {
        this.serverList = new HashMap();
        this.connectedFrom = new HashMap();
        this.commandQueue = new LinkedList();
        this.scommandQueue = new LinkedList();

        this.readConfiguration();
        this.executor = new Executor[executorNum];
        this.registeredUser = new JSONObject();
        this.user = new User();
        this.message = new Message();
        this.feedback = new Feedback();

        InetSocketAddress addr = new InetSocketAddress(Server.http_port);
        HttpServer httpservice = HttpServer.create(addr, 0);

        httpservice.createContext("/", new HTTPservice());
        httpservice.setExecutor(Executors.newCachedThreadPool());
        httpservice.start();
        System.out.println("Server is listening on port " + Server.http_port);

        Thread handshaker = new Thread(new HandShaker());
        handshaker.start();

        for (int i = 0; i < this.executor.length; i++) {
            this.executor[i] = new Executor();
            Thread tExecutor = new Thread(this.executor[i]);
            tExecutor.start();
        }
        Thread network=new Thread(new Network());
        network.start();
        Thread autosave = new Thread(new Autosave());
        autosave.start();
    }

    private void readConfiguration() throws FileNotFoundException, IOException {
        FileReader fileReader = new FileReader(Server.configuration_filename);
        BufferedReader bufferedReader
                = new BufferedReader(fileReader);
        String jsonString = "";
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            jsonString += line;
        }
        fileReader.close();
        this.config = new JSONObject(jsonString);
        Server.http_port = Integer.parseInt(this.config.getString("http_port"));
        Server.socket_port = Integer.parseInt(this.config.getString("socket_port"));
        Server.auto_save_interval = Integer.parseInt(this.config.getString("auto_save_interval"));
        Server.auto_connect_interval = Integer.parseInt(this.config.getString("auto_connect_interval"));
        Server.password = this.config.getString("password");
        Server.message_filename = this.config.getString("message_filename");
        Server.user_filename = this.config.getString("user_filename");
        Server.ip = this.config.getString("ip");
        Server.ttl = Integer.parseInt(this.config.getString("ttl"));
        for (Object s : this.config.optJSONArray("linked_servers")) {
            JSONObject jo = new JSONObject(s.toString());
            this.serverList.put(jo.getString("ip") + ":" + jo.getString("http_port"), jo.put("sessionid", ""));
        }
    }

    public void registerServer(String ip, int http_port, int socket_port, String password) {
        if (!this.serverList.containsKey(ip + ":" + http_port)) {
            JSONObject s = new JSONObject().put("ip", ip).put("http_port", Integer.toString(http_port)).put("socket_port", Integer.toString(socket_port)).put("password", password).put("sessionid","");
            this.config.optJSONArray("linked_servers").put(s);
            this.serverList.put(s.getString("ip") + ":" + s.getString("http_port"), s);
        }
    }

    public String login(String ipID, String password) {
        if (password.equals(Server.password)) {
            final Random r = new SecureRandom();
            byte[] ssid = new byte[32];
            r.nextBytes(ssid);
            String encodedSSID = String.format("%064x", new java.math.BigInteger(1, ssid));
            this.connectedFrom.put(encodedSSID, ipID);
            return encodedSSID;
        }
        return null;
    }

    public String isValidSession(String sessionid) {
        return this.connectedFrom.get(sessionid);
    }

    private void writeDB() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(Server.configuration_filename);
        pw.write(this.config.toString(4));
        pw.close();
    }

    public void save() {
        try {
            this.writeDB();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String logout(String sessionId) {
        return this.connectedFrom.remove(sessionId);
    }
}
