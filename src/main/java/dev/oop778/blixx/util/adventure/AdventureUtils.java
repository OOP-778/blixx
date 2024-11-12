package dev.oop778.blixx.util.adventure;

import net.kyori.adventure.text.Component;

import java.util.Iterator;

public class AdventureUtils {
    public static Component join(Component separator, Iterable<Component> iterable) {
        Component base = Component.empty();

        final Iterator<Component> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            base = base.append(iterator.next());
            if (iterator.hasNext()) {
                base = base.append(separator);
            }
        }
        return base;
    }
}
