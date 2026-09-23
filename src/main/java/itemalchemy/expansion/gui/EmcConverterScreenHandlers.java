package itemalchemy.expansion.gui;

import itemalchemy.expansion.ItemAlchemyExpansion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;
import net.pitan76.mcpitanlib.api.gui.SimpleScreenHandlerTypeBuilder;

/**
 * EMC 转能器 MenuType 注册。
 */
public final class EmcConverterScreenHandlers {

    private EmcConverterScreenHandlers() {}

    private static final SimpleScreenHandlerTypeBuilder<EmcConverterScreenHandler> BUILDER =
            new SimpleScreenHandlerTypeBuilder<>(e -> new EmcConverterScreenHandler(e));

    public static final MenuType<EmcConverterScreenHandler> TYPE;

    static {
        TYPE = Registry.register(BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_converter"),
                BUILDER.build());
    }

    public static void init() {
        ItemAlchemyExpansion.LOGGER.info("[IAExp] emc converter screen handler registered");
    }
}