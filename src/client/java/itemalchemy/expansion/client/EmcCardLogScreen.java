package itemalchemy.expansion.client;

import itemalchemy.expansion.client.util.GuiRenderUtil;
import itemalchemy.expansion.item.EmcCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * EMC 卡交易记录界面：显示最近 {@link EmcCardItem#MAX_TRANSACTIONS} 笔充入/拿取记录。
 */
public class EmcCardLogScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 14;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM-dd HH:mm");

    public EmcCardLogScreen() {
        super(EmcCardMainScreen.getCardName());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int panelTop = this.height / 2 - 100;
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.cancel"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardMainScreen()))
                .bounds(centerX - 60, panelTop + 170, 120, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // The 26.2 GUI manager already extracts the screen background.
        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = this.height / 2 - 100;
        int panelHeight = 200;

        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xE0101420);
        GuiRenderUtil.drawBorder(context, panelLeft, panelTop, PANEL_WIDTH, panelHeight, 0xFF5060A0);

        context.centeredText(this.font, this.title,
                centerX, panelTop + PADDING, 0xFFE0E0FF);
        context.centeredText(this.font,
                Component.translatable("itemalchemy-expansion.emc_card.log.title"),
                centerX, panelTop + PADDING + 11, 0xFF8080A0);

        int lineY = panelTop + PADDING + 23;
        context.fill(panelLeft + PADDING, lineY, panelLeft + PANEL_WIDTH - PADDING, lineY + 1, 0xFF405080);

        ItemStack card = getCardStack();
        ListTag list = EmcCardItem.getTransactions(card);

        if (list.isEmpty()) {
            context.centeredText(this.font,
                    Component.translatable("itemalchemy-expansion.emc_card.log.empty")
                            .withStyle(net.minecraft.ChatFormatting.GRAY),
                    centerX, lineY + 20, 0xFF808080);
        } else {
            int y = lineY + 8;
            // 倒序显示（最新在上）
            for (int i = list.size() - 1; i >= 0 && y < panelTop + panelHeight - 30; i--) {
                CompoundTag entry = list.getCompound(i).orElseGet(CompoundTag::new);
                byte type = entry.getByteOr("type", (byte)0);
                long amount = entry.getLongOr("amount", 0L);
                long time = entry.getLongOr("time", 0L);

                String typeKey = type == EmcCardItem.TX_DEPOSIT
                        ? "itemalchemy-expansion.emc_card.log.deposit"
                        : "itemalchemy-expansion.emc_card.log.withdraw";
                int typeColor = type == EmcCardItem.TX_DEPOSIT ? 0xFF40E060 : 0xFFFF8040;

                String timeStr = DATE_FMT.format(new Date(time));
                Component line = Component.literal("§7" + timeStr + " §r")
                        .append(Component.translatable(typeKey).withStyle(net.minecraft.ChatFormatting.BOLD))
                        .append(" " + EmcCardItem.formatNumber(amount) + " EMC");

                context.text(this.font, line,
                        panelLeft + PADDING, y, typeColor, false);
                y += LINE_HEIGHT;
            }
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private ItemStack getCardStack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return ItemStack.EMPTY;
        ItemStack mainHand = mc.player.getMainHandItem();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof EmcCardItem)) return ItemStack.EMPTY;
        return mainHand;
    }
}
