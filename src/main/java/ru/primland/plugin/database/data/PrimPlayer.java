package ru.primland.plugin.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.database.data.gifts.GiftContent;
import ru.primland.plugin.database.data.gifts.LocalGift;
import ru.primland.plugin.database.data.subdata.*;
import ru.primland.plugin.utils.Utils;
import ru.primland.plugin.database.MySQLDriver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Класс для взаимодействия с данными об игроке<p>
 * Изначально я хотел назвать этот класс как Primer (Prim + Player)<p>
 * Prim Player = Чопорный Игрок)
 */
@Getter @AllArgsConstructor
public class PrimPlayer {
    // Драйвер для работы с базой данных
    private final @NotNull MySQLDriver driver;

    // Ник игрока
    private final @NotNull String name;

    // Количество дней подряд, которые игрок прописывал
    // команду /daily
    private int dailyStrike;

    // Данные (настройки) чата игрока
    private ChatOptions chat;

    // Данные (настройки) администрирования
    private AdminOptions admin;

    // Список с коллекционными карточками, которые игрок
    // "сложил" в коллекцию
    private final List<Card> cards;

    // Баланс игрока
    private Balance balance;

    // Данные репутационной системы для этого игрока
    private Reputation reputation;

    /**
     * Получить объект игрока Bukkit для этого игрока
     * @return Объект Bukkit-игрока, если он онлайн, иначе null
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    /**
     * Обновить "страйк" использования команды /daily (и в базе данных и в объекте)
     * Нужно для отладки (как иначе мне проверять?)
     *
     * @param newStrike Новый страйк
     */
    public void setDailyStrike(int newStrike) {
        dailyStrike = newStrike;

        // Обновляем и в базе данных
        driver.execute("UPDATE %splayers SET daily_strike=%d WHERE name='%s'".formatted(driver.getPrefix(),
                dailyStrike, name));
    }

    /**
     * Добавить 1 день к счётчику "страйка" использования команды /daily
     */
    public void plusDayToDailyStrike() {
        setDailyStrike(dailyStrike+1);
    }

    /**
     * Обновить настройки чата для игрока.<p>
     * Данный метод обновляет только изменённые свойства, чтобы не нагружать базу
     * данных
     *
     * @param options Новые настройки чата
     */
    public void updateChatOptions(@NotNull ChatOptions options) {
        // Проверяем звук из новых опций на то, чтобы он был другим, если это так,
        // то обновляем в базе данных
        if(!Objects.equals(options.getSound(), chat.getSound())) {
            driver.execute("UPDATE %splayers SET `chat.sound`='%s' WHERE name='%s'".formatted(
                    driver.getPrefix(), options.getSound(), name));
        }

        // Проверяем "ник автора последнего полученного сообщения" из новых опций на
        // то, чтобы он был другим, если это так, то обновляем в базе данных
        if(!Objects.equals(options.getLastReceived(), chat.getLastReceived())) {
            driver.execute("UPDATE %splayers SET `chat.lastReceived`='%s' WHERE name='%s'".formatted(
                    driver.getPrefix(), options.getLastReceived(), name));
        }

        // Проверяем "флаги сообщений" из новых опций на то, чтобы он был другим,
        // если это так, то обновляем в базе данных
        if(!Objects.equals(options.getFlags(), chat.getFlags())) {
            driver.execute("UPDATE %splayers SET `chat.flags`=%d WHERE name='%s'".formatted(driver.getPrefix(),
                    options.getFlagsValue(), name));
        }

        // Ну и просто устанавливаем новое значение переменной
        chat = options;
    }

    /**
     * Обновить настройки администрирования для игрока.<p>
     * Данный метод обновляет только изменённые свойства, чтобы не нагружать базу
     * данных
     *
     * @param options Новые настройки администрирования
     */
    public void updateAdminOptions(@NotNull AdminOptions options) {
        // Проверяем "флаги сообщений" из новых опций на то, чтобы он был другим,
        // если это так, то обновляем в базе данных
        if(!Objects.equals(options.getSpy(), admin.getSpy())) {
            driver.execute("UPDATE %splayers SET `admin.spy`=%d WHERE name='%s'".formatted(driver.getPrefix(),
                    options.getSpyFlagsValue(), name));
        }

        // Ну и просто устанавливаем новое значение переменной
        admin = options;
    }

    /**
     * Добавить карточку в коллекцию игрока
     * @param card Объект карточки, которую нужно добавить в коллекцию
     * @return true, если операция прошла успешно, иначе false
     */
    public boolean addCardToCollection(@NotNull Card card) {
        // Если у игрока уже есть такая карточка в коллекции, то возвращаем false
        // (операция отменена)
        if(cards.contains(card))
            return false;

        // Обновляем список карточек у игрока в базе данных
        driver.execute("UPDATE %splayers SET cards=JSON_ARRAY_APPEND(cards, '$', '%s') WHERE name='%s'"
                .formatted(driver.getPrefix(), card.getForSql(), name));

        // Добавляем в список карточек у этого объекта новую карточку
        cards.add(card);
        return true;
    }

