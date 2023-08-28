package ru.primland.plugin.commands.manager;

import io.github.stngularity.epsilon.engine.placeholders.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;
import ru.primland.plugin.commands.manager.argument.GetArgumentContextOutput;
import ru.primland.plugin.commands.manager.argument.type.Argument;
import ru.primland.plugin.commands.manager.argument.type.PlayerArgument;
import ru.primland.plugin.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BukkitCommand extends org.bukkit.command.Command {
    private final Command original;
    private final CommandInfo info;

    public BukkitCommand(Command command, @NotNull CommandInfo info) {
        super(info.name(), info.description(), CommandManager.getUsage(command), List.of(info.aliases()));
        this.original = command;
        this.info = info;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        if(!info.permission().isEmpty() && !sender.hasPermission(info.permission())) {
            PrimPlugin.send(PrimPlugin.i18n.getString("notEnoughRights"));
            return true;
        }

        if(info.playersOnly() && sender instanceof ConsoleCommandSender) {
            PrimPlugin.send(PrimPlugin.i18n.getString("playersOnly"));
            return true;
        }

        Map<String, CachedCommand> subcommands = CommandManager.searchSubCommands(info.name());
        if(!subcommands.isEmpty() && args.length > 0) {
            CachedCommand command = subcommands.get(args[0]);
            if(command == null) {
                PrimPlugin.send(PrimPlugin.i18n.getString("subcommandNotFound"));
                return true;
            }

            command.getBukkitCommand().execute(sender, s, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        GetContextOutput output = CommandContext.getContext(sender, args, original.getArguments());
        CommandContext context = output.context();
        ArgumentOut.ArgumentError error = output.error();
        Object data = output.errorData();

        if(error != null && error.equals(ArgumentOut.ArgumentError.PLAYER_NOT_FOUND)) {
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("playerNotFound"), new Placeholder("player", data)));
            return true;
        }

        if(error != null && error.equals(ArgumentOut.ArgumentError.DATABASE_PLAYER_NOT_FOUND)) {
            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("databasePlayerNotFound"),
                    new Placeholder("player", data)));

            return true;
        }

        if(error != null && Utils.equalsOne(error, ArgumentOut.ArgumentError.INVALID_TYPE_VAR,
                ArgumentOut.ArgumentError.FAILED_TO_PARSE_ARGUMENT)) {
            PrimPlugin.send(PrimPlugin.i18n.getString("internalError"));
            return true;
        }

        if(error != null && error.equals(ArgumentOut.ArgumentError.NOT_ENOUGH_ARGUMENTS)) {
            Argument<?> argument = data == null ? null : (Argument<?>) data;
            if(data instanceof PlayerArgument<?>) {
                PrimPlugin.send(PrimPlugin.i18n.getString("specifyPlayer" + (((PlayerArgument<?>) data)
                        .isRequireNotSelf() ? "NotSelf" : "")));

                return true;
            }

            String placeholder = argument == null ? "null" : (argument.getDisplayName() == null ?
                    argument.getName() : argument.getDisplayName());

            PrimPlugin.send(Utils.parse(PrimPlugin.i18n.getString("notEnoughArguments"),
                    new Placeholder("argument", placeholder)));

            return true;
        }

        if(error != null && error.equals(ArgumentOut.ArgumentError.PLAYER_SELF_SPECIFIED)) {
            PrimPlugin.send(PrimPlugin.i18n.getString("selfSpecified"));
            return true;
        }

        if(error != null && error.equals(ArgumentOut.ArgumentError.NUMBER_REQUIRED)) {
            PrimPlugin.send(PrimPlugin.i18n.getString("numberRequired"));
            return true;
        }

        if(context == null) {
            PrimPlugin.send(PrimPlugin.i18n.getString("internalError"));
            return true;
        }

        String result = original.execute(context);
        context.send((result != null && result.equals(OutputConstants.help)) ?
                CommandManager.buildHelpFor(info) : result);

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
        Map<String, CachedCommand> subcommands = CommandManager.searchSubCommands(info.name());
        if(!subcommands.isEmpty() && args.length == 0) {
            List<String> output = new ArrayList<>();
            subcommands.forEach((name, cmd) -> {
                if(cmd.getInfo().permission().isEmpty() || sender.hasPermission(cmd.getInfo().permission()))
                    output.add(name);
            });

            return output;
        }

        try {
            Argument<?> argument = original.getArguments().get(args.length-2);
            GetArgumentContextOutput output = ArgumentContext.getContext(sender, args, original.getArguments(), argument);
            if(output.context() == null)
                return List.of();

            return argument.getSuggests().apply(output.context());
        } catch(IndexOutOfBoundsException error) {
            return List.of();
        }
    }
}
