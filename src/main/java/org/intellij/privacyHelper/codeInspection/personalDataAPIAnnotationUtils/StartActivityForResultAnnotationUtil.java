package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculationLevel;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class StartActivityForResultAnnotationUtil extends IntentBaseAnnotationUtil {
    public StartActivityForResultAnnotationUtil(int intentPosition) {
        this.intentPosition = intentPosition;
    }

    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        //This set holds all the possible annotations that are needed for currentMethodCallExpression, but only one is needed per API
        Set<PersonalDataGroup> possibleDataTypes = new HashSet<>();
        //The actions as defined in the intent declarations
        ArrayList<String> statements = new ArrayList<>();

        //The intentReference refers to the variable that we need to analyze
        assert (source instanceof PsiMethodCallExpression);
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) source;
        PsiExpression intentExpression = methodCallExpression.getArgumentList().getExpressions()[intentPosition];

        traceAllIntentOccurrences(intentExpression, statements, methodCallExpression);

        // Note that we don't count MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
        // MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE, and MediaStore.INTENT_ACTION_VIDEO_CAMERA, because they
        // just invoke the system Camera app but won't return the result to the app.

        // TODO: handle new intent created in place
        // TODO: handle chooser wrapper
        // TODO: handle extra intents (more than one type of data), see https://stackoverflow.com/questions/16551851/android-intent-for-capturing-both-images-and-videos

        // Note: For actions ended with SECURE, applications responding to this intent must not expose any personal
        //  content like existing photos or videos on the device. The applications should be careful not to share any
        //  photo or video with other applications or internet.
        for(String statement : statements) {
            if (statement.contains("RECORD_SOUND_ACTION")) { // MediaStore.Audio.Media.*
                possibleDataTypes.add(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings);
            }
            if (statement.contains("ACTION_IMAGE_CAPTURE") ||
                    statement.contains("ACTION_IMAGE_CAPTURE_SECURE")) { // MediaStore.*
                // TODO: Analyze EXTRA_OUTPUT. The caller may pass an extra EXTRA_OUTPUT to control where this image
                //  will be written. If the EXTRA_OUTPUT is not present, then a small sized image is returned as a
                //  Bitmap object in the extra field. This is useful for applications that only need a small image. If
                //  the EXTRA_OUTPUT is present, then the full-sized image will be written to the Uri value of
                //  EXTRA_OUTPUT.
                // e.g. Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                possibleDataTypes.add(PersonalDataGroup.PhotosAndVideos_Photos);
            }
            if (statement.contains("ACTION_VIDEO_CAPTURE")) { // MediaStore.*
                // TODO: the developer may also manually mute the microphone to avoid capturing audio
                // https://stackoverflow.com/questions/55662577/record-video-without-sound-recording-with-default-video-intent-action-video-capt
                possibleDataTypes.add(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings);
                possibleDataTypes.add(PersonalDataGroup.PhotosAndVideos_Videos);
            }
            if (statement.contains("ACTION_GET_CONTENT") ||
                    statement.contains("ACTION_OPEN_DOCUMENT") ||
                    statement.contains("ACTION_OPEN_DOCUMENT_TREE") ||
                    statement.contains("ACTION_PICK")) { // Intent.*
                // TODO: verify the URI used for "ACTION_PICK" to determine what type of data is collected in this way
                // FIXME: now we just assume ACTION_PICK always returns user file data (at least it will capture more sensitive data).
                possibleDataTypes.add(PersonalDataGroup.FilesAndDocs_FilesAndDocs);
                possibleDataTypes.add(PersonalDataGroup.PhotosAndVideos_Photos);
                possibleDataTypes.add(PersonalDataGroup.PhotosAndVideos_Videos);
                possibleDataTypes.add(PersonalDataGroup.AudioFiles_VoiceOrSoundRecordings);
                possibleDataTypes.add(PersonalDataGroup.AudioFiles_MusicFiles);
            }
            if (statement.contains("Intent.ACTION_OPEN_DOCUMENT")) {
                possibleDataTypes.add(PersonalDataGroup.FilesAndDocs_FilesAndDocs);
            }
            if (statement.contains("ACTION_RECOGNIZE_SPEECH")) {
                Collections.addAll(possibleDataTypes, SensitiveUserInputAnnotationUtil.possibleDataTypes);
            }
        }

        ArrayList<AnnotationSpeculation> annotationSpeculations = new ArrayList<>();
        for (PersonalDataGroup dataGroup : possibleDataTypes) {
            AnnotationHolder annotationHolder =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataAccess);
            annotationHolder.put(fieldDataAccessDataType, String.format("DataType.%s", dataGroup.toString()));
            annotationSpeculations.add(
                    new AnnotationSpeculation(annotationHolder, AnnotationSpeculationLevel.ANY_POSSIBLE));
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
