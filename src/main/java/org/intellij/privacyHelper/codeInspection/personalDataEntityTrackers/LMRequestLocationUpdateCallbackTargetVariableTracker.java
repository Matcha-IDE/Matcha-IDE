package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.*;

/**
 * Created by tianshi on 11/15/17.
 */
public class LMRequestLocationUpdateCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    private static String listenerTypeCanonicalText = "android.location.LocationListener";
    private static String callbackName = "onLocationChanged";
    private int listenerParameterPosition;

    public LMRequestLocationUpdateCallbackTargetVariableTracker(int listenerParameterPosition) {
        this.listenerParameterPosition = listenerParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return TargetVariableTrackerUtil.getDataEntityFromSource(source, listenerParameterPosition,
                listenerTypeCanonicalText,callbackName, 0);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
