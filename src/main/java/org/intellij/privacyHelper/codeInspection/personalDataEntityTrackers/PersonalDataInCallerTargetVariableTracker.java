package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;

public class PersonalDataInCallerTargetVariableTracker extends PersonalTargetVariableTracker {
    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return TargetVariableTrackerUtil.getResolvedVariable(getTargetVariable(source));
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        if(sourceMethodCallExp != null) {
            return sourceMethodCallExp.getMethodExpression().getQualifierExpression();
        }
        return null;
    }
}
