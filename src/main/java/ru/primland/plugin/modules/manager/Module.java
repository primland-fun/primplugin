package ru.primland.plugin.modules.manager;

import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;

/**
 * Базовый класс для модулей
 */
public abstract class Module {
    public boolean enabled;

    /**
     * Получить доступ к конфигурации модуля
     * @return Конфигурация модуля, если найдена, иначе null
     */
    public @Nullable Config getConfig() {
        ModuleInfo info = ModuleManager.getModuleInfo(this);
        if(info == null)
            return null;

        CachedModule module = ModuleManager.cache.get(info.name());
        return module == null ? null : module.getConfig();
    }

    /**
     * Загрузить (включить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    public abstract void load(PrimPlugin plugin);

    /**
     * Отгрузить (выключить) модуль
     *
     * @param plugin Экземпляр плагина
     */
    public abstract void unload(PrimPlugin plugin);
}
