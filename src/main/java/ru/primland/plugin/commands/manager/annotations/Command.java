package ru.primland.plugin.commands.manager.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    // Название команды
    @NotNull String value();

    // Право для использования команды
    @Nullable String permission();

    // Описание команды
    @Nullable String description();

    // Псевдонимы команды
    @Nullable String[] aliases();
}
