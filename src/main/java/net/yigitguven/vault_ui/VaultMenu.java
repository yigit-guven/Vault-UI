package net.yigitguven.vault_ui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VaultMenu extends AbstractContainerMenu {
    private final IItemHandler vaultHandler;
    private final ItemStackHandler dummyHandler = new ItemStackHandler(54);
    private List<ItemStack> consolidatedStacks = new ArrayList<>();
    private List<ItemStack> filteredStacks = new ArrayList<>();
    private int currentPage = 0;
    private final int slotsPerPage = 54;
    private final net.minecraft.core.BlockPos controllerPos;

    private final Player player;
    private int totalCount = 0;
    private int capacity = 0;
    private long rawTotal = 0;
    private long rawCapacity = 0;
    private int occupiedSlots = 0;
    private int totalSlots = 0;
    private String searchQuery = "";
    private SortMode sortMode = Config.SORT_MODE != null ? Config.SORT_MODE.get() : SortMode.COUNT;
    private int tickCount = 0;
    private String vaultColor = null;
    private final java.util.Map<ItemKey, Long> lastEditedTimes = new java.util.HashMap<>();
    private final java.util.Map<ItemKey, Long> previousTotals = new java.util.HashMap<>();

    public enum SortMode {
        COUNT("Most Items"),
        LAST_EDITED("Last Edited"),
        NAME_ID("A-Z (Mod ID)"),
        NAME("A-Z");

        public final String label;
        SortMode(String label) { this.label = label; }
    }

    // Client constructor
    public VaultMenu(int containerId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf data) {
        this(containerId, playerInventory, new ItemStackHandler(data.readInt()), net.minecraft.core.BlockPos.ZERO);
        if (data.readBoolean()) {
            this.vaultColor = data.readUtf();
        }
    }

    // Default Server constructor
    public VaultMenu(int containerId, Inventory playerInventory, IItemHandler vaultHandler) {
        this(containerId, playerInventory, vaultHandler, net.minecraft.core.BlockPos.ZERO);
    }

    // Master constructor
    public VaultMenu(int containerId, Inventory playerInventory, IItemHandler vaultHandler, net.minecraft.core.BlockPos pos) {
        super(CreateVaultUI.VAULT_MENU.get(), containerId);
        this.vaultHandler = vaultHandler;
        this.player = playerInventory.player;
        this.controllerPos = pos;
        this.currentPage = 0;

        // Vault Slots (9x6)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(dummyHandler, col + row * 9, 8 + col * 18, 18 + row * 18));
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

    public void receiveSync(VaultMenu.SortMode sortMode, boolean ascending, ItemStack[] items) {
        this.consolidatedStacks = new ArrayList<>(List.of(items));
        // Note: For now we just use the order provided by server for simplicity
        applySearchFilter();
        resort();
        updateDummyHandler();
    }
    
    // Compatibility with PacketSync
    public void receiveSync(ItemStack[] items, long[] times, int[] stats) {
        this.consolidatedStacks = new ArrayList<>();
        this.lastEditedTimes.clear();
        for (int i = 0; i < items.length; i++) {
            this.consolidatedStacks.add(items[i]);
            this.lastEditedTimes.put(new ItemKey(items[i]), times[i]);
        }
        this.totalCount = stats[0];
        this.capacity = stats[1];
        this.rawTotal = (long)stats[2];
        this.rawCapacity = (long)stats[3];
        this.occupiedSlots = stats[4];
        this.totalSlots = stats[5];
        applySearchFilter();
        resort();
        updateDummyHandler();
    }

    private void resort() {
        java.util.Comparator<ItemStack> comparator = switch (sortMode) {
            case COUNT ->
                java.util.Comparator.comparingInt(ItemStack::getCount).reversed()
                .thenComparing(s -> s.getHoverName().getString());
            case LAST_EDITED ->
                java.util.Comparator.comparingLong((ItemStack s) -> lastEditedTimes.getOrDefault(new ItemKey(s), 0L)).reversed()
                .thenComparing(s -> s.getHoverName().getString());
            case NAME_ID -> (a, b) -> {
                String idA = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(a.getItem()).toString();
                String idB = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(b.getItem()).toString();
                return idA.compareToIgnoreCase(idB);
            };
            case NAME -> (a, b) -> {
                String nameA = a.getHoverName().getString();
                String nameB = b.getHoverName().getString();
                return nameA.compareToIgnoreCase(nameB);
            };
        };
        if (comparator != null) {
            consolidatedStacks.sort(comparator);
            filteredStacks.sort(comparator);
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
                
                totalFullnessRatio += (double) stack.getCount() / limit;
            }
        }
        
        double combinedRatio = totalSlots > 0 ? totalFullnessRatio / totalSlots : 0;
        this.totalCount = (int) Math.round(combinedRatio * 10000);
        this.capacity = 10000;
        this.rawTotal = currentTotal;
        this.rawCapacity = currentCapacity;
        this.occupiedSlots = occupiedSlots;
        this.totalSlots = totalSlots;

        if (!player.level().isClientSide && !controllerPos.equals(net.minecraft.core.BlockPos.ZERO)) {
            for (var entry : totals.entrySet()) {
                long prev = previousTotals.getOrDefault(entry.getKey(), 0L);
                if (prev != entry.getValue()) {
                    VaultSortData.get(player.level()).updateTimestamp(controllerPos, entry.getKey().stack());
                }
            }
            for (var key : previousTotals.keySet()) {
                if (!totals.containsKey(key)) {
                    VaultSortData.get(player.level()).updateTimestamp(controllerPos, key.stack());
                }
            }
            previousTotals.clear();
            previousTotals.putAll(totals);
        }

        consolidatedStacks.clear();
        for (var entry : totals.entrySet()) {
            ItemStack stack = entry.getKey().stack.copy();
            stack.setCount((int) Math.min(Integer.MAX_VALUE, entry.getValue()));
            consolidatedStacks.add(stack);
        }

        if (!player.level().isClientSide) {
            lastEditedTimes.clear();
            lastEditedTimes.putAll(VaultSortData.get(player.level()).getTimestamps(controllerPos));
        }

        applySearchFilter();
        resort();
        updateDummyHandler();
        
        // Sync to client
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ItemStack[] items = new ItemStack[consolidatedStacks.size()];
            long[] times = new long[consolidatedStacks.size()];
            VaultSortData data = VaultSortData.get(player.level());
            Map<ItemKey, Long> timestamps = data.getTimestamps(controllerPos);
            
            for (int i = 0; i < consolidatedStacks.size(); i++) {
                items[i] = consolidatedStacks.get(i);
                times[i] = timestamps.getOrDefault(new ItemKey(items[i]), 0L);
            }

            int[] stats = {totalCount, capacity, (int)rawTotal, (int)rawCapacity, occupiedSlots, totalSlots};
            VaultNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new VaultNetwork.VaultSyncPacket(sortMode, true, items));
        }
    }

    private void updateDummyHandler() {
        int start = currentPage * slotsPerPage;
        for (int i = 0; i < 54; i++) {
            int index = start + i;
            if (index < filteredStacks.size()) {
                dummyHandler.setStackInSlot(i, filteredStacks.get(index));
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
        return (int) Math.ceil((double) (filteredStacks.size()) / slotsPerPage);
    }
    
    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.toLowerCase() : "";
        this.currentPage = 0;
        applySearchFilter();
        resort();
        updateDummyHandler();
    }
    
    private void applySearchFilter() {
        filteredStacks.clear();
        if (searchQuery.isEmpty()) {
            filteredStacks.addAll(consolidatedStacks);
        } else {
            for (ItemStack stack : consolidatedStacks) {
                if (stack.getHoverName().getString().toLowerCase().contains(searchQuery)) {
                    filteredStacks.add(stack);
                }
            }
        }
    }

    public int getTotalCount() { return totalCount; }
    public int getCapacity() { return capacity; }
    public long getRawTotal() { return rawTotal; }
    public long getRawCapacity() { return rawCapacity; }
    public int getOccupiedSlots() { return occupiedSlots; }
    public int getTotalSlots() { return totalSlots; }
    public String getVaultColor() { return vaultColor; }
    public void setVaultColor(String color) { this.vaultColor = color; }

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
                        insertIntoVault(withdrawn);
                    }
                }
            }
        } else {
            Slot slot = this.slots.get(index);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (!player.level().isClientSide && !controllerPos.equals(net.minecraft.core.BlockPos.ZERO)) {
                    VaultSortData.get(player.level()).updateTimestamp(controllerPos, stack);
                }
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
        if (!player.level().isClientSide && !controllerPos.equals(net.minecraft.core.BlockPos.ZERO)) {
            VaultSortData.get(player.level()).updateTimestamp(controllerPos, stack);
        }
        return ItemHandlerHelper.insertItemStacked(vaultHandler, stack, false);
    }

    private ItemStack withdrawFromVault(ItemStack template, int amount) {
        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;
        
        if (!player.level().isClientSide && !controllerPos.equals(net.minecraft.core.BlockPos.ZERO)) {
            VaultSortData.get(player.level()).updateTimestamp(controllerPos, template);
        }

        for (int i = 0; i < vaultHandler.getSlots(); i++) {
            ItemStack inSlot = vaultHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(inSlot, template)) {
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
                            insertIntoVault(withdrawn);
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
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                ItemStack remaining = insertIntoVault(carried);
                if (!remaining.isEmpty()) {
                    if (!player.getInventory().add(remaining)) {
                        player.drop(remaining, false);
                    }
                }
                setCarried(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index >= 54;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public static record ItemKey(ItemStack stack) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemKey itemKey = (ItemKey) o;
            return ItemStack.isSameItemSameTags(stack, itemKey.stack);
        }
        @Override
        public int hashCode() {
            int result = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
            if (stack.getTag() != null) result = 31 * result + stack.getTag().hashCode();
            return result;
        }
    }
}
