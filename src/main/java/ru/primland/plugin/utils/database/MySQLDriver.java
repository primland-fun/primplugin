package ru.primland.plugin.utils.database;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;
import ru.primland.plugin.utils.database.data.*;

import java.sql.*;
import java.util.List;

@Getter @RequiredArgsConstructor
public class MySQLDriver {
    private final String prefix;

    private Connection connection;
    private boolean working = false;

    /**
     * Подключение к базе данных и создание/обновление таблиц
     *
     * @param host     IP хостинга с СУБД
     * @param port     Порт СУБД
     * @param database Название базы данных, используемой плагином
     * @param username Имя пользователя для авторизации
     * @param password Пароль пользователя для авторизации
     */
    public void connect(String host, int port, String database, String username, String password) {
        String connectionUri = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
        try {
            connection = DriverManager.getConnection(connectionUri, username, password);
        } catch (SQLException error) {
            PrimPlugin.send(String.join("\n", PrimPlugin.i18n.getStringList("connectionError")));
            error.printStackTrace();
        } finally {
            working = true;
            setupTables();
        }
    }

    /**
     * Обработка потери соединения с базой данных
     */
    public void handleConnectionDeath() {
        PrimPlugin.send("&eПлагин отключился от базы данных, пытаюсь переподключиться...");
        disconnect();
        PrimPlugin.instance.connectToDatabase();
    }

    /**
     * Выполнить указанную операцию
     * @param sql Строка-операция
     */
    public void execute(String sql) {
        if(!isWorking() || connection == null)
            return;

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            statement.close();
        } catch(CommunicationsException error) {
            // Если всё пошло по жопе и соединение сдохло,
            // то восстанавливаем его
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    /**
     * Выполнить указанную операцию поиска
     * @param sql Строка-операция
     * @return {@link ResultSet}
     */
    public ResultSet executeQuery(String sql) {
        if(!isWorking() || connection == null)
            return null;

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            return statement.executeQuery();
        } catch(CommunicationsException error) {
            // Если всё пошло по жопе и соединение сдохло,
            // то восстанавливаем его
            handleConnectionDeath();
            return null;
        } catch(SQLException error) {
            error.printStackTrace();
            return null;
        }
    }

    /**
     * Выполнить операцию создания таблицы
     *
     * @param table   Название таблицы
     * @param columns Столбцы таблицы
     */
    private void createTable(String table, String @NotNull ... columns) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("CREATE TABLE IF NOT EXISTS ");  // "Создать таблицу если не существует"
        buffer.append(prefix).append(table);           // = префикс из конфига + название таблицы
        buffer.append(" (");
        for(String column : columns)                   // Проходимся по списку со столбцами и добавляем к выражению
            buffer.append(column).append(",");

        buffer.deleteCharAt(buffer.length()-1);  // Удаляем последнюю запятую (она лишняя)
        buffer.append(")");

