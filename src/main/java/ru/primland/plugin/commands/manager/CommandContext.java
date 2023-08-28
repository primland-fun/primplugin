package ru.primland.plugin.commands.manager;


import io.github.stngularity.epsilon.engine.placeholders.IPlaceholder;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;
import ru.primland.plugin.commands.manager.argument.GetArgumentContextOutput;
import ru.primland.plugin.commands.manager.argument.type.Argument;
import ru.primland.plugin.utils.CustomMenu;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
    public final @NotNull List<ArgumentInfo<?>> arguments;

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
        if(text == null) return;
        sender.sendMessage(Utils.parse(text, placeholders));
    }

    /**
     * Отправить сообщение человека, вызвавшего команду
     *
     * @param text         Текст сообщения
     * @param placeholders Заполнители сообщения
     */
    public void send(String text, List<IPlaceholder> placeholders) {
        if(text == null) return;
        sender.sendMessage(Utils.parse(text, placeholders));
    }

    /**
     * Открыть указанное меню у отправителя команды
     *
     * @param menu Объект меню
     */
    public void open(@NotNull CustomMenu menu) {
        menu.open(sender);
    }

    /**
     * Получить контекст команды
     *
     * @param sender    Отправитель команды
     * @param args      Указанные аргументы
     * @param validArgs Валидные аргументы команды
     * @return {@link GetContextOutput}
     */
    @Contract("_, _, _ -> new")
    public static @NotNull GetContextOutput getContext(@NotNull CommandSender sender, String @NotNull [] args, @NotNull List<Argument<?>> validArgs) {
        List<ArgumentInfo<?>> arguments = new ArrayList<>();
        int index = 0;
        for(Argument<?> argument : validArgs) {
            if(Arrays.copyOfRange(args, index, args.length).length == 0)
                return new GetContextOutput(null, ArgumentOut.ArgumentError.NOT_ENOUGH_ARGUMENTS, argument);

            GetArgumentContextOutput context = ArgumentContext.getContext(sender, args, validArgs, argument);
            if(context.error() != null)
                return new GetContextOutput(null, context.error(), context.errorData());

            if(context.context() == null)
                return new GetContextOutput(null, ArgumentOut.ArgumentError.FAILED_TO_PARSE_ARGUMENT, null);

            ArgumentOut<?> output = argument.getGet().apply(context.context(),
                    Arrays.copyOfRange(args, index, args.length));

            if(output.error() != null)
                return new GetContextOutput(null, output.error(), output.errorData());

            if(output.output() == null)
                return new GetContextOutput(null, ArgumentOut.ArgumentError.FAILED_TO_PARSE_ARGUMENT, null);

            arguments.add(new ArgumentInfo<>(argument, output.output()));
            index += output.size();
        }

        return new GetContextOutput(new CommandContext((Player) sender, arguments), null, null);
    }

    /**
     * Найти аргументы использую фильтр
     *
     * @param list   Список с аргументами
     * @param filter Функция-фильтр для поиска аргументов
     * @return Найденные аргументы
     */
    private static @NotNull List<Argument<?>> searchArguments(@NotNull List<Argument<?>> list, Function<Argument<?>, Boolean> filter) {
        List<Argument<?>> output = new ArrayList<>();
        list.forEach(argument -> {
            if(filter.apply(argument))
                output.add(argument);
        });

        return output;
    }

    /**
     * Класс с информацией об аргументе
     */
    public record ArgumentInfo<T>(@NotNull Argument<?> original, @NotNull T value) {}
}
