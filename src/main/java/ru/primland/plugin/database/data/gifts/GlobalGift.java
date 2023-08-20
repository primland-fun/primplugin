package ru.primland.plugin.database.data.gifts;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.primland.plugin.database.MySQLDriver;

@Getter @Setter @RequiredArgsConstructor @AllArgsConstructor
public class GlobalGift {
    // Драйвер базы данных
    private final @NotNull MySQLDriver driver;

    // ID подарка
    private @NotNull String id;

    // Название подарка
    private @Nullable String name;

    // Тип подарка
    private final @NotNull GiftType type = GiftType.GLOBAL;

    // Содержимое подарка
    private @NotNull GiftContent content = new GiftContent();

    /**
     * Удалить этот подарок из базы данных
     */
    public void delete() {
        // Удаляем и этот подарок, и все подарки, которые ссылаются на него
        driver.execute("DELETE FROM %sgifts WHERE id='%s'".formatted(driver.getPrefix(), id));
    }
}
