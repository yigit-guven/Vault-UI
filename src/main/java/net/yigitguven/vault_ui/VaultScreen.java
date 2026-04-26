package net.yigitguven.vault_ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class VaultScreen extends AbstractContainerScreen<VaultMenu> {
    private Button prevButton;
    private Button nextButton;

    public VaultScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 210;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.prevButton = Button.builder(Component.literal("<"), (btn) -> {
            int page = this.menu.getCurrentPage() - 1;
            if (page >= 0) {
                this.menu.setPage(page);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VaultPagePayload(page));
            }
        }).bounds(x + 175, y + 17, 25, 20).build();

        this.nextButton = Button.builder(Component.literal(">"), (btn) -> {
            int page = this.menu.getCurrentPage() + 1;
            if (page < this.menu.getMaxPages()) {
                this.menu.setPage(page);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new VaultPagePayload(page));
            }
        }).bounds(x + 175, y + 42, 25, 20).build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
        
        // Sort Button
        this.addRenderableWidget(Button.builder(Component.literal("Sort: " + menu.getSortMode().label), (btn) -> {
            VaultMenu.SortMode next = VaultMenu.SortMode.values()[(menu.getSortMode().ordinal() + 1) % VaultMenu.SortMode.values().length];
            menu.setSortMode(next);
            Config.SORT_MODE.set(next);
            btn.setMessage(Component.literal("Sort: " + next.label));
        }).pos(this.leftPos + 132, this.topPos + 4).size(70, 12).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.prevButton.active = this.menu.getCurrentPage() > 0;
        this.nextButton.active = this.menu.getCurrentPage() < this.menu.getMaxPages() - 1;
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        boolean darkMode = Config.DARK_MODE.get();
        
        // Background Colors
        int bgColor = darkMode ? 0xFF181818 : 0xFFC6C6C6;
        int outlineColor = darkMode ? 0xFF555555 : 0xFF333333;
        int gridBgColor = darkMode ? 0xFF0F0F0F : 0xFF8B8B8B;
        
        // Main Panel (Sharp Vanilla Style)
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, bgColor);
        
        if (!darkMode) {
            // Vanilla Bezel (2-pixel, Sharp)
            // Top & Left Highlights
            guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFFFFFFFF); // Outer Top
            guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFFFFFFFF); // Outer Left
            guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + 2, 0xFFFFFFFF); // Inner Top
            guiGraphics.fill(x + 1, y + 1, x + 2, y + this.imageHeight - 1, 0xFFFFFFFF); // Inner Left
            
            // Bottom & Right Shadows
            guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF000000); // Outer Bottom
            guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000); // Outer Right
            guiGraphics.fill(x + 1, y + this.imageHeight - 2, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF555555); // Inner Bottom
            guiGraphics.fill(x + this.imageWidth - 2, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF555555); // Inner Right
        } else {
            // Dark Mode Border (Sharp)
            guiGraphics.renderOutline(x, y, this.imageWidth, this.imageHeight, outlineColor);
        }

        // Vault Grid Background
        int gridX = x + 7;
        int gridY = y + 17;
        guiGraphics.fill(gridX, gridY, gridX + 162, gridY + 108, gridBgColor);
        
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(guiGraphics, gridX + 1 + col * 18, gridY + 1 + row * 18, darkMode);
            }
        }
        
        // Player Inventory Area (Now uses bgColor to avoid division)
        int invX = x + 7;
        int invY = y + 139;
        int inventoryBg = darkMode ? gridBgColor : bgColor;
        guiGraphics.fill(invX, invY, invX + 162, invY + 76, inventoryBg);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(guiGraphics, invX + 1 + col * 18, invY + 1 + row * 18, darkMode);
            }
        }
        
        int hotbarY = y + 197;
        guiGraphics.fill(invX, hotbarY, invX + 162, hotbarY + 18, inventoryBg);
        for (int col = 0; col < 9; col++) {
            drawSlot(guiGraphics, invX + 1 + col * 18, hotbarY + 1, darkMode);
        }

        // Paging Info Area
        int infoX = x + 175;
        int infoY = y + 67;
        int infoBg = darkMode ? 0xFF0A0A0A : 0xFF8B8B8B;
        guiGraphics.fill(infoX, infoY, infoX + 25, infoY + 40, infoBg);
        guiGraphics.renderOutline(infoX, infoY, 25, 40, outlineColor);
        
        String pageStr = (this.menu.getCurrentPage() + 1) + "/" + Math.max(1, this.menu.getMaxPages());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(infoX + 12.5, infoY + 20, 0);
        guiGraphics.pose().scale(0.8f, 0.8f, 1.0f);
        int pageTextColor = darkMode ? 0x777777 : 0xFFFFFF;
        int pageNumColor = darkMode ? 0xFFFF8800 : 0xFFFFFF;
        guiGraphics.drawCenteredString(this.font, "PAGE", 0, -10, pageTextColor);
        guiGraphics.drawCenteredString(this.font, pageStr, 0, 2, pageNumColor);
        guiGraphics.pose().popPose();

        // Fullness Indicator Bar
        int barX = x + 175;
        int barY = y + 115;
        int barW = 25;
        int barH = 100;
        
        // Draw Bar Background
        guiGraphics.fill(barX, barY, barX + barW, barY + barH, darkMode ? 0xFF0A0A0A : 0xFF8B8B8B);
        guiGraphics.renderOutline(barX, barY, barW, barH, outlineColor);
        
        int total = this.menu.getTotalCount();
        int cap = this.menu.getCapacity();
        float ratio = cap > 0 ? (float) total / cap : 0;
        int fillH = (int) (ratio * (barH - 2));
        fillH = Math.min(barH - 2, fillH);
        
        if (fillH > 0) {
            int color = getInterpolatedColor(ratio);
            guiGraphics.fill(barX + 1, barY + barH - 1 - fillH, barX + barW - 1, barY + barH - 1, color);
        }

        // Hover Tooltip for Bar
        if (mouseX >= barX && mouseX < barX + barW && mouseY >= barY && mouseY < barY + barH) {
            java.util.List<net.minecraft.network.chat.Component> tooltip = new java.util.ArrayList<>();
            tooltip.add(Component.literal("Vault Storage Status").withStyle(net.minecraft.ChatFormatting.GOLD));
            
            String percentStr;
            if (ratio >= 1.0f) percentStr = "100%";
            else if (ratio <= 0.0f) percentStr = "0%";
            else {
                float percent = ratio * 100.0f;
                if (percent > 99.9f) percent = 99.9f;
                if (percent < 0.1f) percent = 0.1f;
                percentStr = String.format("%.1f%%", percent);
            }
            
            int barColor = getInterpolatedColor(ratio);
            tooltip.add(Component.literal(percentStr + " Full").withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(barColor)));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Breakdown:").withStyle(net.minecraft.ChatFormatting.GRAY).withStyle(net.minecraft.ChatFormatting.UNDERLINE));
            
            float itemRatio = this.menu.getRawCapacity() > 0 ? (float) this.menu.getRawTotal() / this.menu.getRawCapacity() : 0;
            int itemColor = getInterpolatedColor(itemRatio);
            tooltip.add(Component.literal("Items: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(Component.literal(String.format("%s / %s", formatCountLarge(this.menu.getRawTotal()), formatCountLarge(this.menu.getRawCapacity()))).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(itemColor))));
            
            float slotRatio = this.menu.getTotalSlots() > 0 ? (float) this.menu.getOccupiedSlots() / this.menu.getTotalSlots() : 0;
            int slotColor = getInterpolatedColor(slotRatio);
            tooltip.add(Component.literal("Slots: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(Component.literal(String.format("%d / %d", this.menu.getOccupiedSlots(), this.menu.getTotalSlots())).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(slotColor))));
            
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    private int getInterpolatedColor(float ratio) {
        if (ratio < 0.5f) {
            float t = ratio * 2.0f;
            int r = (int) (255 * t);
            int g = (int) (255 * (1 - t) + 165 * t);
            return 0xFF000000 | (r << 16) | (g << 8);
        } else {
            float t = (ratio - 0.5f) * 2.0f;
            int r = 255;
            int g = (int) (165 * (1 - t));
            return 0xFF000000 | (r << 16) | (g << 8);
        }
    }

    private String formatCountLarge(long count) {
        if (count >= 1000000) return String.format("%.2fM", count / 1000000.0);
        if (count >= 1000) return String.format("%.1fk", count / 1000.0);
        return String.valueOf(count);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y, boolean darkMode) {
        if (darkMode) {
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF333333);
            guiGraphics.fill(x, y, x + 16, y + 16, 0xFF181818);
            guiGraphics.fill(x - 1, y - 1, x + 16, y, 0xFF000000);
            guiGraphics.fill(x - 1, y - 1, x, y + 16, 0xFF000000);
            guiGraphics.fill(x + 16, y, x + 17, y + 17, 0xFF555555);
            guiGraphics.fill(x, y + 16, x + 17, y + 17, 0xFF555555);
        } else {
            // Vanilla Slot (Softer)
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
            guiGraphics.fill(x - 1, y - 1, x + 16, y, 0xFF373737);
            guiGraphics.fill(x - 1, y - 1, x, y + 16, 0xFF373737);
            guiGraphics.fill(x + 16, y, x + 17, y + 17, 0xFFFFFFFF);
            guiGraphics.fill(x, y + 16, x + 17, y + 17, 0xFFFFFFFF);
        }
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        boolean darkMode = Config.DARK_MODE.get();
        int titleColor = darkMode ? 0xFFFFFF : 0x404040;
        int invColor = darkMode ? 0xAAAAAA : 0x404040;
        guiGraphics.drawString(this.font, this.title, 8, 6, titleColor, darkMode);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY + 1, invColor, false);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, net.minecraft.world.inventory.Slot slot) {
        if (slot.index < 54 && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            guiGraphics.renderItem(stack, slot.x, slot.y);
            
            if (stack.getCount() > 1) {
                String countText = formatCount(stack.getCount());
                float scale = 0.8f;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.pose().scale(scale, scale, 1.0f);
                
                // Position the scaled text in the bottom-right of the 16x16 slot
                float x = (slot.x + 16) / scale - this.font.width(countText);
                float y = (slot.y + 16) / scale - 8;
                
                guiGraphics.drawString(this.font, countText, (int)x, (int)y, 0xFFFFFF, true);
                guiGraphics.pose().popPose();
            }
            // Use "" instead of null to suppress the default count rendering
            guiGraphics.renderItemDecorations(this.font, stack, slot.x, slot.y, "");
        } else {
            super.renderSlot(guiGraphics, slot);
        }
    }

    private String formatCount(int count) {
        if (count >= 1000000) {
            double value = count / 1000000.0;
            return value % 1 == 0 ? String.format("%.0fM", value) : String.format("%.1fM", value);
        }
        if (count >= 1000) {
            double value = count / 1000.0;
            return value % 1 == 0 ? String.format("%.0fk", value) : String.format("%.1fk", value);
        }
        return String.valueOf(count);
    }
}
