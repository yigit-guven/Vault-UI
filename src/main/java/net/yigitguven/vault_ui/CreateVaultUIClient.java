package net.yigitguven.vault_ui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = CreateVaultUI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateVaultUIClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(CreateVaultUI.VAULT_MENU.get(), VaultScreen::new);
        });
        CreateVaultUI.LOGGER.info("Vault UI client setup initialized.");
    }

    public static void handleVaultSync(VaultMenu.SortMode sortMode, boolean ascending, ItemStack[] items) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.containerMenu instanceof VaultMenu menu) {
            menu.receiveSync(sortMode, ascending, items);
        }
    }
}
