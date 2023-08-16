package ru.primland.plugin.modules;

import ru.primland.plugin.PrimPlugin;

public interface IPluginModule {
    String getName();
    String getConfigName();
    String getDescription();
    void enable(PrimPlugin plugin);
    void disable(PrimPlugin plugin);
    boolean isEnabled();
    String information();
}
