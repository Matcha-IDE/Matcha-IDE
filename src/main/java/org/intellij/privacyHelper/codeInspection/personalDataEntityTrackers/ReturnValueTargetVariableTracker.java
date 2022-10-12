package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.*;

/**
 * Created by tianshi on 11/15/17.
 */
public class ReturnValueTargetVariableTracker extends PersonalTargetVariableTracker {

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return TargetVariableTrackerUtil.getResolvedVariable(source);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return source;
    }
}
