package dev.jacobruby.util;

import io.papermc.paper.util.Tick;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class Title {
    private Title() {
    }

    public static void sendAll(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void send(Player player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle, net.kyori.adventure.title.Title.Times.times(
            Tick.of(fadeIn), // WHY ARE THESE IN JAVA DURATIONS, PAPER?
            Tick.of(stay),
            Tick.of(fadeOut)
        )));
    }
}
