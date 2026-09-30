package itemalchemy.expansion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 「新功能」升级提醒 Toast（屏幕右上角弹窗）。
 *
 * <p>当 {@code IAExpConfigHolder.wasUpgradedFromLegacy()} 返回 true（玩家从旧版本配置升级）
 * 且 {@code featureNoticeShown=false} 时，服务端在玩家加入时推 {@code new_feature_toast}
 * S2C 包，客户端收到后弹此 toast 一次。</p>
 *
 * <p>布局比 {@link NoEmcShulkerBoxToast} 宽以容纳两行说明：标题（金色）+ 描述行 1（白色）
 * + 描述行 2（灰色）。图标用 vanilla 的 {@code experience_bottle} 纹理。</p>
 *
 * <p>持续时长 8000ms，比一般 toast 长一倍以确保玩家能读完。服务端已用
 * {@code featureNoticeShown} 标志去重，客户端不重复抑制。</p>
 */
public class NewFeatureToast implements Toast {

    /** Toast 背景纹理（vanilla 的 toast 纹理） */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/toasts.png");

    /** 经验瓶纹理（作为「新功能」图标） */
    private static final Identifier ICON_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/item/experience_bottle.png");

    /** Toast 持续时长（毫秒），比一般 toast 长，确保玩家读完 */
    private static final long DURATION_MS = 8000L;

    /** Toast 创建时间，用于淡入淡出 */
    private long startTime = -1;
    private Visibility visibility = Visibility.SHOW;

    @Override public Visibility getWantedVisibility() { return visibility; }

    @Override public void update(ToastManager manager, long currentTime) {
        if (startTime < 0) startTime = currentTime;
        visibility = currentTime - startTime >= DURATION_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int width() {
        return 240;
    }

    @Override
    public int height() {
        return 44;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, net.minecraft.client.gui.Font font, long currentTime) {
        if (startTime == -1) startTime = currentTime;

        context.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("minecraft", "toast/system"), 0, 0, width(), height());

        Minecraft client = Minecraft.getInstance();

        Component title = Component.translatable("itemalchemy-expansion.new_feature_toast.title");
        context.text(client.font, title, 30, 7, 0xFFFFAA00, false);

        Component desc1 = Component.translatable("itemalchemy-expansion.new_feature_toast.desc1");
        context.text(client.font, desc1, 30, 20, 0xFFFFFFFF, false);

        // 描述行 2 超宽时省略号截断以紧凑显示
        Component desc2 = Component.translatable("itemalchemy-expansion.new_feature_toast.desc2");
        String desc2Str = desc2.getString();
        int maxW = width() - 35;
        if (client.font.width(desc2Str) > maxW) {
            while (client.font.width(desc2Str + "...") > maxW && desc2Str.length() > 1) {
                desc2Str = desc2Str.substring(0, desc2Str.length() - 1);
            }
            desc2Str = desc2Str + "...";
        }
        context.text(client.font, desc2Str, 30, 31, 0xFFA0A0A0, false);

        context.item(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE), 8, 12);

        
    }

    /**
     * 显示「新功能」Toast 通知。
     *
     * <p>由 {@code SetEmcClientNetwork} 在收到 {@code new_feature_toast} S2C 包后调用。</p>
     */
    public static void show() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.gui.toastManager().addToast(new NewFeatureToast());
        } catch (Throwable ignored) {
            // 防御性：Toast 显示失败不影响游戏逻辑
        }
    }
}
