package itemalchemy.expansion.compat.port;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.pitan76.mcpitanlib.api.registry.CompatRegistryLookup;
import java.util.function.Supplier;

/** Registry-aware component serialization, separate from mutable card custom data. */
public final class StackData {
    private static volatile HolderLookup.Provider serverLookup;
    private static Supplier<HolderLookup.Provider> clientLookup = () -> null;
    private static final class DefaultLookup { static final HolderLookup.Provider VALUE = new CompatRegistryLookup().getRegistryLookup(); }
    public static void serverLookup(HolderLookup.Provider provider) { serverLookup = provider; }
    public static void clientLookup(Supplier<HolderLookup.Provider> supplier) { clientLookup = supplier; }
    public static HolderLookup.Provider lookup() {
        HolderLookup.Provider client = clientLookup.get();
        return client != null ? client : serverLookup != null ? serverLookup : DefaultLookup.VALUE;
    }
    public static net.minecraft.resources.RegistryOps<Tag> ops() { return lookup().createSerializationContext(NbtOps.INSTANCE); }
    public static CompoundTag getNbt(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag components = (CompoundTag) DataComponentPatch.CODEC.encodeStart(ops(), stack.getComponentsPatch()).getOrThrow();
        components.remove("minecraft:custom_data");
        if (!components.isEmpty()) tag.put("__iaexp_components", components);
        return tag.isEmpty() ? null : tag;
    }
    public static boolean hasNbt(ItemStack stack) { return stack != null && !stack.getComponentsPatch().isEmpty(); }
    public static void setNbt(ItemStack stack, CompoundTag tag) {
        if (tag == null) return;
        CompoundTag data = tag.copy();
        Tag components = data.remove("__iaexp_components");
        if (components != null) stack.applyComponents(DataComponentPatch.CODEC.parse(ops(), components).getOrThrow());
        if (data.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
    public static CompoundTag writeNbt(ItemStack stack, CompoundTag into) {
        if (stack.isEmpty()) return into;
        into.merge((CompoundTag) ItemStack.CODEC.encodeStart(ops(), stack).getOrThrow());
        return into;
    }
    public static ItemStack fromNbt(CompoundTag data) {
        return ItemStack.CODEC.parse(ops(), data).result().orElse(ItemStack.EMPTY);
    }
    public static ItemStack fromNbt(java.util.Optional<CompoundTag> data) { return data.map(StackData::fromNbt).orElse(ItemStack.EMPTY); }
    public static MutableData getOrCreateNbt(ItemStack stack) { return new MutableData(stack); }
    public static final class MutableData {
        private final ItemStack stack;
        private final CompoundTag tag;
        private MutableData(ItemStack stack) { this.stack=stack; tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag(); }
        private void save() { stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag)); }
        public void put(String key, Tag value) { tag.put(key,value); save(); }
        public void putLong(String key,long value) { tag.putLong(key,value); save(); }
        public void putString(String key,String value) { tag.putString(key,value); save(); }
        public void putBoolean(String key,boolean value) { tag.putBoolean(key,value); save(); }
        public void putInt(String key,int value) { tag.putInt(key,value); save(); }
        public void remove(String key) { tag.remove(key); save(); }
        public boolean contains(String key) { return tag.contains(key); }
        public ListTag getListOrEmpty(String key) { return tag.getListOrEmpty(key); }
    }
}
