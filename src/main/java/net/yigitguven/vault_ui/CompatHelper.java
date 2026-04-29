package net.yigitguven.vault_ui;

import net.minecraftforge.fml.ModList;
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
                VaultJEIPlugin.setFilterText(query);
            } catch (Throwable ignored) {}
        }
    }

    public static String getSyncSearch() {
        if (!Config.JEI_SYNC.get()) return null;

        if (emiLoaded) {
            try {
                Class<?> emiApi = Class.forName("dev.emi.emi.api.EmiApi");
                Method getSearchText = emiApi.getMethod("getSearchText");
                return (String) getSearchText.invoke(null);
            } catch (Exception ignored) {}
        }

        if (jeiLoaded) {
            try {
                return VaultJEIPlugin.getFilterText();
            } catch (Throwable ignored) {}
        }

        return null;
    }
}
