package net.yigitguven.vault_ui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record VaultSyncPayload(List<ItemTimestamp> items, VaultStats stats) implements CustomPacketPayload {
    public static final Type<VaultSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_sync"));

    public record ItemTimestamp(ItemStack item, long time) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemTimestamp> CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, ItemTimestamp::item,
                ByteBufCodecs.VAR_LONG, ItemTimestamp::time,
                ItemTimestamp::new
        );
    }

    public record VaultStats(int barProgress, int barMax, long rawTotal, long rawCapacity, int occupiedSlots, int totalSlots) {
        public static final StreamCodec<RegistryFriendlyByteBuf, VaultStats> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, VaultStats::barProgress,
                ByteBufCodecs.VAR_INT, VaultStats::barMax,
                ByteBufCodecs.VAR_LONG, VaultStats::rawTotal,
                ByteBufCodecs.VAR_LONG, VaultStats::rawCapacity,
                ByteBufCodecs.VAR_INT, VaultStats::occupiedSlots,
                ByteBufCodecs.VAR_INT, VaultStats::totalSlots,
                VaultStats::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultSyncPayload> CODEC = StreamCodec.composite(
            ItemTimestamp.CODEC.apply(ByteBufCodecs.list()), VaultSyncPayload::items,
            VaultStats.CODEC, VaultSyncPayload::stats,
            VaultSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
