package itemalchemy.expansion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 「无法放入潜影盒」Toast 通知（屏幕右上角弹窗）。
 *
 * <p>当玩家尝试把含无 EMC 物品的潜影盒放入转换桌输入槽时，{@code MixinRegisterSlot}
 * 通过反射调用 {@link #show(ItemStack)} 触发此 Toast，比聊天消息更醒目。</p>
 *
 * <p>布局：标题（红色）+ 描述（白色，XXX 为物品名），图标用 {@link #WARNING_TEXTURE}
 * （vanilla 的 warning 纹理，16×16）。持续时长 5000ms，与 vanilla SystemToast 一致。</p>
 *
 * <p>重复抑制：{@link #show(ItemStack)} 内部用 {@code lastShownItem} 记录上次物品，
 * 500ms 内同物品不重复弹出，避免短时间内多次 canInsert 调用导致刷屏。</p>
 */
public class NoEmcShulkerBoxToast implements Toast {

    /** Toast 背景纹理（vanilla 的 toast 纹理） */
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/toasts.png");

    /** Toast 持续时长（毫秒） */
    private static final long DURATION_MS = 5000L;

    /** 触发此 Toast 的无 EMC 物品 */
    private final ItemStack noEmcItem;
    /** Toast 创建时间，用于淡入淡出 */
    private long startTime = -1;
    private Visibility visibility = Visibility.SHOW;

    @Override public Visibility getWantedVisibility() { return visibility; }

    @Override public void update(ToastManager manager, long currentTime) {
        if (startTime < 0) startTime = currentTime;
        visibility = currentTime - startTime >= DURATION_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE : Visibility.SHOW;
    }

    public NoEmcShulkerBoxToast(ItemStack noEmcItem) {
        this.noEmcItem = noEmcItem;
    }

    @Override
    public int width() {
        return 160;
    }

    @Override
    public int height() {
        return 32;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, net.minecraft.client.gui.Font font, long currentTime) {
        if (startTime == -1) startTime = currentTime;

        context.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("minecraft", "toast/system"), 0, 0, width(), height());

        Minecraft client = Minecraft.getInstance();

        Component title = Component.translatable("itemalchemy-expansion.shulker_box.toast.title");
        context.text(client.font, title, 30, 7, 0xFFFF5555, false);

        // 描述行超宽时省略号截断
        Component desc = Component.translatable("itemalchemy-expansion.shulker_box.toast.desc",
                noEmcItem.getHoverName());
        String descStr = desc.getString();
        int maxW = width() - 35;
        if (client.font.width(descStr) > maxW) {
            while (client.font.width(descStr + "...") > maxW && descStr.length() > 1) {
                descStr = descStr.substring(0, descStr.length() - 1);
            }
            descStr = descStr + "...";
        }
        context.text(client.font, descStr, 30, 18, 0xFFFFFFFF, false);

        context.item(noEmcItem, 8, 8);

        
    }

    // ===== 静态触发入口（被 MixinRegisterSlot 反射调用） =====

    /** 上次弹 Toast 的时间（毫秒），用于重复抑制 */
    private static long lastShownTime = 0;
    /** 上次弹 Toast 的物品，用于重复抑制 */
    private static ItemStack lastShownItem = ItemStack.EMPTY;

    /**
     * 显示「无法放入潜影盒」Toast 通知。
     *
     * <p>由 {@code MixinRegisterSlot.sendNoEmcRejectMessage} 通过反射调用
     * （main 源集不能直接引用客户端类）。包含重复抑制：500ms 内同物品不重复弹出。</p>
     */
    public static void show(ItemStack noEmcItem) {
        if (noEmcItem == null || noEmcItem.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;

        long now = System.currentTimeMillis();
        // 500ms 内同物品不重复弹出
        if (now - lastShownTime < 500 && ItemStack.matches(lastShownItem, noEmcItem)) {
            return;
        }
        lastShownTime = now;
        lastShownItem = noEmcItem.copy();

        try {
            client.gui.toastManager().addToast(new NoEmcShulkerBoxToast(noEmcItem));
        } catch (Throwable ignored) {
            // 防御性：Toast 显示失败不影响游戏逻辑
        }
    }
}
