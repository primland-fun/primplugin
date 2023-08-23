package ru.primland.plugin.commands.manager.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter @Setter @AllArgsConstructor
public class ArgumentData {
    // Название аргумента
    private @NotNull String name;

    // Значение аргумента
    private @NotNull Object value;
}
