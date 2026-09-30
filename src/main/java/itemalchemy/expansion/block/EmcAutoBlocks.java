package itemalchemy.expansion.block;

import itemalchemy.expansion.ItemAlchemyExpansion;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.pitan76.mcpitanlib.api.block.v2.CompatibleBlockSettings;
import net.pitan76.mcpitanlib.api.util.CompatIdentifier;

/**
 * 自动装置（EMC 转能器 / EMC 输出器）注册：方块、方块物品、BlockEntityType。
 * 沿用制卡台的 Fabric 原生 {@link Registry#register} 注册方式。
 */
public final class EmcAutoBlocks {

    private EmcAutoBlocks() {}

    /** EMC 转能器方块 */
    public static EmcConverterBlock CONVERTER;
    /** EMC 转能器 BlockItem */
    public static BlockItem CONVERTER_ITEM;
    /** EMC 转能器 BlockEntityType */
    public static BlockEntityType<EmcConverterBlockEntity> CONVERTER_TILE;

    /** EMC 输出器方块 */
    public static EmcEmitterBlock EMITTER;
    /** EMC 输出器 BlockItem */
    public static BlockItem EMITTER_ITEM;
    /** EMC 输出器 BlockEntityType */
    public static BlockEntityType<EmcEmitterBlockEntity> EMITTER_TILE;

    public static void init() {
        CONVERTER = Registry.register(BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_converter"),
                new EmcConverterBlock(CompatibleBlockSettings.of(
                                CompatIdentifier.of(ItemAlchemyExpansion.MOD_ID, "emc_converter"))
                        .strength(2.0f, 6.0f)));

        CONVERTER_ITEM = Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_converter"),
                new BlockItem(CONVERTER, new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_converter"))).stacksTo(64)));

        CONVERTER_TILE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_converter"),
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(EmcConverterBlockEntity::new, CONVERTER).build());

        EMITTER = Registry.register(BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter"),
                new EmcEmitterBlock(CompatibleBlockSettings.of(
                                CompatIdentifier.of(ItemAlchemyExpansion.MOD_ID, "emc_emitter"))
                        .strength(2.0f, 6.0f)));

        EMITTER_ITEM = Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter"),
                new BlockItem(EMITTER, new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter"))).stacksTo(64)));

        EMITTER_TILE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter"),
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(EmcEmitterBlockEntity::new, EMITTER).build());

        ItemAlchemyExpansion.LOGGER.info("[IAExp] emc automation blocks registered: converter + emitter");
    }
}
