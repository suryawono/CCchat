/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.util.Objects;

/**
 *
 * @author CIA
 */
public class MessageEntity implements Comparable<MessageEntity> {

    public long timestamp;
    public String username;
    public String message;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageEntity(long timestamp, String username, String message) {
        this.timestamp = timestamp;
        this.username = username;
        this.message = message;
    }

    @Override
    public int compareTo(MessageEntity o) {
        return Long.compare(this.timestamp, o.timestamp);
    }

    public boolean isIdentic(MessageEntity me) {
        return this.message.equals(me.getMessage()) && this.username.equals(me.getUsername()) && (Long.compare(this.timestamp, me.getTimestamp()) == 0);
    }
}