    /**
     * Обновить баланс игрока.<p>
     * Данный метод обновляет только изменённые свойства, чтобы не нагружать базу
     * данных
     *
     * @param newBalance Новый баланс
     */
    public void updateBalance(@NotNull Balance newBalance) {
        // Проверяем репутационный баланс из нового баланса на то, чтобы он был
        // другим, если это так, то обновляем в базе данных
        if(!Objects.equals(newBalance.getReputation(), balance.getReputation())) {
            driver.execute("UPDATE %splayers SET `balance.reputation`=%d WHERE name='%s'".formatted(
                    driver.getPrefix(), newBalance.getReputation(), name));
        }

        // Проверяем баланс донат-валюты из нового баланса на то, чтобы он был
        // другим, если это так, то обновляем в базе данных
        if(!Objects.equals(newBalance.getDonate(), balance.getDonate())) {
            driver.execute("UPDATE %splayers SET `balance.donate`=%d WHERE name='%s'".formatted(
                    driver.getPrefix(), newBalance.getDonate(), name));
        }

        // Ну и просто устанавливаем новое значение переменной
        balance = newBalance;
    }

    /**
     * Обновить данные репутационной системы для игрока.<p>
     * Данный метод обновляет только изменённые свойства, чтобы не нагружать базу
     * данных
     *
     * @param newReputation Новые данные
     */
    public void updateReputation(@NotNull Reputation newReputation) {
        // Проверяем репутацию игрока из новых данных на то, чтобы она была другой,
        // если это так, то обновляем в базе данных
        if(!Objects.equals(newReputation.getValue(), reputation.getValue())) {
            driver.execute("UPDATE %splayers SET `reputation.value`=%d WHERE name='%s'".formatted(
                    driver.getPrefix(), newReputation.getValue(), name));
        }

        // Проверяем время последнего использования /rep +|- из новых данных на то,
        // чтобы оно было другим, если это так, то обновляем в базе данных
        if(!Objects.equals(newReputation.getLastAction(), reputation.getLastAction())) {
            driver.execute("UPDATE %splayers SET `reputation.lastAction`=%d WHERE name='%s'".formatted(
                    driver.getPrefix(), newReputation.getLastAction(), name));
        }

        // Ну и просто устанавливаем новое значение переменной
        reputation = newReputation;
    }

    /**
     * Может ли игрок использовать команду /rep +|-
     * @return true, если может, иначе false
     */
    public boolean canUseReputationCommand() {
        return reputation.getLastAction() == -1 || !LocalDateTime.now().isBefore(
                reputation.getLastActionAsDateTime().plusDays(3));
    }

    /**
     * Найти непрочитанные сообщения, адресованные этому игроку
     * @return Список с сообщениями
     */
    public List<Message> searchUnreadMessages() {
        List<Message> output = new ArrayList<>();
        ResultSet result = driver.executeQuery("SELECT * FROM %smessages WHERE receiver='%s'".formatted(
                driver.getPrefix(), name));

        if(result == null)
            return output;

        try {
            while(result.next()) {
                output.add(new Message(driver, result.getString("sender"), name, result.getString(
                        "content"), Utils.convertTimestampToTime(result.getLong("time"))));
            }
        } catch(SQLException error) {
            error.printStackTrace();
        }

        return output;
    }

    /**
     * Отправить этому игроку приватное сообщение
     *
     * @param sender  Ник отправителя
     * @param content Содержимое сообщения
     */
    public void sendMessage(String sender, String content) {
        driver.execute("INSERT INTO %smessages VALUES ('%s', '%s', '%s', %d)".formatted(driver.getPrefix(),
                sender, name, content, Timestamp.valueOf(LocalDateTime.now()).getTime()));
    }

    /**
     * Пометить как прочитанные все сообщения, отправленные этому игроку
     * (просто удалить их из базы данных)
     */
    public void markAllMessagesAsRead() {
        driver.execute("DELETE FROM %smessages WHERE receiver='%s'".formatted(driver.getPrefix(), name));
    }

    /**
     * Найти неполученные подарки, адресованные этому игроку
     * @return Список с подарками
     */
    public List<LocalGift> searchGifts() {
        List<LocalGift> output = new ArrayList<>();
        ResultSet result = driver.executeQuery("SELECT * FROM %sgifts WHERE receiver='%s'".formatted(
                driver.getPrefix(), name));

        if(result == null)
            return output;

        try {
            while(result.next()) {
                output.add(new LocalGift(
                        driver,
                        result.getString("id"),
                        result.getString("name"),
                        GiftContent.deserialize(result.getString("content"))
                ));
            }
        } catch(SQLException error) {
            error.printStackTrace();
        }

        return output;
    }
}
