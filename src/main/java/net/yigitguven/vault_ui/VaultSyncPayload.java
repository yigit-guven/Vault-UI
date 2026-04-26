package net.yigitguven.vault_ui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record VaultSyncPayload(List<ItemStack> items) implements CustomPacketPayload {
    public static final Type<VaultSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultSyncPayload> CODEC = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC, VaultSyncPayload::items,
            VaultSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
