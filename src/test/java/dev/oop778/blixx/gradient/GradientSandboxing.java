package dev.oop778.blixx.gradient;

import dev.oop778.blixx.BaseBlixxTest;
import dev.oop778.blixx.api.component.BlixxComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GradientSandboxing extends BaseBlixxTest {

    public static void main(String[] args) {
        final BlixxComponent component = BLIXX.parseComponent("<bold><gradient:#00FF00:#FF007F:every[4]>----------------------------------------");
        final Component component1 = component.asComponent();
        System.out.println(LegacyComponentSerializer.legacyAmpersand().serialize(component1));
    }
}
