package org.intellij.privacyHelper.codeInspection.severitiesProviders;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.SeveritiesProvider;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Created by tianshi on 11/28/17.
 */
public class InconsistentAnnotationSeveritiesProvider extends SeveritiesProvider {
    private static final String SeverityName = "Inconsistent";
    public static final HighlightSeverity inconsistent = new HighlightSeverity(SeverityName, HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING.myVal);

    @NotNull
    @Override
    public List<HighlightInfoType> getSeveritiesHighlightInfoTypes() {
        final TextAttributes attributes = new TextAttributes();

        attributes.setBackgroundColor(new Color(255, 150, 150));

        HighlightInfoType type = new HighlightInfoType.HighlightInfoTypeImpl(inconsistent, TextAttributesKey.createTextAttributesKey(SeverityName, attributes));
        return Collections.singletonList(type);
    }
}