        // Создаём, выполняем и закрываем выражение (обращение к СУБД)
        execute(buffer.toString());
    }

    /**
     * Создать все нужные таблицы
     */
    public void setupTables() {
        if(!isWorking() || connection == null)
            return;

        // Таблица с игроками сервера
        // -------------------------------------------------------------------
        // name                  - ник игрока
        // daily_strike          - количество дней подряд использования /daily
        //
        // chat.sound            - название звука приватных сообщений
        // chat.lastReceived     - ник последнего написавшего в ЛС игрока
        // chat.flags            - флаги (настройки) чата
        //
        // admin.spy             - флаги "шпионажа"
        //
        // cards                 - карточки, имеющиеся у игрока
        //
        // balance.reputation    - количество репутационной валюты у игрока
        // balance.donate        - количество донат-валюты у игрока
        //
        // reputation.value      - количество репутации у игрока
        // reputation.lastAction - дата последнего использования /rep +|-
        createTable("players",
                "name VARCHAR(16) NOT NULL",
                "daily_strike INT",
                "`chat.sound` TEXT",
                "`chat.lastReceived` VARCHAR(16)",
                "`chat.flags` INT",
                "`admin.spy` INT",
                "cards JSON",                      // Список со строками формата "<редкость> <id>"
                "`balance.reputation` INT",
                "`balance.donate` INT",
                "`reputation.value` INT",
                "`reputation.lastAction` BIGINT",  // BIGINT = Long = Timestamp из Java
                "PRIMARY KEY (name)");

        // Таблица приватных сообщений
        // -----------------------------------
        // sender   - отправитель сообщения
        // receiver - получатель сообщения
        //
        // content  - содержимое сообщения
        // time     - время отправки сообщения
        createTable("messages",
                "sender VARCHAR(16) NOT NULL",
                "receiver VARCHAR(16) NOT NULL",
                "content TEXT",
                "time BIGINT",
                "PRIMARY KEY (receiver)");

        // Таблица подарков
        // ----------------------------------------------------------------------
        // id       - ID подарка, несколько подарков могут иметь 1 и тот же ID
        //            при условии, если 1 из них - глобальный подарок
        // name     - название подарка, опционально
        //
        // type     - тип подарка; 1 - глобальный, 2 - локальный (для 1 игрока)
        // content  - содержимое подарка (список с данными)
        //   * type - тип... вещи из подарка?
        //   * data - данные этой вещи
        //
        // receiver - получатель подарка; указывать только для локальных подарков
        createTable("gifts",
                "id VARCHAR(32) NOT NULL",
                "name VARCHAR(64)",
                "type TINYINT NOT NULL",
                "content JSON NOT NULL",
                "receiver VARCHAR(16)",
                "PRIMARY KEY (id)");

        // Таблица логов (+ телеметрии)
        // -----------------------------------------
        // action  - действие, которое было записано
        // time    - время совершения действия
        //
        // message - текстовое сообщение о действии
        // data    - данные действия
        createTable("logs",
                "action VARCHAR(64) NOT NULL",
                "time BIGINT NOT NULL",
                "message TEXT NOT NULL",
                "data JSON NOT NULL");
    }

    /**
     * Проверяет, существует ли в таблице объект
     * с указанными данными
     *
     * @param table Название таблицы для поиска
     * @param key   Ключ для проверки
     * @param value Значение, которое должен иметь указанный ключ
     *
     * @return true, если объект существует, иначе false
     *
     * @throws CommunicationsException Ошибка потери соединения с базой данных
     * @throws SQLException            Другие ошибки SQL
     */
    private boolean isExists(String table, String key, Object value) throws CommunicationsException, SQLException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SELECT EXISTS (SELECT 1 FROM ");
        buffer.append(prefix).append(table);
        buffer.append(" WHERE ");
        buffer.append(key).append("=");
        if(value instanceof CharSequence)
            buffer.append("'");

        buffer.append(value);
        if(value instanceof CharSequence)
            buffer.append("'");

        buffer.append(" LIMIT 1)");

        boolean exists = false;
        ResultSet result = executeQuery(buffer.toString());
        while(result.next())
            exists = result.getInt(1) == 1;

        return exists;
    }

    public boolean playerExists(@NotNull String player) {
        if(!isWorking() || connection == null)
            return true;

        try {
            return isExists("players", "name", player);
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return playerExists(player);
        } catch(SQLException error) {
            error.printStackTrace();
            return true;
        }
    }

    public void addPlayer(@NotNull Player player) {
        Config reputationConfig = Config.load("reputation.yml");
        execute("INSERT INTO %splayers VALUES ('%s', 0, NULL, NULL, 0, 0, '[]', 0, 0, %d, -1)".formatted(
                prefix, player.getName(), reputationConfig.getInteger("defaultReputation", 0)));
    }

    public ResultSet getTopByReputation(int limit) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SELECT * FROM ").append(prefix).append("players ");
        buffer.append("ORDER BY JSON_EXTRACT(reputation, '$.value') DESC LIMIT ").append(limit);

        try {
            return connection.prepareStatement(buffer.toString()).executeQuery();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return null;
        } catch(SQLException error) {
            error.printStackTrace();
            return null;
        }
    }

    public PrimPlayer getPlayer(@NotNull String name) {
        try {
            ResultSet result = executeQuery("SELECT * FROM %splayers WHERE name='%s'".formatted(prefix, name));
            if(!result.next())
                return null;

            return new PrimPlayer(
                    this, name,
                    result.getInt("daily_strike"),
                    new ChatOptions(
                            result.getString("chat.sound"),
                            result.getString("chat.lastReceived"),
                            ChatOptions.toFlags(result.getInt("chat.flags"))
                    ),
                    new AdminOptions(AdminOptions.toSpyFlags(result.getInt("admin.spy"))),
                    Card.toCardList((new Gson()).fromJson(result.getString("cards"), List.class)),
                    new Balance(
                            result.getInt("balance.reputation"),
                            result.getInt("balance.donate")
                    ),
                    new Reputation(
                            result.getInt("reputation.value"),
                            result.getLong("reputation.lastAction")
                    )
            );
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return null;
        } catch(SQLException error) {
            error.printStackTrace();
            return null;
        }
    }

    public void disconnect() {
        if(!isWorking() || connection == null)
            return;

        working = false;

        try {
            connection.close();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }
}
