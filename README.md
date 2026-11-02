# YARA Language Plugin for IntelliJ IDEA
Vibecoded® simple syntax highlighter for YARA, since some people want to earn money on stupid sh like this. Simple,stupid, ai slop. Enjoy your yara files not looking like a Graveyard anymore!

## Features

- Full syntax highlighting for YARA language constructs
- Recognition of `.yar` and `.yara` file extensions
- Support for keywords, section headers, comments, strings, regex patterns, hex strings, variables, operators, and more
- Adapts to your current editor theme (Darcula, Light, etc.)

## Installation

1. Build the plugin:
   ```bash
   ./gradlew build
   ```
2. The distributable zip is produced at `build/distributions/yara-language-plugin-1.0.0.zip`.
3. In IntelliJ IDEA, go to **Settings → Plugins → ⚙️ → Install Plugin from Disk…** and select the zip file.

## Project Structure

```
yara-language-plugin/
├── build.gradle.kts                        # Gradle build configuration
├── settings.gradle.kts                     # Project settings
├── gradle.properties                       # Build properties
├── examples/
│   └── sample.yar                          # Example YARA rule file
└── src/main/
    ├── java/com/yara/
    │   ├── YaraLanguage.java               # Language singleton
    │   ├── YaraFileType.java               # File type registration
    │   ├── YaraFile.java                   # PSI file class
    │   ├── YaraIcons.java                  # Icon loader
    │   ├── YaraParserDefinition.java       # Parser definition
    │   ├── lexer/
    │   │   ├── YaraLexer.java              # Hand-written lexer
    │   │   └── YaraTokenTypes.java         # Token type constants
    │   └── highlighting/
    │       ├── YaraSyntaxHighlighter.java  # Token-to-color mapping
    │       └── YaraSyntaxHighlighterFactory.java
    └── resources/
        ├── META-INF/plugin.xml             # Plugin manifest
        └── icons/yara.svg                  # File type icon
```

## How It Works

The plugin follows IntelliJ's standard **Custom Language Support** architecture. It has four layers that work together: **language registration**, **file type recognition**, **lexical analysis (tokenization)**, and **syntax highlighting**.

---

### Layer 1 — Language & File Type Registration

When IntelliJ starts, it reads `plugin.xml` (the manifest in `src/main/resources/META-INF/`). That file declares three extension points that wire everything together:

- **`YaraLanguage`** — A singleton (`YaraLanguage.INSTANCE`) that extends `Language` and simply registers the name `"YARA"` with the platform. This is the anchor that every other component refers to.
- **`YaraFileType`** — Extends `LanguageFileType` and tells IntelliJ: *"any file with extension `.yar` or `.yara` belongs to the YARA language."* It also supplies the display name ("YARA"), a description, and a custom SVG icon loaded by `YaraIcons`.
- **`YaraFile`** — A PSI file class (`PsiFileBase` subclass) that represents an open YARA file in memory. IntelliJ creates one of these every time you open a `.yar`/`.yara` file.

The `plugin.xml` `<fileType>` tag registers extensions `yar;yara` so IntelliJ knows to activate this plugin for those files automatically.

---

### Layer 2 — The Lexer (Tokenizer): `YaraLexer`

This is the heart of the plugin. It's a **hand-written character-by-character scanner** (extends `LexerBase`) that reads the raw text of a YARA file and splits it into **tokens** — tagged chunks of text that the highlighter can color.

When IntelliJ opens a YARA file, it calls `start(buffer, startOffset, endOffset, state)` on the lexer, handing it the file's text. Then it repeatedly calls `advance()`, and each call produces the next token by setting three fields: `tokenStart`, `tokenEnd`, and `tokenType`.

The `advance()` method works like a big decision tree — it looks at the current character and decides what to do:

| Current character | What it does | Token type produced |
|---|---|---|
| Whitespace | Consumes all consecutive whitespace | `WHITE_SPACE` |
| `//` | Reads to end of line | `LINE_COMMENT` |
| `/*` | Reads until `*/` | `BLOCK_COMMENT` |
| `/` after `=`, `(`, `,`, `~` | Reads a regex pattern like `/foo/i` | `REGEX_LITERAL` |
| `"` | Reads a double-quoted string (handling `\"` escapes) | `STRING_LITERAL` |
| `$` | Reads a variable name like `$my_string` | `VARIABLE` |
| `#` + letter | Reads a count reference like `#my_string` | `VARIABLE_COUNT` |
| `@` + letter | Reads an offset reference like `@my_string` | `VARIABLE_OFFSET` |
| `!` + letter | Reads a length reference like `!my_string` | `VARIABLE_LENGTH` |
| `0x` or `0X` | Reads a hex number like `0xFF` | `NUMBER` |
| Digit | Reads a decimal number, including suffixes like `KB`, `MB` | `NUMBER` |
| Letter or `_` | Reads a word, then checks if it's a keyword, section keyword, boolean, or plain identifier | `KEYWORD`, `SECTION_KEYWORD`, `BOOLEAN_LITERAL`, or `IDENTIFIER` |
| `{`, `}`, `(`, `)`, etc. | Single-character punctuation | `LBRACE`, `RBRACE`, `LPAREN`, etc. |
| `=`, `<`, `>`, `!`, `+`, `-`, etc. | Reads operators (including two-char ones like `==`, `<<`) | `OPERATOR` |
| Anything else | Unrecognized character | `BAD_CHARACTER` |

