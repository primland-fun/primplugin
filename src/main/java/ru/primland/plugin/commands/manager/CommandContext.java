package ru.primland.plugin.commands.manager;


import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.type.Argument;
import ru.primland.plugin.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Класс контекста команды
 */
@AllArgsConstructor
public class CommandContext {
    /**
     * Отправитель команды
     */
    public final @NotNull Player sender;

    /**
     * Список с информацией об аргументах
     */
    private final @NotNull List<ArgumentInfo<?>> arguments;

    /**
     * Получить данные аргумента с указанным названием
     *
     * @param argument Название аргумента, который вам нужно получить
     * @return Значение value аргумента, если он был найден, иначе null
     * @param <T> Тип данных аргумента
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(String argument) {
        AtomicReference<ArgumentInfo<?>> info = new AtomicReference<>();
        arguments.forEach(arg -> {
            if(arg.original.getName().equals(argument))
                info.set(arg);
        });

        return info.get() == null ? null : (T) info.get().value;
    }

    /**
     * Отправить сообщение человека, вызвавшего команду
     *
     * @param text         Текст сообщения
     * @param placeholders Заполнители сообщения
     */
    public void send(String text, IPlaceholder... placeholders) {
        sender.sendMessage(Utils.parse(text, placeholders));
    }

    /**
     * Отправить сообщение человека, вызвавшего команду
     *
     * @param text         Текст сообщения
     * @param placeholders Заполнители сообщения
     */
    public void send(String text, List<IPlaceholder> placeholders) {
        sender.sendMessage(Utils.parse(text, placeholders));
    }

    /**
     * Класс с информацией об аргументе
     */
    public record ArgumentInfo<T>(@NotNull Argument<?> original, @NotNull T value) {}
}
