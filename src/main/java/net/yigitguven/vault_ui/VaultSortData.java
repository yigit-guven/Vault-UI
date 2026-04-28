package net.yigitguven.vault_ui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;

public class VaultSortData extends SavedData {
    private final Map<BlockPos, Map<VaultMenu.ItemKey, Long>> vaultTimestamps = new HashMap<>();

    public VaultSortData() {}

    public static VaultSortData get(Level level) {
        if (level.isClientSide) return new VaultSortData();
        var storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(VaultSortData::new, VaultSortData::load), "vault_sort_data");
    }

    public void updateTimestamp(BlockPos controllerPos, ItemStack stack) {
        if (stack.isEmpty()) return;
        Map<VaultMenu.ItemKey, Long> timestamps = vaultTimestamps.computeIfAbsent(controllerPos, k -> new HashMap<>());
        timestamps.put(new VaultMenu.ItemKey(stack), System.currentTimeMillis());
        setDirty();
    }

    public Map<VaultMenu.ItemKey, Long> getTimestamps(BlockPos controllerPos) {
        return vaultTimestamps.getOrDefault(controllerPos, new HashMap<>());
    }

    public static VaultSortData load(CompoundTag nbt, HolderLookup.Provider provider) {
        VaultSortData data = new VaultSortData();
        ListTag vaultsList = nbt.getList("vaults", Tag.TAG_COMPOUND);
        for (int i = 0; i < vaultsList.size(); i++) {
            CompoundTag vaultTag = vaultsList.getCompound(i);
            BlockPos pos = BlockPos.of(vaultTag.getLong("pos"));
            Map<VaultMenu.ItemKey, Long> timestamps = new HashMap<>();
            ListTag itemsList = vaultTag.getList("items", Tag.TAG_COMPOUND);
            for (int j = 0; j < itemsList.size(); j++) {
                CompoundTag itemTag = itemsList.getCompound(j);
                ItemStack stack = ItemStack.parse(provider, itemTag.getCompound("item")).orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    timestamps.put(new VaultMenu.ItemKey(stack), itemTag.getLong("time"));
                }
            }
            data.vaultTimestamps.put(pos, timestamps);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag vaultsList = new ListTag();
        for (Map.Entry<BlockPos, Map<VaultMenu.ItemKey, Long>> entry : vaultTimestamps.entrySet()) {
            CompoundTag vaultTag = new CompoundTag();
            vaultTag.putLong("pos", entry.getKey().asLong());
            ListTag itemsList = new ListTag();
            for (Map.Entry<VaultMenu.ItemKey, Long> itemEntry : entry.getValue().entrySet()) {
                CompoundTag itemTag = new CompoundTag();
                ItemStack saveStack = itemEntry.getKey().stack().copy();
                saveStack.setCount(1); // Crucial: avoid counts > 99 which crash ItemStack.save()
                itemTag.put("item", saveStack.save(provider));
                itemTag.putLong("time", itemEntry.getValue());
                itemsList.add(itemTag);
            }
            vaultTag.put("items", itemsList);
            vaultsList.add(vaultTag);
        }
        nbt.put("vaults", vaultsList);
        return nbt;
    }
}
