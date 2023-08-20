package ru.primland.plugin.database.data.gifts;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.database.data.subdata.Balance;
import ru.primland.plugin.utils.Utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GiftContent {
    // Содержимое подарка
    private @NotNull List<ItemStack> itemContent = new ArrayList<>();
    private @NotNull List<Node> permissionContent = new ArrayList<>();
    private @NotNull Balance balanceContent = new Balance(0, 0);

    /**
     * Добавить к подарку предмет
     * @param item Объект предмета
     */
    public void addItem(ItemStack item) {
        itemContent.add(item);
    }

    /**
     * Убрать из подарка предмет
     * @param index Индекс предмета
     */
    public void removeItem(int index) {
        itemContent.remove(index);
    }

    /**
     * Добавить к подарку право
     * @param permission Название права
     */
    public void addPermission(Node permission) {
        permissionContent.add(permission);
    }

    /**
     * Убрать из подарка право
     * @param index Индекс права
     */
    public void removePermission(int index) {
        permissionContent.remove(index);
    }

    /**
     * Преобразовать указанные данные в {@link Node} (право из LuckPerms)
     *
     * @param start Начало названия права
     * @param data  Данные (name - название права; expires - через сколько его забирать)
     *
     * @return Новый экземпляр {@link Node}
     */
    private static @NotNull Node getPermission(@NotNull String start, @NotNull Map<String, Object> data) {
        String name = String.valueOf(data.get("name"));
        @Nullable Long expiry = data.get("expiry") == null ? null : Long.getLong(
                String.valueOf(data.get("expiry")));

        NodeBuilder<?, ?> builder = Node.builder(start + name);
        if(expiry != null)
            builder.expiry(Instant.ofEpochMilli(expiry));

        return builder.build();
    }

    /**
     * Преобразовывает указанную JSON-строку в содержимое подарка
     *
     * @param json JSON-строка
     * @return {@link GiftContent}
     */
    public static @NotNull GiftContent deserialize(String json) {
        GiftContent output = new GiftContent();

        List<?> parsed = (new Gson()).fromJson(json, List.class);
        parsed.forEach(raw -> {
            Map<String, Object> object = Utils.convertObjectToMap(raw, Object.class);
            String type = String.valueOf(object.get("type"));

            // Если тип объекта - это item, то просто вызываем ItemStack.deserialize
            // с данными ($.data) в качестве аргумента
            if(type.equals("item"))
                output.addItem(ItemStack.deserialize(Utils.convertObjectToMap(object.get("data"), Object.class)));

            // Если тип объекта - это group, то преобразовываем его в право с помощью
            // функции getPermission
            if(type.equals("group")) {
                output.addPermission(getPermission("group.", Utils.convertObjectToMap(object.get("data"),
                        Object.class)));
            }

            // Если тип объекта - это pet, то преобразовываем его в право с помощью
            // функции getPermission
            if(type.equals("pet")) {
                output.addPermission(getPermission("mcpets.", Utils.convertObjectToMap(object.get("data"),
                        Object.class)));
            }

            // Если тип объекта - это permission, то преобразовываем его в право с
            // помощью функции getPermission
            if(type.equals("permission")) {
                output.addPermission(getPermission("", Utils.convertObjectToMap(object.get("data"),
                        Object.class)));
            }

            // Если тип объекта - это balance, то создаём новый объект Balance и
            // указываем в качестве аргументов одноимённые данные
            if(type.equals("balance")) {
                Map<String, Integer> data = Utils.convertObjectToMap(object.get("data"), Integer.class);
                output.setBalanceContent(new Balance(data.get("reputation"), data.get("donate")));
            }
        });

        return output;
    }

    /**
     * Преобразовать содержимое подарка в JSON-строку
     * @return JSON-строка
     */
    public String serialize() {
        if(itemContent.isEmpty() && permissionContent.isEmpty() && balanceContent.getReputation() == 0
                && balanceContent.getDonate() == 0)
            return "[]";

        StringBuilder output = new StringBuilder("[");

        itemContent.forEach(item -> {
            output.append("{\"type\":\"item\",\"data\":");
            output.append((new Gson()).toJson(item.serialize()));
            output.append("},");
        });

        permissionContent.forEach(permission -> {
            String type = permission.getKey().startsWith("group.") ? "group" : (permission.getKey()
                    .startsWith("mcpets.") ? "pet" : "permission");

            output.append("{\"type\":\"").append(type);
            output.append("\",\"data\":{\"name\":\"");
            output.append(permission.getKey().replace("group.", "").replace("mcpets.", ""));
            output.append("\",\"expiry\":");
            output.append(permission.getExpiry() == null ? "null" : Timestamp.from(
                    permission.getExpiry()).getTime());

            output.append("}},");
        });

        if(balanceContent.getReputation() != 0 || balanceContent.getDonate() != 0) {
            output.append("{\"type\":\"balance\",\"data\":{\"reputation\":");
            output.append(balanceContent.getReputation());
            output.append(",\"donate\":");
            output.append(balanceContent.getDonate());

            // Запятая нужна, чтобы не страдать с проверкой на её существование
            output.append("}},");
        }

        // Удаляем последнюю запятую и добавляем "]", после чего преобразовываем в
        // строку
        return output.deleteCharAt(output.length()-2).append(']').toString();
    }
}
