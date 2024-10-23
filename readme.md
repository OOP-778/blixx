# Blixx
![Maven Central](https://img.shields.io/maven-central/v/dev.oop778.blixx/blixx-core)
![Static Badge](https://img.shields.io/badge/Java_version-8-brightgreene)
![Scc Count Badge](https://sloc.xyz/github/oop-778/blixx?category=code)

## Note
Currently Blixx is still in development...

## Why Blixx?

Adventure's MiniMessage has its limitations and flaws. **Blixx** is built to be a blazing-fast, more feature-packed alternative that solves these issues.

Here’s why you should use **Blixx**:

- **Lazy Parsing**: Improves performance by parsing components that do not have placeholders at initialization
- **Mixed Color Format Support**: Blixx handles both legacy color codes and MiniMessage formats within the same component.
- **Preserves Original Input**: Keeps the original input intact, so you can serialize back into configs without any applied tags.
- **Advanced Placeholder & Replacement System**: Blixx offers a highly flexible and robust system for placeholders, making complex replacements easy.
- **Placeholder Indexing for Speed**: Placeholders are indexed at initialization, allowing rapid replacement throughout the component structure.
- **Flexible Decoration Replacement**: Supports replacing decorations both during initialization and at build time for dynamic customization.

## Benchmark against MiniMessage
noConversion - means it parses into our tree  
withConversion - means it converts to adventure post parsing  
```text
Benchmark                                   Mode  Cnt       Score   Error  Units
MiniMessageBench.blixxNoConversion          avgt        20026.217          ns/op
MiniMessageBench.blixxWithConversion        avgt        21302.531          ns/op
MiniMessageBench.miniMessageNoPlaceholders  avgt       323573.625          ns/op
```

## Placeholders
Blixx offers highly robust & flexible placeholder system that allows you to optimize your replacements with ease with caching.
Here's an overview of the fluent placeholder builder

Here's an example of parse time placeholder that allows you to create project wide color scheme.
```java
BlixxPlaceholder.<String>builder()
                                .contextual()
                                .withExact(ColorScheme.class)
                                .pattern()
                                .withPattern(Pattern.compile("\\{([a-z]+)_color(?:_([1-9]))?}"))
                                .withContextAndMatcherSupplying((context, matcher) -> {
                                    // main color
                                    if (matcher.group(2) == null) {
                                        return context.getHexColor(matcher.group(1), 0);
                                    }

                                    // Shaded color
                                    return context.getHexColor(matcher.group(1), Integer.parseInt(matcher.group(2)));
                                })
                                .build()
```
```aiignore
{primary_color}Prefix> {secondary_color}<italic>Hello {secondary_color_5}World
```