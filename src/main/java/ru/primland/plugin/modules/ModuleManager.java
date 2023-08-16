package ru.primland.plugin.modules;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.plugin.IPluginCommand;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ModuleManager {
    private final HashMap<String, IPluginModule> modules;
    private final HashMap<String, Config> moduleConfigs;
    private final HashMap<String, List<IPluginCommand>> moduleSubCommands;

    public ModuleManager() {
        this.modules = new HashMap<>();
        this.moduleConfigs = new HashMap<>();
        this.moduleSubCommands = new HashMap<>();
    }

    public void registerModule(IPluginModule module) {
        modules.put(module.getName(), module);
        moduleConfigs.put(module.getName(), Config.load("modules/" + module.getConfigName()));
    }

    public void enableModules() {
        modules.keySet().forEach(name -> {
            if(!moduleConfigs.get(name).getBoolean("enabled", true)) return;
            enable(name, true);
        });
    }

    public void enableModules(@NotNull Collection<IPluginModule> modules) {
        modules.forEach(module -> enable(module.getName(), false));
    }

    public void disableModules() {
        modules.forEach((name, module) -> {
            if(!module.isEnabled()) return;
            disable(name, false);
        });
    }

    private void doLog(boolean log, String key, IPluginModule module) {
        PrimPlugin plugin = PrimPlugin.getInstance();
        boolean dsmmas = plugin.getI18n().getBoolean("doSendModulesMessagesAtStartup", false);

        if(dsmmas ? log && PrimPlugin.isRealEnabled() : log)
            PrimPlugin.send(Utils.parse(plugin.getI18n().getString(key), new Placeholder("module", module.getName())));
    }

    public void disable(@NotNull String moduleName, boolean log) {
        IPluginModule module = getModule(moduleName);
        if(module == null)
            return;

        module.disable(PrimPlugin.getInstance());
        doLog(log, "disableModule", module);

        Config moduleConfig = moduleConfigs.get(moduleName);
        moduleConfig.reload();
        if(moduleConfig.getBoolean("enabled", true)) {
            moduleConfig.set("enabled", false);
            moduleConfig.save();
        }
    }

    public void enable(@NotNull String moduleName, boolean log) {
        IPluginModule module = getModule(moduleName);
        if(module == null)
            return;

        PrimPlugin plugin = PrimPlugin.getInstance();
        module.enable(plugin);
        doLog(log, "enableModule", module);

        Config moduleConfig = moduleConfigs.get(moduleName);
        moduleConfig.reload();
        if(!moduleConfig.getBoolean("enabled", true)) {
            moduleConfig.set("enabled", true);
            moduleConfig.save();
        }
    }

    public void registerCommandFor(String moduleName, IPluginCommand command) {
        IPluginModule module = getModule(moduleName);
        if(module == null)
            return;

        PrimPlugin plugin = PrimPlugin.getInstance();
        if(!plugin.getPrimaryCommand().hasCommand(moduleName))
            plugin.getPrimaryCommand().addCommand(new IPluginCommand() {
                @Override
                public void execute(@NotNull CommandSender sender, List<String> args) {
                    AtomicReference<IPluginCommand> command = new AtomicReference<>(null);
                    plugin.getManager().getModuleSubCommands(moduleName).forEach(cmd -> {
                        if(!args.get(0).equals("_primary")) return;
                        command.set(cmd);
                    });

                    if(args.get(0) == null && command.get() != null) {
                        command.get().execute(sender, args);
                        return;
                    }

                    if(args.get(0) == null) {
                        PrimPlugin.send(sender, Utils.parse(plugin.getI18n().getString("specifyCommand")));
                        return;
                    }

                    command.set(null);
                    plugin.getManager().getModuleSubCommands(moduleName).forEach(cmd -> {
                        if(!args.get(0).equals(cmd.getName())) return;
                        command.set(cmd);
                    });

                    if(command.get() == null) {
                        PrimPlugin.send(sender, Utils.parse(plugin.getI18n().getString("commandNotFound"),
                                new Placeholder("command", args.get(0))));

                        return;
                    }

                    args.remove(0);
                    command.get().execute(sender, args);
                }

                @Override
                public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
                    int argNumber = Math.max(args.length-1, 0);
                    if(argNumber == 1) {
                        List<String> names = new ArrayList<>();
                        plugin.getManager().getModuleSubCommands(moduleName).forEach(cmd -> names.add(cmd.getName()));
                        return names;
                    }

                    for(IPluginCommand cmd : plugin.getManager().getModuleSubCommands(moduleName)) {
                        if(!List.of(args).get(1).equals(cmd.getName())) continue;
                        return cmd.tabComplete(sender, args);
                    }

                    return null;
                }

                @Override
                public String getName() {
                    return moduleName;
                }

                @Override
                public String getDescription() {
                    return module.getDescription();
                }

                @Override
                public List<String> getRequiredPermissions() {
                    return List.of("primplugin.commands." + moduleName);
                }

                @Override
                public String getUsage() {
                    return "{команда} [аргументы...]";
                }
            });

        if(!moduleSubCommands.containsKey(moduleName))
            moduleSubCommands.put(moduleName, new ArrayList<>());

        moduleSubCommands.get(moduleName).add(command);
    }

    public void unregisterCommandsFor(String moduleName) {
        PrimPlugin.getInstance().getPrimaryCommand().removeCommand(moduleName);
        moduleSubCommands.remove(moduleName);
    }

    public IPluginModule getModule(String name) {
        return modules.get(name);
    }

    public Config getModuleConfig(String module) {
        return moduleConfigs.get(module);
    }

    public HashMap<String, List<IPluginCommand>> getModuleSubCommands() {
        return moduleSubCommands;
    }

    public List<IPluginCommand> getModuleSubCommands(String module) {
        List<IPluginCommand> commands = moduleSubCommands.get(module);
        if(commands == null) return List.of();
        return commands;
    }

    public Collection<IPluginModule> getModules() {
        return modules.values();
    }

    public Collection<IPluginModule> getActiveModules() {
        Collection<IPluginModule> output = new ArrayList<>();
        modules.values().forEach(module -> {
            if(!module.isEnabled()) return;
            output.add(module);
        });

        return output;
    }

    public List<String> getModulesNames() {
        return getModulesNames((module) -> true);
    }

    public List<String> getModulesNames(Function<IPluginModule, Boolean> check) {
        List<String> names = new ArrayList<>();
        getModules().forEach(module -> {
            if(!check.apply(module)) return;
            names.add(module.getName());
        });

        return names;
    }
}
