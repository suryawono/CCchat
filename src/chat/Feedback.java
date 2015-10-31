/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author CIA
 */
public class Feedback {

    public static int USERNAME_TERDAFTAR = 1;
    public static int PENDAFTARAN_BERHASIL = 2;
    public static int LOGIN_BERHASIL = 3;
    public static int LOGIN_GAGAL = 4;
    public static int MESSAGE_SENT = 5;
    public static int NEED_AUTH = 6;
    public static int LOGOUT_SUCCESS = 7;
    public static int INVALID_PARAMS = 66;
    public static int OK = 10;

    public static String[] STATUS_MESSAGE = new String[150];

    static {
        STATUS_MESSAGE[USERNAME_TERDAFTAR] = "Username telah terdaftar";
        STATUS_MESSAGE[PENDAFTARAN_BERHASIL] = "Pendaftaran berhasil";
        STATUS_MESSAGE[LOGIN_BERHASIL] = "Login berhasil";
        STATUS_MESSAGE[LOGIN_GAGAL] = "Login gagal";
        STATUS_MESSAGE[MESSAGE_SENT] = "Pesan dikirim";
        STATUS_MESSAGE[NEED_AUTH] = "Membutuhkan autentikasi";
        STATUS_MESSAGE[INVALID_PARAMS] = "Parameter salah";
        STATUS_MESSAGE[LOGOUT_SUCCESS] = "Logout berhasil";
        STATUS_MESSAGE[OK] = "OK";
    }

    public volatile long currentQ;
    public HashMap<Long, JSONObject> feedbacks;

    public Feedback() {
        this.currentQ = 1;
        this.feedbacks = new HashMap();
    }

    public synchronized long getQueueNumber() {
        return this.currentQ++;
    }

    public JSONObject getResponse(long queueNumber) {
        return this.feedbacks.get(queueNumber);
    }

    public void putResult(long queueNumber, JSONObject o) {
        this.feedbacks.put(queueNumber, o);
    }

}
