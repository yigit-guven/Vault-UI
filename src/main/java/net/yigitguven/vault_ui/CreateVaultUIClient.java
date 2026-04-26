package net.yigitguven.vault_ui;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(value = CreateVaultUI.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateVaultUI.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class CreateVaultUIClient {
    public CreateVaultUIClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CreateVaultUI.LOGGER.info("Vault UI client setup initialized.");
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(CreateVaultUI.VAULT_MENU.get(), VaultScreen::new);
    }

    public static void handleVaultSync(VaultSyncPayload payload, IPayloadContext context) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.containerMenu instanceof VaultMenu menu) {
            menu.receiveSync(payload.items(), payload.stats());
        }
    }
}
