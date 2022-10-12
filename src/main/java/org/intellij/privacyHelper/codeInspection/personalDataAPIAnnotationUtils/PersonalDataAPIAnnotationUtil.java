package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.instances.SensitiveAPIInstance;
import org.intellij.privacyHelper.codeInspection.utils.SensitiveAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tianshi on 11/14/17.
 */
public abstract class PersonalDataAPIAnnotationUtil {
    protected SensitiveAPI api;

    public abstract AnnotationSpeculation[] createAnnotationInferences(PsiElement source);

    public void setAPI(SensitiveAPI api) {
        this.api = api;
    }

    @Nullable
    public abstract LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue);

    @Nullable
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<SensitiveAPIInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    public abstract LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue);

    @Nullable
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<SensitiveAPIInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        // TODO: temporary overload
        return null;
    }

    @Nullable
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, HashMap<PsiNameValuePair, ArrayList<String>> annotationFieldChangeMap) {
        // TODO: temporary overload
        return null;
    }
}
