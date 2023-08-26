package ru.primland.plugin.commands.manager.argument;

import org.jetbrains.annotations.Nullable;

public record ArgumentOut<T>(int size, @Nullable T output, @Nullable ArgumentError error, @Nullable String errorData) {
    public enum ArgumentError {
        PLAYER_NOT_FOUND,
        DATABASE_PLAYER_NOT_FOUND,
        INVALID_TYPE_VAR,
        NOT_ENOUGH_ARGUMENTS,
        FAILED_TO_PARSE_ARGUMENT,
        PLAYER_SELF_SPECIFIED,
        NUMBER_REQUIRED
    }
}
