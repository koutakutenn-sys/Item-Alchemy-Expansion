package itemalchemy.expansion.client.util;

import itemalchemy.expansion.ItemAlchemyExpansion;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.pitan76.itemalchemy.client.screen.AlchemyTableScreen;
import net.pitan76.itemalchemy.gui.screen.AlchemyTableScreenHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 客户端 GUI 渲染共享工具：边框绘制与悬停槽位反射读取。统一维护反射字段名与缓存，
 * 供 {@code AlchemyTableScreenShulkerPreview}、{@code AlchemyTableSlotMatchOverlay}、
 * {@code SetEmcScreen}、{@code RepriceConfirmScreen} 复用。
 */
public final class GuiRenderUtil {

    /**
     * 26.2 里 {@code AbstractContainerScreen.hoveredSlot} 不再由鼠标事件写入，
     * 而是每帧在 {@code extractRenderState} 内用私有 {@code getHoveredSlot(mouseX, mouseY)} 重新赋值；
     * 方法的可见性也变为 private，无法直接调用。因此这里按官方字段名读取容器左上角，
     * 再用鼠标坐标自己做几何命中判定。
     */
    private static final String LEFT_POS_FIELD = "leftPos";
    private static final String TOP_POS_FIELD = "topPos";
    private static final int SLOT_SIZE = 16;
    private static final int NOT_FOUND = Integer.MIN_VALUE;

    /** 缓存的容器左上角反射 Field，首次使用时查找；查找失败置 null 表示不可用 */
    private static Field leftPosField;
    private static Field topPosField;
    private static boolean positionFieldsResolved = false;

    /** 缓存的 getScreenHandlerOverride Method，首次使用时查找 */
    private static Method getHandlerMethod;
    private static boolean getHandlerMethodResolved = false;

    private GuiRenderUtil() {}

    /**
     * 绘制 1px 粗的矩形边框（4 条线）。调用前应已通过 {@code matrices.translate(0, 0, z)}
     * 设定所需的 z-level，边框与背景同 z。颜色为 ARGB 格式（如 {@code 0xFF505050}）。
     */
    public static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);                   // top
        context.fill(x, y + height - 1, x + width, y + height, color);  // bottom
        context.fill(x, y, x + 1, y + height, color);                   // left
        context.fill(x + width - 1, y, x + width, y + height, color);   // right
    }

    /** 按鼠标 GUI 坐标返回当前悬停槽位；不可用时返回 null（调用方应静默跳过）。 */
    public static Slot getHoveredSlot(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        if (screen == null) return null;
        int left = readPosition(screen, true);
        int top = readPosition(screen, false);
        if (left == NOT_FOUND || top == NOT_FOUND) return null;
        try {
            for (Slot slot : screen.getMenu().slots) {
                if (!slot.isActive()) continue;
                int slotX = left + slot.x;
                int slotY = top + slot.y;
                if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE
                        && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                    return slot;
                }
            }
        } catch (Throwable t) {
            return null;
        }
        return null;
    }

    /** 读取容器左上角坐标；解析失败返回 {@link #NOT_FOUND} */
    private static int readPosition(AbstractContainerScreen<?> screen, boolean horizontal) {
        resolvePositionFields();
        Field field = horizontal ? leftPosField : topPosField;
        if (field == null) return NOT_FOUND;
        try {
            return field.getInt(screen);
        } catch (Throwable t) {
            return NOT_FOUND;
        }
    }

    /** 懒加载并缓存 {@link AbstractContainerScreen} 的 leftPos/topPos 反射 Field */
    private static void resolvePositionFields() {
        if (positionFieldsResolved) return;
        try {
            leftPosField = AbstractContainerScreen.class.getDeclaredField(LEFT_POS_FIELD);
            leftPosField.setAccessible(true);
            topPosField = AbstractContainerScreen.class.getDeclaredField(TOP_POS_FIELD);
            topPosField.setAccessible(true);
        } catch (Throwable t) {
            ItemAlchemyExpansion.LOGGER.warn("[IAExp] Could not resolve AbstractContainerScreen {} / {}; hover-slot feature disabled",
                    LEFT_POS_FIELD, TOP_POS_FIELD, t);
            leftPosField = null;
            topPosField = null;
        }
        positionFieldsResolved = true;
    }

    /**
     * 反射调用 {@link AlchemyTableScreen#getScreenHandlerOverride()} 获取 ScreenHandler。
     * Method 懒加载并缓存以避免每帧反射查找；调用失败返回 null（调用方应静默跳过）。
     */
    public static AlchemyTableScreenHandler getScreenHandler(AlchemyTableScreen screen) {
        Method m = resolveGetHandlerMethod();
        if (m == null) return null;
        try {
            return (AlchemyTableScreenHandler) m.invoke(screen);
        } catch (Throwable t) {
            return null;
        }
    }

    /** 懒加载并缓存 AlchemyTableScreen.getScreenHandlerOverride 的反射 Method */
    private static Method resolveGetHandlerMethod() {
        if (getHandlerMethodResolved) return getHandlerMethod;
        try {
            getHandlerMethod = AlchemyTableScreen.class.getMethod("getScreenHandlerOverride");
        } catch (Throwable t) {
            ItemAlchemyExpansion.LOGGER.warn("[IAExp] Could not resolve AlchemyTableScreen.getScreenHandlerOverride; preview/overlay disabled",
                    t);
            getHandlerMethod = null;
        }
        getHandlerMethodResolved = true;
        return getHandlerMethod;
    }
}
