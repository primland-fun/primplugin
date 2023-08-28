package ru.primland.plugin.modules.manager;

import ru.primland.plugin.PrimPlugin;

/**
 * Базовый класс для модулей
 */
public abstract class Module {
    public boolean enabled;

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
