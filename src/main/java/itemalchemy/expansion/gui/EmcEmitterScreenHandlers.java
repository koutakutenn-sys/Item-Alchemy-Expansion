package itemalchemy.expansion.gui;

import itemalchemy.expansion.ItemAlchemyExpansion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;
import net.pitan76.mcpitanlib.api.gui.SimpleScreenHandlerTypeBuilder;

/**
 * EMC 输出器 MenuType 注册。
 */
public final class EmcEmitterScreenHandlers {

    private EmcEmitterScreenHandlers() {}

    private static final SimpleScreenHandlerTypeBuilder<EmcEmitterScreenHandler> BUILDER =
            new SimpleScreenHandlerTypeBuilder<>(e -> new EmcEmitterScreenHandler(e));

    /** EMC 输出器 MenuType */
    public static final MenuType<EmcEmitterScreenHandler> TYPE;

    static {
        TYPE = Registry.register(BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter"),
                BUILDER.build());
    }

    public static void init() {
        ItemAlchemyExpansion.LOGGER.info("[IAExp] emc emitter screen handler registered");
    }
}
