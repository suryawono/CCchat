/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

/**
 *
 * @author CIA
 */
public class ServerEntity {

    public String ip;
    public int http_port;
    public int socket_port;
    public int capacity;
    public int current;

    public ServerEntity(String ip, int http_port, int socket_port, int capacity, int current) {
        this.ip = ip;
        this.http_port = http_port;
        this.socket_port = socket_port;
        this.capacity = capacity;
        this.current = current;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getHttp_port() {
        return http_port;
    }

    public void setHttp_port(int http_port) {
        this.http_port = http_port;
    }

    public int getSocket_port() {
        return socket_port;
    }

    public void setSocket_port(int socket_port) {
        this.socket_port = socket_port;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

}
