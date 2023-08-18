package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class Card {
    // Редкость карточки
    private String rarity;

    // ID карточки
    private String id;
}
