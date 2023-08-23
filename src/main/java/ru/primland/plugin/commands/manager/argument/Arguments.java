package ru.primland.plugin.commands.manager.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Getter @AllArgsConstructor
public class Arguments {
    private final List<ArgumentData> arguments;

    /**
     * Получить данные об аргументе по его названию
     *
     * @param name Название аргумента
     * @return {@link ArgumentData}
     */
    public @Nullable ArgumentData getArgument(String name) {
        AtomicReference<ArgumentData> output = new AtomicReference<>();
        arguments.forEach(argument -> {
            if(!argument.getName().equals(name)) return;
            output.set(argument);
        });

        return output.get();
    }

    @Contract("_, _ -> new")
    public static @NotNull Arguments parse(String @NotNull [] args, Argument[] arguments) throws IllegalArgumentException {
        if(args.length < process(arguments, Argument::required).size())
            throw new IllegalArgumentException(ArgumentParserError.NOT_ENOUGH_ARGUMENTS.getCode());

        List<ArgumentData> data = new ArrayList<>();
        int index = 0;
        for(Argument argument : arguments) {
            if(argument.type().equals(String.class) && argument.stringIsText()) {
                String text = String.join(" ", Arrays.copyOfRange(args, index, args.length));
                data.add(new ArgumentData(argument.name(), text));
                break;
            }

            data.add(processArgument(args[index], argument));
            index++;
        }

        return new Arguments(data);
    }

    @Contract("_, _ -> new")
    private static @NotNull ArgumentData processArgument(@NotNull String arg, @NotNull Argument argument) throws IllegalArgumentException {
        if(Utils.equalsOne(argument.type(), Player.class, String.class))
            return new ArgumentData(argument.name(), arg);

        if(argument.type().equals(Character.class) && arg.length() > 1)
            throw new IllegalArgumentException(ArgumentParserError.CHAR_REQUIRED.code);

        if(argument.type().equals(Character.class))
            return new ArgumentData(argument.name(), arg.charAt(0));

        if(argument.type().equals(Integer.class)) {
            try {
                return new ArgumentData(argument.name(), Integer.parseInt(arg));
            } catch(NumberFormatException error) {
                throw new IllegalArgumentException(ArgumentParserError.NUMBER_REQUIRED.code);
            }
        }

        throw new IllegalArgumentException(ArgumentParserError.NOT_PARSABLE.code);
    }

    private static @NotNull List<Argument> process(Argument @NotNull [] arguments, Function<Argument, Boolean> condition) {
        List<Argument> output = new ArrayList<>();
        for(Argument argument : arguments) {
            if(condition.apply(argument))
                output.add(argument);
        }

        return output;
    }

    @Getter @AllArgsConstructor
    public enum ArgumentParserError {
        NOT_ENOUGH_ARGUMENTS ("nea"),
        NUMBER_REQUIRED ("nr"),
        CHAR_REQUIRED ("cr"),
        NOT_PARSABLE ("np");

        private final String code;
    }
}
