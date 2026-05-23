# Blixx
![Maven Central](https://img.shields.io/maven-central/v/dev.oop778.blixx/blixx)
![Static Badge](https://img.shields.io/badge/java_version-8--latest-brightgreen)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/OOP-778/blixx)

A fast text parsing and replacement library for Minecraft and beyond. Parses MiniMessage-style tags, replaces placeholders efficiently, and outputs platform-native components.

**~8x faster** than MiniMessage 5.1.1 on complex inputs with 10 placeholders (44 us/op vs 362 us/op). Allocates **~8x less memory** per operation (182 KB vs 1.5 MB).

```
Benchmark (parse + replace + build component)      Mode   Score          Units
Blixx    parseReplaceToComponent                    avgt    44.313       us/op
MiniMessage 5.1.1  miniMessageWithPlaceholders      avgt   362.002       us/op
```

## Features

- MiniMessage-compatible tag syntax (`<red>`, `<bold>`, `<gradient:red:blue>`, `<hover:show_text:'...'>`, etc.)
- Legacy `&` color code support mixed freely with tags
- Multiple placeholder formats in the same input (`{player}`, `%player%`, `<player>`)
- Placeholders indexed at parse time for fast replacement
- Immutable, mutable, and composed replacers
- Contextual placeholders that resolve from runtime objects
- Parse-time placeholders for config-driven values
- Platform modules for Adventure (Minecraft) and Hytale

## Quick Start

```java
// Use the standard instance (includes all tags, {} and <> placeholder formats)
Blixx blixx = Blixx.standard();

// Parse and replace
BlixxComponent component = blixx.parseComponent("<red>Hello <bold>{player}!");
BlixxComponent result = component.replace(List.of(
    BlixxPlaceholder.literal("player", "Steve")
));

// Convert to Adventure component
Component adventure = AdventureBlixxComponent.asComponent(result);
```

## Custom Configuration

```java
Blixx blixx = Blixx.builder()
    .withStandardParserConfig(config -> config
        .withPlaceholderFormat('%', '%')
        .withPlaceholderFormat('{', '}')
    )
    .withStandardPlaceholderConfig()
    .withPlatform(AdventureBlixx.INSTANCE)
    .build();
```

## Placeholders

Three types, all built through a fluent type-safe builder:

```java
// Literal - simple key/value
BlixxPlaceholder.literal("player", "Steve");

// Literal with lazy supplier
BlixxPlaceholder.literal("online", () -> server.getOnlineCount());

// Pattern - regex-based matching
BlixxPlaceholder.<String>builder()
    .pattern()
    .withPattern(Pattern.compile("player_stat_([a-z]+)"))
    .withMatcherSupplying(matcher -> getPlayerStat(matcher.group(1)))
    .build();

// Contextual - resolves from runtime context
BlixxPlaceholder.<String>builder()
    .contextual()
    .withExact(Player.class)
    .literal()
    .withKey("display_name")
    .withContextSupplying(player -> player.getDisplayName())
    .build();
```

## Replacers

Store and apply placeholders. Three types with distinct mutability:

```java
// Immutable - returns new instance on modification
Replacer replacer = Replacer.createImmutable()
    .withLiteral("name", "Steve")
    .withLiteral("level", 42);

// Apply to a component
BlixxComponent result = replacer.accept(component).complete();

// Mutable - modifies in place
MutableReplacer mutable = MutableReplacer.create();
mutable.withLiteral("score", 100);

// Composed - layers multiple replacers
ComposedReplacer composed = ComposedReplacer.create(globalReplacer, playerReplacer);
```

## Component Decorations

Apply style tags to placeholder regions without parsing new components:

```java
BlixxComponent template = blixx.parseComponent("{color}Title{/color} - {text}");
BlixxComponent result = Replacer.createImmutable()
    .withLiteral("color", ComponentDecoration.of("<bold><red>"))
    .withLiteral("text", "Hello")
    .accept(template)
    .complete();
```

The `{color}...{/color}` syntax applies the decoration to everything between the tags.

## Components

```java
// Parse
BlixxComponent a = blixx.parseComponent("<red>Hello");
BlixxComponent b = blixx.parseComponent("<blue>World");

// Combine
BlixxComponent joined = a.append(BlixxComponent.space(), b);

// Join a list with newlines
BlixxComponent lines = BlixxComponent.joinWithNewLine(componentList);

// Collect from a stream
BlixxComponent collected = stream.collect(
    BlixxComponent.joiningCollector(BlixxComponent.newLine(), false)
);

// Copy (independent deep copy)
BlixxComponent copy = component.copy();
```

## Tags

All standard MiniMessage tags are supported:

| Tag | Aliases | Example |
|-----|---------|---------|
| Colors | `red`, `blue`, `gold`, ... | `<red>text` |
| Hex colors | | `<#FF5555>text` or `<color:#FF5555>text` |
| Bold | `b` | `<bold>text` or `<b>text` |
| Italic | `i`, `em` | `<italic>text` |
| Underlined | `u` | `<underlined>text` |
| Strikethrough | `st` | `<strikethrough>text` |
| Obfuscated | `obf` | `<obfuscated>text` |
| Gradient | | `<gradient:red:blue>text` |
| Hover | | `<hover:show_text:'tooltip'>text` |
| Click | | `<click:run_command:'/say hi'>text` |
| Small Caps | `sc` | `<small_caps>text` |
| Reset | | `<reset>text` |

Legacy codes (`&a`, `&l`, `&o`, etc.) work alongside tags in the same string.

## Formatters

Control how non-string values are converted to text during replacement:

```java
// Built-in: Numbers strip trailing zeros, Booleans show "Enabled"/"Disabled"
BlixxPlaceholder.literal("health", 20.0);  // renders as "20"
BlixxPlaceholder.literal("active", true);  // renders as "Enabled"

// Custom formatters
BlixxFormatters formatters = BlixxFormatters.create()
    .withExact(Duration.class, d -> d.toMinutes() + "m");
```

## Platform Support

Blixx core is platform-agnostic. Platform modules convert components to native types:

```java
// Adventure (Minecraft: Paper, Velocity, etc.)
Component adventure = AdventureBlixxComponent.asComponent(blixxComponent);

// Hytale
Message hytale = HytaleBlixx.INSTANCE.build(blixxComponent);
```

## Benchmarks

Full parse + replace + component build with 10 placeholders, complex formatting, gradients, hover/click events. Benchmarked against MiniMessage 5.1.1 (JMH, Java 21, 1 fork, 10s measurement):

```
Benchmark                                    Mode   Cnt      Score         Units
Blixx    parseReplaceToComponent            thrpt           0.024        ops/us
MiniMessage  miniMessageWithPlaceholders    thrpt           0.003        ops/us

Blixx    parseReplaceToComponent             avgt          44.313         us/op
MiniMessage  miniMessageWithPlaceholders     avgt         362.002         us/op

Blixx    parseReplaceToComponent               ss        2193.292         us/op
MiniMessage  miniMessageWithPlaceholders       ss        3632.708         us/op
```

| Metric | Blixx | MiniMessage 5.1.1 |
|--------|-------|-------------------|
| Throughput | 0.024 ops/us | 0.003 ops/us |
| Avg latency | 44.3 us/op | 362.0 us/op |
| Allocation/op | 182 KB | 1.5 MB |

## Requirements

- Java 8+ (core and adventure modules)
- Java 21+ (hytale module)
