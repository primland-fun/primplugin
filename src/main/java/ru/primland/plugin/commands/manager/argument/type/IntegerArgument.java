package ru.primland.plugin.commands.manager.argument.type;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter @Setter
public class IntegerArgument extends Argument<Integer> {
    /**
     * Название аргумента
     */
    private @NotNull String name;

    /**
     * Лямбда-функция для получения значения<br>
     * На вход подаётся: контекст команды и значение<br>
     * На выход: значение аргумента
     */
    private @NotNull BiFunction<ArgumentContext, String[], ArgumentOut<Integer>> get = (ctx, value) -> {
        try {
            return new ArgumentOut<>(1, Integer.parseInt(value[0]), null, null);
        } catch(NumberFormatException error) {
            return new ArgumentOut<>(0, null, ArgumentOut.ArgumentError.NUMBER_REQUIRED, null);
        }
    };

    /**
     * Отображаемое в подсказке название аргумента
     */
    private @Nullable String displayName;

    /**
     * Обязателен ли аргумент
     */
    private boolean required;

    /**
     * Лямбда-функция для получения предложения
     */
    private @NotNull Function<ArgumentContext, List<String>> suggests = (ctx) -> List.of("2", "4", "8", "16", "32");

    public IntegerArgument(@NotNull String name, @Nullable String displayName, boolean required) {
        this.name = name;
        this.displayName = displayName;
        this.required = required;
    }

    public IntegerArgument(@NotNull String name, @Nullable String displayName, boolean required, Function<ArgumentContext, List<String>> suggests) {
        this.name = name;
        this.displayName = displayName;
        this.required = required;
        this.suggests = suggests;
    }
}