A clever detail: for **section keywords** (`meta`, `strings`, `condition`), the lexer peeks ahead past any spaces to see if a `:` follows. Only `meta:`, `strings:`, `condition:` are tagged as `SECTION_KEYWORD` — the same words used elsewhere are treated as regular keywords or identifiers. This prevents false highlighting.

For **regex vs. division**, the `isRegexContext()` method looks backward to see if the `/` is preceded by `=`, `(`, `,`, or `~`. If so, it's a regex literal; otherwise it's treated as the division operator.

All token types are defined as `IElementType` constants in `YaraTokenTypes`.

---

### Layer 3 — The Parser Definition: `YaraParserDefinition`

IntelliJ requires a `ParserDefinition` even for syntax-highlighting-only plugins. `YaraParserDefinition` provides:

- **`createLexer()`** — returns a new `YaraLexer` instance.
- **`createParser()`** — returns a *pass-through parser* that simply consumes every token the lexer produces without building any structure. This is the minimal parser needed — it just wraps everything in a single root node. (A full parser with AST structure would be needed for features like code completion or refactoring, but not for highlighting.)
- **`getCommentTokens()`** / **`getStringLiteralElements()`** / **`getWhitespaceTokens()`** — tell IntelliJ which token types are comments, strings, and whitespace, so features like "find usages" and brace matching know to skip them.
- **`createFile()`** — returns a `YaraFile` PSI node.

---

### Layer 4 — The Syntax Highlighter: `YaraSyntaxHighlighter` + `YaraSyntaxHighlighterFactory`

This is where tokens get **colors**.

`YaraSyntaxHighlighterFactory` is registered in `plugin.xml` against the `"YARA"` language. When IntelliJ needs to highlight a YARA file, it calls `getSyntaxHighlighter()` which returns a `YaraSyntaxHighlighter`.

`YaraSyntaxHighlighter` does two things:

1. **`getHighlightingLexer()`** — returns a `YaraLexer`. IntelliJ runs this lexer over the file text to produce the stream of tokens.

2. **`getTokenHighlights(tokenType)`** — for each token type, returns the **`TextAttributesKey`** that defines its color. This mapping is stored in a static `HashMap`:

| Token type | Maps to (IntelliJ default color scheme) |
|---|---|
| `KEYWORD`, `SECTION_KEYWORD`, `BOOLEAN_LITERAL` | **Keyword** color (usually bold blue/orange) |
| `LINE_COMMENT`, `BLOCK_COMMENT` | **Comment** color (usually gray/green) |
| `STRING_LITERAL`, `REGEX_LITERAL`, `HEX_STRING` | **String** color (usually green) |
| `NUMBER` | **Number** color |
| `VARIABLE`, `VARIABLE_COUNT`, `VARIABLE_OFFSET`, `VARIABLE_LENGTH` | **Local variable** color |
| `IDENTIFIER` | **Identifier** color |
| `OPERATOR`, `COLON` | **Operation sign** color |
| `LBRACE`/`RBRACE`, `LPAREN`/`RPAREN`, `LBRACKET`/`RBRACKET` | **Braces/Parens/Brackets** color |
| `BAD_CHARACTER` | **Bad character** (usually red squiggly) |

The `TextAttributesKey` entries use `createTextAttributesKey("YARA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)` — this means they inherit the user's current theme colors. If you use Darcula, you get Darcula colors; if you use the light theme, you get light-theme colors. No custom colors are hardcoded.

---

### The Full Flow — What Happens When You Open a `.yar` File

1. You open `sample.yar` in IntelliJ.
2. IntelliJ sees the `.yar` extension → matches it to `YaraFileType` → knows it's YARA language.
3. It creates a `YaraFile` PSI node via `YaraParserDefinition.createFile()`.
4. For highlighting, it asks `YaraSyntaxHighlighterFactory` for a highlighter → gets `YaraSyntaxHighlighter`.
5. The highlighter creates a `YaraLexer` and feeds the file's text into it.
6. The lexer scans character-by-character, producing tokens: `KEYWORD("rule")`, `WHITE_SPACE(" ")`, `IDENTIFIER("MyRule")`, `LBRACE("{")`, `SECTION_KEYWORD("strings")`, `COLON(":")`, `VARIABLE("$a")`, `OPERATOR("=")`, `STRING_LITERAL("\"hello\"")`, …
7. For each token, `getTokenHighlights()` returns the color key → IntelliJ paints the editor with those colors.

That's the entire pipeline. It's a clean, minimal architecture — the lexer does the heavy lifting of understanding YARA syntax, and the highlighter just maps token types to colors.

## Requirements

- IntelliJ IDEA 2024.1 or later
- Java 17+

## License

This project is provided as-is for educational and practical use.
