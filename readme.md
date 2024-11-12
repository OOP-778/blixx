# Blixx
![Maven Central](https://img.shields.io/maven-central/v/dev.oop778.blixx/blixx-core)
![Java Version](https://img.shields.io/badge/Java-8-brightgreen)
![Code Count Badge](https://sloc.xyz/github/oop-778/blixx?category=code)

**Blixx** is a blazing-fast, feature-rich text parsing and replacement library for Minecraft, designed as a robust alternative to Adventure's MiniMessage with added flexibility, performance, and customization options.

---

## ⚠️ Status: Under Development

Blixx is actively being developed. Upcoming features:
- Placeholder caching
- Quality improvements (e.g., moving implementation classes out of API)
- Add documentation to all API classes
- Initial release

---

## 🌟 Why Choose Blixx?

While MiniMessage is a great start, Blixx addresses its limitations by providing:

- **Lazy Parsing**: Parse only when necessary, skipping components without placeholders.
- **Mixed Color Formats**: Support for both legacy Minecraft color codes and MiniMessage formatting in one component.
- **Original Input Preservation**: Retain the unprocessed input for easy re-serialization back into configs.
- **Advanced Placeholder System**: A flexible, customizable approach to placeholders and replacements, optimized for complex text handling.
- **Efficient Placeholder Indexing**: Index placeholders on initialization for high-speed replacements.
- **Flexible Tags Handling**: Manage and replace tags at both initialization and runtime.

---

## 📊 Benchmark Comparison

The following benchmarks show Blixx's performance relative to MiniMessage:

```plaintext
Benchmark                                                    Mode  Cnt      Score   Error   Units
MiniMessageBench.blixxWithKeysParse                         thrpt           0.027          ops/us
MiniMessageBench.blixxWithKeysReplaceAndToComponent         thrpt           0.018          ops/us
MiniMessageBench.blixxWithMemoryIndexParse                  thrpt           0.026          ops/us
MiniMessageBench.blixxWithMemoryIndexReplaceAndToComponent  thrpt           0.017          ops/us
MiniMessageBench.miniMessageWithPlaceholders                thrpt           0.001          ops/us
MiniMessageBench.blixxWithKeysParse                          avgt          33.391           us/op
MiniMessageBench.blixxWithKeysReplaceAndToComponent          avgt          52.453           us/op
MiniMessageBench.blixxWithMemoryIndexParse                   avgt          37.271           us/op
MiniMessageBench.blixxWithMemoryIndexReplaceAndToComponent   avgt          60.291           us/op
MiniMessageBench.miniMessageWithPlaceholders                 avgt         966.316           us/op
```

---
## 🛠️ Getting Started with Blixx

Blixx offers a standard constant configuration with commonly used tags and placeholder formats (like < and >). Access it with:
```java
Blixx.standard();
```

### Customizing Blixx
For advanced use, build your own instance of Blixx to include custom placeholders or add time-sensitive placeholders like color schemes:
```java
Blixx.builder()
    .withStandardParserConfig(config -> config
            .withTags(BlixxTags.STANDARD)
            .withPlaceholderFormat('%', '%')
            .withPlaceholderFormat('{', '}')
            .withPlaceholderFormat('<', '>')
            .useKeyBasedPlaceholderIndexing()
            .withParsePlaceholder(BlixxPlaceholder.<String>builder()
                    .contextual()
                    .withExact(ColorScheme.class)
                    .pattern()
                    .withPattern(Pattern.compile("\\{([a-z]+)_color(?:_([1-9]))?}"))
                    .withContextAndMatcherSupplying((context, matcher) -> {
                        if (matcher.group(2) == null) {
                            return "<" + context.getHexColor(matcher.group(1), 0) + ">";
                        }
                        return "<" + context.getHexColor(matcher.group(1), Integer.parseInt(matcher.group(2))) + ">";
                    })
                    .build()))
    .withStandardPlaceholderConfig(config -> config.withDefaultContext(defaultContext ->
            defaultContext.withDefaultInheritanceContext(new ColorSchemeImpl())))
    .build();
```

> [!NOTE]
> Parse-time placeholders only support string-based input.

### 📝 Parsing Example
Blixx uses MiniMessage-compatible syntax for text parsing. Example usage:
```java
blixx.parseComponent("{primary_color}Hello {secondary_color_5}World");
```

> [!NOTE]
> For format reference have a look at [MiniMessage format docs](https://docs.advntr.dev/minimessage/format.html)

---

## 🧩 Components
Blixx components store data in nodes for efficient parsing and traversal. Direct manipulation of components is limited to maintain Blixx’s parsing integrity, though creation of components without parser is in development.

---

## 🔄 Placeholders
Blixx's placeholder system is flexible, supporting caching for efficient replacements.
```java
// Create a literal placeholder
BlixxPlaceholder.literal("hello", 1);

// Create a contextual placeholder for player display names
BlixxPlaceholder.builder()
                .contextual()
                .withExact(Audience.class)
                .literal()
                .withKey("display_name")
                .withContextSupplying(audience -> audience.get(Identity.DISPLAY_NAME).get())
                .build();
```
> [!IMPORTANT]
> BlixxPlaceholderBuilder is fluent & type safe, so no matter how hard you try you can't make a mistake whilst building it :P

---

## 🔧 Replacers
Replacers are integral part of Blixx ecosystem, they allow you to store your placeholders.  
Blixx offers three different Replacers, all of them implement internal interface called ``PlacholderHolder`` && public ``ReplaceActionCaller`` which allows them to share similar methods, but quite different inner workings.

- **Replacer (Immutable)**: A default, immutable replacer. Use `Replacer.create()` to initialize.
- **MutableReplacer (Mutable)**: A replacer that allows modifications. Initialize it with `MutableReplacer.create()`.
- **ComposedReplacer (Immutable)**: Holds references to multiple replacers, enabling layered replacements. Create it with `ComposedReplacer.create(Replacers...)`.

> [!NOTE]
> Immutable and mutable replacers are purposefully distinct to prevent unintended modifications. For example, an instance of `Replacer` (immutable) cannot be reassigned as a `MutableReplacer`, ensuring replacers retain their intended mutability properties.

---
With its unique features and flexible API, Blixx is built for those who need a highly customizable, high-performance text parsing and replacement solution for Minecraft and similar platforms. Enjoy building with Blixx!