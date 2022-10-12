package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;

/**
 * Created by tianshi on 11/15/17.
 */
public abstract class PersonalTargetVariableTracker {
    public abstract PsiElement getResolvedTargetVariable(PsiElement source);

    public abstract PsiElement getTargetVariable(PsiElement source);
}
