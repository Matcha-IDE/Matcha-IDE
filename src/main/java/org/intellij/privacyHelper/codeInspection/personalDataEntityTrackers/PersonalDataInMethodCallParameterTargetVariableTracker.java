package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by tianshi on 2/2/18.
 */
public class PersonalDataInMethodCallParameterTargetVariableTracker extends PersonalTargetVariableTracker {
    private final int parameterPosition;

    public PersonalDataInMethodCallParameterTargetVariableTracker(int parameterPosition) {
        this.parameterPosition = parameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        if (sourceMethodCallExp != null) {
            if (sourceMethodCallExp.getArgumentList().getExpressions().length > parameterPosition) {
                PsiExpression expression = sourceMethodCallExp.getArgumentList().getExpressions()[parameterPosition];
                return TargetVariableTrackerUtil.getResolvedVariable(expression);
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        if (sourceMethodCallExp != null) {
            if (sourceMethodCallExp.getArgumentList().getExpressions().length > parameterPosition) {
                return sourceMethodCallExp.getArgumentList().getExpressions()[parameterPosition];
            } else {
                return null;
            }
        }
        return null;
    }
}
