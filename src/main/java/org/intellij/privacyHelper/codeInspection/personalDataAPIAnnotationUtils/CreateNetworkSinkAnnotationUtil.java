package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
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

public class CreateNetworkSinkAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        AnnotationHolder annotationHolder =
                CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataTransmission);
        annotationHolder.put(fieldDataTransmissionCollectionAttributeList,
                "collectionAttribute.TransmittedOffDevice.True");
        return new AnnotationSpeculation[] {
                new AnnotationSpeculation(annotationHolder, AnnotationSpeculationLevel.AT_LEAST_ONE_REQUIRED)};
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
