package ru.primland.plugin.modules.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;

@Getter @Setter @AllArgsConstructor
public class CachedModule {
    // Объект модуля
    private @NotNull Module module;

    // Информация о модуле
    private @NotNull ModuleInfo info;

    // Конфигурация модуля
    private @Nullable Config config;
}
