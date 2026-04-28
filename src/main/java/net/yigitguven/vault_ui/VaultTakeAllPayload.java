package net.yigitguven.vault_ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VaultTakeAllPayload(int slotId) implements CustomPacketPayload {
    public static final Type<VaultTakeAllPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_take_all"));

    public static final StreamCodec<FriendlyByteBuf, VaultTakeAllPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, VaultTakeAllPayload::slotId,
            VaultTakeAllPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
