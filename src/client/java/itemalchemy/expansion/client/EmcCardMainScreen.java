package itemalchemy.expansion.client;

import itemalchemy.expansion.client.util.GuiRenderUtil;
import itemalchemy.expansion.item.EmcCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.pitan76.itemalchemy.ItemAlchemyClient;

/**
 * EMC 卡主菜单：显示卡片价值/存储/总计与玩家 EMC，提供「充入 / 拿取 / 关闭」入口。
 *
 * <p>右键卡时服务端发 S2C 信号，客户端打开本界面。
 * 卡内 EMC 从 {@code mc.player.getMainHandItem()} NBT 实时读取，
 * 玩家 EMC 从上游 {@link ItemAlchemyClient#itemAlchemyNbt} 的 {@code team.emc} 读取。</p>
 */
public class EmcCardMainScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 16;

    public EmcCardMainScreen() {
        super(getCardName());
    }

    /** 读取主手卡的实际显示名称（含铁砧重命名），未持卡时回退默认标题 */
    public static Component getCardName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null)
            return Component.translatable("itemalchemy-expansion.emc_card.title");
        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof EmcCardItem))
            return Component.translatable("itemalchemy-expansion.emc_card.title");
        return mainHand.getHoverName();
    }

    @Override
    public boolean isPauseScreen() {
        // 不暂停：GUI 与服务端实时通信（充入/拿取），暂停会阻塞服务端包处理
        return false;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int panelTop = this.height / 2 - 100;
        int btnY = panelTop + 118;

        int btnWidth = 96;
        int gap = 8;
        // 第一行：充入 / 拿取
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.deposit"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardDepositScreen()))
                .bounds(centerX - btnWidth - gap / 2, btnY, btnWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.withdraw"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardWithdrawScreen()))
                .bounds(centerX + gap / 2, btnY, btnWidth, 20).build());

        // 第二行：设置 / 记录
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.config"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardConfigScreen()))
                .bounds(centerX - btnWidth - gap / 2, btnY + 24, btnWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.log"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardLogScreen()))
                .bounds(centerX + gap / 2, btnY + 24, btnWidth, 20).build());

        // 第三行：关闭
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.close"),
                b -> this.onClose())
                .bounds(centerX - 60, btnY + 48, 120, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // The 26.2 GUI manager already extracts the screen background.

        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = this.height / 2 - 100;
        int panelHeight = 200;

        // 面板背景：深蓝紫色调
        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xE0101420);
        GuiRenderUtil.drawBorder(context, panelLeft, panelTop, PANEL_WIDTH, panelHeight, 0xFF5060A0);

        // 标题
        context.centeredText(this.font, this.title,
                centerX, panelTop + PADDING, 0xFFE0E0FF);

        // 标题下装饰线
        int lineY = panelTop + PADDING + 12;
        context.fill(panelLeft + PADDING, lineY, panelLeft + PANEL_WIDTH - PADDING, lineY + 1, 0xFF405080);

        long cardEmc = getCardEmc();
        long playerEmc = getPlayerEmc();

        int dataY = lineY + 12;

        // 绑卡：额外显示「已绑定：玩家名」并把卡内余额行改名为绑定余额
        Minecraft mc = Minecraft.getInstance();
        ItemStack mainHand = (mc != null && mc.player != null)
                ? mc.player.getMainHandItem() : ItemStack.EMPTY;
        boolean bound = !mainHand.isEmpty()
                && mainHand.getItem() instanceof EmcCardItem && EmcCardItem.isBound(mainHand);

        if (bound) {
            String bindName = resolveBindName(mainHand);
            // 已绑定：<名>。%s 在翻译键内，须传参渲染，否则会显示字面 %s
            Component bindLabel = Component.translatable("itemalchemy-expansion.emc_card.bind.label", bindName);
            context.text(this.font, bindLabel, panelLeft + PADDING, dataY, 0xFF40FF80, false);
            // 绑定余额：同步绑定玩家的队 EMC（服务端下发的权威值）
            drawDataRow(context, "itemalchemy-expansion.emc_card.bind_emc",
                    EmcCardItem.formatNumber(cardEmc), panelLeft, dataY + LINE_HEIGHT + 8, 0xFF40A0FF, 0xFF60C0FF);
        } else {
            // 数据行：标签左对齐，数值右对齐（卡片本身价值不在此展示）
            drawDataRow(context, "itemalchemy-expansion.emc_card.card_emc",
                    EmcCardItem.formatNumber(cardEmc), panelLeft, dataY, 0xFF40A0FF, 0xFF60C0FF);
            drawDataRow(context, "itemalchemy-expansion.emc_card.player_emc",
                    EmcCardItem.formatNumber(playerEmc), panelLeft, dataY + LINE_HEIGHT + 8, 0xFFC0C0C0, 0xFFFFFF55);
        }

        // 分隔线
        int divY = dataY + LINE_HEIGHT * 2 + 16;
        context.fill(panelLeft + PADDING, divY, panelLeft + PANEL_WIDTH - PADDING, divY + 1, 0xFF405080);

        // 合并提示（灰色小字）
        context.centeredText(this.font,
                Component.translatable("itemalchemy-expansion.emc_card.merge_hint"),
                centerX, divY + 8, 0xFF8080A0);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    /** 绘制一行数据：左侧标签（灰），右侧数值（带颜色） */
    private void drawDataRow(GuiGraphicsExtractor context, String labelKey, String valueStr,
                             int panelLeft, int y, int labelColor, int valueColor) {
        Component label = Component.translatable(labelKey);
        Component value = Component.literal(valueStr);
        context.text(this.font, label,
                panelLeft + PADDING, y, labelColor, false);
        context.text(this.font, value,
                panelLeft + PANEL_WIDTH - PADDING - font.width(value), y, valueColor, false);
    }

    /** 解析绑卡玩家名：卡上存的绑定名（离线可读）> 在线玩家档案 > UUID 截断 */
    private static String resolveBindName(ItemStack card) {
        String stored = EmcCardItem.getBindName(card);
        if (stored != null && !stored.isEmpty()) return stored;
        String uuid = EmcCardItem.getBindUuid(card);
        if (uuid == null) return "";
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getConnection() != null) {
                var entry = mc.getConnection().getPlayerInfo(java.util.UUID.fromString(uuid));
                if (entry != null && entry.getProfile() != null) {
                    return entry.getProfile().name();
                }
            }
        } catch (Throwable t) {
            // fallthrough
        }
        return uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
    }

    /** 当前主手卡内 EMC（优先用服务端同步余额，反映关联账户值）。 */
    private long getCardEmc() {
        return EmcCardClientNetwork.getCardBalance();
    }

    /** 当前玩家 Team EMC（从上游客户端缓存的 team NBT 读取）。 */
    public static long getPlayerEmc() {
        try {
            CompoundTag root = ItemAlchemyClient.itemAlchemyNbt;
            if (root == null) return 0;
            CompoundTag team = root.getCompoundOrEmpty("team");
            if (team == null) return 0;
            return team.getLongOr("emc", 0L);
        } catch (Throwable t) {
            return 0;
        }
    }
}
