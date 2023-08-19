package ru.primland.plugin.utils.database.data.subdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter @AllArgsConstructor
public class Card {
    // Редкость карточки
    private String rarity;

    // ID карточки
    private String id;

    /**
     * Получить строку с данными о карточке для сохранения в базе данных
     * @return Строка в формате "{редкость} {id}"
     */
    public String getForSql() {
        return rarity + " " + id;
    }

    /**
     * Конвертировать список строк в список карточек
     * @param original Оригинальный список строк
     * @return Список карточек
     */
    public static @NotNull List<Card> toCardList(@NotNull List<?> original) {
        List<Card> output = new ArrayList<>();
        original.forEach(raw -> {
            String element = String.valueOf(raw);
            output.add(new Card(element.split(" ", 2)[0], element.split(" ", 2)[1]));
        });

        return output;
    }
}
