package ru.primland.plugin.database.data.subdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Balance {
    // Количество репутационной валюты
    private int reputation;

    // Количество донат-валюты
    private int donate;
}
