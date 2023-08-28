package ru.primland.plugin.modules.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;

@Getter @Setter @AllArgsConstructor
public class CachedModule {
    // Объект модуля
    @NotNull Module module;

    // Информация о модуле
    @NotNull ModuleInfo info;

    // Конфигурация модуля
    @NotNull Config config;
}
