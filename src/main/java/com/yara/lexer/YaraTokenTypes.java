package com.yara.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.yara.YaraLanguage;

public interface YaraTokenTypes {

    IElementType LINE_COMMENT = new IElementType("LINE_COMMENT", YaraLanguage.INSTANCE);
    IElementType BLOCK_COMMENT = new IElementType("BLOCK_COMMENT", YaraLanguage.INSTANCE);

    IElementType KEYWORD = new IElementType("KEYWORD", YaraLanguage.INSTANCE);
    IElementType SECTION_KEYWORD = new IElementType("SECTION_KEYWORD", YaraLanguage.INSTANCE);
    IElementType BOOLEAN_LITERAL = new IElementType("BOOLEAN_LITERAL", YaraLanguage.INSTANCE);

    IElementType STRING_LITERAL = new IElementType("STRING_LITERAL", YaraLanguage.INSTANCE);
    IElementType REGEX_LITERAL = new IElementType("REGEX_LITERAL", YaraLanguage.INSTANCE);
    IElementType HEX_STRING = new IElementType("HEX_STRING", YaraLanguage.INSTANCE);

    IElementType NUMBER = new IElementType("NUMBER", YaraLanguage.INSTANCE);

    IElementType VARIABLE = new IElementType("VARIABLE", YaraLanguage.INSTANCE);
    IElementType VARIABLE_COUNT = new IElementType("VARIABLE_COUNT", YaraLanguage.INSTANCE);
    IElementType VARIABLE_OFFSET = new IElementType("VARIABLE_OFFSET", YaraLanguage.INSTANCE);
    IElementType VARIABLE_LENGTH = new IElementType("VARIABLE_LENGTH", YaraLanguage.INSTANCE);

    IElementType IDENTIFIER = new IElementType("IDENTIFIER", YaraLanguage.INSTANCE);

    IElementType OPERATOR = new IElementType("OPERATOR", YaraLanguage.INSTANCE);

    IElementType LBRACE = new IElementType("LBRACE", YaraLanguage.INSTANCE);
    IElementType RBRACE = new IElementType("RBRACE", YaraLanguage.INSTANCE);
    IElementType LPAREN = new IElementType("LPAREN", YaraLanguage.INSTANCE);
    IElementType RPAREN = new IElementType("RPAREN", YaraLanguage.INSTANCE);
    IElementType LBRACKET = new IElementType("LBRACKET", YaraLanguage.INSTANCE);
    IElementType RBRACKET = new IElementType("RBRACKET", YaraLanguage.INSTANCE);
    IElementType COLON = new IElementType("COLON", YaraLanguage.INSTANCE);
    IElementType DOT = new IElementType("DOT", YaraLanguage.INSTANCE);
    IElementType COMMA = new IElementType("COMMA", YaraLanguage.INSTANCE);

    IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
}
