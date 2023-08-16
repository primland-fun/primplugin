package ru.primland.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Config {
    private FileConfiguration original;
    private final File file;

    private Config(FileConfiguration original, File file) {
        this.original = original;
        this.file = file;
    }

    @Contract("_ -> new")
    public static @NotNull Config load(String filename) {
        PrimPlugin plugin = PrimPlugin.getInstance();
        File file = new File(plugin.getDataFolder(), filename);
        if(file.exists())
            return new Config(YamlConfiguration.loadConfiguration(file), file);

        PrimPlugin.send("&eКонфигурация " + file.getName() + " не существует, создание...");
        if(!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
            PrimPlugin.send("&cНе удалось создать папку плагина, выключение...");
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        try {
            if(!file.createNewFile()) {
                PrimPlugin.send("&cНе удалось создать конфигурацию, выключение...");
                plugin.getPluginLoader().disablePlugin(plugin);
            }

            InputStream input = plugin.getResource(filename);
            if(input == null) {
                PrimPlugin.send("&cНе удалось прочитать дефолтную конфигурацию, выключение...");
                plugin.getPluginLoader().disablePlugin(plugin);
            }

            input.transferTo(new FileOutputStream(file));
        } catch(IOException error) {
            PrimPlugin.send("&cНе удалось создать конфигурацию, выключение...");
            error.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }

        return load(filename);
    }

    public void reload() {
        original = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            original.save(file);
        } catch(IOException error) {
            PrimPlugin.send("&cНе удалось сохранить конфигурацию");
            error.printStackTrace();
        }
    }

    public void set(String key, Object value) {
        original.set(key, value);
    }

    public String getString(String key) {
        return original.getString(key, key);
    }

    public String getString(String key, String def) {
        return original.getString(key, def);
    }

    public double getDouble(String key, double def) {
        return original.getDouble(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return original.getBoolean(key, def);
    }

    public int getInteger(String key, int def) {
        return original.getInt(key, def);
    }

    public List<Map<?, ?>> getMapList(String key) {
        return original.getMapList(key);
    }

    public List<String> getStringList(String key) {
        return original.getStringList(key);
    }
}
