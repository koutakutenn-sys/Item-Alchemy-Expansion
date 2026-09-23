package itemalchemy.expansion.network;

import itemalchemy.expansion.ItemAlchemyExpansion;
import itemalchemy.expansion.block.EmcConverterBlockEntity;
import itemalchemy.expansion.block.EmcEmitterBlockEntity;
import itemalchemy.expansion.config.IAExpConfigHolder;
import itemalchemy.expansion.gui.EmcConverterScreenHandler;
import itemalchemy.expansion.gui.EmcEmitterScreenHandler;
import itemalchemy.expansion.compat.port.PacketByteBufs;
import itemalchemy.expansion.compat.port.ServerPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.pitan76.itemalchemy.api.PlayerRegisteredItemUtil;
import net.pitan76.mcpitanlib.api.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动装置（EMC 转能器 / EMC 输出器）网络层。
 *
 * <p>输出器为容器 GUI：右键方块由 {@code SimpleScreenHandlerFactory} 打开，服务端通过
 * {@code player.containerMenu} 定位对应 {@link EmcEmitterBlockEntity}（菜单的
 * {@code canUse} 每 tick 校验距离，越界自动关闭，防伪造包）。客户端请求打开者转换桌列表、
 * 设置所选物品；卡槽为真实容器槽，直接拖动放入/取出，无需额外协议。</p>
 */
public final class EmcAutoNetwork {

    /** C2S：请求列表（无载荷，服务端按当前打开的输出器菜单定位） */
    public static final Identifier REQ_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter_req");
    /** C2S：设置所选物品（携带变体键） */
    public static final Identifier SET_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter_set");
    /** S2C：下发列表 + 当前所选 + 卡余额 + 卡栈 */
    public static final Identifier LIST_S2C_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter_list_s2c");
    /** S2C：下发更新后的所选物品 */
    public static final Identifier SELECTED_S2C_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_emitter_selected_s2c");
    /** C2S：配置保存后通知服务端同步自动装置合成配方（开关变更即时生效） */
    public static final Identifier CFG_SYNC_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_auto_cfg_sync");
    /** C2S：请求卡余额（轻量，GUI 心跳刷新用，避免整列表重发） */
    public static final Identifier BALANCE_REQ_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_auto_balance_req");
    /** S2C：下发当前卡余额 */
    public static final Identifier BALANCE_S2C_ID =
            Identifier.fromNamespaceAndPath(ItemAlchemyExpansion.MOD_ID, "emc_auto_balance_s2c");

    private EmcAutoNetwork() {}

    /** 服务端注册 C2S 接收器 */
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(REQ_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> handleListRequest(player));
        });
        ServerPlayNetworking.registerGlobalReceiver(SET_ID, (server, player, handler, buf, responseSender) -> {
            final boolean has = buf.readBoolean();
            final String variant = has ? buf.readUtf() : null;
            server.execute(() -> handleSet(player, variant));
        });
        ServerPlayNetworking.registerGlobalReceiver(CFG_SYNC_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                try {
                    ItemAlchemyExpansion.syncAutomationRecipes(server);
                } catch (Throwable t) {
                    ItemAlchemyExpansion.LOGGER.warn("[IAExp] emc emitter: failed to sync automation recipes: {}", t.toString());
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(BALANCE_REQ_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> handleBalanceRequest(player));
        });
    }

    /** 按当前打开的转能器/输出器菜单定位 tile，下发卡实时余额（GUI 心跳刷新用） */
    private static void handleBalanceRequest(ServerPlayer player) {
        if (!IAExpConfigHolder.get().automationEnabled) return;
        if (player.containerMenu instanceof EmcEmitterScreenHandler sh) {
            sendBalance(player, EmcCardBalanceUtil.getBalance(
                    player.level().getServer(), sh.tile.getItem(EmcEmitterBlockEntity.CARD_SLOT)));
        } else if (player.containerMenu instanceof EmcConverterScreenHandler ch) {
            sendBalance(player, EmcCardBalanceUtil.getBalance(
                    player.level().getServer(), ch.tile.getItem(EmcConverterBlockEntity.CARD_SLOT)));
        }
    }

    private static void sendBalance(ServerPlayer player, long balance) {
        try {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeLong(balance);
            ServerPlayNetworking.send(player, BALANCE_S2C_ID, buf);
        } catch (Throwable t) {
            ItemAlchemyExpansion.LOGGER.warn("[IAExp] emc auto: failed to send balance: {}", t.toString());
        }
    }

    /** 通过当前打开的容器菜单定位输出器方块（菜单 canUse 已校验距离） */
    private static EmcEmitterBlockEntity currentEmitterTile(ServerPlayer player) {
        if (player.containerMenu instanceof EmcEmitterScreenHandler sh) {
            return sh.tile;
        }
        return null;
    }

    private static void handleListRequest(ServerPlayer player) {
        // 自动装置总开关关闭时拒绝（与方块右键/tick 闸门一致，防伪造包绕过）
        if (!IAExpConfigHolder.get().automationEnabled) return;
        EmcEmitterBlockEntity tile = currentEmitterTile(player);
        if (tile == null) return;

        List<String> ids;
        try {
            ids = new ArrayList<>(PlayerRegisteredItemUtil.getItemsAsString(new Player(player)));
        } catch (Throwable t) {
            ids = new ArrayList<>();
        }

        long balance = EmcCardBalanceUtil.getBalance(player.level().getServer(), tile.getItem(EmcEmitterBlockEntity.CARD_SLOT));
        String selected = tile.getSelectedVariant() == null ? "" : tile.getSelectedVariant();
        String facing = tile.getFacing().getName();
        ItemStack card = tile.getItem(EmcEmitterBlockEntity.CARD_SLOT);

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(selected);
        buf.writeLong(balance);
        buf.writeUtf(facing);
        buf.writeNbt(itemalchemy.expansion.compat.port.StackData.writeNbt(card, new net.minecraft.nbt.CompoundTag()));
        buf.writeInt(ids.size());
        for (String s : ids) {
            buf.writeUtf(s);
        }
        ServerPlayNetworking.send(player, LIST_S2C_ID, buf);
    }

    private static void handleSet(ServerPlayer player, String variant) {
        if (!IAExpConfigHolder.get().automationEnabled) return;
        EmcEmitterBlockEntity tile = currentEmitterTile(player);
        if (tile == null) return;

        // 校验所选物品属于打开者自己的转换桌列表（防止设置任意物品）
        if (variant != null && !variant.isEmpty()) {
            List<String> ids;
            try {
                ids = new ArrayList<>(PlayerRegisteredItemUtil.getItemsAsString(new Player(player)));
            } catch (Throwable t) {
                ids = new ArrayList<>();
            }
            if (!ids.contains(variant)) return;
        }

        tile.setSelectedVariant(variant);
        String sel = tile.getSelectedVariant() == null ? "" : tile.getSelectedVariant();
        // 共享所选物品：广播给所有正打开同一输出器的玩家，避免不同玩家看到不同选择
        for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
            if (p == player) continue;
            if (p.containerMenu instanceof EmcEmitterScreenHandler sh && sh.tile == tile) {
                sendSelected(p, sel);
            }
        }
        sendSelected(player, sel);
    }

    private static void sendSelected(ServerPlayer player, String selected) {
        try {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(selected);
            ServerPlayNetworking.send(player, SELECTED_S2C_ID, buf);
        } catch (Throwable t) {
            ItemAlchemyExpansion.LOGGER.warn("[IAExp] emc emitter: failed to send selected: {}", t.toString());
        }
    }
}
