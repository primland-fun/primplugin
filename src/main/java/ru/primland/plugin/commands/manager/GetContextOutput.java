package ru.primland.plugin.commands.manager;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;

import java.util.List;

/**
 * Ответ функции {@link CommandContext#getContext(CommandSender, String[], List)}
 *
 * @param context   Контекст
 * @param error     Ошибка
 * @param errorData Данные ошибки
 */
public record GetContextOutput(@Nullable CommandContext context, @Nullable ArgumentOut.ArgumentError error, @Nullable Object errorData) {
}
