# Blixx
![Maven Central](https://img.shields.io/maven-central/v/dev.oop778.blixx/blixx-core)
![Static Badge](https://img.shields.io/badge/Java_version-8-brightgreene)
![Scc Count Badge](https://sloc.xyz/github/oop-778/blixx?category=code)

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
Benchmark                                   Mode  Cnt       Score      Error  Units
MiniMessageBench.blixxNoConversion          avgt    5   27168.283 ± 1713.271  ns/op
MiniMessageBench.blixxWithConversion        avgt    5   29380.644 ±  373.988  ns/op
MiniMessageBench.miniMessageNoPlaceholders  avgt    5  342239.584 ± 9614.071  ns/op
```