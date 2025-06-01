package me.aleols1.core.language;


import me.aleols1.core.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class Language {
    private static Map<String, Object> messages;
    private static Map<String, Object> serverInfo;

    public static void init(Main plugin) {
        plugin.saveResource("language.yml", false);

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "language.yml")
        );

        messages = langConfig.getConfigurationSection("messages").getValues(true);
        serverInfo = langConfig.getConfigurationSection("Server").getValues(true);
    }

    public static String get(String key, Map<String, String> placeholders) {
        String message = (String) messages.getOrDefault(key, "Missing lang: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String server(String key) {
        String value = (String) serverInfo.getOrDefault(key, "Missing server: " + key);
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}