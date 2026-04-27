package com.katt.changedextras.client;

import com.katt.changedextras.common.LatexSpawnVariantEntry;
import com.katt.changedextras.network.ChangedExtrasNetwork;
import com.katt.changedextras.network.UpdateLatexSpawnRulesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LatexSpawnControlScreen extends Screen {
    private static final int ENTRY_HEIGHT = 24;
    private static final int LIST_PADDING = 8;
    private static final int TOGGLE_WIDTH = 56;
    private static final boolean ENTRY_TOGGLES_ENABLED = false;
    private static final int DAY_TOGGLE_Y = 44;
    private static final int DAY_TOGGLE_HEIGHT = 20;
    private static final int WIP_TEXT_Y = 68;

    private final List<ClientEntry> entries;
    private boolean allowDaySpawns;
    private int firstVisibleEntry;
    private int listLeft;
    private int listTop;
    private int listWidth;
    private int listHeight;
    private int visibleEntryCount;

    public LatexSpawnControlScreen(boolean allowDaySpawns, List<LatexSpawnVariantEntry> variants) {
        super(Component.translatable("screen.changedextras.latex_spawn_control"));
        this.allowDaySpawns = allowDaySpawns;
        this.entries = new ArrayList<>(variants.size());
        for (LatexSpawnVariantEntry variant : variants) {
            this.entries.add(new ClientEntry(variant.entityTypeId(), variant.displayName(), variant.enabled()));
        }
    }

    @Override
    protected void init() {
        super.init();
        configureLayout();
        buildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isInsideList(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int maxScroll = Math.max(0, entries.size() - visibleEntryCount);
        int nextScroll = Math.max(0, Math.min(maxScroll, firstVisibleEntry - (int) Math.signum(delta)));
        if (nextScroll != firstVisibleEntry) {
            firstVisibleEntry = nextScroll;
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderListBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        graphics.drawCenteredString(font, title, centerX, 14, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.changedextras.latex_spawn_control.subtitle"), centerX, 28, 0xB8B8B8);
        graphics.drawCenteredString(font, Component.literal("Work in progress! wait for next update"), centerX, WIP_TEXT_Y, 0xA0A0A0);
        graphics.drawString(font, Component.literal("Entries " + Math.min(entries.size(), firstVisibleEntry + 1) + "-" + Math.min(entries.size(), firstVisibleEntry + visibleEntryCount) + " / " + entries.size()), listLeft, listTop - 12, 0xD0D0D0);
        renderListEntries(graphics);

        if (entries.size() > visibleEntryCount) {
            int scrollbarHeight = Math.max(18, (int) ((listHeight / (float) entries.size()) * visibleEntryCount));
            int scrollbarTravel = Math.max(0, listHeight - scrollbarHeight);
            int maxScroll = Math.max(1, entries.size() - visibleEntryCount);
            int scrollbarY = listTop + (int) ((firstVisibleEntry / (float) maxScroll) * scrollbarTravel);
            graphics.fill(listLeft + listWidth - 5, listTop, listLeft + listWidth - 1, listTop + listHeight, 0xFF2D2D2D);
            graphics.fill(listLeft + listWidth - 5, scrollbarY, listLeft + listWidth - 1, scrollbarY + scrollbarHeight, 0xFFB7B7B7);
        }
    }

    private void configureLayout() {
        listWidth = Math.min(width - 32, 360);
        listLeft = (width - listWidth) / 2;
        listTop = 92;
        listHeight = Math.max(72, height - 168);
        visibleEntryCount = Math.max(1, listHeight / ENTRY_HEIGHT);
        int maxScroll = Math.max(0, entries.size() - visibleEntryCount);
        firstVisibleEntry = Math.min(firstVisibleEntry, maxScroll);
    }

    private void buildWidgets() {
        clearWidgets();
        addRenderableWidget(Button.builder(toggleDayLabel(), button -> {
                    allowDaySpawns = !allowDaySpawns;
                    button.setMessage(toggleDayLabel());
                }).bounds(listLeft, DAY_TOGGLE_Y, listWidth, DAY_TOGGLE_HEIGHT)
                .build());

        int startIndex = firstVisibleEntry;
        int endIndex = Math.min(entries.size(), startIndex + visibleEntryCount);

        for (int index = startIndex; index < endIndex; index++) {
            ClientEntry entry = entries.get(index);
            int row = index - startIndex;
            int y = listTop + row * ENTRY_HEIGHT;
            int buttonX = listLeft + listWidth - TOGGLE_WIDTH - LIST_PADDING;

            Button toggleButton = Button.builder(toggleEntryLabel(entry), button -> {
                        entry.enabled = !entry.enabled;
                        button.setMessage(toggleEntryLabel(entry));
                    }).bounds(buttonX, y, TOGGLE_WIDTH, 20)
                    .build();
            toggleButton.active = ENTRY_TOGGLES_ENABLED;
            addRenderableWidget(toggleButton);
        }

        int footerY = listTop + listHeight + 10;
        int footerButtonWidth = (listWidth - 6) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(listLeft, footerY, footerButtonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(listLeft + footerButtonWidth + 6, footerY, footerButtonWidth, 20)
                .build());
    }

    @Override
    protected void rebuildWidgets() {
        buildWidgets();
    }

    private void renderListBackground(GuiGraphics graphics) {
        graphics.fill(listLeft - 2, listTop - 2, listLeft + listWidth + 2, listTop + listHeight + 2, 0xFF2A2A2A);
        graphics.fill(listLeft, listTop, listLeft + listWidth, listTop + listHeight, 0xFF111111);
        if (!ENTRY_TOGGLES_ENABLED) {
            graphics.fill(listLeft, listTop, listLeft + listWidth, listTop + listHeight, 0x88101010);
        }
    }

    private void renderListEntries(GuiGraphics graphics) {
        int startIndex = firstVisibleEntry;
        int endIndex = Math.min(entries.size(), startIndex + visibleEntryCount);
        int textWidth = listWidth - TOGGLE_WIDTH - (LIST_PADDING * 3);

        for (int index = startIndex; index < endIndex; index++) {
            ClientEntry entry = entries.get(index);
            int row = index - startIndex;
            int y = listTop + row * ENTRY_HEIGHT;
            int textY = y + 6;
            int lineColor = (row & 1) == 0 ? 0x221E1E1E : 0x22303030;

            graphics.fill(listLeft + 1, y, listLeft + listWidth - 7, y + 20, lineColor);
            int textColor = ENTRY_TOGGLES_ENABLED ? 0xF0F0F0 : 0x8A8A8A;
            graphics.drawString(font, font.plainSubstrByWidth(entry.displayName, textWidth), listLeft + LIST_PADDING, textY, textColor, false);
        }
    }

    private boolean isInsideList(double mouseX, double mouseY) {
        return mouseX >= listLeft && mouseX <= listLeft + listWidth && mouseY >= listTop && mouseY <= listTop + listHeight;
    }

    private Component toggleDayLabel() {
        return Component.translatable("screen.changedextras.latex_spawn_control.day", Component.translatable(allowDaySpawns ? "options.on" : "options.off"));
    }

    private Component toggleEntryLabel(ClientEntry entry) {
        return Component.literal(entry.enabled ? "ON" : "OFF");
    }

    private void saveAndClose() {
        Set<String> disabledIds = new HashSet<>();
        for (ClientEntry entry : entries) {
            if (!entry.enabled) {
                disabledIds.add(entry.entityTypeId);
            }
        }

        ChangedExtrasNetwork.INSTANCE.sendToServer(new UpdateLatexSpawnRulesPacket(allowDaySpawns, disabledIds));
        onClose();
    }

    private static final class ClientEntry {
        private final String entityTypeId;
        private final String displayName;
        private boolean enabled;

        private ClientEntry(String entityTypeId, String displayName, boolean enabled) {
            this.entityTypeId = entityTypeId;
            this.displayName = displayName;
            this.enabled = enabled;
        }
    }
}
