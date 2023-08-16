package ru.primland.plugin.utils.database;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class Reputation {
    private int value;
    private long lastGiveOrTake;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getLastGiveOrTake() {
        return lastGiveOrTake;
    }

    public void setLastGiveOrTake(long lastGiveOrTake) {
        this.lastGiveOrTake = lastGiveOrTake;
    }

    public static @NotNull @Unmodifiable Reputation fromJSON(String json) {
        return (new Gson()).fromJson(json, Reputation.class);
    }
}
