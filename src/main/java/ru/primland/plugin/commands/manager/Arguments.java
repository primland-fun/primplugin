package ru.primland.plugin.commands.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.annotations.Argument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    public static @NotNull Arguments parse(String[] args, Argument[] arguments) {
        List<ArgumentData> argumentDataList = new ArrayList<>();
        return new Arguments(argumentDataList);
    }

    @Contract(pure = true)
    public static @Nullable String checkForErrors(String[] args, Argument @NotNull [] arguments) {
        if(arguments.length == 0)
            return null;

        // TODO
    }
}
