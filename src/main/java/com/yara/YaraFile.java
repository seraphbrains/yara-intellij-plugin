package com.yara;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class YaraFile extends PsiFileBase {

    public YaraFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, YaraLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return YaraFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "YARA File";
    }
}
