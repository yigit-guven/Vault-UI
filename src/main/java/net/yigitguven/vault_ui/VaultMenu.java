package net.yigitguven.vault_ui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class VaultMenu extends AbstractContainerMenu {
    private final IItemHandler vaultHandler;
    private final ItemStackHandler dummyHandler = new ItemStackHandler(54);
    private List<ItemStack> consolidatedStacks = new ArrayList<>();
    private int currentPage = 0;
    private final int slotsPerPage = 54;

    private final Player player;
    private int totalCount = 0;
    private int capacity = 0;
    private long rawTotal = 0;
    private long rawCapacity = 0;
    private int occupiedSlots = 0;
    private int totalSlots = 0;
    private SortMode sortMode = Config.SORT_MODE != null ? Config.SORT_MODE.get() : SortMode.COUNT;
    private int tickCount = 0;

    public enum SortMode {
        COUNT("Most Items"),
        NAME_ID("A-Z (Mod ID)"),
        NAME("A-Z");

        public final String label;
        SortMode(String label) { this.label = label; }
    }

    // Client constructor
    public VaultMenu(int containerId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf data) {
        this(containerId, playerInventory, new ItemStackHandler(54));
    }

    // Server constructor
    public VaultMenu(int containerId, Inventory playerInventory, IItemHandler vaultHandler) {
        super(CreateVaultUI.VAULT_MENU.get(), containerId);
        this.vaultHandler = vaultHandler;
        this.player = playerInventory.player;

        // Vault Slots (9x6)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.neoforged.neoforge.items.SlotItemHandler(dummyHandler, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
        
        if (!(vaultHandler instanceof ItemStackHandler)) {
            refreshServerData();
        }
    }

    // Called on Client via Packet
    public void receiveSync(List<ItemStack> items, VaultSyncPayload.VaultStats stats) {
        this.consolidatedStacks = new ArrayList<>(items);
        this.totalCount = stats.barProgress();
        this.capacity = stats.barMax();
        this.rawTotal = stats.rawTotal();
        this.rawCapacity = stats.rawCapacity();
        this.occupiedSlots = stats.occupiedSlots();
        this.totalSlots = stats.totalSlots();
        resort();
        updateDummyHandler();
    }

    private void resort() {
        switch (sortMode) {
            case COUNT -> consolidatedStacks.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
            case NAME_ID -> consolidatedStacks.sort((a, b) -> {
                String idA = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(a.getItem()).toString();
                String idB = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(b.getItem()).toString();
                return idA.compareToIgnoreCase(idB);
            });
            case NAME -> consolidatedStacks.sort((a, b) -> {
                String nameA = a.getHoverName().getString();
                String nameB = b.getHoverName().getString();
                return nameA.compareToIgnoreCase(nameB);
            });
        }
    }

    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        resort();
        updateDummyHandler();
    }

    public SortMode getSortMode() { return sortMode; }

    private void refreshServerData() {
        if (player.level().isClientSide) return;

        java.util.Map<ItemKey, Long> totals = new java.util.LinkedHashMap<>();
        long currentTotal = 0;
        long currentCapacity = 0;
        int occupiedSlots = 0;
        int totalSlots = vaultHandler.getSlots();
        double totalFullnessRatio = 0;
        
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = vaultHandler.getStackInSlot(i);
            int limit = vaultHandler.getSlotLimit(i);
            currentCapacity += limit;
            
            if (!stack.isEmpty()) {
                ItemKey key = new ItemKey(stack);
                totals.put(key, totals.getOrDefault(key, 0L) + stack.getCount());
                currentTotal += stack.getCount();
                occupiedSlots++;
                
                // Dynamic Slot Fullness: How much more of THIS item can fit in THIS slot?
                ItemStack testStack = stack.copy();
                testStack.setCount(limit); // Try to fill the slot completely
                ItemStack remaining = vaultHandler.insertItem(i, testStack, true);
                int spaceLeft = limit - remaining.getCount();
                
                if (spaceLeft <= 0) {
                    totalFullnessRatio += 1.0;
                } else {
                    totalFullnessRatio += (double) stack.getCount() / (stack.getCount() + spaceLeft);
                }
            }
        }
        
        double combinedRatio = totalSlots > 0 ? totalFullnessRatio / totalSlots : 0;
        
        // Scale totalCount to represent this ratio for the UI bar
        this.totalCount = (int) Math.round(combinedRatio * 10000);
        this.capacity = 10000;
        
        // Keep track of the raw counts for the tooltip
        this.rawTotal = currentTotal;
        this.rawCapacity = currentCapacity;
        this.occupiedSlots = occupiedSlots;
        this.totalSlots = totalSlots;
        
        if (consolidatedStacks.isEmpty()) {
            // First load or empty: perform full sort
            for (var entry : totals.entrySet()) {
                ItemStack stack = entry.getKey().stack.copy();
                stack.setCount((int) Math.min(Integer.MAX_VALUE, entry.getValue()));
                consolidatedStacks.add(stack);
            }
            resort();
        } else {
            // Stable update: update counts in place, append new items to the end
            java.util.Set<ItemKey> seenKeys = new java.util.HashSet<>();
            
            // 1. Update existing
            for (int i = 0; i < consolidatedStacks.size(); i++) {
                ItemStack stack = consolidatedStacks.get(i);
                ItemKey key = new ItemKey(stack);
                long count = totals.getOrDefault(key, 0L);
                stack.setCount((int) Math.min(Integer.MAX_VALUE, count));
                seenKeys.add(key);
            }
            
            // 2. Add new items to the end (prevents jumping)
            for (var entry : totals.entrySet()) {
                if (!seenKeys.contains(entry.getKey())) {
                    ItemStack newStack = entry.getKey().stack.copy();
                    newStack.setCount((int) Math.min(Integer.MAX_VALUE, entry.getValue()));
                    consolidatedStacks.add(newStack);
                }
            }
            
            // 3. Optional: Remove items that are completely gone?
            // Actually, keeping them with count 0 until close might be safer for mapping, 
            // but let's just let them stay or be removed if they are at the end.
            consolidatedStacks.removeIf(s -> s.getCount() <= 0);
            resort();
        }
        
        updateDummyHandler();
        
        // Sync to client
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            VaultSyncPayload.VaultStats stats = new VaultSyncPayload.VaultStats(totalCount, capacity, rawTotal, rawCapacity, occupiedSlots, totalSlots);
            PacketDistributor.sendToPlayer(serverPlayer, new VaultSyncPayload(new ArrayList<>(consolidatedStacks), stats));
        }
    }

    private void updateDummyHandler() {
        int start = currentPage * slotsPerPage;
        for (int i = 0; i < 54; i++) {
            int index = start + i;
            if (index < consolidatedStacks.size()) {
                dummyHandler.setStackInSlot(i, consolidatedStacks.get(index));
            } else {
                dummyHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        if (!(vaultHandler instanceof ItemStackHandler)) {
            if (tickCount++ % 20 == 0) {
                refreshServerData();
            }
        }
        super.broadcastChanges();
    }

    public void setPage(int page) {
        this.currentPage = Math.max(0, page);
        updateDummyHandler();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPages() {
        return (int) Math.ceil((double) (consolidatedStacks.size()) / slotsPerPage);
    }

    public int getTotalCount() { return totalCount; }
    public int getCapacity() { return capacity; }
    public long getRawTotal() { return rawTotal; }
    public long getRawCapacity() { return rawCapacity; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public int getTotalSlots() { return totalSlots; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 54) {
            ItemStack stackInSlot = dummyHandler.getStackInSlot(index);
            if (!stackInSlot.isEmpty()) {
                ItemStack template = stackInSlot.copy();
                int toTake = Math.min(template.getMaxStackSize(), template.getCount());
                ItemStack withdrawn = withdrawFromVault(template, toTake);
                if (!withdrawn.isEmpty()) {
                    if (!this.moveItemStackTo(withdrawn, 54, this.slots.size(), true)) {
                        player.drop(withdrawn, false);
                    }
                }
            }
        } else {
            Slot slot = this.slots.get(index);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                ItemStack remaining = ItemHandlerHelper.insertItemStacked(vaultHandler, stack.copy(), false);
                slot.set(remaining);
            }
        }
        if (!(vaultHandler instanceof ItemStackHandler)) refreshServerData();
        return ItemStack.EMPTY;
    }

    public void takeAll(Player player, int slotId) {
        if (slotId >= 0 && slotId < 54) {
            ItemStack stackInSlot = dummyHandler.getStackInSlot(slotId);
            if (!stackInSlot.isEmpty()) {
                ItemStack template = stackInSlot.copy();
                
                while (true) {
                    int toTake = template.getMaxStackSize();
                    ItemStack withdrawn = withdrawFromVault(template, toTake);
                    
                    if (withdrawn.isEmpty()) break;
                    
                    if (!this.moveItemStackTo(withdrawn, 54, this.slots.size(), true)) {
                        if (!withdrawn.isEmpty()) {
                            insertIntoVault(withdrawn);
                        }
                        break;
                    }
                }
                if (!(vaultHandler instanceof ItemStackHandler)) refreshServerData();
            }
        }
    }

    private ItemStack insertIntoVault(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        return ItemHandlerHelper.insertItemStacked(vaultHandler, stack, false);
    }

    private ItemStack withdrawFromVault(ItemStack template, int amount) {
        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;
        for (int i = 0; i < vaultHandler.getSlots(); i++) {
            ItemStack inSlot = vaultHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(inSlot, template)) {
                ItemStack taken = vaultHandler.extractItem(i, remaining, false);
                if (!taken.isEmpty()) {
                    if (result.isEmpty()) result = taken.copy();
                    else result.grow(taken.getCount());
                    remaining -= taken.getCount();
                }
                if (remaining <= 0) break;
            }
        }
        return result;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (clickType == net.minecraft.world.inventory.ClickType.QUICK_CRAFT) {
            super.clicked(slotId, button, clickType, player);
            if (!(vaultHandler instanceof ItemStackHandler)) refreshServerData();
            return;
        }

        if (slotId >= 0 && slotId < 54) {
            ItemStack carried = getCarried();
            if (clickType == net.minecraft.world.inventory.ClickType.PICKUP) {
                if (!carried.isEmpty()) {
                    ItemStack remaining = insertIntoVault(carried);
                    setCarried(remaining);
                    refreshServerData();
                    return;
                } else {
                    ItemStack stackInSlot = dummyHandler.getStackInSlot(slotId);
                    if (!stackInSlot.isEmpty()) {
                        int toTake = (button == 1) ? 1 : Math.min(stackInSlot.getCount(), stackInSlot.getMaxStackSize());
                        ItemStack withdrawn = withdrawFromVault(stackInSlot, toTake);
                        setCarried(withdrawn);
                        refreshServerData();
                        return;
                    }
                }
            } else if (clickType == net.minecraft.world.inventory.ClickType.QUICK_MOVE) {
                ItemStack stackInSlot = dummyHandler.getStackInSlot(slotId);
                if (!stackInSlot.isEmpty()) {
                    ItemStack withdrawn = withdrawFromVault(stackInSlot, stackInSlot.getMaxStackSize());
                    if (!withdrawn.isEmpty()) {
                        if (!this.moveItemStackTo(withdrawn, 54, this.slots.size(), true)) {
                            player.drop(withdrawn, false);
                        }
                    }
                    refreshServerData();
                    return;
                }
            }
            if (clickType != net.minecraft.world.inventory.ClickType.PICKUP && clickType != net.minecraft.world.inventory.ClickType.QUICK_MOVE) {
                return;
            }
        }
        
        super.clicked(slotId, button, clickType, player);
        if (!(vaultHandler instanceof ItemStackHandler)) refreshServerData();
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index >= 54;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    private static record ItemKey(ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemKey itemKey = (ItemKey) o;
            return ItemStack.isSameItemSameComponents(stack, itemKey.stack);
        }
        @Override
        public int hashCode() {
            int result = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
            var components = stack.getComponents();
            if (components != null) result = 31 * result + components.hashCode();
            return result;
        }
    }
}
