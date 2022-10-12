package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;

public class PersonalDataInMethodDefinitionParameterTargetVariableTracker extends PersonalTargetVariableTracker {
    private final int parameterPosition;

    public PersonalDataInMethodDefinitionParameterTargetVariableTracker(int parameterPosition) {
        this.parameterPosition = parameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return getTargetVariable(source);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        PsiMethod method;
        if (!(source instanceof PsiMethod)) {
            method = PsiTreeUtil.getParentOfType(source, PsiMethod.class);
        } else {
            method = (PsiMethod) source;
        }
        if (method != null) {
            if (method.getParameterList().getParameters().length > parameterPosition) {
                return method.getParameterList().getParameters()[parameterPosition];
            } else {
                return null;
            }
        }
        return null;
    }
}
