package com.yara.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class YaraLexer extends LexerBase {

    private static final Set<String> KEYWORDS = Set.of(
            "rule", "import", "include", "private", "global",
            "and", "or", "not",
            "any", "all", "none", "of", "them", "at", "in", "for",
            "entrypoint", "filesize",
            "fullword", "nocase", "wide", "ascii",
            "contains", "matches", "icontains", "iequals",
            "startswith", "istartswith", "endswith", "iendswith",
            "defined", "xor", "base64", "base64wide"
    );

    private static final Set<String> SECTION_KEYWORDS = Set.of(
            "meta", "strings", "condition"
    );

    private static final Set<String> BOOLEAN_LITERALS = Set.of("true", "false");

    private CharSequence buffer;
    private int bufferEnd;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.tokenType = null;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;

        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }

        char c = buffer.charAt(tokenStart);

        if (Character.isWhitespace(c)) {
            tokenType = YaraTokenTypes.WHITE_SPACE;
            tokenEnd = tokenStart + 1;
            while (tokenEnd < bufferEnd && Character.isWhitespace(buffer.charAt(tokenEnd))) {
                tokenEnd++;
            }
            return;
        }

        if (c == '/' && tokenStart + 1 < bufferEnd) {
            char next = buffer.charAt(tokenStart + 1);
            if (next == '/') {
                readLineComment();
                return;
            }
            if (next == '*') {
                readBlockComment();
                return;
            }
            if (isRegexContext()) {
                readRegex();
                return;
            }
        }

        if (c == '"') {
            readStringLiteral();
            return;
        }

        if (c == '$') {
            readVariable(YaraTokenTypes.VARIABLE);
            return;
        }

        if (c == '#') {
            if (tokenStart + 1 < bufferEnd && isIdentifierPart(buffer.charAt(tokenStart + 1))) {
                readVariable(YaraTokenTypes.VARIABLE_COUNT);
                return;
            }
        }

        if (c == '@') {
            if (tokenStart + 1 < bufferEnd && isIdentifierPart(buffer.charAt(tokenStart + 1))) {
                readVariable(YaraTokenTypes.VARIABLE_OFFSET);
                return;
            }
        }

        if (c == '!') {
            if (tokenStart + 1 < bufferEnd && isIdentifierPart(buffer.charAt(tokenStart + 1))) {
                readVariable(YaraTokenTypes.VARIABLE_LENGTH);
                return;
            }
        }

        if (c == '0' && tokenStart + 1 < bufferEnd
                && (buffer.charAt(tokenStart + 1) == 'x' || buffer.charAt(tokenStart + 1) == 'X')) {
            readHexNumber();
            return;
        }

        if (Character.isDigit(c)) {
            readDecimalNumber();
            return;
        }

        if (Character.isLetter(c) || c == '_') {
            readIdentifierOrKeyword();
            return;
        }

        if (c == '{') {
            tokenType = YaraTokenTypes.LBRACE;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == '}') {
            tokenType = YaraTokenTypes.RBRACE;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == '(') {
            tokenType = YaraTokenTypes.LPAREN;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == ')') {
            tokenType = YaraTokenTypes.RPAREN;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == '[') {
            tokenType = YaraTokenTypes.LBRACKET;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == ']') {
            tokenType = YaraTokenTypes.RBRACKET;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == ':') {
            tokenType = YaraTokenTypes.COLON;
            tokenEnd = tokenStart + 1;
            return;
        }
        if (c == '.') {
            if (tokenStart + 1 < bufferEnd && buffer.charAt(tokenStart + 1) == '.') {
                tokenType = YaraTokenTypes.OPERATOR;
                tokenEnd = tokenStart + 2;
            } else {
                tokenType = YaraTokenTypes.DOT;
                tokenEnd = tokenStart + 1;
            }
            return;
        }
        if (c == ',') {
            tokenType = YaraTokenTypes.COMMA;
            tokenEnd = tokenStart + 1;
            return;
        }

        if (isOperatorChar(c)) {
            readOperator();
            return;
        }

        tokenType = YaraTokenTypes.BAD_CHARACTER;
        tokenEnd = tokenStart + 1;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    private void readLineComment() {
        tokenType = YaraTokenTypes.LINE_COMMENT;
        tokenEnd = tokenStart + 2;
        while (tokenEnd < bufferEnd && buffer.charAt(tokenEnd) != '\n') {
            tokenEnd++;
        }
    }

    private void readBlockComment() {
        tokenType = YaraTokenTypes.BLOCK_COMMENT;
        tokenEnd = tokenStart + 2;
        while (tokenEnd < bufferEnd) {
            if (buffer.charAt(tokenEnd) == '*' && tokenEnd + 1 < bufferEnd && buffer.charAt(tokenEnd + 1) == '/') {
                tokenEnd += 2;
                return;
            }
            tokenEnd++;
        }
    }

    private void readStringLiteral() {
        tokenType = YaraTokenTypes.STRING_LITERAL;
        tokenEnd = tokenStart + 1;
        while (tokenEnd < bufferEnd) {
            char c = buffer.charAt(tokenEnd);
            if (c == '\\' && tokenEnd + 1 < bufferEnd) {
                tokenEnd += 2;
                continue;
            }
            if (c == '"') {
                tokenEnd++;
                return;
            }
            if (c == '\n') {
                return;
            }
            tokenEnd++;
        }
    }

    private void readRegex() {
        tokenType = YaraTokenTypes.REGEX_LITERAL;
        tokenEnd = tokenStart + 1;
        while (tokenEnd < bufferEnd) {
            char c = buffer.charAt(tokenEnd);
            if (c == '\\' && tokenEnd + 1 < bufferEnd) {
                tokenEnd += 2;
                continue;
            }
            if (c == '/') {
                tokenEnd++;
                while (tokenEnd < bufferEnd && Character.isLetter(buffer.charAt(tokenEnd))) {
                    tokenEnd++;
                }
                return;
            }
            if (c == '\n') {
                return;
            }
            tokenEnd++;
        }
    }

    private void readVariable(IElementType type) {
        tokenType = type;
        tokenEnd = tokenStart + 1;
        if (tokenEnd < bufferEnd && buffer.charAt(tokenEnd) == '*') {
            tokenEnd++;
            return;
        }
        while (tokenEnd < bufferEnd && isIdentifierPart(buffer.charAt(tokenEnd))) {
            tokenEnd++;
        }
    }

    private void readHexNumber() {
        tokenType = YaraTokenTypes.NUMBER;
        tokenEnd = tokenStart + 2;
        while (tokenEnd < bufferEnd && isHexDigit(buffer.charAt(tokenEnd))) {
            tokenEnd++;
        }
    }

    private void readDecimalNumber() {
        tokenType = YaraTokenTypes.NUMBER;
        tokenEnd = tokenStart + 1;
        while (tokenEnd < bufferEnd && Character.isDigit(buffer.charAt(tokenEnd))) {
            tokenEnd++;
        }
        if (tokenEnd < bufferEnd) {
            char c = buffer.charAt(tokenEnd);
            if (c == 'K' || c == 'M') {
                tokenEnd++;
            } else if (c == 'B' || c == 'b') {
                tokenEnd++;
            }
        }
    }

    private void readIdentifierOrKeyword() {
        tokenEnd = tokenStart + 1;
        while (tokenEnd < bufferEnd && isIdentifierPart(buffer.charAt(tokenEnd))) {
            tokenEnd++;
        }
        String word = buffer.subSequence(tokenStart, tokenEnd).toString();

        if (BOOLEAN_LITERALS.contains(word)) {
            tokenType = YaraTokenTypes.BOOLEAN_LITERAL;
        } else if (SECTION_KEYWORDS.contains(word)) {
            int lookAhead = tokenEnd;
            while (lookAhead < bufferEnd && buffer.charAt(lookAhead) == ' ') {
                lookAhead++;
            }
            if (lookAhead < bufferEnd && buffer.charAt(lookAhead) == ':') {
                tokenType = YaraTokenTypes.SECTION_KEYWORD;
            } else {
                tokenType = KEYWORDS.contains(word) ? YaraTokenTypes.KEYWORD : YaraTokenTypes.IDENTIFIER;
            }
        } else if (KEYWORDS.contains(word)) {
            tokenType = YaraTokenTypes.KEYWORD;
        } else {
            tokenType = YaraTokenTypes.IDENTIFIER;
        }
    }

    private void readOperator() {
        tokenType = YaraTokenTypes.OPERATOR;
        char c = buffer.charAt(tokenStart);
        tokenEnd = tokenStart + 1;

        if (tokenEnd < bufferEnd) {
            char next = buffer.charAt(tokenEnd);
            if ((c == '=' || c == '!' || c == '<' || c == '>') && next == '=') {
                tokenEnd++;
            } else if (c == '<' && next == '<') {
                tokenEnd++;
            } else if (c == '>' && next == '>') {
                tokenEnd++;
            }
        }
    }

    private boolean isRegexContext() {
        int i = tokenStart - 1;
        while (i >= 0 && Character.isWhitespace(buffer.charAt(i))) {
            i--;
        }
        if (i >= 0) {
            char prev = buffer.charAt(i);
            return prev == '=' || prev == '(' || prev == ',' || prev == '~';
        }
        return false;
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isOperatorChar(char c) {
        return c == '=' || c == '!' || c == '<' || c == '>' || c == '+' || c == '-'
                || c == '*' || c == '\\' || c == '%' || c == '&' || c == '|'
                || c == '^' || c == '~' || c == '/';
    }
}
