package dev.oop778.blixx.util.adventure;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

// A LOT less memory consumption than using adventure's text component builder
@Getter
@Setter
public class FastComponentBuilder {
    private List<Component> children;
    private Style style;
    private String content;

    private static final Unsafe UNSAFE = getUnsafe();
    private static final Supplier<TextComponent> CREATE_TEXT_COMPONENT;
    private static final MethodHandle SET_CONTENT_HANDLE;
    private static final MethodHandle SET_STYLE_HANDLE;
    private static final MethodHandle SET_CHILDREN_HANDLE;

    @SneakyThrows
    public static Unsafe getUnsafe() {
        final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);

        return (Unsafe) theUnsafe.get(null);
    }

    static {
        try {
            final Class<?> clazz = Class.forName("net.kyori.adventure.text.TextComponentImpl");
            final Constructor<?> declaredConstructor = clazz.getDeclaredConstructors()[0];
            declaredConstructor.setAccessible(true);

            CREATE_TEXT_COMPONENT = () -> {
                try {
                    return (TextComponent) UNSAFE.allocateInstance(clazz);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            };

            SET_CONTENT_HANDLE = getSetterMethodHandle(clazz, "content");
            SET_STYLE_HANDLE = getSetterMethodHandle(clazz, "style");
            SET_CHILDREN_HANDLE = getSetterMethodHandle(clazz, "children");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public FastComponentBuilder() {
    }

    public FastComponentBuilder append(Component component) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }

        this.children.add(component);
        return this;
    }

    @SneakyThrows
    private static MethodHandle getSetterMethodHandle(Class<?> clazz, String fieldName) {
        final Queue<Class<?>> toVisit = new LinkedList<>();
        toVisit.add(clazz);

        Field declaredField = null;
        while (!toVisit.isEmpty()) {
            final Class<?> current = toVisit.poll();
            try {
                declaredField = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ignored) {}

            if (current.getSuperclass() != Object.class) {
                toVisit.add(current.getSuperclass());
            }
        }

        if (declaredField == null) {
            throw new NoSuchFieldException(fieldName);
        }

        declaredField.setAccessible(true);
        return MethodHandles.publicLookup().unreflectSetter(declaredField);
    }

    @SneakyThrows
    public TextComponent build() {
        final TextComponent textComponent = CREATE_TEXT_COMPONENT.get();
        SET_CONTENT_HANDLE.invoke(textComponent, this.content);
        SET_STYLE_HANDLE.invoke(textComponent, this.style == null ? Style.empty() : this.style);
        SET_CHILDREN_HANDLE.invoke(textComponent, this.children == null ? Collections.emptyList() : this.children);

        return textComponent;
    }
}
