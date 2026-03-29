package com.katt.changedextras.client.discovery;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class DiscoveryServerList extends ObjectSelectionList<DiscoveryServerList.Entry> {
    private final DiscoveryScreen screen;

    public DiscoveryServerList(DiscoveryScreen screen, Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.screen = screen;
    }

    public void updateEntries(List<DiscoveryTrackedServer> visibleServers) {
        this.clearEntries();
        for (DiscoveryTrackedServer visibleServer : visibleServers) {
            this.addEntry(new Entry(visibleServer));
        }
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.screen.onSelectedChange();
    }

    public final class Entry extends ObjectSelectionList.Entry<Entry> {
        private final DiscoveryTrackedServer trackedServer;
        private long lastClickTime;

        private Entry(DiscoveryTrackedServer trackedServer) {
            this.trackedServer = trackedServer;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            ServerData server = this.trackedServer.serverData();
            guiGraphics.drawString(minecraft.font, server.name, left + 3, top + 1, 16777215, false);

            List<FormattedCharSequence> motd = minecraft.font.split(server.motd, width - 70);
            for (int i = 0; i < Math.min(motd.size(), 2); i++) {
                guiGraphics.drawString(minecraft.font, motd.get(i), left + 3, top + 12 + 9 * i, 8421504, false);
            }

            Component status = server.ping >= 0L
                    ? Component.translatable("changedextras.discovery.ping", server.ping)
                    : Component.translatable("changedextras.discovery.ping_pending");
            int statusWidth = minecraft.font.width(status);
            guiGraphics.drawString(minecraft.font, status, left + width - statusWidth - 3, top + 1, 8421504, false);

            String address = minecraft.options.hideServerAddress
                    ? Component.translatable("selectServer.hiddenAddress").getString()
                    : server.ip;
            guiGraphics.drawString(minecraft.font, address, left + 3, top + 32, 3158064, false);
            if (!server.status.getString().isEmpty()) {
                int playerWidth = minecraft.font.width(server.status);
                guiGraphics.drawString(minecraft.font, server.status, left + width - playerWidth - 3, top + 32, 3158064, false);
            }
        }

        @Override
        public Component getNarration() {
            MutableComponent narration = Component.empty()
                    .append(Component.translatable("narrator.select", this.trackedServer.serverData().name))
                    .append(CommonComponents.NARRATION_SEPARATOR)
                    .append(this.trackedServer.serverData().motd);
            if (this.trackedServer.serverData().players != null) {
                narration.append(CommonComponents.NARRATION_SEPARATOR).append(this.trackedServer.serverData().status);
            }
            return narration;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            DiscoveryServerList.this.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                screen.joinSelectedServer();
            }

            this.lastClickTime = Util.getMillis();
            return true;
        }

        public DiscoveryTrackedServer trackedServer() {
            return this.trackedServer;
        }
    }
}
