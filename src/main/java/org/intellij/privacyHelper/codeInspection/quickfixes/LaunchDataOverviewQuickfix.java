package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 5/4/17.
 */
public class LaunchDataOverviewQuickfix implements LocalQuickFix {
    private String LAUNCH_DATA_OVERVIEW_QUICKFIX = "Open the personal data usage overview";

    @Nls
    @NotNull
    @Override
    public String getName() {
        return LAUNCH_DATA_OVERVIEW_QUICKFIX;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return LAUNCH_DATA_OVERVIEW_QUICKFIX;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        // The ToolWindow ID is "PrivacyChecker"
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("PrivacyChecker");
        window.activate(null);
    }
}
