package me.aleols1.core.commands.staff;

import me.aleols1.core.database.Database;
import me.aleols1.core.language.Language;
import me.aleols1.core.logs.DiscordWebhook;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            try {
                DiscordWebhook.sendNoPermsEmbed(sender.getName(), cmd.getName());
            } catch (Exception ignored) {}
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ban")) {
            if (args.length < 2) return false;

            String target = args[0];
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            String actor = sender.getName();

            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, null, actor);
            kickIfOnline(target, reason);
            sendMessage(sender, "ban-success", target, reason, null);

            try {
                DiscordWebhook.sendBanEmbed(actor, target, reason);
            } catch (Exception e) {
                sender.sendMessage("§c[Feil] Klarte ikke sende ban til Discord webhook.");
            }

        } else if (cmd.getName().equalsIgnoreCase("tempban")) {
            if (args.length < 3) return false;

            String target = args[0];
            String timeStr = args[1];
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            String actor = sender.getName();

            long durationMillis = parseTimeToMillis(timeStr);
            Date expire = Date.from(Instant.now().plusMillis(durationMillis));

            Bukkit.getBanList(BanList.Type.NAME).addBan(target, reason, expire, actor);
            kickIfOnline(target, reason);

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO bans (player, reason, until, bywho) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, target);
                ps.setString(2, reason);
                ps.setTimestamp(3, new java.sql.Timestamp(expire.getTime()));
                ps.setString(4, actor);
                ps.executeUpdate();
            } catch (SQLException e) {
                sender.sendMessage("§c[Feil] Kunne ikke lagre ban til databasen.");
                e.printStackTrace();
            }

            sendMessage(sender, "tempban-success", target, reason, timeStr);

            try {
                DiscordWebhook.sendTempbanEmbed(actor, target, reason, timeStr);
            } catch (Exception e) {
                sender.sendMessage("§c[Feil] Klarte ikke sende tempban til Discord webhook.");
            }

        } else if (cmd.getName().equalsIgnoreCase("unban")) {
            if (args.length < 1) return false;

            String target = args[0];
            String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Ingen grunn spesifisert";
            String actor = sender.getName();

            Bukkit.getBanList(BanList.Type.NAME).pardon(target);
            sendMessage(sender, "unban-success", target, reason, null);

            try {
                DiscordWebhook.sendUnbanEmbed(actor, target, reason);
            } catch (Exception e) {
                sender.sendMessage("§c[Feil] Klarte ikke sende unban til Discord webhook.");
            }
        }

        return true;
    }

    private void kickIfOnline(String target, String reason) {
        Player p = Bukkit.getPlayerExact(target);
        if (p != null && p.isOnline()) {
            p.kickPlayer("Du ble bannlyst for: " + reason);
        }
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
