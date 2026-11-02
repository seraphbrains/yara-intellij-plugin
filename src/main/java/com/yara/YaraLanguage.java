package com.yara;

import com.intellij.lang.Language;

public class YaraLanguage extends Language {

    public static final YaraLanguage INSTANCE = new YaraLanguage();

    private YaraLanguage() {
        super("YARA");
    }
}
