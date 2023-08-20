package ru.primland.plugin.commands.manager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.commands.manager.annotations.Command;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private static final String packageName = "ru.primland.plugin";

    private static final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
    private static final Map<String, LiteralCommandNode<CommandSourceStack>> cache = new HashMap<>();

    public static void registerCommands() {
        try {
            List<Class<?>> classes = getAllAnnotatedClasses();
            classes.forEach(clazz -> {
                Command annotation = getAnnotation(clazz, Command.class);
                assert annotation != null;


            });
        } catch(ClassNotFoundException error) {
            PrimPlugin.send("&cНе удалось найти класс, выключение...");
            error.printStackTrace();
            PrimPlugin.instance.getServer().getPluginManager().disablePlugin(PrimPlugin.instance);
        }
    }

    private static @Nullable <T> T getAnnotation(@NotNull Class<?> clazz, Class<T> annotationClass) {
        for(Annotation annotation : clazz.getAnnotations()) {
            if(!annotation.annotationType().equals(annotationClass))
                continue;

            return (T) annotation;
        }

        return null;
    }

    @Contract(" -> new")
    private static @NotNull List<Class<?>> getAllAnnotatedClasses() throws ClassNotFoundException {
        ClassLoader loader = PrimPlugin.class.getClassLoader();
        URL urls = loader.getResource(packageName.replace(".", "/"));
        if(urls == null)
            return new ArrayList<>();

        File folder = new File(urls.getPath());
        File[] classes = folder.listFiles();
        if(classes == null)
            return new ArrayList<>();

        List<Class<?>> output = new ArrayList<>();
        for(File clazz : classes) {
            int index = clazz.getName().indexOf(".");
            String className = clazz.getName().substring(0, index);
            String classNamePath = packageName + "." + className;
            Class<?> repoClass = Class.forName(classNamePath);

            Annotation[] annotations = repoClass.getAnnotations();
            for(Annotation annotation : annotations) {
                if(annotation.annotationType() != Command.class)
                    continue;

                output.add(repoClass);
            }
        }

        return output;
    }
}
