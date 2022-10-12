package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculationLevel;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class MediaRecorderAnnotationUtil extends PersonalDataAPIAnnotationUtil {

    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        /*
          If the MediaRecorder object is recording video, we need a CameraSource. If it records audio, we need a MicrophoneSource.

          It can also record both, in which case we need both annotations
         */
        //This set holds all the possible annotations that are needed for currentMethodCallExpression, but only one is needed per API
        Set<PersonalDataGroup> possibleDataTypes = new HashSet<>();
        assert source instanceof PsiMethodCallExpression;
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) source;
        //This block gets a reference to the object that setOutput is called on (our MediaRecorder object), and the finds the element itself
        PsiReferenceExpression mediaRecorder = PsiTreeUtil.getChildOfType(methodCallExpression.getMethodExpression(), PsiReferenceExpression.class);
        //Checks to be sure our object reference actually is a PsiReferenceExpression
        if (mediaRecorder != null) {
            //This block iterates over reference made to our object throughout our code
            //By "reference" I'm referring to methods called on the object (like MediaRecorder.start()), etc.
            ArrayList<PsiElement> mediaRecorderOccurrences = CodeInspectionUtil.getGlobalAndLocalRefExpsBeforeMethodExp(
                    mediaRecorder, methodCallExpression
            );
            for (PsiElement occurrence : mediaRecorderOccurrences) {
                if (PsiTreeUtil.getParentOfType(occurrence, PsiMethodCallExpression.class) != null) {
                    //getParentOfType gives us the entire line of code, including the method call
                    methodCallExpression = PsiTreeUtil.getParentOfType(occurrence, PsiMethodCallExpression.class);

                    if (methodCallExpression != null) {
                        //If we set an AudioSource to our MediaRecorder object, we need a microphone annotation
                        if (Pattern.compile(".*setAudioSource", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                            possibleDataTypes.add(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings);
                        }
                        //If we set a VideoSource to our MediaRecorder object, we need a camera annotation
                        if (Pattern.compile(".*setVideoSource", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                            possibleDataTypes.add(PersonalDataGroup.PhotosAndVideos_Videos);
                        }
                    }
                }
            }
        }
        ArrayList<AnnotationSpeculation> annotationSpeculations = new ArrayList<>();
        for (PersonalDataGroup dataGroup : possibleDataTypes) {
            AnnotationHolder annotationHolder =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataAccess);
            annotationHolder.put(fieldDataAccessDataType, String.format("DataType.%s", dataGroup.toString()));
            annotationSpeculations.add(
                    new AnnotationSpeculation(annotationHolder, AnnotationSpeculationLevel.AT_LEAST_ONE_REQUIRED));
        }
        return annotationSpeculations.toArray(new AnnotationSpeculation[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }
}
