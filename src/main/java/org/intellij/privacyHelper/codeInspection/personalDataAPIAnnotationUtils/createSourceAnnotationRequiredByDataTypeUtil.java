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
import java.util.Collections;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class createSourceAnnotationRequiredByDataTypeUtil extends PersonalDataAPIAnnotationUtil {
    ArrayList<PersonalDataGroup> dataGroupList = new ArrayList<>();

    public createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup dataGroup) {
        this.dataGroupList.add(dataGroup);
    }

    public createSourceAnnotationRequiredByDataTypeUtil(PersonalDataGroup ... dataGroup) {
        Collections.addAll(this.dataGroupList, dataGroup);
    }

    @Override
    public AnnotationSpeculation[] createAnnotationInferences(PsiElement source) {
        ArrayList<AnnotationHolder> holders = new ArrayList<>();
        for (PersonalDataGroup dataGroup : dataGroupList) {
            AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.DataAccess);
            annotationHolder.put(fieldDataAccessDataType, String.format("DataType.%s", dataGroup.toString()));
            holders.add(annotationHolder);
        }
        return new AnnotationSpeculation[] {
                new AnnotationSpeculation(holders.toArray(new AnnotationHolder[0]),
                        AnnotationSpeculationLevel.AT_LEAST_ONE_REQUIRED)};
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
