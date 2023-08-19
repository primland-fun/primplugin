package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @AllArgsConstructor
public class ChatOptions {
    // Звук, проигрываемый при получении сообщения
    // null - звук по умолчанию
    @Setter @Nullable private String sound;

    // Ник автора последнего полученного сообщения.
    // Может иметь значение null
    @Setter @Nullable private String lastReceived;

    // Флаги сообщений
    private List<MessagesFlag> flags;

    /**
     * Установить флаги сообщений
     * @param value Цифровое значение
     */
    public static @NotNull List<MessagesFlag> toFlags(int value) {
        List<MessagesFlag> flags = new ArrayList<>();
        for(MessagesFlag flag : MessagesFlag.values()) {
            if(!flag.hasFlag(value)) continue;
            flags.add(flag);
        }

        return flags;
    }

    /**
     * Добавить новый флаг сообщений
     * @param flag Флаг
     */
    public void addFlag(MessagesFlag flag) {
        if(flags == null) flags = new ArrayList<>();
        flags.add(flag);
    }

    /**
     * Убрать N флаг из списка
     * @param flag Флаг
     */
    public void removeFlag(MessagesFlag flag) {
        if(flags == null) return;
        flags.remove(flag);
    }

    /**
     * Получить цифровое значение флагов
     * @return Цифровое значение всех флагов
     */
    public int getFlagsValue() {
        if(flags == null)
            return 0;

        AtomicInteger output = new AtomicInteger();
        flags.forEach(flag -> output.set(output.get() | flag.getValue()));
        return output.get();
    }

    @Getter @AllArgsConstructor
    public enum MessagesFlag {
        PLAYER_JOIN_LEAVE (1),
        PLAYER_GOT_ACHIEVEMENT (1 << 1),
        PLAYER_DEATH (1 << 2),
        DISABLE_PM_SOUND (1 << 3);

        private final int value;

        public boolean hasFlag(int value) {
            return (value & this.value) == this.value;
        }
    }
}
