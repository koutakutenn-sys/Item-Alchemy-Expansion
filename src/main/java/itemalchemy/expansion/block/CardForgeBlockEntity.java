package itemalchemy.expansion.block;

import itemalchemy.expansion.gui.CardForgeScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.container.factory.DisplayNameArgs;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.args.CreateMenuEvent;
import net.pitan76.mcpitanlib.api.gui.v2.SimpleScreenHandlerFactory;
import net.pitan76.mcpitanlib.api.tile.CompatBlockEntity;
import net.pitan76.mcpitanlib.api.util.TextUtil;
import org.jetbrains.annotations.Nullable;

/**
 * 制卡台 BlockEntity：持有两张 EMC 卡槽，实现 {@link SimpleScreenHandlerFactory} 供右键打开 GUI。
 * 卡槽持久化到 NBT；配置操作（私有/公有、关联、合并）由 AbstractContainerMenu 通过 C2S 触发。
 */
public class CardForgeBlockEntity extends CompatBlockEntity implements Container, SimpleScreenHandlerFactory {

    /** 卡槽数量 */
    public static final int SLOT_COUNT = 2;

    private final ItemStack[] cardSlots = new ItemStack[SLOT_COUNT];

    public CardForgeBlockEntity(BlockEntityType<?> type, TileCreateEvent event) {
        super(type, event);
        for (int i = 0; i < SLOT_COUNT; i++) {
            cardSlots[i] = ItemStack.EMPTY;
        }
    }

    /** BlockEntityType.Builder.create 用（pos/state 构造） */
    public CardForgeBlockEntity(BlockPos pos, BlockState state) {
        super(CardForgeBlocks.FORGE_TILE, pos, state);
        for (int i = 0; i < SLOT_COUNT; i++) {
            cardSlots[i] = ItemStack.EMPTY;
        }
    }

    // ==================== Container ====================

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : cardSlots) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        return cardSlots[slot];
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack cur = cardSlots[slot];
        if (cur.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = cur.split(amount);
        setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack cur = cardSlots[slot];
        cardSlots[slot] = ItemStack.EMPTY;
        setChanged();
        return cur;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        cardSlots[slot] = stack;
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        BlockPos pos = getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            cardSlots[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    // ==================== NBT 持久化 ====================

    @Override
    public void writeNbt(WriteNbtArgs args) {
        CompoundTag nbt = args.getNbt();
        for (int i = 0; i < SLOT_COUNT; i++) {
            nbt.put("slot_" + i, itemalchemy.expansion.compat.port.StackData.writeNbt(cardSlots[i], new CompoundTag()));
        }
    }

    @Override
    public void readNbt(ReadNbtArgs args) {
        CompoundTag nbt = args.getNbt();
        for (int i = 0; i < SLOT_COUNT; i++) {
            cardSlots[i] = itemalchemy.expansion.compat.port.StackData.fromNbt(nbt.getCompound("slot_" + i));
        }
    }

    // ==================== SimpleScreenHandlerFactory ====================

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(CreateMenuEvent e) {
        return new CardForgeScreenHandler(e, this);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName(DisplayNameArgs args) {
        return TextUtil.translatable("block.itemalchemy-expansion.card_forge");
    }
}