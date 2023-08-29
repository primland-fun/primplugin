package ru.primland.plugin.modules.head_drop;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.Command;
import ru.primland.plugin.commands.manager.CommandContext;
import ru.primland.plugin.commands.manager.CommandInfo;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.utils.Utils;

import java.util.Objects;

@CommandInfo(
        name="give",
        description="Выдать указанному получателю голову любого игроку",
        permission="primplugin.commands.heads.give",
        parent="heads"
)
public class GiveCommand extends Command {
    /**
     * Загрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void load(PrimPlugin plugin) {
        addArgument(new PlayerArgument<Player>("player", "игрок", true, false));
        addArgument(new PlayerArgument<Player>("receiver", "получатель", false, false));
    }

    /**
     * Отгрузить данные команды
     *
     * @param plugin Экземпляр плагина
     */
    @Override
    public void unload(PrimPlugin plugin) {}

    /**
     * Выполнить команду с указанными данными
     *
     * @param ctx Контекст команды
     * @return Сообщение для отправителя команды
     */
    @Override
    public @Nullable String execute(@NotNull CommandContext ctx) {
        Player player = Objects.requireNonNull(ctx.get("player"));
        Player receiver = ctx.get("receiver");
        if(receiver == null)
            receiver = ctx.sender;

        ItemStack item = HeadDrop.getPlayerHead(player, HeadDrop.getChanceForPlayer(player));
        // TODO: Выдача с помощью функции из issue #29
        receiver.getInventory().addItem(item);

        return Utils.parse(
                HeadDrop.config.getString("command.doneMessage"),
                new Placeholder("player", player.getName()),
                new Placeholder("receiver", receiver.getName())
        );
    }
}
