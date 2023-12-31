package ru.primland.plugin.commands.manager;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания информации о команде.
 * <br><br>
 * Это замена полной реализации команд на аннотациях, т.к. я ещё не настолько умён
 * для этого
 */
@Target({ElementType.TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    // Название команды
    @NotNull String name();

    // Описание команды
    @NotNull String description() default "";

    // Псевдонимы команды, не обязательно
    @NotNull String[] aliases() default {};

    // Требуемое для использования команды право, не обязательно
    @NotNull String permission() default "";

    // Эту команду могут выполнять только игроки?
    boolean playersOnly() default false;

    // Родительская команда
    @NotNull String parent() default "";
}
