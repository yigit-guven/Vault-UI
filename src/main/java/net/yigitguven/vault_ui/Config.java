package net.yigitguven.vault_ui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DARK_MODE = BUILDER
            .comment("Whether to use the dark theme for the Vault UI. If false, uses a vanilla-like light theme.")
            .define("darkMode", false);
    
    static final ModConfigSpec SPEC = BUILDER.build();
}
