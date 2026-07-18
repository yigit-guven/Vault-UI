package net.yigitguven.vault_ui;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;

@Mod(CreateVaultUI.MODID)
public class CreateVaultUI {
    public static final String MODID = "vault_ui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<VaultMenu>> VAULT_MENU = MENUS.register("vault_menu",
            () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(VaultMenu::new));

    public CreateVaultUI(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerNetworking);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Vault UI common setup initialized.");
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                VaultPagePayload.TYPE,
                VaultPagePayload.CODEC,
                CreateVaultUI::handleVaultPage
        );

        registrar.playToServer(
                VaultSortPayload.TYPE,
                VaultSortPayload.CODEC,
                CreateVaultUI::handleVaultSort
        );

        registrar.playToServer(
                VaultTakeAllPayload.TYPE,
                VaultTakeAllPayload.CODEC,
                CreateVaultUI::handleVaultTakeAll
        );

        registrar.playToServer(
                VaultSearchPayload.TYPE,
                VaultSearchPayload.CODEC,
                CreateVaultUI::handleVaultSearch
        );

        registrar.playToClient(
                VaultSyncPayload.TYPE,
                VaultSyncPayload.CODEC,
                (payload, context) -> {
                    if (context.flow().isClientbound()) {
                        context.enqueueWork(() -> CreateVaultUIClient.handleVaultSync(payload, context));
                    }
                }
        );
    }

    private static void handleVaultPage(VaultPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof VaultMenu menu) {
                menu.setPage(payload.page());
            }
        });
    }

    private static void handleVaultSort(VaultSortPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof VaultMenu menu) {
                menu.setSortMode(payload.sortMode());
            }
        });
    }

    private static void handleVaultTakeAll(VaultTakeAllPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof VaultMenu menu) {
                menu.takeAll(context.player(), payload.slotId());
            }
        });
    }

    private static void handleVaultSearch(VaultSearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof VaultMenu menu) {
                menu.setSearchQuery(payload.query());
            }
        });
    }

    private boolean isValidVaultType(ResourceLocation id) {
        String ns = id.getNamespace();
        String path = id.getPath();
        boolean validNamespace = ns.equals("create") || ns.contains("vibrant_vaults") || ns.contains("vibrantvaults");
        boolean validPath = path.contains("vault") || path.contains("shipping_container");
        return validNamespace && validPath;
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public void onVaultInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            // On client, we just need to signal success if it's a vault to stop other interactions
            BlockState state = event.getLevel().getBlockState(event.getPos());
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (isValidVaultType(id)) {
                if (!event.getEntity().isCrouching() && !isVault(event.getEntity().getMainHandItem()) && !isVault(event.getEntity().getOffhandItem())) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
            return;
        }
        
        // Allow vault expansion if player is holding a vault - check this BEFORE logging
        if (isVault(event.getEntity().getMainHandItem()) || isVault(event.getEntity().getOffhandItem())) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (isValidVaultType(id)) {
            if (event.getEntity().isCrouching()) return;

            net.minecraft.world.level.block.entity.BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
            if (be != null) {
                net.neoforged.neoforge.items.IItemHandler handler = event.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, event.getPos(), event.getFace());

                if (handler != null) {
                    final net.neoforged.neoforge.items.IItemHandler finalHandler = handler;
                    
                    // Extract color from vibrant_vaults
                    String color = null;
                    if (id.getNamespace().contains("vibrant_vaults") || id.getNamespace().contains("vibrantvaults")) {
                        String path = id.getPath();
                        // Handle patterns like "white_vault", "blue_shipping_container", "light_blue_vault"
                        String[] parts = path.split("_");
                        if (parts.length > 0) {
                            String colorCandidate = parts[0].toLowerCase();
                            // Handle "light_blue" and "light_gray"
                            if (colorCandidate.equals("light") && parts.length > 1) {
                                String secondPart = parts[1].toLowerCase();
                                if (secondPart.equals("blue") || secondPart.equals("gray")) {
                                    colorCandidate = "light_" + secondPart;
                                }
                            }

                            java.util.List<String> validColors = java.util.Arrays.asList(
                                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", 
                                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
                            );
                            if (validColors.contains(colorCandidate)) {
                                color = colorCandidate;
                            }
                        }
                    }
                    final String finalColor = color;

                    event.getEntity().openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (id1, inventory, player) -> {
                                VaultMenu menu = new VaultMenu(id1, inventory, finalHandler, event.getPos());
                                if (finalColor != null) menu.setVaultColor(finalColor);
                                return menu;
                            },
                            state.getBlock().getName()
                    ), buf -> {
                        buf.writeInt(finalHandler.getSlots());
                        if (finalColor != null) {
                            buf.writeBoolean(true);
                            buf.writeUtf(finalColor);
                        } else {
                            buf.writeBoolean(false);
                        }
                    });
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isVault(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return isValidVaultType(id);
    }
}
