package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSLocationCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    private int callbackParameterPosition;
    static private final String callbackTypeCanonicalText = "com.google.android.gms.location.LocationCallback";
    static private final String callbackName = "onLocationResult";


    public GMSLocationCallbackTargetVariableTracker(int callbackParameterPosition) {
        this.callbackParameterPosition = callbackParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return TargetVariableTrackerUtil.getDataEntityFromSource(source, callbackParameterPosition,
                callbackTypeCanonicalText,callbackName, 0);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
