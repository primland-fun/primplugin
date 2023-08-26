package ru.primland.plugin.commands.manager.argument.type;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.argument.ArgumentContext;
import ru.primland.plugin.commands.manager.argument.ArgumentOut;
import ru.primland.plugin.database.data.PrimPlayer;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter @Setter
public class PlayerArgument<T> extends Argument<T> {
    /**
     * Название аргумента
     */
    private @NotNull String name;

    /**
     * Лямбда-функция для получения значения<br>
     * На вход подаётся: контекст команды и значение<br>
     * На выход: значение аргумента
     */
    @SuppressWarnings("unchecked")
    private @NotNull BiFunction<ArgumentContext, String[], ArgumentOut<T>> get = (ctx, value) -> {
        if(value[0].equals(ctx.sender.getName()))
            return new ArgumentOut<>(0, null, ArgumentOut.ArgumentError.PLAYER_SELF_SPECIFIED, null);

        if(getTypeVar().equals(Player.class) && Bukkit.getPlayer(value[0]) == null)
            return new ArgumentOut<>(0, null, ArgumentOut.ArgumentError.PLAYER_NOT_FOUND, value[0]);

        if(getTypeVar().equals(Player.class))
            return (ArgumentOut<T>) new ArgumentOut<>(1, Bukkit.getPlayer(value[0]), null, null);

        PrimPlayer player = PrimPlugin.driver.getPlayer(value[0]);
        if(getTypeVar().equals(PrimPlayer.class) && player == null && Bukkit.getPlayer(value[0]) == null)
            return new ArgumentOut<>(0, null, ArgumentOut.ArgumentError.DATABASE_PLAYER_NOT_FOUND, value[0]);

        if(getTypeVar().equals(PrimPlayer.class) && player == null) {
            PrimPlugin.driver.addPlayer(Objects.requireNonNull(Bukkit.getPlayer(value[0])));
            player = PrimPlugin.driver.getPlayer(value[0]);
        }

        if(getTypeVar().equals(PrimPlayer.class))
            return (ArgumentOut<T>) new ArgumentOut<>(1, player, null, null);

        return new ArgumentOut<>(0, null, ArgumentOut.ArgumentError.INVALID_TYPE_VAR, null);
    };

    /**
     * Отображаемое в подсказке название аргумента
     */
    private @Nullable String displayName;

    /**
     * Обязателен ли аргумент
     */
    private boolean required;

    /**
     * Требовать любого игрока кроме себя?
     */
    private boolean requireNotSelf;

    /**
     * Лямбда-функция для получения предложения
     */
    private @NotNull Function<ArgumentContext, List<String>> suggests = (ctx) -> PrimPlugin.getOnlinePlayersNames();

    public PlayerArgument(@NotNull String name, @Nullable String displayName, boolean required, boolean requireNotSelf) {
        this.name = name;
        this.displayName = displayName;
        this.required = required;
        this.requireNotSelf = requireNotSelf;
    }

    /**
     * Получить переменный тип этого класса
     * @return {@link Class<T>}
     */
    private Class<T> getTypeVar() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass()
                .getGenericSuperclass();

        @SuppressWarnings("unchecked")
        Class<T> ret = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        return ret;
    }
}
