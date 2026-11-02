package com.yara.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.yara.lexer.YaraLexer;
import com.yara.lexer.YaraTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class YaraSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("YARA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey SECTION_KEYWORD =
            createTextAttributesKey("YARA_SECTION_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BOOLEAN =
            createTextAttributesKey("YARA_BOOLEAN", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("YARA_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("YARA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("YARA_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey REGEX =
            createTextAttributesKey("YARA_REGEX", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey HEX_STRING =
            createTextAttributesKey("YARA_HEX_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("YARA_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("YARA_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("YARA_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("YARA_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("YARA_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("YARA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("YARA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("YARA_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey DOT =
            createTextAttributesKey("YARA_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey COLON =
            createTextAttributesKey("YARA_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("YARA_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    private static final Map<IElementType, TextAttributesKey[]> ATTRIBUTES = new HashMap<>();

    static {
        ATTRIBUTES.put(YaraTokenTypes.KEYWORD, pack(KEYWORD));
        ATTRIBUTES.put(YaraTokenTypes.SECTION_KEYWORD, pack(SECTION_KEYWORD));
        ATTRIBUTES.put(YaraTokenTypes.BOOLEAN_LITERAL, pack(BOOLEAN));
        ATTRIBUTES.put(YaraTokenTypes.LINE_COMMENT, pack(LINE_COMMENT));
        ATTRIBUTES.put(YaraTokenTypes.BLOCK_COMMENT, pack(BLOCK_COMMENT));
        ATTRIBUTES.put(YaraTokenTypes.STRING_LITERAL, pack(STRING));
        ATTRIBUTES.put(YaraTokenTypes.REGEX_LITERAL, pack(REGEX));
        ATTRIBUTES.put(YaraTokenTypes.HEX_STRING, pack(HEX_STRING));
        ATTRIBUTES.put(YaraTokenTypes.NUMBER, pack(NUMBER));
        ATTRIBUTES.put(YaraTokenTypes.VARIABLE, pack(VARIABLE));
        ATTRIBUTES.put(YaraTokenTypes.VARIABLE_COUNT, pack(VARIABLE));
        ATTRIBUTES.put(YaraTokenTypes.VARIABLE_OFFSET, pack(VARIABLE));
        ATTRIBUTES.put(YaraTokenTypes.VARIABLE_LENGTH, pack(VARIABLE));
        ATTRIBUTES.put(YaraTokenTypes.IDENTIFIER, pack(IDENTIFIER));
        ATTRIBUTES.put(YaraTokenTypes.OPERATOR, pack(OPERATOR));
        ATTRIBUTES.put(YaraTokenTypes.LBRACE, pack(BRACES));
        ATTRIBUTES.put(YaraTokenTypes.RBRACE, pack(BRACES));
        ATTRIBUTES.put(YaraTokenTypes.LPAREN, pack(PARENTHESES));
        ATTRIBUTES.put(YaraTokenTypes.RPAREN, pack(PARENTHESES));
        ATTRIBUTES.put(YaraTokenTypes.LBRACKET, pack(BRACKETS));
        ATTRIBUTES.put(YaraTokenTypes.RBRACKET, pack(BRACKETS));
        ATTRIBUTES.put(YaraTokenTypes.COLON, pack(COLON));
        ATTRIBUTES.put(YaraTokenTypes.DOT, pack(DOT));
        ATTRIBUTES.put(YaraTokenTypes.COMMA, pack(COMMA));
        ATTRIBUTES.put(YaraTokenTypes.BAD_CHARACTER, pack(BAD_CHARACTER));
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new YaraLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] keys = ATTRIBUTES.get(tokenType);
        return keys != null ? keys : TextAttributesKey.EMPTY_ARRAY;
    }

    public static TextAttributesKey[] pack(TextAttributesKey key) {
        return new TextAttributesKey[]{key};
    }
}
