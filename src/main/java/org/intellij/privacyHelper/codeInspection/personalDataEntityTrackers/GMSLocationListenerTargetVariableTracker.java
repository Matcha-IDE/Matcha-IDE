package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSLocationListenerTargetVariableTracker extends PersonalTargetVariableTracker {
    private final int listenerParameterPosition;
    static private final String listenerTypeCanonicalText = "com.google.android.gms.location.LocationListener";
    static private final String callbackName = "onLocationChanged";

    public GMSLocationListenerTargetVariableTracker(int listenerParameterPosition) {
        this.listenerParameterPosition = listenerParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return TargetVariableTrackerUtil.getDataEntityFromSource(source,
                listenerParameterPosition, listenerTypeCanonicalText,callbackName, 0);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
