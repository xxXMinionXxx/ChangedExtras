package com.katt.changedextras.client.discovery;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraftforge.network.ServerStatusPing;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class DiscoveryStatusPinger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<Connection> connections = new CopyOnWriteArrayList<>();

    public void pingServer(ServerData serverData, BiConsumer<ServerData, ServerStatusPing> callback) throws UnknownHostException {
        ServerAddress serverAddress = ServerAddress.parseString(serverData.ip);
        Optional<InetSocketAddress> resolved = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
        if (resolved.isEmpty()) {
            onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, serverData);
            callback.accept(serverData, null);
            return;
        }

        InetSocketAddress socketAddress = resolved.get();
        Connection connection = Connection.connectToServer(socketAddress, false);
        this.connections.add(connection);
        serverData.motd = Component.translatable("multiplayer.status.pinging");
        serverData.status = CommonComponents.EMPTY;
        serverData.ping = -1L;
        serverData.playerList = Collections.emptyList();

        connection.setListener(new ClientStatusPacketListener() {
            private boolean success;
            private boolean receivedPing;
            private long pingStart;

            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
                if (this.receivedPing) {
                    connection.disconnect(Component.translatable("multiplayer.status.unrequested"));
                    return;
                }

                this.receivedPing = true;
                ServerStatus serverStatus = packet.status();
                serverData.motd = serverStatus.description();
                serverStatus.version().ifPresentOrElse(version -> {
                    serverData.version = Component.literal(version.name());
                    serverData.protocol = version.protocol();
                }, () -> {
                    serverData.version = Component.translatable("multiplayer.status.old");
                    serverData.protocol = 0;
                });
                serverStatus.players().ifPresentOrElse(players -> {
                    serverData.status = formatPlayerCount(players.online(), players.max());
                    serverData.players = players;
                    if (!players.sample().isEmpty()) {
                        List<Component> playerList = new ArrayList<>(players.sample().size());
                        for (GameProfile gameProfile : players.sample()) {
                            playerList.add(Component.literal(gameProfile.getName()));
                        }
                        serverData.playerList = playerList;
                    } else {
                        serverData.playerList = List.of();
                    }
                }, () -> serverData.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY));

                this.pingStart = Util.getMillis();
                connection.send(new ServerboundPingRequestPacket(this.pingStart));
                this.success = true;
                callback.accept(serverData, serverStatus.forgeData().orElse(null));
            }

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket packet) {
                serverData.ping = Util.getMillis() - this.pingStart;
                connection.disconnect(Component.translatable("multiplayer.status.finished"));
            }

            @Override
            public void onDisconnect(Component reason) {
                if (!this.success) {
                    DiscoveryStatusPinger.this.onPingFailed(reason, serverData);
                    callback.accept(serverData, null);
                }
            }

            @Override
            public boolean isAcceptingMessages() {
                return connection.isConnected();
            }
        });

        try {
            connection.send(new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.STATUS));
            connection.send(new ServerboundStatusRequestPacket());
        } catch (Throwable throwable) {
            LOGGER.error("Failed to ping server {}", serverAddress, throwable);
            callback.accept(serverData, null);
        }
    }

    public void tick() {
        Iterator<Connection> iterator = Lists.newArrayList(this.connections).iterator();
        while (iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connection.isConnected()) {
                connection.tick();
                continue;
            }

            this.connections.remove(connection);
            connection.handleDisconnection();
        }
    }

    public void removeAll() {
        Iterator<Connection> iterator = Lists.newArrayList(this.connections).iterator();
        while (iterator.hasNext()) {
            Connection connection = iterator.next();
            if (connection.isConnected()) {
                connection.disconnect(Component.translatable("multiplayer.status.cancelled"));
            }
            this.connections.remove(connection);
        }
    }

    private void onPingFailed(Component reason, ServerData serverData) {
        LOGGER.debug("Can't ping {}: {}", serverData.ip, reason.getString());
        serverData.motd = Component.translatable("multiplayer.status.cannot_connect").withStyle(style -> style.withColor(-65536));
        serverData.status = CommonComponents.EMPTY;
    }

    private static Component formatPlayerCount(int online, int max) {
        return Component.literal(Integer.toString(online))
                .append(Component.literal("/").withStyle(ChatFormatting.DARK_GRAY))
                .append(Integer.toString(max))
                .withStyle(ChatFormatting.GRAY);
    }
}
