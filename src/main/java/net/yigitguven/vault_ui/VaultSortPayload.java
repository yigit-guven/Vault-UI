package net.yigitguven.vault_ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VaultSortPayload(VaultMenu.SortMode sortMode) implements CustomPacketPayload {
    public static final Type<VaultSortPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateVaultUI.MODID, "vault_sort"));

    public static final StreamCodec<FriendlyByteBuf, VaultSortPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.sortMode()),
            buf -> new VaultSortPayload(buf.readEnum(VaultMenu.SortMode.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
