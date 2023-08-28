package ru.primland.plugin.modules.manager;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

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
        ModuleInfo info = module.getClass().getAnnotation(ModuleInfo.class);
        if(info == null) {
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("moduleInfoNotFound"),
                    new Placeholder("class", module.getClass().getName())));

            return;
        }

        // Регестрируем модуль
        Config config = Config.load(info.config() + ".yml");
        cache.put(info.name(), new CachedModule(module, info, config));

        // Включаем модуль, если это требуется
        if(config.getBoolean("enabled", false)) {
            module.load(PrimPlugin.instance);
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("enableModule"),
                    new Placeholder("module", info.name())));
        }
    }

    /**
     * Выключить указанный модуль
     *
     * @param name Название модуля
     */
    public static void disable(String name) {
        CachedModule module = cache.get(name);
        if(module == null)
            return;

        module.getModule().unload(PrimPlugin.instance);
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
        if(module == null)
            return;

        module.getModule().load(PrimPlugin.instance);
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
