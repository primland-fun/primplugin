package ru.primland.plugin.commands.manager.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.argument.type.Argument;

@Getter @AllArgsConstructor
public class ArgumentContext extends CommandContext {
    private final @NotNull Argument<?> argument;
}
