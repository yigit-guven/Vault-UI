package net.yigitguven.vault_ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VaultPagePayload(int page) implements CustomPacketPayload {
    public static final Type<VaultPagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_page"));

    public static final StreamCodec<FriendlyByteBuf, VaultPagePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, VaultPagePayload::page,
            VaultPagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
