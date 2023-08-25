package ru.primland.plugin.commands.manager.argument;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.type.Argument;

import java.util.List;

/**
 * Ответ функции {@link ArgumentContext#getContext(CommandSender, String[], List, Argument)}
 *
 * @param context   Контекст
 * @param error     Ошибка
 * @param errorData Данные ошибки
 */
public record GetArgumentContextOutput(@Nullable ArgumentContext context, @Nullable ArgumentOut.ArgumentError error, @Nullable Object errorData) {
}
