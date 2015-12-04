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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import mq.*;
import org.json.JSONArray;

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
    public static String server_filename;
    public ChatExecutor[] executor;
    public HTTPservice httpservice;
    public SocketService socketservice;
    public JSONObject registeredUser;
    public Message message;
    public User user;
    public Feedback feedback;
    public JSONObject config;
    public HashMap<String, String> connectedFrom;
    public Thread handshaker;
    public Thread network;
    public Thread autosave;
    public Thread socketServiceThread;
    public Thread checker;
    public Thread[] tExecutor;
    public JSONObject userstatus;

    //chat
    public Publisher topicChatPublisher;
    public Thread topicChatListener;
    public Publisher queueAuthPublisher;
    public Thread queueFeedbackListener;
    public Thread topicUserstatusListener;
    public Publisher topicServerPublisher;
    public HashMap<String, String> users;
    public Thread userRipple;

    //auth
    public HashMap<String, Thread> queueAuthListenerList;
    public HashMap<String, Publisher> queueFeedbackPublisherList;
    public Publisher topicUserstatusPublisher;
    public Thread topicServerListener;
    public Thread authExec;
    public Thread ripple;

    //registry
    public Registry registry;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Server.configuration_filename = args[0];
        String type = args[1];
        Server.server = new Server(1, type);
    }

    Server(int executorNum, String type) throws IOException {

        this.connectedFrom = new HashMap();
        this.scommandQueue = new LinkedList();
        this.commandQueue = new LinkedList();

        this.readConfiguration();

        this.feedback = new Feedback();

        this.executor = new ChatExecutor[executorNum];
        String ipid = Server.ip + ":" + Server.http_port;
        switch (type) {
            case "chat":
                this.users = new HashMap();
                this.message = new Message();

                this.topicChatPublisher = new Publisher("topic://chat");
                this.queueAuthPublisher = new Publisher("auth:" + ip + ":" + http_port);
                this.topicServerPublisher = new Publisher("topic://server");
                this.topicServerPublisher.add(new JSONObject().put("ip", Server.ip).put("http_port", Server.http_port).put("socket_port", Server.socket_port).toString());
                this.topicChatListener = new Thread(new Listener("topic://chat", ipid + "-chat") {

                    @Override
                    public void extractor(String s) {
                        JSONObject jo = new JSONObject(s);
                        Server.server.message.putMessage(jo.getString("username"), jo.getString("message"), jo.getLong("timestamp"));
                    }

                });
                this.topicChatListener.start();
                this.queueFeedbackListener = new Thread(new Listener("feedback:" + ip + ":" + http_port, ipid + "-feedback") {
                    @Override
                    public void extractor(String s) {
                        JSONObject jo = new JSONObject(s);
                        if (jo.getInt("status") == Feedback.LOGIN_BERHASIL) {
                            users.put(jo.getJSONObject("data").getString("sessionid"), jo.getJSONObject("data").getString("username"));
                        } else if (jo.getInt("status") == Feedback.LOGOUT_SUCCESS) {
                            users.remove(jo.getJSONObject("data").getString("sessionid"));
                        }
                        feedback.putResult(jo.getString("queueNumber"), jo);
                        System.out.println(users);
                    }
                });
                this.queueFeedbackListener.start();
                this.topicUserstatusListener = new Thread(new Listener("topic://userstatus", ipid + "-userstatus") {

                    @Override
                    public void extractor(String s) {
                        userstatus = new JSONObject(s);
                    }
                });
                this.topicUserstatusListener.start();

                InetSocketAddress addr = new InetSocketAddress(Server.http_port);
                HttpServer httpservice = HttpServer.create(addr, 0);

                httpservice.createContext("/", new HTTPservice());
                httpservice.setExecutor(Executors.newCachedThreadPool());
                httpservice.start();

                this.socketservice = new SocketService(socket_port);
                this.socketServiceThread = new Thread(this.socketservice);
                socketServiceThread.start();

                this.tExecutor = new Thread[this.executor.length];
                for (int i = 0; i < this.executor.length; i++) {
                    this.executor[i] = new ChatExecutor();
                    tExecutor[i] = new Thread(this.executor[i]);
                    tExecutor[i].start();
                }
                this.userRipple = new Thread(new UserRipple());
                this.userRipple.start();
                
                System.out.println("Server is listening on port " + Server.http_port);
                break;
            case "auth":
                this.user = new User();
                this.queueAuthListenerList = new HashMap();
                this.queueFeedbackPublisherList = new HashMap();
                this.topicServerListener = new Thread(new Listener("topic://server", ipid + "-server") {

                    @Override
                    public void extractor(String s) {
                        JSONObject jo = new JSONObject(s);
                        String ip = jo.getString("ip");
                        int http_port = jo.getInt("http_port");
                        queueAuthListenerList.put(ip + ":" + http_port, new Thread(new Listener("auth:" + ip + ":" + http_port, "auth:" + ip + ":" + http_port + "-auth") {
                            @Override
                            public void extractor(String s) {
                                System.out.println(s);
                                JSONObject jo = new JSONObject(s);
                                jo.getJSONObject("data").put("ipid", ip + ":" + http_port);
                                commandQueue.add(new Command(jo.getString("command"), jo.getJSONObject("data"), jo.getString("queueNumber")));
                            }
                        }));
                        queueAuthListenerList.get(ip + ":" + http_port).start();
                        queueFeedbackPublisherList.put(ip + ":" + http_port, new Publisher("feedback:" + ip + ":" + http_port));
                    }
                });
                this.topicServerListener.start();
                this.topicUserstatusPublisher = new Publisher("topic://userstatus");
                authExec = new Thread(new AuthExecutor());
                authExec.start();
                ripple = new Thread(new AuthRipple());
                ripple.start();
                break;
            case "registry":
                break;
        }

        this.autosave = new Thread(new Autosave(type));
        autosave.start();
    }

    private void readConfiguration() throws FileNotFoundException, IOException {
        FileReader fileReader = new FileReader(Server.configuration_filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
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
        Server.server_filename = this.config.getString("server_filename");
        Server.ip = this.config.getString("ip");
        Server.ttl = Integer.parseInt(this.config.getString("ttl"));
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

    public static String generateTicketId() {
        String id = ip + ":" + http_port + ":";
        Date d = new Date();
        long t = d.getTime();
        return id + Long.toString(t) + "-" + Server.random4c();
    }

    public static String random4c() {
        Random r = new Random();

        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = "";
        for (int i = 0; i < 50; i++) {
            result += alphabet.charAt(r.nextInt(alphabet.length()));
        }
        return result;
    }
}
