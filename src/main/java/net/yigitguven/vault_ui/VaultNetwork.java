package net.yigitguven.vault_ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class VaultNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CreateVaultUI.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, VaultPagePacket.class, VaultPagePacket::encode, VaultPagePacket::new, VaultPagePacket::handle);
        CHANNEL.registerMessage(id++, VaultSortPacket.class, VaultSortPacket::encode, VaultSortPacket::new, VaultSortPacket::handle);
        CHANNEL.registerMessage(id++, VaultTakeAllPacket.class, VaultTakeAllPacket::encode, VaultTakeAllPacket::new, VaultTakeAllPacket::handle);
        CHANNEL.registerMessage(id++, VaultSearchPacket.class, VaultSearchPacket::encode, VaultSearchPacket::new, VaultSearchPacket::handle);
        CHANNEL.registerMessage(id++, VaultSyncPacket.class, VaultSyncPacket::encode, VaultSyncPacket::new, VaultSyncPacket::handle);
    }

    public static class VaultPagePacket {
        private final int page;
        public VaultPagePacket(int page) { this.page = page; }
        public VaultPagePacket(FriendlyByteBuf buf) { this.page = buf.readInt(); }
        public void encode(FriendlyByteBuf buf) { buf.writeInt(page); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.containerMenu instanceof VaultMenu menu) {
                    menu.setPage(page);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class VaultSortPacket {
        private final VaultMenu.SortMode sortMode;
        public VaultSortPacket(VaultMenu.SortMode sortMode) { this.sortMode = sortMode; }
        public VaultSortPacket(FriendlyByteBuf buf) { this.sortMode = buf.readEnum(VaultMenu.SortMode.class); }
        public void encode(FriendlyByteBuf buf) { buf.writeEnum(sortMode); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.containerMenu instanceof VaultMenu menu) {
                    menu.setSortMode(sortMode);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class VaultTakeAllPacket {
        private final int slotId;
        public VaultTakeAllPacket(int slotId) { this.slotId = slotId; }
        public VaultTakeAllPacket(FriendlyByteBuf buf) { this.slotId = buf.readInt(); }
        public void encode(FriendlyByteBuf buf) { buf.writeInt(slotId); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.containerMenu instanceof VaultMenu menu) {
                    menu.takeAll(player, slotId);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class VaultSearchPacket {
        private final String query;
        public VaultSearchPacket(String query) { this.query = query; }
        public VaultSearchPacket(FriendlyByteBuf buf) { this.query = buf.readUtf(); }
        public void encode(FriendlyByteBuf buf) { buf.writeUtf(query); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.containerMenu instanceof VaultMenu menu) {
                    menu.setSearchQuery(query);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class VaultSyncPacket {
        private final VaultMenu.SortMode sortMode;
        private final boolean ascending;
        private final ItemStack[] lastEditedItems;

        public VaultSyncPacket(VaultMenu.SortMode sortMode, boolean ascending, ItemStack[] lastEditedItems) {
            this.sortMode = sortMode;
            this.ascending = ascending;
            this.lastEditedItems = lastEditedItems;
        }

        public VaultSyncPacket(FriendlyByteBuf buf) {
            this.sortMode = buf.readEnum(VaultMenu.SortMode.class);
            this.ascending = buf.readBoolean();
            int len = buf.readInt();
            this.lastEditedItems = new ItemStack[len];
            for (int i = 0; i < len; i++) this.lastEditedItems[i] = buf.readItem();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(sortMode);
            buf.writeBoolean(ascending);
            buf.writeInt(lastEditedItems.length);
            for (ItemStack item : lastEditedItems) buf.writeItem(item);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                CreateVaultUIClient.handleVaultSync(sortMode, ascending, lastEditedItems);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
