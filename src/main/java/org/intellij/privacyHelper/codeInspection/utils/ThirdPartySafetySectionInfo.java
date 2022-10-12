package org.intellij.privacyHelper.codeInspection.utils;

import org.jetbrains.annotations.NotNull;

public class ThirdPartySafetySectionInfo {
    public SafetySectionDataElement safetySectionDataElement;
    boolean isDefaultCollection;
    public String note;

    public ThirdPartySafetySectionInfo (boolean isDefaultCollection, @NotNull String note) {
        this.safetySectionDataElement = new SafetySectionDataElement();
        this.isDefaultCollection = isDefaultCollection;
        this.note = note;
    }

    public ThirdPartySafetySectionInfo (boolean isDefaultCollection, @NotNull String note,
                                        SafetySectionDataElement safetySectionDataElement) {
        this.safetySectionDataElement = safetySectionDataElement;
        this.isDefaultCollection = isDefaultCollection;
        this.note = note;
    }

    public ThirdPartySafetySectionInfo (boolean isDefaultCollection, SafetySectionDataElement safetySectionDataElement) {
        this.safetySectionDataElement = safetySectionDataElement;
        this.isDefaultCollection = isDefaultCollection;
        this.note = "";
    }
}
