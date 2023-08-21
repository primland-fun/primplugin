package ru.primland.plugin.commands.manager.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Аннотация для указания информации об аргументе
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
    // Название аргумента
    @NotNull String name();

    // Тип аргумента
    @NotNull Class<?> type();

    // Отображаемое название
    @NotNull String displayName() default "";

    // Обязателен ли аргумент
    boolean required() default false;

    // Готовые динамичные предложения
    @NotNull ArgumentSuggestion dynamicSuggestion() default ArgumentSuggestion.NULL;

    // Статичные предложения
    @NotNull String[] suggestions() default {};
}
