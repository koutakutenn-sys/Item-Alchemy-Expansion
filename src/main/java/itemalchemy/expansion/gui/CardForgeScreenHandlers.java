package itemalchemy.expansion.gui;

import itemalchemy.expansion.ItemAlchemyExpansion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;
import net.pitan76.mcpitanlib.api.gui.SimpleScreenHandlerTypeBuilder;

/**
 * 制卡台 MenuType 注册。
 */
public final class CardForgeScreenHandlers {

    private CardForgeScreenHandlers() {}

    private static final SimpleScreenHandlerTypeBuilder<CardForgeScreenHandler> BUILDER =
            new SimpleScreenHandlerTypeBuilder<>(e -> new CardForgeScreenHandler(e));

    /** 制卡台 MenuType */
    public static final MenuType<CardForgeScreenHandler> TYPE;

    static {
        TYPE = Registry.register(BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "card_forge"),
                BUILDER.build());
    }

    public static void init() {
        // 静态初始化已注册；此处仅用于显式触发类加载
        ItemAlchemyExpansion.LOGGER.info("[IAExp] card forge screen handler registered");
    }
}