/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.nbt.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class NbtUtils {
    public static <T extends ISerializable<?>> NbtList listToTag(Iterable<T> list) {
        NbtList tag = new NbtList();
        for (T item : list) tag.add(item.toTag());
        return tag;
    }

    public static <T> List<T> listFromTag(NbtList tag, ToValue<T> toItem) {
        List<T> list = new ArrayList<>(tag.size());
        for (NbtElement itemTag : tag) {
            T value = toItem.toValue(itemTag);
            if (value != null) list.add(value);
        }
        return list;
    }

    public static <K, V extends ISerializable<?>> NbtCompound mapToTag(Map<K, V> map) {
        NbtCompound tag = new NbtCompound();
        for (K key : map.keySet()) tag.put(key.toString(), map.get(key).toTag());
        return tag;
    }

    public static <K, V> Map<K, V> mapFromTag(NbtCompound tag, ToKey<K> toKey, ToValue<V> toValue) {
        Map<K, V> map = new HashMap<>(tag.getSize());
        for (String key : tag.getKeys()) map.put(toKey.toKey(key), toValue.toValue(tag.get(key)));
        return map;
    }

    public static boolean toClipboard(ISerializable<?> serializable) {
        return toClipboard(serializable.toTag());
    }

    public static boolean toClipboard(NbtCompound tag) {
        String preClipboard = mc.keyboard.getClipboard();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, byteArrayOutputStream);
            mc.keyboard.setClipboard(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return true;
        } catch (Exception e) {
            MeteorClient.LOG.error("Error copying NBT to clipboard!", e);
            mc.keyboard.setClipboard(preClipboard);
            return false;
        }
    }

    public static boolean fromClipboard(ISerializable<?> serializable) {
        NbtCompound tag = fromClipboard();
        if (tag == null) return false;

        NbtCompound sourceTag = serializable.toTag();
        for (String key : sourceTag.getKeys()) {
            if (!tag.contains(key)) return false;
        }

        serializable.fromTag(tag);
        return true;
    }

    public static NbtCompound fromClipboard() {
        try {
            byte[] data = Base64.getDecoder().decode(mc.keyboard.getClipboard().trim());
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            return NbtIo.readCompressed(new DataInputStream(bis), NbtSizeTracker.ofUnlimitedBytes());
        } catch (Exception e) {
            MeteorClient.LOG.error("Invalid NBT data pasted!", e);
            return null;
        }
    }

    public interface ToKey<T> {
        T toKey(String string);
    }

    public interface ToValue<T> {
        T toValue(NbtElement tag);
    }
}
