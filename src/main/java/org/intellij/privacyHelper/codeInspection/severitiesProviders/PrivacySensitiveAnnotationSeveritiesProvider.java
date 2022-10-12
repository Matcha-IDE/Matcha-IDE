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
 * Created by tianshi on 11/20/17.
 */
public class PrivacySensitiveAnnotationSeveritiesProvider extends SeveritiesProvider {
    private static final String SeverityName = "PrivacySensitive";
    public static final HighlightSeverity privacySensitive = new HighlightSeverity(SeverityName, HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING.myVal);

    @NotNull
    @Override
    public List<HighlightInfoType> getSeveritiesHighlightInfoTypes() {
        final TextAttributes attributes = new TextAttributes();

        attributes.setBackgroundColor(new Color(240, 200, 255));

        HighlightInfoType type = new HighlightInfoType.HighlightInfoTypeImpl(privacySensitive, TextAttributesKey.createTextAttributesKey(SeverityName, attributes));
        return Collections.singletonList(type);
    }
}
