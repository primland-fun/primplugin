package ru.primland.plugin.commands.manager.argument.type;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter @Setter
public class TextArgument extends Argument<String> {
    /**
     * Название аргумента
     */
    private @NotNull String name;

    /**
     * Лямбда-функция для получения значения<br>
     * На вход подаётся: контекст команды и значение<br>
     * На выход: значение аргумента
     */
    private @NotNull BiFunction<ArgumentContext, String[], ArgumentOut<String>> get = (ctx, value) ->
            new ArgumentOut<>(value.length, String.join(" ", value), null, null);

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
    private @NotNull Function<ArgumentContext, List<String>> suggests = (ctx) -> PrimPlugin.getOnlinePlayersNames();

    public TextArgument(@NotNull String name, @Nullable String displayName, boolean required) {
        this.name = name;
        this.displayName = displayName;
        this.required = required;
    }
}
