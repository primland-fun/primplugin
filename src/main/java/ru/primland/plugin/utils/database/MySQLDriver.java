package ru.primland.plugin.utils.database;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.primland.plugin.Config;
import ru.primland.plugin.PrimPlugin;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class MySQLDriver {
    private final String prefix;

    private Connection connection;
    private boolean working = false;

    public MySQLDriver(String prefix) {
        this.prefix = prefix;
    }

    public boolean connect(String host, int port, String database, String username, String password) {
        String connectionUri = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
        try {
            connection = DriverManager.getConnection(connectionUri, username, password);
        } catch(SQLException error) {
            PrimPlugin.send(String.join("\n", PrimPlugin.getInstance().getI18n().getStringList("connectionError")));
            error.printStackTrace();
        } finally {
            working = true;
            setupTables();
        }

        return working;
    }

    public boolean isNotWorking() {
        return !working;
    }

    private void handleConnectionDeath() {
        PrimPlugin.send("&eПлагин отключился от базы данных, пытаюсь переподключиться...");
        disconnect();
        PrimPlugin.getInstance().connectToDatabase();
    }

    private void cts(String table, String @NotNull ... columns) throws CommunicationsException, SQLException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("CREATE TABLE IF NOT EXISTS ");
        buffer.append(prefix).append(table);
        buffer.append(" (");
        for(String column : columns)
            buffer.append(column).append(",");

        buffer.deleteCharAt(buffer.length()-1);
        buffer.append(")");

        connection.prepareStatement(buffer.toString()).execute();
    }

    public void setupTables() {
        if(isNotWorking() || connection == null)
            return;

        try {
            cts("players", "name VARCHAR(32) NOT NULL", "gifts JSON", "chat_settings JSON", "reputation JSON",
                    "PRIMARY KEY (name)");

            cts("global_gifts", "id VARCHAR(64) NOT NULL", "type VARCHAR(16) NOT NULL", "data JSON NOT NULL",
                    "PRIMARY KEY (id)");
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    private boolean ses(String table, String key, Object value) throws CommunicationsException, SQLException {
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
        ResultSet result = connection.prepareStatement(buffer.toString()).executeQuery();
        while(result.next())
            exists = result.getInt(1) == 1;

        return exists;
    }

    public boolean playerExists(@NotNull String player) {
        if(isNotWorking() || connection == null)
            return true;

        try {
            return ses("players", "name", player);
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return playerExists(player);
        } catch(SQLException error) {
            error.printStackTrace();
            return true;
        }
    }

    public void addPlayer(@NotNull Player player) {
        if(isNotWorking() || connection == null)
            return;

        Config reputationConfig = Config.load("reputation.yml");
        StringBuilder buffer = new StringBuilder();
        buffer.append("INSERT INTO ").append(prefix).append("players VALUES (");
        buffer.append("'").append(player.getName()).append("', ");
        buffer.append("'[]', ");
        buffer.append("'{\"sound\": \"$DEFAULT\", \"messages\": [], \"listen\": []}', ");
        buffer.append("'{\"value\": ").append(reputationConfig.getInteger("defaultReputation", 0));
        buffer.append(", \"lastGiveOrTake\": -1}')");

        try {
            connection.prepareStatement(buffer.toString()).execute();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    public void updateReputation(@NotNull String player, int count) {
        if(isNotWorking() || connection == null)
            return;

        StringBuilder buffer = new StringBuilder();
        buffer.append("UPDATE ").append(prefix).append("players SET reputation=JSON_SET(reputation, '$.value', ");
        buffer.append(count);
        buffer.append(") WHERE name='").append(player).append("'");

        try {
            connection.prepareStatement(buffer.toString()).executeUpdate();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    public void updateLastGiveOrTake(@NotNull String player) {
        if(isNotWorking() || connection == null)
            return;

        StringBuilder buffer = new StringBuilder();
        buffer.append("UPDATE ").append(prefix).append("players SET reputation=JSON_SET(reputation, '$.lastGiveOrTake', ");
        buffer.append(Timestamp.valueOf(LocalDateTime.now()).getTime());
        buffer.append(") WHERE name='").append(player).append("'");

        try {
            connection.prepareStatement(buffer.toString()).executeUpdate();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    public boolean canNotGiveOrTake(String player) {
        if(isNotWorking() || connection == null)
            return true;

        ResultSet dbPlayer = getPlayer(player);
        try {
            String json = "";
            while(dbPlayer.next())
                json = dbPlayer.getString("reputation");

            long lastGiveOrTake = Reputation.fromJSON(json).getLastGiveOrTake();
            if(lastGiveOrTake == -1)
                return false;

            LocalDateTime nextOpportunity = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastGiveOrTake),
                    TimeZone.getDefault().toZoneId()).plusDays(7);

            LocalDateTime now = LocalDateTime.now();
            return !now.isAfter(nextOpportunity) && !now.isEqual(nextOpportunity);
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return true;
        } catch(SQLException error) {
            error.printStackTrace();
            return true;
        }
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

    public void addMessage(String player, Message message) {
        if(isNotWorking() || connection == null)
            return;

        StringBuilder buffer = new StringBuilder();
        buffer.append("UPDATE ").append(prefix).append("players SET chat_settings=JSON_ARRAY_APPEND(chat_settings, '$.messages', ");
        buffer.append(message.toJSON());
        buffer.append(") WHERE name='").append(player).append("'");

        try {
            connection.prepareStatement(buffer.toString()).executeUpdate();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    public ChatSettings getChatSettings(String player) {
        if(isNotWorking() || connection == null)
            return null;

        ResultSet dbPlayer = getPlayer(player);
        try {
            String json = "";
            while(dbPlayer.next())
                json = dbPlayer.getString("chat_settings");

            return (new Gson()).fromJson(json, ChatSettings.class);
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return null;
        } catch(SQLException error) {
            error.printStackTrace();
            return null;
        }
    }

    public int getReputation(String player) {
        Config reputationConfig = Config.load("reputation.yml");
        if(isNotWorking() || connection == null)
            return reputationConfig.getInteger("defaultReputation", 0);

        ResultSet dbPlayer = getPlayer(player);
        try {
            String json = "";
            while(dbPlayer.next())
                json = dbPlayer.getString("reputation");

            return Reputation.fromJSON(json).getValue();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
            return reputationConfig.getInteger("defaultReputation", 0);
        } catch(SQLException error) {
            error.printStackTrace();
            return reputationConfig.getInteger("defaultReputation", 0);
        }
    }

    public void clearMessages(String player) {
        if(isNotWorking() || connection == null)
            return;

        StringBuilder buffer = new StringBuilder();
        buffer.append("UPDATE ").append(prefix).append("players SET chat_settings=JSON_SET(chat_settings, ");
        buffer.append("'$.messages', []");
        buffer.append(") WHERE name='").append(player).append("'");

        try {
            connection.prepareStatement(buffer.toString()).executeUpdate();
        } catch(CommunicationsException error) {
            handleConnectionDeath();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }

    public ResultSet getPlayer(@NotNull String player) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SELECT * FROM ").append(prefix).append("players ");
        buffer.append("WHERE name='").append(player).append("'");

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

    public void disconnect() {
        if(isNotWorking() || connection == null)
            return;

        working = false;

        try {
            connection.close();
        } catch(SQLException error) {
            error.printStackTrace();
        }
    }
}
