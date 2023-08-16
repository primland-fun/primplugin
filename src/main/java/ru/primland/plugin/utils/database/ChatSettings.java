package ru.primland.plugin.utils.database;

import javax.annotation.Nullable;
import java.util.List;

public class ChatSettings {
    @Nullable
    private String sound;

    private List<Message> messages;

    private List<String> listen;

    public @Nullable String getSound() {
        return sound;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<String> getListen() {
        return listen;
    }

    public void setSound(@Nullable String sound) {
        this.sound = sound;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setListen(List<String> listen) {
        this.listen = listen;
    }
}
