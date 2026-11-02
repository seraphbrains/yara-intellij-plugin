package com.yara;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class YaraFileType extends LanguageFileType {

    public static final YaraFileType INSTANCE = new YaraFileType();

    private YaraFileType() {
        super(YaraLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "YARA";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "YARA rule file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "yar";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return YaraIcons.FILE;
    }
}
