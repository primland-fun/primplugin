package ru.primland.plugin.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.database.MySQLDriver;

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
}
