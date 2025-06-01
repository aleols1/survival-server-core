package me.aleols1.core.logs;

import me.aleols1.core.Main;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhook {

    private static String banWebhook;
    private static String tempbanWebhook;
    private static String unbanWebhook;
    private static String noPermsWebhook;

    public static void init(Main plugin) {
        banWebhook = plugin.getConfig().getString("discord.webhooks.ban");
        tempbanWebhook = plugin.getConfig().getString("discord.webhooks.tempban");
        unbanWebhook = plugin.getConfig().getString("discord.webhooks.unban");
        noPermsWebhook = plugin.getConfig().getString("discord.webhooks.noperms");
    }

    public static void sendBanEmbed(String actor, String target, String reason) {
        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"🔨 Permanent ban\","
                + "\"color\": 16711680,"
                + "\"fields\": ["
                + "{\"name\": \"Spiller\", \"value\": \"" + target + "\", \"inline\": true},"
                + "{\"name\": \"Bannet av\", \"value\": \"" + actor + "\", \"inline\": true},"
                + "{\"name\": \"Grunnlag\", \"value\": \"" + reason + "\"}"
                + "],"
                + "\"footer\": {\"text\": \"Overvåket av SurvivalServerCore\"}"
                + "}]}";

        sendWebhook(banWebhook, json);
    }

    public static void sendTempbanEmbed(String actor, String target, String reason, String duration) {
        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"⏰ Midlertidig ban\","
                + "\"color\": 16753920,"
                + "\"fields\": ["
                + "{\"name\": \"Spiller\", \"value\": \"" + target + "\", \"inline\": true},"
                + "{\"name\": \"Bannet av\", \"value\": \"" + actor + "\", \"inline\": true},"
                + "{\"name\": \"Varighet\", \"value\": \"" + duration + "\", \"inline\": true},"
                + "{\"name\": \"Grunnlag\", \"value\": \"" + reason + "\"}"
                + "],"
                + "\"footer\": {\"text\": \"Overvåket av SurvivalServerCore\"}"
                + "}]}";

        sendWebhook(tempbanWebhook, json);
    }

    public static void sendUnbanEmbed(String actor, String target, String reason) {
        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"✅ Unban utført\","
                + "\"color\": 65280,"
                + "\"fields\": ["
                + "{\"name\": \"Spiller\", \"value\": \"" + target + "\", \"inline\": true},"
                + "{\"name\": \"Utført av\", \"value\": \"" + actor + "\", \"inline\": true},"
                + "{\"name\": \"Grunnlag\", \"value\": \"" + reason + "\"}"
                + "],"
                + "\"footer\": {\"text\": \"Overvåket av SurvivalServerCore\"}"
                + "}]}";

        sendWebhook(unbanWebhook, json);
    }

    public static void sendNoPermsEmbed(String player, String command) {
        String json = "{"
                + "\"embeds\": [{"
                + "\"title\": \"🚫 Forsøk på kommando uten tillatelse\","
                + "\"color\": 16711680,"
                + "\"fields\": ["
                + "{\"name\": \"Spiller\", \"value\": \"" + player + "\", \"inline\": true},"
                + "{\"name\": \"Kommando\", \"value\": \"/" + command + "\", \"inline\": true},"
                + "{\"name\": \"Tidspunkt\", \"value\": \"" + java.time.LocalDateTime.now() + "\"}"
                + "],"
                + "\"footer\": {\"text\": \"Overvåket av SurvivalServerCore\"}"
                + "}]}";

        sendWebhook(noPermsWebhook, json);
    }

    private static void sendWebhook(String url, String payload) {
        if (url == null || url.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes());
                }

                conn.getInputStream().close();
            } catch (Exception e) {
                Bukkit.getLogger().warning("Webhook-feil: " + e.getMessage());
            }
        });
    }
}
