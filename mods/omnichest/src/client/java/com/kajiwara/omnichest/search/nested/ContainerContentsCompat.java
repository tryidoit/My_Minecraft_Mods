package com.kajiwara.omnichest.search.nested;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 26.3 で {@code ItemContainerContents#allItemsCopyStream} が削除されたための版差吸収。
 * 後継メソッド名がマッピング非依存 (26.x は非難読化) なので、 既知の候補名を reflection で探す。
 * 26.3 の正式名が分かったら、 ここを直接呼びに置き換えてよい。
 */
final class ContainerContentsCompat {
    private static final String[] CANDIDATES = {
            "allItemsCopyStream", "nonEmptyItemCopyStream", "nonEmptyItemsCopyStream",
            "allItemsCopy", "nonEmptyItemsCopy", "allItems", "nonEmptyItems", "stream", "nonEmptyStream"
    };
    private static volatile Method cached;
    private static volatile boolean resolved;

    private ContainerContentsCompat() {
    }

    /** 空スロットを含む全アイテム (防御的コピー)。 取得できなければ空リスト。 */
    @SuppressWarnings("unchecked")
    static List<ItemStack> allItems(ItemContainerContents contents) {
        List<ItemStack> out = new ArrayList<>();
        try {
            Method m = resolve();
            if (m == null) {
                return out;
            }
            Object r = m.invoke(contents);
            if (r instanceof Stream<?> st) {
                ((Stream<ItemStack>) st).forEach(s -> out.add(s == null ? ItemStack.EMPTY : s.copy()));
            } else if (r instanceof Iterable<?> it) {
                for (Object o : it) {
                    out.add(o == null ? ItemStack.EMPTY : ((ItemStack) o).copy());
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // 取得失敗時は空扱い (入れ子プレビューが出ないだけで致命的ではない)
        }
        return out;
    }

    private static Method resolve() {
        if (resolved) {
            return cached;
        }
        synchronized (ContainerContentsCompat.class) {
            if (!resolved) {
                for (String name : CANDIDATES) {
                    try {
                        Method m = ItemContainerContents.class.getMethod(name);
                        Class<?> rt = m.getReturnType();
                        if (Stream.class.isAssignableFrom(rt) || Iterable.class.isAssignableFrom(rt)) {
                            cached = m;
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {
                        // 次の候補へ
                    }
                }
                resolved = true;
            }
        }
        return cached;
    }
}
