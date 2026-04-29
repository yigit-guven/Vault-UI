package net.yigitguven.vault_ui;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class VaultJEIPlugin implements IModPlugin {
    private static IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CreateVaultUI.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        VaultJEIPlugin.jeiRuntime = jeiRuntime;
    }

    public static void setFilterText(String text) {
        if (jeiRuntime != null) {
            jeiRuntime.getIngredientFilter().setFilterText(text);
        }
    }

    public static String getFilterText() {
        return jeiRuntime != null ? jeiRuntime.getIngredientFilter().getFilterText() : "";
    }
}
