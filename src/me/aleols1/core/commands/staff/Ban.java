package me.aleols1.core.commands.staff;

import me.aleols1.core.database.Database;
import me.aleols1.core.language.Language;
import me.aleols1.core.logs.DiscordWebhook;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Ban implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String perm = "core.command." + cmd.getName().toLowerCase();
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(Language.server("Noperms"));
            DiscordWebhook.sendNoPermsEmbed(sender.getName(), cmd.getName());
            return true;
        }

        if (args.length < 2) return false;

        String target = args[0];
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        String actor = sender.getName();

        if (cmd.getName().equalsIgnoreCase("ban")) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, null, actor);
            sendMessage(sender, "ban-success", target, reason, null);
            DiscordWebhook.sendBanEmbed(actor, target, reason);
        } else if (cmd.getName().equalsIgnoreCase("tempban")) {
            if (args.length < 3) return false;
            String timeStr = args[1];
            reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

            long durationMillis = parseTimeToMillis(timeStr);
            Date expire = Date.from(Instant.now().plusMillis(durationMillis));
            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, expire, actor);

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO bans (player, reason, until, bywho) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, target);
                ps.setString(2, reason);
                ps.setTimestamp(3, new java.sql.Timestamp(expire.getTime()));
                ps.setString(4, actor);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            sendMessage(sender, "tempban-success", target, reason, timeStr);
            DiscordWebhook.sendTempbanEmbed(actor, target, reason, timeStr);
        } else if (cmd.getName().equalsIgnoreCase("unban")) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(target);
            sendMessage(sender, "unban-success", target, reason, null);
            DiscordWebhook.sendUnbanEmbed(actor, target, reason);
        }
        return true;
    }

    private void sendMessage(CommandSender sender, String key, String target, String reason, String duration) {
        Map<String, String> ph = new HashMap<>();
        ph.put("target", target);
        ph.put("reason", reason);
        if (duration != null) ph.put("duration", duration);
        sender.sendMessage(Language.get(key, ph));
    }

    private long parseTimeToMillis(String s) {
        long unit = 1000L;
        if (s.endsWith("s")) unit = 1000L;
        else if (s.endsWith("m")) unit = 60 * 1000L;
        else if (s.endsWith("h")) unit = 60 * 60 * 1000L;
        else if (s.endsWith("d")) unit = 24 * 60 * 60 * 1000L;
        else if (s.endsWith("w")) unit = 7 * 24 * 60 * 60 * 1000L;

        return Long.parseLong(s.replaceAll("\\D", "")) * unit;
    }
}
