package net.yigitguven.vault_ui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DARK_MODE = BUILDER
            .comment("Whether to use the dark theme for the Vault UI. If false, uses a vanilla-like light theme.")
            .translation("vault_ui.config.darkMode")
            .define("darkMode", false);

    public static final ModConfigSpec.EnumValue<VaultMenu.SortMode> SORT_MODE = BUILDER
            .comment("The default sort mode for the Vault UI.")
            .translation("vault_ui.config.sortMode")
            .defineEnum("sortMode", VaultMenu.SortMode.COUNT);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
