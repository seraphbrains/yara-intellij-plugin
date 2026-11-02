package com.yara;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.lang.PsiBuilder;
import com.yara.lexer.YaraLexer;
import com.yara.lexer.YaraTokenTypes;
import org.jetbrains.annotations.NotNull;

public class YaraParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IStubFileElementType<>(YaraLanguage.INSTANCE);

    private static final TokenSet COMMENT_TOKENS = TokenSet.create(
            YaraTokenTypes.LINE_COMMENT,
            YaraTokenTypes.BLOCK_COMMENT
    );

    private static final TokenSet STRING_LITERAL_TOKENS = TokenSet.create(
            YaraTokenTypes.STRING_LITERAL
    );

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new YaraLexer();
    }

    @NotNull
    @Override
    public PsiParser createParser(Project project) {
        return (root, builder) -> {
            PsiBuilder.Marker marker = builder.mark();
            while (!builder.eof()) {
                builder.advanceLexer();
            }
            marker.done(root);
            return builder.getTreeBuilt();
        };
    }

    @NotNull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENT_TOKENS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRING_LITERAL_TOKENS;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(com.intellij.psi.TokenType.WHITE_SPACE);
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return new LeafPsiElement(node.getElementType(), node.getText());
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new YaraFile(viewProvider);
    }
}
