package itemalchemy.expansion.nbt;

import itemalchemy.expansion.config.IAExpConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NBT 指纹生成器：把物品 NBT 压缩为规范化 SNBT 字符串。
 *
 * <p>保留全部 NBT（扣除 ignoreNbtKeys、可选的 Damage/RepairCost、以及按 namespace 的内置易变键），
 * 输出 CompoundTag 的 SNBT。</p>
 *
 * <p>输出的是合法 SNBT（如 {@code {GunId:"tacz:ak47"}}），可用 {@link #parseFingerprint(String)}
 * 通过 {@link TagParser} 反解析回 {@link CompoundTag}，从而在提取槽重建带 NBT 的 ItemStack。</p>
 *
 * <p>规范化：key 按字典序复制到新 CompoundTag，确保等价 NBT 产生相同指纹（顺序无关）。</p>
 */
public final class NbtFingerprinter {

    private final IAExpConfig config;

    public NbtFingerprinter(IAExpConfig config) {
        this.config = config;
    }

    /**
     * 生成指纹。
     *
     * @param stack 物品堆（用于查询 namespace）
     * @param nbt   物品 NBT（itemalchemy.expansion.compat.port.StackData.getNbt(stack)）
     * @return SNBT 字符串；若过滤后无 key 返回空串（表示无需区分，等同于纯 ID）
     */
    public String fingerprint(ItemStack stack, CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) return "";

        // 计算应忽略的 key 集合
        Set<String> ignore = collectIgnoreKeys();

        // 追加按 namespace 的内置易变键（如 TACZ 的弹药数/开火模式/配件等），避免运行时状态变化导致失配
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String namespace = NbtIgnoreRules.namespaceOf(itemId);
        ignore.addAll(NbtIgnoreRules.builtinIgnoreKeysFor(namespace));

        // 保留全部 NBT，扣除 ignore 集合
        return filterNbt(nbt, ignore);
    }

    /**
     * 收集本次应忽略的 NBT key 集合。
     * <ul>
     *   <li>{@code config.ignoreNbtKeys}：用户配置的通用忽略</li>
     *   <li>{@code config.fullIgnoreDamageAndRepairCost}：默认忽略 Damage/RepairCost，
     *       避免工具因耐久不同产生大量变体</li>
     * </ul>
     */
    private Set<String> collectIgnoreKeys() {
        Set<String> ignore = new HashSet<>();
        if (config.ignoreNbtKeys != null) {
            ignore.addAll(config.ignoreNbtKeys);
        }
        if (config.fullIgnoreDamageAndRepairCost) {
            ignore.add("Damage");
            ignore.add("RepairCost");
        }
        return ignore;
    }

    /** 把 nbt 中不在 ignore 集合中的 key 按字典序复制到新 CompoundTag，返回其 SNBT */
    private String filterNbt(CompoundTag nbt, Set<String> ignore) {
        List<String> keys = new ArrayList<>();
        for (String key : nbt.keySet()) {
            if (!ignore.contains(key)) keys.add(key);
        }
        if (keys.isEmpty()) return "";
        Collections.sort(keys);
        CompoundTag filtered = new CompoundTag();
        for (String key : keys) {
            if (key.equals("__iaexp_components")) {
                CompoundTag components = nbt.getCompoundOrEmpty(key).copy();
                if (ignore.contains("Damage")) components.remove("minecraft:damage");
                if (ignore.contains("RepairCost")) components.remove("minecraft:repair_cost");
                for (String ignored : ignore) components.remove(ignored);
                if (!components.isEmpty()) filtered.put(key, components);
            } else filtered.put(key, nbt.get(key).copy());
        }
        return filtered.isEmpty() ? "" : canonicalSnbt(filtered);
    }

    /** Recursive ordering is necessary for equivalent nested component maps. */
    private static String canonicalSnbt(net.minecraft.nbt.Tag tag) {
        if (tag instanceof CompoundTag compound) {
            return compound.keySet().stream().sorted()
                    .map(key -> net.minecraft.nbt.StringTag.valueOf(key).toString() + ":" + canonicalSnbt(compound.get(key)))
                    .collect(java.util.stream.Collectors.joining(",", "{", "}"));
        }
        if (tag instanceof net.minecraft.nbt.ListTag list) {
            java.util.List<String> elements = new java.util.ArrayList<>();
            for (net.minecraft.nbt.Tag element : list) elements.add(canonicalSnbt(element));
            return "[" + String.join(",", elements) + "]";
        }
        return tag.toString();
    }

    /**
     * 把指纹 SNBT 解析回 CompoundTag。解析失败返回 null（调用方回退为无 NBT 物品）。
     */
    public static CompoundTag parseFingerprint(String snbt) {
        if (snbt == null || snbt.isEmpty()) return null;
        try {
            return TagParser.parseCompoundFully(snbt);
        } catch (Exception e) {
            return null;
        }
    }
}
