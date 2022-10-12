package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 5/10/17.
 */
public class DeclareVariableQuickfix implements LocalQuickFix {
    private String declarationTypeText;
    private String declarationNameText;
    private PsiExpression targetExpression;
    final static private String DeclareVariableQuickfixName = "Generate a declaration for this value";
    final static private String DeclareVariableQuickfixFamilyName = "Declare variable quickfixes";

    public DeclareVariableQuickfix(String declarationNameText, PsiExpression targetExpression) {
        super();
        PsiType type = targetExpression.getType();
        if (type != null) {
            this.declarationTypeText = type.getCanonicalText();
        }
        this.declarationNameText = declarationNameText;
        this.targetExpression = targetExpression;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return DeclareVariableQuickfixName;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return DeclareVariableQuickfixFamilyName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        if (declarationTypeText == null) {
            return;
        }
        String expressionText = targetExpression.getText();
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        assert PsiTreeUtil.getParentOfType(targetExpression, PsiStatement.class) != null;
        PsiStatement currentStatement;
        currentStatement = PsiTreeUtil.getParentOfType(targetExpression, PsiStatement.class);
        PsiStatement declarationStatement = factory.createStatementFromText(
                "TYPE NAME = ".replace("TYPE", declarationTypeText).replace("NAME", declarationNameText)
                        + expressionText + ";",
                null);
        currentStatement.getParent().addBefore(declarationStatement, currentStatement);
        targetExpression.replace(factory.createExpressionFromText(declarationNameText, null));
    }
}
