package ru.primland.plugin.utils.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.utils.database.MySQLDriver;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor
public class Message {
    // Драйвер для работы с базой данных
    private @NotNull MySQLDriver driver;

    // Отправитель сообщения
    private @NotNull String sender;

    // Получатель сообщения
    private @NotNull String receiver;

    // Содержимое сообщения
    private @NotNull String content;

    // Время отправки сообщения
    private @NotNull LocalDateTime time;

    /**
     * Пометить сообщение как прочитанное
     * (удалить его из базы данных)
     */
    public void markAsRead() {
        driver.execute("DELETE FROM %smessages WHERE sender='%s' AND receiver='%s' AND content='%s' AND time=%d"
                .formatted(driver.getPrefix(), sender, receiver, content, Timestamp.valueOf(time).getTime()));
    }
}
