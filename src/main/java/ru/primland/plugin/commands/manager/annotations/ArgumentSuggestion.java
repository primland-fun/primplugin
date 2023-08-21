package ru.primland.plugin.commands.manager.annotations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.CommandSender;
import ru.primland.plugin.PrimPlugin;

import java.util.List;
import java.util.function.Function;

@Getter @NoArgsConstructor @AllArgsConstructor
public enum ArgumentSuggestion {
    NULL,
    ONLINE_PLAYERS(sender -> PrimPlugin.getOnlinePlayersNames()),
    COUNT(sender -> List.of((new String[]{"2", "4", "8", "16", "32", "64"})));

    // Генератор предложения
    private Function<CommandSender, List<String>> generator;
}
