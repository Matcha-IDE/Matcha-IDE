package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;

/**
 * This class specifically tracks variables for callbacks in the camera2.CameraCaptureSession class, looking at two
 * different listeners in the CameraCaptureSession.CaptureCallback subclass.
 *
 * @author elijahneundorfer on 6/3/19
 * @version 6/6/19
 */
public class CameraUpdateCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    private static final String listenerTypeCanonicalText = "android.hardware.camera2.CameraCaptureSession.CaptureCallback";
    private static final String onCaptureCompletedCallbackName = "onCaptureCompleted";
    private static final int onCaptureCompletedDataInCallbackParameterPosition = 2;
    private static final String onCaptureProgressedCallbackName = "onCaptureProgressed";
    private static final int onCaptureProgressedDataInCallbackParameterPosition = 2;
    private final int listenerParameterPosition;

    public CameraUpdateCallbackTargetVariableTracker(int listenerParameterPosition) {
        this.listenerParameterPosition = listenerParameterPosition;
    }

    /**
     *
     *
     * @param source
     * @return A data element corresponding to the method we're tracking
     */
    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiElement onCaptureCompleteDataEntity = TargetVariableTrackerUtil.getDataEntityFromSource(source,
                listenerParameterPosition, listenerTypeCanonicalText, onCaptureCompletedCallbackName,
                onCaptureCompletedDataInCallbackParameterPosition);
        PsiElement onCaptureProgressedDataEntity = TargetVariableTrackerUtil.getDataEntityFromSource(source,
                listenerParameterPosition, listenerTypeCanonicalText, onCaptureProgressedCallbackName,
                onCaptureProgressedDataInCallbackParameterPosition);
        if (onCaptureCompleteDataEntity != null) {
            return onCaptureCompleteDataEntity;
        } else {
            return onCaptureProgressedDataEntity;
        }
    }
    //TODO: Annotate both data entities by switching return element to an array and then adjusting the quickfixes

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}