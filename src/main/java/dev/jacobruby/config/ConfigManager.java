package dev.jacobruby.config;

import dev.jacobruby.util.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.List;

public class ConfigManager {

    private final Plugin plugin;

    private FileConfiguration config;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;

//        plugin.saveDefaultConfig();
        plugin.saveResource("config.yml", true); // Temp: replace every time while testing

        this.config = plugin.getConfig();
    }

    public void reload() {
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
    }

    public World getWorld(String path) {
        return Bukkit.getWorld(this.config.getString(path));
    }

    public Position getPosition(String path) {
        return Position.deserialize(this.config.getString(path));
    }

    public List<Location> getPositionListAsLocations(String path, World world) {
        return this.config.getStringList(path).stream().map(Position::deserialize).map(p -> p.toLocation(world)).toList();
    }

    public BoundingBox getBoundingBox(String path) {
        ConfigurationSection section = this.config.getConfigurationSection(path);

        Position a = Position.deserialize(section.getString("a"));
        Position b = Position.deserialize(section.getString("b"));

        return BoundingBox.of(a.toVector(), b.toVector());
    }

    @SuppressWarnings("unchecked")
    public EntityType<? extends PathfinderMob> getEntityType(String path) {
        return (EntityType<? extends PathfinderMob>)
                EntityType.byString(this.config.getString(path)).orElseThrow(() -> new IllegalArgumentException("Unknown entity type"));
    }

    public File getDataFile(String path) {
        String filePath = this.config.getString(path);
        return new File(this.plugin.getDataFolder(), filePath);
    }

    public List<String> getCommands(String path, Player subject) {
        return this.config.getStringList(path).stream().map(cmd -> cmd.replaceAll("%player%", subject.getName())).toList();
    }

    public void dispatchCommands(String path, Player subject) {
        for (String command : getCommands(path, subject)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

}
