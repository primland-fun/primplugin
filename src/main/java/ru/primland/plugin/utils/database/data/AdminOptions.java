package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @AllArgsConstructor
public class AdminOptions {
    // Настройки "шпиона"
    private List<SpyFlag> spy;

    /**
     * Установить флаги шпиона
     * @param value Цифровое значение
     */
    public static @NotNull List<SpyFlag> toSpyFlags(int value) {
        List<SpyFlag> flags = new ArrayList<>();
        for(SpyFlag flag : SpyFlag.values()) {
            if(!flag.hasFlag(value)) continue;
            flags.add(flag);
        }

        return flags;
    }

    /**
     * Добавить новый флаг "шпиона"
     * @param flag Флаг
     */
    public void addSpyFlag(SpyFlag flag) {
        if(spy == null) spy = new ArrayList<>();
        spy.add(flag);
    }

    /**
     * Убрать N флаг из списка
     * @param flag Флаг
     */
    public void removeSpyFlag(SpyFlag flag) {
        if(spy == null) return;
        spy.remove(flag);
    }

    /**
     * Получить цифровое значение флагов
     * @return Цифровое значение всех флагов
     */
    public int getSpyFlagsValue() {
        if(spy == null)
            return 0;

        AtomicInteger output = new AtomicInteger();
        spy.forEach(flag -> output.set(output.get() | flag.getValue()));
        return output.get();
    }

    @Getter @AllArgsConstructor
    public enum SpyFlag {
        PRIVATE_MESSAGES (1, "pm"),
        RAIDS (1 << 1, "raids"),
        TELEMETRY_JOIN (1 << 2, "telemetry.join"),
        TELEMETRY_LEAVE (1 << 3, "telemetry.leave"),
        TELEMETRY_COMMANDS (1 << 4, "telemetry.commands");

        private final int value;
        private final String name;

        public boolean hasFlag(int value) {
            return (value & this.value) == this.value;
        }
    }
}
