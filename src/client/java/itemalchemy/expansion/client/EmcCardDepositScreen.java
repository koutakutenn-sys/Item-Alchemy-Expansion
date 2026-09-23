package itemalchemy.expansion.client;

import itemalchemy.expansion.client.util.GuiRenderUtil;
import itemalchemy.expansion.item.EmcCardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import itemalchemy.expansion.compat.port.FilteredEditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * EMC 卡充入界面：输入充入数量，从玩家 Team EMC 转入卡内。
 *
 * <p>显示玩家当前 EMC 与卡内 EMC，输入数量后确认。服务端再次校验余额，
 * 不足时通过 actionbar 反馈。充入后回到主菜单（卡内 EMC 已刷新）。</p>
 */
public class EmcCardDepositScreen extends Screen {

    private static final int PANEL_WIDTH = 260;
    private static final int PADDING = 14;
    private static final int LINE_HEIGHT = 16;

    private FilteredEditBox amountField;
    private Component errorText;

    public EmcCardDepositScreen() {
        super(EmcCardMainScreen.getCardName());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        errorText = null;
        int centerX = this.width / 2;
        int fieldY = this.height / 2 + 2;

        int fieldWidth = 180;
        amountField = new FilteredEditBox(this.font,
                centerX - fieldWidth / 2, fieldY, fieldWidth, 16,
                Component.translatable("itemalchemy-expansion.emc_card.amount_field"));
        amountField.setMaxLength(18);
        amountField.setTextPredicate(this::isNumeric);
        amountField.setValue("");
        addRenderableWidget(amountField);
        this.setFocused(amountField);

        int btnY = fieldY + 28;
        int btnWidth = 84;
        int gap = 8;
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.confirm"),
                b -> onConfirm())
                .bounds(centerX - btnWidth - gap / 2, btnY, btnWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("itemalchemy-expansion.emc_card.cancel"),
                b -> Minecraft.getInstance().setScreenAndShow(new EmcCardMainScreen()))
                .bounds(centerX + gap / 2, btnY, btnWidth, 20).build());
    }

    private boolean isNumeric(String s) {
        if (s.isEmpty()) return true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private void onConfirm() {
        String raw = amountField.getValue().trim();
        if (raw.isEmpty()) {
            errorText = Component.translatable("itemalchemy-expansion.emc_card.fail.empty")
                    .withStyle(ChatFormatting.RED);
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(raw);
        } catch (NumberFormatException e) {
            errorText = Component.translatable("itemalchemy-expansion.emc_card.fail.parse")
                    .withStyle(ChatFormatting.RED);
            return;
        }
        if (amount <= 0) {
            errorText = Component.translatable("itemalchemy-expansion.emc_card.fail.nonpositive")
                    .withStyle(ChatFormatting.RED);
            return;
        }
        long playerEmc = EmcCardMainScreen.getPlayerEmc();
        if (amount > playerEmc) {
            errorText = Component.translatable("itemalchemy-expansion.emc_card.deposit.fail.insufficient",
                    Component.literal(EmcCardItem.formatNumber(playerEmc))).withStyle(ChatFormatting.RED);
            return;
        }
        EmcCardClientNetwork.sendDeposit(amount);
        Minecraft.getInstance().setScreenAndShow(new EmcCardMainScreen());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // The 26.2 GUI manager already extracts the screen background.

        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = this.height / 2 - 90;
        int panelHeight = 180;

        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, 0xE0101420);
        GuiRenderUtil.drawBorder(context, panelLeft, panelTop, PANEL_WIDTH, panelHeight, 0xFF5060A0);

        context.centeredText(this.font, this.title,
                centerX, panelTop + PADDING, 0xFFE0E0FF);
        // 副标题：操作类型
        context.centeredText(this.font,
                Component.translatable("itemalchemy-expansion.emc_card.deposit.title"),
                centerX, panelTop + PADDING + 11, 0xFF8080A0);

        int lineY = panelTop + PADDING + 23;
        context.fill(panelLeft + PADDING, lineY, panelLeft + PANEL_WIDTH - PADDING, lineY + 1, 0xFF405080);

        long playerEmc = EmcCardMainScreen.getPlayerEmc();
        long cardEmc = getCardEmc();

        int dataY = lineY + 10;
        drawDataRow(context, "itemalchemy-expansion.emc_card.player_emc",
                EmcCardItem.formatNumber(playerEmc), panelLeft, dataY, 0xFFC0C0C0, 0xFFFFFF55);
        drawDataRow(context, "itemalchemy-expansion.emc_card.card_emc",
                EmcCardItem.formatNumber(cardEmc), panelLeft, dataY + LINE_HEIGHT, 0xFF40A0FF, 0xFF60C0FF);

        Component fieldLabel = Component.translatable("itemalchemy-expansion.emc_card.deposit.field_label")
                .withStyle(ChatFormatting.GRAY);
        context.text(this.font, fieldLabel,
                panelLeft + PADDING, amountField.getY() - 12, 0xFFA0A0C0, false);

        if (errorText != null) {
            context.centeredText(this.font, errorText,
                    centerX, amountField.getY() + 64, 0xFFFF5555);
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void drawDataRow(GuiGraphicsExtractor context, String labelKey, String valueStr,
                             int panelLeft, int y, int labelColor, int valueColor) {
        Component label = Component.translatable(labelKey);
        Component value = Component.literal(valueStr);
        context.text(this.font, label,
                panelLeft + PADDING, y, labelColor, false);
        context.text(this.font, value,
                panelLeft + PANEL_WIDTH - PADDING - font.width(value), y, valueColor, false);
    }

    private long getCardEmc() {
        return EmcCardClientNetwork.getCardBalance();
    }
}
