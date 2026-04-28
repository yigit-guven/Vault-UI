package net.yigitguven.vault_ui;

import net.neoforged.fml.ModList;
import java.lang.reflect.Method;

public class CompatHelper {
    private static final boolean jeiLoaded = ModList.get().isLoaded("jei");
    private static final boolean emiLoaded = ModList.get().isLoaded("emi");

    public static void syncSearch(String query) {
        if (!Config.JEI_SYNC.get()) return;

        // Sync to EMI
        if (emiLoaded) {
            try {
                Class<?> emiApi = Class.forName("dev.emi.emi.api.EmiApi");
                Method setSearchText = emiApi.getMethod("setSearchText", String.class);
                setSearchText.invoke(null, query);
            } catch (Exception ignored) {}
        }

        // Sync to JEI
        if (jeiLoaded) {
            try {
                Class<?> internal = Class.forName("mezz.jei.Internal");
                Method getRuntime = internal.getMethod("getRuntime");
                Object runtime = getRuntime.invoke(null);
                if (runtime != null) {
                    Method getIngredientFilter = runtime.getClass().getMethod("getIngredientFilter");
                    Object filter = getIngredientFilter.invoke(runtime);
                    Method setFilterText = filter.getClass().getMethod("setFilterText", String.class);
                    setFilterText.invoke(filter, query);
                }
            } catch (Exception ignored) {}
        }
    }
}
