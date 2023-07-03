package dev.jacobruby.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import dev.jacobruby.DodgeBallPlugin;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProtocolUtil {

    public static void sendPacket(@NonNull Player player, @NonNull PacketContainer... packets) {
        for (final PacketContainer packet : packets) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public static void registerListener(@NotNull PacketType type, @NotNull Consumer<PacketEvent> handler, ListenerOptions... options) {
        PacketAdapter.AdapterParameteters parameters = new PacketAdapter.AdapterParameteters()
                .plugin(DodgeBallPlugin.get())
                .listenerPriority(ListenerPriority.NORMAL)
                .options(options)
                .types(type);

        PacketListener listener = new PacketAdapter(parameters) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                handler.accept(event);
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                handler.accept(event);
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
    }
}
