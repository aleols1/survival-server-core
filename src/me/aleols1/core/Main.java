package me.aleols1.core;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("Survival core startet");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("Survival core stoppet");
        super.onDisable();
    }
}
