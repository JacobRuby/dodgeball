package dev.jacobruby.util;

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
            Duration.ofMillis(fadeIn * 50L), // WHY ARE THESE IN JAVA DURATIONS, PAPER?
            Duration.ofMillis(stay * 50L),
            Duration.ofMillis(fadeOut * 50L)
        )));
    }
}
