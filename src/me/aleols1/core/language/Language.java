package me.aleols1.core.language;

import me.aleols1.core.Main;
import net.md_5.bungee.api.ChatColor;
import java.util.Map;

public class Language {
    private static Map<String, Object> messages;

    public static void init(Main plugin) {
        plugin.saveResource("language.yml", false);
        messages = plugin.getConfig().getConfigurationSection("messages").getValues(true);
    }

    public static String get(String key, Map<String, String> placeholders) {
        String message = (String) messages.getOrDefault(key, "Missing lang: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}