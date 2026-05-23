package dev.oop778.blixx.adventure.util;

import java.util.Iterator;
import net.kyori.adventure.text.Component;

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
