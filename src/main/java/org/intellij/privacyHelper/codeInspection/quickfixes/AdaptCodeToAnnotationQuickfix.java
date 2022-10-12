package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 3/11/18.
 */
public class AdaptCodeToAnnotationQuickfix implements LocalQuickFix {
    private final String adaptCodeToAnnotationQuickfixName;
    private SmartPsiElementPointer methodCallExpressionPointer;
    ChangeCodeFunction changeCodeFunction;


    public AdaptCodeToAnnotationQuickfix(String quickfixName,
                                           PsiMethodCallExpression methodCallExpression,
                                           ChangeCodeFunction changeCodeFunction) {
        this.adaptCodeToAnnotationQuickfixName = quickfixName;
        this.methodCallExpressionPointer = SmartPointerManager.createPointer(methodCallExpression);
        this.changeCodeFunction = changeCodeFunction;
    }


    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Coconut quick-fixes";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return adaptCodeToAnnotationQuickfixName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        if (methodCallExpressionPointer == null) {
            return;
        }
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) methodCallExpressionPointer.getElement();
        if (methodCallExpression != null) {
            changeCodeFunction.change(methodCallExpression);
        }
    }
}
