package ru.primland.plugin.utils.database;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Message {
    private String sender;
    private String content;
    private long time;

    public Message() {}

    public Message(String sender, String content, LocalDateTime time) {
        this.sender = sender;
        this.content = content;
        this.time = Timestamp.valueOf(time).getTime();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toJSON() {
        return "{'sender': " + sender + "', 'content': " + content + ", 'time': " + time + "}";
    }

    public static Message fromJSON(String json) {
        return (new Gson()).fromJson(json, Message.class);
    }
}
