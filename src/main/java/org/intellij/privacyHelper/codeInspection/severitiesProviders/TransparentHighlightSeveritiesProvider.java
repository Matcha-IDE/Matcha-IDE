package org.intellij.privacyHelper.codeInspection.severitiesProviders;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.SeveritiesProvider;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Created by tianshi on 1/17/18.
 */
public class TransparentHighlightSeveritiesProvider extends SeveritiesProvider {
    private static final String SeverityName = "Transparent";
    public static final HighlightSeverity transparent = new HighlightSeverity(SeverityName, HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING.myVal);

    @NotNull
    @Override
    public List<HighlightInfoType> getSeveritiesHighlightInfoTypes() {
        final TextAttributes attributes = new TextAttributes();

        HighlightInfoType type = new HighlightInfoType.HighlightInfoTypeImpl(transparent, TextAttributesKey.createTextAttributesKey(SeverityName, attributes));
        return Collections.singletonList(type);
    }
}
