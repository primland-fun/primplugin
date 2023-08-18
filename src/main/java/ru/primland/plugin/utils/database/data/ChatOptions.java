package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter @AllArgsConstructor
public class ChatOptions {
    // Звук, проигрываемый при получении сообщения
    // null - звук по умолчанию
    @Setter @Nullable private String sound;

    // Ник автора последнего полученного сообщения.
    // Может иметь значение null
    @Setter @Nullable private String lastReceived;

    // Флаги сообщений
    private List<MessagesFlags> flags;

    /**
     * Установить флаги сообщений
     * @param value Цифровое значение
     */
    public void setFlags(int value) {
        List<MessagesFlags> flags = new ArrayList<>();
        for(MessagesFlags flag : MessagesFlags.values()) {
            if(!flag.hasFlag(value)) continue;
            flags.add(flag);
        }

        this.flags = flags;
    }

    @Getter @AllArgsConstructor
    enum MessagesFlags {
        PLAYER_JOIN_LEAVE (1),
        PLAYER_GOT_ACHIEVEMENT (1 << 1),
        PLAYER_DEATH (1 << 2),
        DO_PLAY_SOUND (1 << 3);

        private final int value;

        public boolean hasFlag(int value) {
            return (value & this.value) == this.value;
        }
    }
}
