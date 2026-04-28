package net.yigitguven.vault_ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VaultSearchPayload(String query) implements CustomPacketPayload {
    public static final Type<VaultSearchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_search"));

    public static final StreamCodec<FriendlyByteBuf, VaultSearchPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.query()),
            buf -> new VaultSearchPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
