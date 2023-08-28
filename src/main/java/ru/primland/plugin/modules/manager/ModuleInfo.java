package ru.primland.plugin.modules.manager;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания информации о модуле.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    // Название модуля
    @NotNull String name();

    // Название конфигурации модуля
    @NotNull String config();

    // Описание модуля
    @NotNull String description() default "";

    // Требуется ли для работы модуля база данных
    boolean databaseRequired() default false;
}
