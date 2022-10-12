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
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataAccessDataType;
import static org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup.*;
import static org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup.Contacts_Contacts;

public class SensitiveUserSelectionAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    public static PersonalDataGroup[] possibleDataTypes = new PersonalDataGroup[] {
            PersonalInfo_Address, PersonalInfo_RaceAndEthnicity, PersonalInfo_PoliticalOrReligiousBeliefs,
            PersonalInfo_OtherPersonalInfo, PersonalInfo_SexualOrientation,
            FinancialInfo_UserPaymentInfo, FinancialInfo_OtherFinancialInfo, AppActivity_OtherUserGeneratedContent,
            HealthAndFitness_HealthInfo, HealthAndFitness_FitnessInfo, Contacts_Contacts
    };


    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        // TODO: further narrow down the scope of the hints based on semantics such as variable names etc.
        ArrayList<AnnotationSpeculation> annotationSpeculations = new ArrayList<>();
        for (PersonalDataGroup dataType : possibleDataTypes) {
            AnnotationHolder annotationHolder =
                    CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataAccess);
            annotationHolder.put(fieldDataAccessDataType, String.format("DataType.%s", dataType));
            annotationSpeculations.add(
                    new AnnotationSpeculation(annotationHolder, AnnotationSpeculationLevel.ANY_POSSIBLE));
        }
        return annotationSpeculations.toArray(AnnotationSpeculation[]::new);
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
