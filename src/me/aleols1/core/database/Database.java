package me.aleols1.core.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection connection;

    public static void init(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String db = config.getString("mysql.database");
        String user = config.getString("mysql.username");
        String pass = config.getString("mysql.password");

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false", user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}