package ru.primland.plugin.database.data.gifts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum GiftType {
    GLOBAL (1),
    LOCAL (2);

    // Цифровое значение типа
    private final int value;
}
