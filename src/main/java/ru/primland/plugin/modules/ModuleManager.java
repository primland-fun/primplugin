package ru.primland.plugin.modules;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import lombok.Getter;
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
    // TODO: Полная перепись

    private final HashMap<String, IPluginModule> modules;
    private final HashMap<String, Config> moduleConfigs;
    @Getter private final HashMap<String, List<IPluginCommand>> moduleSubCommands;

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

    private void doLog(boolean log, String key, @NotNull IPluginModule module) {
        if(!log) return;
        PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString(key), new Placeholder("module", module.getName())));
    }

    public void disable(@NotNull String moduleName, boolean log) {
        IPluginModule module = getModule(moduleName);
        if(module == null)
            return;

        module.disable(PrimPlugin.instance);
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

        module.enable(PrimPlugin.instance);
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

        if(!PrimPlugin.command.hasCommand(moduleName))
            PrimPlugin.command.addCommand(new IPluginCommand() {
                @Override
                public void execute(@NotNull CommandSender sender, List<String> args) {
                    AtomicReference<IPluginCommand> command = new AtomicReference<>(null);
                    PrimPlugin.manager.getModuleSubCommands(moduleName).forEach(cmd -> {
                        if(!args.get(0).equals("_primary")) return;
                        command.set(cmd);
                    });

                    if(args.get(0) == null && command.get() != null) {
                        command.get().execute(sender, args);
                        return;
                    }

                    if(args.get(0) == null) {
                        PrimPlugin.send(sender, Utils.parse(PrimPlugin.i18n.getString("specifyCommand")));
                        return;
                    }

                    command.set(null);
                    PrimPlugin.manager.getModuleSubCommands(moduleName).forEach(cmd -> {
                        if(!args.get(0).equals(cmd.getName())) return;
                        command.set(cmd);
                    });

                    if(command.get() == null) {
                        PrimPlugin.send(sender, Utils.parse(PrimPlugin.i18n.getString("commandNotFound"),
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
                        PrimPlugin.manager.getModuleSubCommands(moduleName).forEach(cmd -> names.add(cmd.getName()));
                        return names;
                    }

                    for(IPluginCommand cmd : PrimPlugin.manager.getModuleSubCommands(moduleName)) {
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
        PrimPlugin.command.removeCommand(moduleName);
        moduleSubCommands.remove(moduleName);
    }

    public IPluginModule getModule(String name) {
        return modules.get(name);
    }

    public Config getModuleConfig(String module) {
        return moduleConfigs.get(module);
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
