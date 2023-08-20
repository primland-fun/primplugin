package ru.primland.plugin.database.data.gifts;

import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.database.MySQLDriver;
import ru.primland.plugin.database.data.PrimPlayer;
import ru.primland.plugin.database.data.subdata.Balance;
import ru.primland.plugin.utils.Utils;

import java.util.Arrays;

@Getter @Setter
public class LocalGift extends GlobalGift {
    // Тип подарка
    private final @NotNull GiftType type = GiftType.LOCAL;

    // Получатель подарка
    private @NotNull String receiver;

    public LocalGift(@NotNull MySQLDriver driver, @NotNull String id, @Nullable String name, @NotNull GiftContent content) {
        super(driver, id, name, content);
    }

    public LocalGift(@NotNull MySQLDriver driver, @NotNull String id) {
        super(driver, id);
    }

    /**
     * Получить объект игрока Bukkit для получателя этого игрока
     * @return Объект Bukkit-игрока, если получатель онлайн, иначе null
     */
    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(receiver);
    }

    /**
     * Открыть подарок от лица игрока
     * @return Код ошибки или null
     */
    public @Nullable String open() {
        Player player = getPlayer();
        if(player == null)
            return "playerOffline";

        Inventory inventory = player.getInventory();
        getContent().getItemContent().forEach(item -> {
            if(Arrays.asList(player.getInventory().getStorageContents()).contains(null))
                inventory.addItem(item);

            Utils.dropItem(player, item);
        });

        Balance balance = getContent().getBalanceContent();
        if(balance.getReputation() != 0 || balance.getDonate() != 0) {
            PrimPlayer primPlayer = getDriver().getPlayer(receiver);
            Balance oldBalance = primPlayer.getBalance();
            primPlayer.updateBalance(new Balance(
                    oldBalance.getReputation() + balance.getReputation(),
                    oldBalance.getDonate() + balance.getDonate()
            ));
        }

        if(PrimPlugin.lpApi == null)
            return "luckPermsNotFound";

        User user = PrimPlugin.lpApi.getUserManager().getUser(receiver);
        if(user == null)
            return "luckPermsUserNotFound";

        getContent().getPermissionContent().forEach(permission -> user.data().add(permission));
        PrimPlugin.lpApi.getUserManager().saveUser(user);
        return null;
    }

    /**
     * Удалить этот подарок из базы данных
     */
    public void delete() {
        getDriver().execute("DELETE FROM %sgifts WHERE id='%s' AND receiver='%s'".formatted(getDriver().getPrefix(),
                 getId(), receiver));
    }
}
