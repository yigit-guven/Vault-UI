package net.yigitguven.vault_ui;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue DARK_MODE = BUILDER
            .comment("Whether to use the dark theme for the Vault UI. If false, uses a vanilla-like light theme.")
            .translation("vault_ui.config.darkMode")
            .define("darkMode", false);

    public static final ForgeConfigSpec.EnumValue<VaultMenu.SortMode> SORT_MODE = BUILDER
            .comment("The default sort mode for the Vault UI.")
            .translation("vault_ui.config.sortMode")
            .defineEnum("sortMode", VaultMenu.SortMode.COUNT);

    public static final ForgeConfigSpec.BooleanValue VIBRANT_COLORS = BUILDER
            .comment("Whether to use colors from Create: Vibrant Vaults in the UI.")
            .translation("vault_ui.config.vibrantColors")
            .define("vibrantColors", true);

    public static final ForgeConfigSpec.BooleanValue JEI_SYNC = BUILDER
            .comment("Whether to sync the Vault UI search bar with JEI/EMI search bar.")
            .translation("vault_ui.config.jeiSync")
            .define("jeiSync", true);
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
