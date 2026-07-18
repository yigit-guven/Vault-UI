package net.yigitguven.vault_ui;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PREVENT_ITEM_RETRIEVAL = BUILDER
            .comment("If true, players will not be able to retrieve items directly from the vault interface.")
            .translation("vault_ui.config.preventItemRetrieval")
            .define("preventItemRetrieval", false);
    public static final ModConfigSpec.BooleanValue PREVENT_ITEM_INSERTION = BUILDER
            .comment("If true, players will not be able to insert items directly into the vault interface.")
            .translation("vault_ui.config.preventItemInsertion")
            .define("preventItemInsertion", false);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
