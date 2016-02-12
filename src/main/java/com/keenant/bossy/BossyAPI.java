package com.keenant.bossy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BossyAPI {
    private static Plugin plugin;
    private static Bossy instance;

    static {
        plugin = Bukkit.getPluginManager().getPlugins()[0];
        instance = new Bossy(plugin);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Bossy getInstance() {
        return instance;
    }

    public static void show(Player player) {
        instance.show(player);
    }

    public void hide(Player player) {
        instance.hide(player);
    }

    public void setText(Player player, String text) {
        instance.setText(player, text);
    }

    public void setPercent(Player player, float percent) {
        instance.setPercent(player, percent);
    }

    public void set(Player player, String text, float percent) {
        instance.set(player, text, percent);
    }
}
