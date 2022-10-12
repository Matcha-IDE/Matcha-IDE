package org.intellij.privacyHelper.codeInspection.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import org.jetbrains.annotations.NotNull;

public class TooltipInfo {
    String id;
    String header;
    String text;
    Project project;
    Balloon.Position position;
    @NotNull Object target;

    public TooltipInfo(String id, String header, String text, Balloon.Position position,
                       Project project, @NotNull Object target) {
        this.id = id;
        this.header = header;
        this.text = text;
        this.position = position;
        this.project = project;
        this.target = target;
    }
}
