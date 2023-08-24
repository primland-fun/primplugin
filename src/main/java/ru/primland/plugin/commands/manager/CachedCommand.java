package ru.primland.plugin.commands.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter @Setter @AllArgsConstructor
public class CachedCommand {
    // Объект команды
    private @NotNull Command command;

    // Информация о команде
    private @NotNull CommandInfo info;

    // Объект команды для регистрации через Bukkit
    private @NotNull BukkitCommand bukkitCommand;
}
