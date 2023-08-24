package ru.primland.plugin.commands.manager.argument.type;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Базовый класс для аргументов
 */
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @AllArgsConstructor
public class Argument<T> {
    /**
     * Название аргумента
     */
    private @NotNull String name;

    /**
     * Лямбда-функция для получения значения<br>
     * На вход подаётся: контекст команды и значение<br>
     * На выход: значение аргумента
     */
    @SuppressWarnings("unchecked")
    private @NotNull BiFunction<ArgumentContext, String[], ArgumentOut<T>> get = (ctx, value) ->
            (ArgumentOut<T>) new ArgumentOut<>(value.length, String.join(" ", value), null);

    /**
     * Отображаемое в подсказке название аргумента
     */
    private @Nullable String displayName = null;

    /**
     * Обязателен ли аргумент
     */
    private boolean required = false;

    /**
     * Лямбда-функция для получения предложения
     */
    private @NotNull Function<ArgumentContext, List<String>> suggests = (ctx) -> List.of();
}
