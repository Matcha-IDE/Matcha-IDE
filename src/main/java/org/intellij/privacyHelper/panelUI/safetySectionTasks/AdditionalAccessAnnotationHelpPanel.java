package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

public class AdditionalAccessAnnotationHelpPanel extends HelpMessagePanel {

    static String html = null;

    @Override
    public void loadHelpMessageHtml() {
        if (html == null) {
            try {
                URL htmlUrl = HelpMessagePanel.class.getResource("/matchaUseGuide/Matcha-help-additional-source-annotation.html");
                if (htmlUrl != null) {
                    html = ResourceUtil.loadText(htmlUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected @NotNull
    @Nls String getBody() {
        loadHelpMessageHtml();
        return html == null ? "" : html;
    }
}
