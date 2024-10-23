package dev.oop778.blixx.api.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<R, L> {
    private R left;
    private L right;
}
