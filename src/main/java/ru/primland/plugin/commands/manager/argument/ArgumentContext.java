package ru.primland.plugin.commands.manager.argument;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.GetContextOutput;
import ru.primland.plugin.commands.manager.argument.type.Argument;

import java.util.List;

/**
 * Класс контекста аргументов
 */
@Getter
public class ArgumentContext extends CommandContext {
    private final @NotNull Argument<?> argument;

    private ArgumentContext(@NotNull Player sender, @NotNull List<ArgumentInfo<?>> arguments, @NotNull Argument<?> argument) {
        super(sender, arguments);
        this.argument = argument;
    }

    /**
     * Получить контекст аргумента
     *
     * @param sender    Отправитель команды
     * @param args      Указанные аргументы
     * @param validArgs Валидные аргументы команды
     * @param argument  Объект аргумента
     * @return {@link GetArgumentContextOutput}
     */
    @Contract("_, _, _, _ -> new")
    public static @NotNull GetArgumentContextOutput getContext(@NotNull CommandSender sender, String @NotNull [] args, @NotNull List<Argument<?>> validArgs, Argument<?> argument) {
        GetContextOutput output = getContext(sender, args, validArgs);
        if(output.error() != null)
            return new GetArgumentContextOutput(null, output.error(), output.errorData());

        if(output.context() == null)
            return new GetArgumentContextOutput(null, ArgumentOut.ArgumentError.FAILED_TO_PARSE_ARGUMENT, null);

        CommandContext context = output.context();
        return new GetArgumentContextOutput(new ArgumentContext(context.sender, context.arguments, argument),
                null, null);
    }
}
