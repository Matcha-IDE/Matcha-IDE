package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;


import com.intellij.psi.PsiElement;

/**
 * @author Elijah Neundorfer 6/17/19
 * @version 6/17/19
 */
public class SensorCallbackTargetVariableTracker extends PersonalTargetVariableTracker{
    private int callbackParameterPosition;
    static private final String[][] callbackTypeCanonicalTextsAndNames = {{"android.hardware.SensorEventListener", "onSensorChanged"}, {"android.hardware.TriggerEventListener", "onTrigger"}};
    public SensorCallbackTargetVariableTracker(int callbackParameterPosition) {
        this.callbackParameterPosition = callbackParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiElement dataElement = null;
        for (String[] pair : callbackTypeCanonicalTextsAndNames) {
            dataElement = TargetVariableTrackerUtil.getDataEntityFromSource(
                    source, callbackParameterPosition, pair[0], pair[1], 0);
            if (dataElement != null) {
                break;
            }
        }
        return dataElement;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
