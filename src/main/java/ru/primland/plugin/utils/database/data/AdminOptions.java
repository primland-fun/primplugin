package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AdminOptions {
    // Настройки "шпиона"
    private List<SpyFlag> spy;

    /**
     * Установить флаги шпиона
     * @param value Цифровое значение
     */
    public void setSpy(int value) {
        List<SpyFlag> flags = new ArrayList<>();
        for(SpyFlag flag : SpyFlag.values()) {
            if(!flag.hasFlag(value)) continue;
            flags.add(flag);
        }

        spy = flags;
    }

    @Getter @AllArgsConstructor
    enum SpyFlag {
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
