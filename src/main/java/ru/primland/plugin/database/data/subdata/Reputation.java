package ru.primland.plugin.database.data.subdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.primland.plugin.utils.Utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor
public class Reputation {
    // Количество репутации у игрока
    private int value;

    // Время последнего использования /rep +|-
    private long lastAction;

    /**
     * Установить время последнего использования /rep +|-
     * @param time Новое время
     */
    public void setLastAction(LocalDateTime time) {
        lastAction = Timestamp.valueOf(time).getTime();
    }

    /**
     * Получить время последнего использования /rep +|- как {@link LocalDateTime}
     * @return {@link LocalDateTime}
     */
    public LocalDateTime getLastActionAsDateTime() {
        return Utils.convertTimestampToTime(lastAction);
    }
}
