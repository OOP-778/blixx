package dev.oop778.blixx.api.component;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
/** Platform-agnostic click event with an action name (e.g. "open_url", "run_command") and a value. */
public class BlixxClickEvent {
    private final String action;
    private final String value;

    private BlixxClickEvent(String action, String value) {
        this.action = action;
        this.value = value;
    }

    public static BlixxClickEvent of(String action, String value) {
        return new BlixxClickEvent(action.toLowerCase(), value);
    }

    @Override
    public String toString() {
        return "BlixxClickEvent{action=" + this.action + ", value=" + this.value + "}";
    }
}
