package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculationLevel;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataTransmissionCollectionAttributeList;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataTransmissionSharingAttributeList;

public class ActivitySendFileAnnotationUtil extends IntentBaseAnnotationUtil {

    public ActivitySendFileAnnotationUtil(int intentPosition) {
        this.intentPosition = intentPosition;
    }

    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        //The actions as defined in the intent declarations
        ArrayList<String> statements = new ArrayList<>();

        //The intentReference refers to the variable that we need to analyze
        assert (source instanceof PsiMethodCallExpression);
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) source;
        PsiExpression intentExpression = methodCallExpression.getArgumentList().getExpressions()[intentPosition];

        traceAllIntentOccurrences(intentExpression, statements, methodCallExpression);

        boolean shareWithOtherApps = false;
        // Note: For actions ended with SECURE, applications responding to this intent must not expose any personal
        //  content like existing photos or videos on the device. The applications should be careful not to share any
        //  photo or video with other applications or internet.
        for(String statement : statements) {
            if (statement.contains("FLAG_GRANT_READ_URI_PERMISSION")) {
                shareWithOtherApps = true;
                break;
            }
        }

        if (shareWithOtherApps) {
            AnnotationHolder annotationHolder =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataTransmission);
            annotationHolder.put(fieldDataTransmissionCollectionAttributeList,
                    "collectionAttribute.TransmittedOffDevice.False");
            annotationHolder.put(fieldDataTransmissionSharingAttributeList,
                    "sharingAttribute.SharedWithThirdParty.True");
            return new AnnotationSpeculation[] {
                    new AnnotationSpeculation(annotationHolder, AnnotationSpeculationLevel.AT_LEAST_ONE_REQUIRED)};
        }
        return new AnnotationSpeculation[0];
    }

    @Override
    public @Nullable LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }

    @Override
    public @Nullable LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }
}
