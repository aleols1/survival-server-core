package me.aleols1.core;

import me.aleols1.core.commands.staff.Ban;
import me.aleols1.core.database.Database;
import me.aleols1.core.language.Language;
import me.aleols1.core.logs.DiscordWebhook;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        Language.init(this);
        Database.init(this);
        DiscordWebhook.init(this);

        Ban banCmd = new Ban();
        getCommand("ban").setExecutor(banCmd);
        getCommand("tempban").setExecutor(banCmd);
        getCommand("unban").setExecutor(banCmd);

        getLogger().info("SurvivalServerCore er aktivert.");
    }

    @Override
    public void onDisable() {
        Database.close();
        getLogger().info("SurvivalServerCore er deaktivert.");
    }

    public static Main getInstance() {
        return instance;
    }
}