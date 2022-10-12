package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 1/18/18.
 */
public class NavigateToCodeQuickfix implements LocalQuickFix {
    private static final String JUMP_TO_CODE_QUICKFIX_NAME_PATTERN = "Navigate to %s";
    private final String JUMP_TO_CODE_QUICKFIX_NAME;
    private SmartPsiElementPointer<PsiElement> targetElementPointer;

    public NavigateToCodeQuickfix(SmartPsiElementPointer<PsiElement> targetElementPointer, String targetDescription) {
        this.targetElementPointer = targetElementPointer;
        JUMP_TO_CODE_QUICKFIX_NAME = String.format(JUMP_TO_CODE_QUICKFIX_NAME_PATTERN, targetDescription);
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return JUMP_TO_CODE_QUICKFIX_NAME;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Coconut quick-fixes";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        CoconutUIUtil.navigateMainEditorToPsiElement(targetElementPointer);
    }
}
