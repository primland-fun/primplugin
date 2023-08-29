package ru.primland.plugin.modules.manager;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Менеджер модулей
 */
public class ModuleManager {
    public static Map<String, CachedModule> cache;

    /**
     * Инициализировать менеджер.
     * Данная функция создаёт кеш
     */
    public static void init() {
        cache = new HashMap<>();
    }

    /**
     * Зарегистрировать все команды из этого проекта (просто ищет все реализации
     * ICommand и вызывает функции регистрации для каждой)
     */
    public static void registerAll() {
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
        loader.forEach(ModuleManager::register);
    }

    /**
     * Зарегистрировать указанную команду
     *
     * @param module Объект реализации модуля
     */
    public static void register(@NotNull Module module) {
        // Получаем и проверяем информацию о команде
        ModuleInfo info = getModuleInfo(module);
        if(info == null)
            return;

        // Регестрируем модуль
        Config config = Config.load(info.config() + ".yml");
        cache.put(info.name(), new CachedModule(module, info, config));

        // Включаем модуль, если это требуется
        if(config.getBoolean("enabled", false)) {
            module.load(PrimPlugin.instance);
            module.enabled = true;
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("enableModule"),
                    new Placeholder("module", info.name())));
        }
    }

    /**
     * Получить информацию о модуле
     *
     * @param module Объект модуля
     * @return {@link ModuleInfo}, если найдено, иначе null
     */
    public static @Nullable ModuleInfo getModuleInfo(@NotNull Module module) {
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if(info == null) {
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("moduleInfoNotFound"),
                    new Placeholder("class", module.getClass().getName())));

            return null;
        }

        return info;
    }

    /**
     * Выключить указанный модуль
     *
     * @param name Название модуля
     */
    public static void disable(String name) {
        CachedModule module = cache.get(name);
        if(module == null || !module.getModule().enabled)
            return;

        module.getModule().unload(PrimPlugin.instance);
        module.getModule().enabled = false;
        module.setConfig(null);

        PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("disableModule"), new Placeholder("module", name)));
    }

    /**
     * Включить указанный модуль
     *
     * @param name Название модуля
     */
    public static void enable(String name) {
        CachedModule module = cache.get(name);
        if(module == null || module.getModule().enabled)
            return;

        if(module.getConfig() != null && !module.getConfig().getBoolean("enabled", false))
            return;

        module.getModule().load(PrimPlugin.instance);
        module.getModule().enabled = true;
        module.setConfig(Config.load(module.getInfo().config() + ".yml"));

        PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("enableModule"), new Placeholder("module", name)));
    }

    /**
     * Снять регистрацию со всех модулей
     */
    public static void unregisterAll() {
        cache.keySet().forEach(ModuleManager::unregister);
    }

    /**
     * Снять регистрацию с указанного модуля
     *
     * @param name Название модуля
     */
    public static void unregister(@NotNull String name) {
        CachedModule module = cache.get(name);
        if(module == null)
            return;

        module.getModule().unload(PrimPlugin.instance);
        cache.remove(name);
    }
}
