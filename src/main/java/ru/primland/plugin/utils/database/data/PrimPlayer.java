package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.utils.database.MySQLDriver;

import java.util.List;

/**
 * Класс для взаимодействия с данными об игроке<p>
 * Изначально я хотел назвать этот класс как Primer (Prim + Player)<p>
 * Prim Player = Чопорный Игрок)
 */
@Getter @AllArgsConstructor
public class PrimPlayer {
    // TODO: Перенести весь функционал изменения данных об игроке сюда

    // Драйвер для работы с базой данных
    private final @NotNull MySQLDriver driver;

    // Ник игрока
    private final @NotNull String name;

    // Количество дней подряд, которые игрок прописывал
    // команду /daily
    private final int dailyStrike;

    // Данные (настройки) чата игрока
    private final ChatOptions chat;

    // Данные (настройки) администрирования
    private final AdminOptions admin;

    // Список с коллекционными карточками, которые игрок
    // "сложил" в коллекцию
    private final List<Card> cards;

    // Баланс игрока
    private final Balance balance;

    // Данные репутационной системы для этого игрока
    private final Reputation reputation;

    /**
     * Получить объект игрока Bukkit для этого игрока
     * @return Объект Bukkit-игрока, если он онлайн, иначе null
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(name);
    }
}
