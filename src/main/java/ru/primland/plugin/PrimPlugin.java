package ru.primland.plugin;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.CommandManager;
import ru.primland.plugin.commands.plugin.HelpCommand;
import ru.primland.plugin.commands.plugin.InfoCommand;
import ru.primland.plugin.commands.plugin.PluginPrimaryCommand;
import ru.primland.plugin.commands.plugin.ReloadCommand;
import ru.primland.plugin.database.MySQLDriver;
import ru.primland.plugin.modules.IPluginModule;
import ru.primland.plugin.modules.ModuleManager;
import ru.primland.plugin.modules.a2border.Achievements2Border;
import ru.primland.plugin.modules.cards.CollectibleCards;
import ru.primland.plugin.modules.head_drop.HeadDrop;
import ru.primland.plugin.modules.jll.JoinLeaveListener;
import ru.primland.plugin.modules.owner_check.CustomItem;
import ru.primland.plugin.modules.recipes.CustomRecipes;
import ru.primland.plugin.modules.recipes.GlowEnchantment;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PrimPlugin extends JavaPlugin {
    public static PrimPlugin instance;
    public static Config config;
    public static Config i18n;
    public static MySQLDriver driver;
    public static PluginPrimaryCommand command;
    public static ModuleManager manager;
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

        // Регистрируем команду плагина
        command = new PluginPrimaryCommand();
        command.addCommand(new HelpCommand());
        command.addCommand(new InfoCommand());
        command.addCommand(new ReloadCommand());

        PluginCommand pluginCommand = getCommand("primplugin");
        if(pluginCommand == null) {
            send(i18n.getString("registerError"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        // Регистрируем JLl
        listener = new JoinLeaveListener();
        listener.enable(this);

        // Регистрируем зачарование "свечение"
        GlowEnchantment.register();

        // Регистрируем все модули плагин и включаем их
        manager = new ModuleManager();
        manager.registerModule(new Achievements2Border());
        manager.registerModule(new HeadDrop());
        manager.registerModule(new CustomRecipes());
        manager.registerModule(new CustomItem());
        manager.registerModule(new CollectibleCards());
        manager.enableModules();
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
        Collection<IPluginModule> modules = manager.getActiveModules();
        manager.disableModules();
        manager.enableModules(modules);

        // Отправляем сообщение
        send(i18n.getString("reload"));
    }
}
