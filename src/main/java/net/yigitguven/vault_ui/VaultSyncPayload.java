package net.yigitguven.vault_ui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record VaultSyncPayload(List<ItemStack> items, int barProgress, int barMax, long rawTotal, long rawCapacity) implements CustomPacketPayload {
    public static final Type<VaultSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultSyncPayload> CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC, VaultSyncPayload::items,
            ByteBufCodecs.VAR_INT, VaultSyncPayload::barProgress,
            ByteBufCodecs.VAR_INT, VaultSyncPayload::barMax,
            ByteBufCodecs.VAR_LONG, VaultSyncPayload::rawTotal,
            ByteBufCodecs.VAR_LONG, VaultSyncPayload::rawCapacity,
            VaultSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
