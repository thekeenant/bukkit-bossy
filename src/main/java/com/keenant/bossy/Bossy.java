package com.keenant.bossy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Bossy {
    private static List<Plugin> plugins = new ArrayList<>();

    private final Plugin plugin;
    private final Map<Player,BossBar> bossBars;
    private final BukkitRunnable respawnTask;

    public Bossy(@Nonnull Plugin plugin) {
        this(plugin, 5);
    }

    public Bossy(@Nonnull Plugin plugin, int frequency) throws IllegalArgumentException {
        if (plugins.contains(plugin))
            throw new IllegalArgumentException("Bossy already registered with " + plugin.getName());
        plugins.add(plugin);

        this.plugin = plugin;
        this.bossBars = new HashMap<>();
        this.respawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (BossBar bar : bossBars.values()) {
                    if (bar.isSpawned()) {
                        bar.setLocation(getDistantLocation(bar.getPlayer()));
                        teleport(bar);
                    }
                }
            }
        };
        this.respawnTask.runTaskTimer(this.plugin, 0, frequency);
    }

    /**
     * Reveal the boss bar to a player. Also creates a boss bar
     * for the player if it didn't exist.
     * @param player
     */
    public void show(Player player) {
        BossBar bar = getBossBar(player);

        if (bar == null)
            bar = newBossBar(player, "null", 1F);

        if (bar.isSpawned())
            despawn(bar);
        spawn(bar, getDistantLocation(player));
    }

    /**
     * Hides the boss bar from a player.
     * @param player
     */
    public void hide(Player player) {
        BossBar bar = getBossBar(player);
        if (bar != null && bar.isSpawned())
            despawn(bar);
    }

    /**
     * Sets the text of the boss bar for a player. Also
     * creates the boss bazr if it didn't exist.
     * @param player
     * @param text
     */
    public void setText(Player player, String text) {
        BossBar bar = getBossBar(player);

        if (bar == null)
            bar = newBossBar(player, text, 1F);
        else
            bar.setText(text);

        if (bar.isSpawned()) {
            render(bar);
        } else spawn(bar, getDistantLocation(player));
    }

    /**
     * Sets the percent of the boss bar for a player. Also
     * creates the boss bazr if it didn't exist.
     * @param player
     * @param percent A value in the range [0,1]
     */
    public void setPercent(Player player, float percent) {
        BossBar bar = getBossBar(player);

        if (bar == null)
            bar = newBossBar(player, "null", percent);
        else
            bar.setHealth(convertToHealth(percent));

        if (bar.isSpawned()) {
            render(bar);
        } else spawn(bar, getDistantLocation(player));
    }

    /**
     * Sets the text and percent of the boss bar for a player.
     * Also creates the boss bazr if it didn't exist.
     * @param player
     * @param text
     * @param percent A value in the range [0,1]
     */
    public void set(Player player, String text, float percent) {
        BossBar bar = getBossBar(player);

        if (bar == null) {
            bar = newBossBar(player, text, percent);
        }
        else {
            bar.setText(text);
            bar.setHealth(convertToHealth(percent));
        }

        if (bar.isSpawned())
            despawn(bar);
        spawn(bar, getDistantLocation(player));
    }

    @Nullable
    private BossBar getBossBar(Player player) {
        return bossBars.get(player);
    }

    private BossBar newBossBar(Player player, String text, float percent) {
        BossBar bossBar = new BossBar(player, text, convertToHealth(percent), null, false);
        bossBars.put(player, bossBar);
        return bossBar;
    }

    private int convertToHealth(float percent) {
        return (int) Math.floor(percent * 300.0F);
    }

    private Location getDistantLocation(Player player) {
        Location location = player.getEyeLocation().clone();
        location.setPitch(location.getPitch() - 21);

        return location.add(location.getDirection().normalize().multiply(50));
    }

    private void render(BossBar bar) {
        PacketContainer metadataPacket = new PacketContainer(Server.ENTITY_METADATA);
        metadataPacket.getModifier().write(0, CUSTOM_ID);
        metadataPacket.getWatchableCollectionModifier().write(0, bar.getDataWatcher().getWatchableObjects());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(bar.getPlayer(), metadataPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void teleport(BossBar bar) {
        PacketContainer teleportPacket = new PacketContainer(Server.ENTITY_TELEPORT);
        StructureModifier<Object> teleportPacketModifier = teleportPacket.getModifier();

        teleportPacketModifier.write(0, CUSTOM_ID);
        teleportPacketModifier.write(1,  (bar.getLocation().getBlockX() * 32));
        teleportPacketModifier.write(2,  (bar.getLocation().getBlockY() * 32));
        teleportPacketModifier.write(3,  (bar.getLocation().getBlockZ() * 32));
        teleportPacketModifier.write(4, (byte) (bar.getLocation().getYaw() * 256 / 360));
        teleportPacketModifier.write(5, (byte) (bar.getLocation().getPitch() * 256 / 360));
        teleportPacketModifier.write(6, true);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(bar.getPlayer(), teleportPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void spawn(BossBar bar, Location location) {
        bar.setSpawned(true);
        bar.setLocation(location);

        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        StructureModifier<Object> spawnPacketModifier = spawnPacket.getModifier();
        spawnPacketModifier.write(0, CUSTOM_ID);
        spawnPacketModifier.write(1, (byte) 64);
        spawnPacketModifier.write(2, bar.getLocation().getBlockX() * 32);
        spawnPacketModifier.write(3, bar.getLocation().getBlockY() * 32);
        spawnPacketModifier.write(4, bar.getLocation().getBlockZ() * 32);

        spawnPacket.getDataWatcherModifier().write(0, bar.getDataWatcher());

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(bar.getPlayer(), spawnPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void despawn(BossBar bar) {
        bar.setSpawned(false);
        bar.setLocation(null);

        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        spawnPacket.getIntegerArrays().write(0, new int[] { CUSTOM_ID });

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(bar.getPlayer(), spawnPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Data
    @AllArgsConstructor
    private class BossBar {
        @Getter private final Player player;
        @Getter private String text;
        @Getter private int health;
        @Getter private Location location;
        @Getter private boolean spawned;

        public BossBar(Player player, String text, int health) {
            this.player = player;
            this.text = text;
            this.health = health;
        }

        private WrappedDataWatcher getDataWatcher() {
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 0x20); // invisible
            watcher.setObject(1, (short) 300); // air ticks
            watcher.setObject(2, this.text); // name
            watcher.setObject(3, (byte) 1); // show name
            watcher.setObject(6, (float) this.health, true); // Set health
            watcher.setObject(7, (int) Color.BLACK.asRGB()); // potion color
            watcher.setObject(8, (byte) 0); // potion ambient
            watcher.setObject(15, (byte) 1); // No AI
            watcher.setObject(20, 819); // Invulnerable
            return watcher;
        }
    }

    private static int CUSTOM_ID;

    static {
        try {
            String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
            Field field = Class.forName("net.minecraft.server." + version + ".Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            CUSTOM_ID = field.getInt(null);
            field.set(null, CUSTOM_ID + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}