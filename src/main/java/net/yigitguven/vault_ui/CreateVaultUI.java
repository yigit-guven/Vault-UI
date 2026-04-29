package net.yigitguven.vault_ui;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

@Mod(CreateVaultUI.MODID)
public class CreateVaultUI {
    public static final String MODID = "vault_ui";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<MenuType<VaultMenu>> VAULT_MENU = MENUS.register("vault_menu",
            () -> IForgeMenuType.create(VaultMenu::new));

    public CreateVaultUI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MENUS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        VaultNetwork.register();
        LOGGER.info("Vault UI common setup initialized.");
    }

    private boolean isValidVaultType(ResourceLocation id) {
        String ns = id.getNamespace();
        String path = id.getPath();
        boolean validNamespace = ns.equals("create") || ns.contains("vibrant_vaults") || ns.contains("vibrantvaults");
        boolean validPath = path.contains("vault") || path.contains("shipping_container");
        return validNamespace && validPath;
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onVaultInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            BlockState state = event.getLevel().getBlockState(event.getPos());
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (isValidVaultType(id)) {
                if (!event.getEntity().isCrouching() && !isVault(event.getEntity().getMainHandItem()) && !isVault(event.getEntity().getOffhandItem())) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
            return;
        }
        
        if (isVault(event.getEntity().getMainHandItem()) || isVault(event.getEntity().getOffhandItem())) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());

        if (isValidVaultType(id)) {
            if (event.getEntity().isCrouching()) return;

            net.minecraft.world.level.block.entity.BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
            if (be != null) {
                // Use standard Forge Capability for ItemHandler
                net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> cap = be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, event.getFace());

                cap.ifPresent(handler -> {
                    String color = null;
                    if (id.getNamespace().contains("vibrant_vaults") || id.getNamespace().contains("vibrantvaults")) {
                        String path = id.getPath();
                        String[] parts = path.split("_");
                        if (parts.length > 0) {
                            String colorCandidate = parts[0].toLowerCase();
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

                    net.minecraftforge.network.NetworkHooks.openScreen((ServerPlayer) event.getEntity(), new net.minecraft.world.SimpleMenuProvider(
                            (id1, inventory, player) -> {
                                VaultMenu menu = new VaultMenu(id1, inventory, handler, event.getPos());
                                if (finalColor != null) menu.setVaultColor(finalColor);
                                return menu;
                            },
                            state.getBlock().getName()
                    ), buf -> {
                        buf.writeInt(handler.getSlots());
                        if (finalColor != null) {
                            buf.writeBoolean(true);
                            buf.writeUtf(finalColor);
                        } else {
                            buf.writeBoolean(false);
                        }
                    });
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                });
            }
        }
    }

    private boolean isVault(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return isValidVaultType(id);
    }
}
