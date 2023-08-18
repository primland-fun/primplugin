package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Reputation {
    // Количество репутации у игрока
    private int value;

    // Время последнего использования /rep +|-
    private long lastAction;
}
