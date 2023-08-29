package ru.primland.plugin;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.CommandManager;
import ru.primland.plugin.database.MySQLDriver;
import ru.primland.plugin.modules.jll.JoinLeaveListener;
import ru.primland.plugin.modules.manager.ModuleManager;
import ru.primland.plugin.modules.recipes.GlowEnchantment;
import ru.primland.plugin.utils.Utils;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class PrimPlugin extends JavaPlugin {
    public static final ZoneOffset msk = ZoneOffset.ofHours(3);

    public static PrimPlugin instance;
    public static Config config;
    public static Config i18n;
    public static MySQLDriver driver;
    public static LuckPerms lpApi;

    private JoinLeaveListener listener;

    @Override
    public void onEnable() {
        instance = this;

        // Пытаемся загрузить конфиг и локализацию
        config = Config.load("config.yml");
        i18n = Config.load("messages.yml");

        // Пытаемся подключиться к базе данных
        connectToDatabase();

        // Пытаемся получить доступ к API LuckPerms
        lpApi = getLuckPermsApi();

        // Регистрируем команды плагина
        CommandManager.init();
        CommandManager.registerAll();
        CommandManager.checkSubCommands();

        // Регистрируем JLl
        listener = new JoinLeaveListener();
        listener.enable(this);

        // Регистрируем зачарование "свечение"
        GlowEnchantment.register();

        // Регистрируем все модули плагин и включаем их
        ModuleManager.init();
        ModuleManager.registerAll();
    }

    public void connectToDatabase() {
        driver = new MySQLDriver(config.getString("database.prefix", "prim_"));
        driver.connect(config.getString("database.connection.host", "127.0.0.1"),
                config.getInteger("database.connection.port", 3306),
                config.getString("database.connection.database", "minecraft"),
                config.getString("database.authorization.username", null),
                config.getString("database.authorization.password", null));
    }

    private @Nullable LuckPerms getLuckPermsApi() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider == null) {
            send("&eНе удалось получить доступ к LuckPerms. Он не установлен?");
            return null;
        }

        return provider.getProvider();
    }

    public static void send(String... strings) {
        send(Bukkit.getConsoleSender(), strings);
    }

    public static void send(@NotNull CommandSender sender, String... strings) {
        List<String> stringList = new ArrayList<>(List.of(strings));

        Config i18n = PrimPlugin.i18n;
        String prefix = i18n == null ? "ξ &#65caefPrim&#f4d172Land › &r" : i18n
                .getString("prefix", "ξ &#65caefPrim&#f4d172Land › &r");

        stringList.add(0, prefix);
        sender.sendMessage(Utils.translate(String.join("", stringList)));
    }

    public static @NotNull List<String> getOnlinePlayersNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
        return names;
    }

    public void reload() {
        // Перезагружаем конфиги
        config.reload();
        i18n.reload();

        // Заново подключаемся к базе данных
        driver.disconnect();
        connectToDatabase();

        // Снимаем регистрацию с команд и регистрируем их заново
        CommandManager.unregisterAll();
        CommandManager.registerAll();

        // Перезагружаем JLl
        listener.disable();
        listener.enable(this);

        // Перезагружаем модули
        ModuleManager.unregisterAll();
        ModuleManager.registerAll();

        // Пишем в консоль о завершении перезагрузки
        send(i18n.getString("reload"));
    }
}
