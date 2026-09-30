package itemalchemy.expansion.client;

import itemalchemy.expansion.ItemAlchemyExpansion;
import itemalchemy.expansion.nbt.ShulkerBoxSupport;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

/**
 * 「设置 EMC」快捷键：手持物品时按下打开 {@link SetEmcScreen}。
 *
 * <p>默认绑定到 K 键。玩家可在「选项 → 控制 → 按键绑定」中的
 * 「Item Alchemy Expansion」分类重新绑定或解绑。</p>
 *
 * <p>触发条件（在 {@link ClientTickEvents#END_CLIENT_TICK} 中检测）：按键被按下
 * （wasPressed，每 tick 至多触发一次）、玩家在主世界（非 GUI 中）、玩家主手或副手有物品。
 * 选物优先级：主手优先，主手为空时用副手；两手都空则不打开 GUI。</p>
 */
public final class SetEmcKeybind {

    /** 按键分类，显示在按键绑定界面的分组名 */
    public static final String KEY_CATEGORY = "itemalchemy-expansion.keybind.category";

    /** 翻译键 */
    public static final String KEY_NAME = "itemalchemy-expansion.keybind.set_emc";

    /** 唯一 KeyMapping 实例 */
    private static KeyMapping keyBinding;

    private SetEmcKeybind() {}

    /** 注册按键 + 注册 tick 监听。在 {@code onInitializeClient} 中调用。 */
    public static void register() {
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                KEY_NAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("itemalchemy-expansion", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SetEmcKeybind::onEndTick);
    }

    private static void onEndTick(Minecraft client) {
        // wasPressed 返回 true 后会消费一次按下状态，避免重复触发
        while (keyBinding.consumeClick()) {
            tryOpenSetEmcScreen(client);
        }
    }

    private static void tryOpenSetEmcScreen(Minecraft client) {
        if (client.player == null) return;
        // 玩家在 GUI 中时不触发（避免与转换桌等界面的按键冲突）
        if (client.gui.screen() != null) return;

        Player player = client.player;
        ItemStack target = pickHeldItem(player);
        if (target.isEmpty()) {
            // 没有手持物品：不打开 GUI，静默（玩家可能误按）
            return;
        }

        // 带内容物的潜影盒不允许手动定价：其 EMC 按内容物求和，固定值无意义
        if (ShulkerBoxSupport.hasContents(target)) {
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(
                    "itemalchemy-expansion.set_emc.fail.shulker_with_contents"));
            return;
        }

        // tick 回调本身在主线程，直接 setScreen 即可
        client.setScreenAndShow(new SetEmcScreen(target));
    }

    /** 主手优先，主手为空时用副手 */
    private static ItemStack pickHeldItem(Player player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!main.isEmpty()) return main;
        return player.getItemInHand(InteractionHand.OFF_HAND);
    }
}
