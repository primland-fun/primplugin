package ru.primland.plugin.commands.manager;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.type.Argument;

import java.util.ArrayList;
import java.util.List;

/**
 * Интерфейс для команд
 */
@Getter
public abstract class Command {
    private final @NotNull List<Argument<?>> arguments = new ArrayList<>();

    /**
     * Добавить аргумент к команде
     *
     * @param argument Объект аргумента
     * @param <T>      Тип аргумента
     */
    public <T> void addArgument(Argument<T> argument) {
        arguments.add(argument);
    }

    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public abstract void load(PrimPlugin plugin);

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    public abstract void unload(PrimPlugin plugin);

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    public abstract @Nullable String execute(CommandContext ctx);
}
