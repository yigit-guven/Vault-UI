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
    public void receiveSync(List<ItemStack> items, int totalCount, int capacity) {
        this.consolidatedStacks = new ArrayList<>(items);
        this.totalCount = totalCount;
        this.capacity = capacity;
        updateDummyHandler();
    }

    private void refreshServerData() {
        if (player.level().isClientSide) return;

        consolidatedStacks.clear();
        java.util.Map<ItemKey, Long> totals = new java.util.LinkedHashMap<>();
        double currentVolume = 0;
        int currentCapacity = vaultHandler.getSlots() * 64;
        
        for (int i = 0; i < vaultHandler.getSlots(); i++) {
            ItemStack stack = vaultHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemKey key = new ItemKey(stack);
                totals.put(key, totals.getOrDefault(key, 0L) + stack.getCount());
                
                // Volume-based fullness: 1 sword = 64 units, 1 ender pearl = 4 units
                double slotFullness = (double) stack.getCount() / stack.getMaxStackSize();
                currentVolume += slotFullness;
            }
        }
        // Scale volume to 64-based "effective items" for the UI
        this.totalCount = (int) Math.round(currentVolume * 64);
        this.capacity = currentCapacity;
        
        for (var entry : totals.entrySet()) {
            ItemStack stack = entry.getKey().stack.copy();
            stack.setCount((int) Math.min(Integer.MAX_VALUE, entry.getValue()));
            consolidatedStacks.add(stack);
        }
        updateDummyHandler();
        
        // Sync to client
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new VaultSyncPayload(new ArrayList<>(consolidatedStacks), totalCount, capacity));
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
            refreshServerData();
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
