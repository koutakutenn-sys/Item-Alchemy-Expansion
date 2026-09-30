package itemalchemy.expansion.nbt;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.pitan76.itemalchemy.EMCManager;
import java.util.Arrays;

/** Reads the native container component while preserving the original pricing policy. */
public final class ShulkerBoxSupport {
    private ShulkerBoxSupport() {}
    public static boolean isShulkerBox(ItemStack stack) { return stack != null && !stack.isEmpty() && isShulkerBox(stack.getItem()); }
    public static boolean isShulkerBox(Item item) { return item instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock; }
    public static boolean hasContents(ItemStack stack) {
        return isShulkerBox(stack) && stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems().iterator().hasNext();
    }
    public static ItemStack[] getContents(ItemStack stack) {
        ItemStack[] result = new ItemStack[27];
        Arrays.fill(result,ItemStack.EMPTY);
        if (!isShulkerBox(stack)) return result;
        var items = stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).allItemsCopyStream().toList();
        for(int i=0;i<Math.min(27,items.size());i++) result[i]=items.get(i).copy();
        return result;
    }
    public static long sumEmc(ItemStack stack) { return getContentsAndSumEmc(stack).sumEmc; }
    public static ContentsAndEmc getContentsAndSumEmc(ItemStack stack) {
        ItemStack[] contents=getContents(stack);
        long sum=isShulkerBox(stack)?EMCManager.get(stack.getItem()):0;
        for(ItemStack content:contents) if(!content.isEmpty()) sum=Math.addExact(sum,Math.multiplyExact(EMCManager.get(content.getItem()),content.getCount()));
        return new ContentsAndEmc(contents,sum);
    }
    public static ItemStack findNoEmcItem(ItemStack stack) {
        for(ItemStack item:getContents(stack)) if(!item.isEmpty() && EMCManager.get(item.getItem())==0) return item;
        return null;
    }
    public static final class ContentsAndEmc {
        public final ItemStack[] contents; public final long sumEmc;
        public ContentsAndEmc(ItemStack[] contents,long sumEmc){this.contents=contents;this.sumEmc=sumEmc;}
    }
}
