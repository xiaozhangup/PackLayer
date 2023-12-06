package io.th0rgal.packsmanager.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.ResourcePackRequest;
import io.github._4drian3d.vpacketevents.api.event.PacketSendEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(
        id = "packlayer",
        name = "PackLayer",
        version = "${version}",
        description = "${description}",
        authors = {"th0rgal", "xiaozhangup"},
        dependencies = {
                @Dependency(id = "vpacketevents")
        }
)
public class PackLayer {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private static PackLayer INSTANCE;
    private final Map<UUID, String> map = new HashMap<>();

    @Inject
    public PackLayer(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        INSTANCE = this;
    }

    @Subscribe
    public void onPacketReceive(PacketSendEvent event) {
        final MinecraftPacket packet = event.getPacket();
        if (event.getPlayer().getProtocolVersion().getProtocol() == 763) return; // 不能在 1.20.2 处理此数据包

        if (packet instanceof ResourcePackRequest resourcePackRequest) {
            final UUID uuid = event.getPlayer().getUniqueId();
            if (map.containsKey(uuid) && map.get(uuid).equals(resourcePackRequest.getHash())) {
                event.setResult(ResultedEvent.GenericResult.denied());
                return;
            }
            map.put(uuid, resourcePackRequest.getHash());
        }
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        map.remove(event.getPlayer().getUniqueId());
    }
}
