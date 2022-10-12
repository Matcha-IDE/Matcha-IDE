package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.regex.Pattern;

import static org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil.getDataEntityFromSource;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSTaskLocationCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    static private final String onSuccessListenerTypeCanonicalText = "com.google.android.gms.tasks.OnSuccessListener<android.location.Location>";
    static private final String onSuccessCallbackName = "onSuccess";
    static private final String onCompleteListenerTypeCanonicalText = "com.google.android.gms.tasks.OnCompleteListener<android.location.Location>";
    static private final String onCompleteCallbackName = "onComplete";

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiElement locationTask = TargetVariableTrackerUtil.getResolvedVariable(source);
        // First check if the return value task is assigned to a variable
        if (locationTask != null) {
            PsiReference [] referenceCollections = ReferencesSearch.search(locationTask).findAll().toArray(new PsiReference[0]);
            for (int i = referenceCollections.length - 1 ; i >= 0 ; i--) {
                PsiElement psiElement = referenceCollections[i].getElement();
                PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
                while (methodCallExpression != null) {
                    if (Pattern.compile(".*addOnSuccessListener", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches() ||
                            Pattern.compile(".*addOnCompleteListener", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        if (methodCallExpression.getArgumentList().getExpressions().length == 2 ||
                                methodCallExpression.getArgumentList().getExpressions().length == 1) {
                            PsiElement onSuccessDataEntity = getDataEntityFromSource(methodCallExpression,
                                    methodCallExpression.getArgumentList().getExpressions().length == 2 ? 1 : 0,
                                    onSuccessListenerTypeCanonicalText, onSuccessCallbackName,
                                    0);
                            PsiElement onCompleteDataEntity = getDataEntityFromSource(methodCallExpression,
                                    methodCallExpression.getArgumentList().getExpressions().length == 2 ? 1 : 0,
                                    onCompleteListenerTypeCanonicalText, onCompleteCallbackName,
                                    0);
                            if (onSuccessDataEntity != null) {
                                return onSuccessDataEntity;
                            } else if (onCompleteDataEntity != null) {
                                return onCompleteDataEntity;
                            }
                        }
                    }
                    methodCallExpression = PsiTreeUtil.getParentOfType(methodCallExpression, PsiMethodCallExpression.class);
                }
            }
        }
        // If it can reach this point, it means no valid callback has been detected, then we check whether the callback is directly attached to the method call exp.
        PsiMethodCallExpression addListenerCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        while (addListenerCallExp != null) {
            if (Pattern.compile(".*addOnSuccessListener", Pattern.DOTALL).matcher(addListenerCallExp.getMethodExpression().getText()).matches() ||
                    Pattern.compile(".*addOnCompleteListener", Pattern.DOTALL).matcher(addListenerCallExp.getMethodExpression().getText()).matches()) {
                if (addListenerCallExp.getArgumentList().getExpressions().length == 2 ||
                        addListenerCallExp.getArgumentList().getExpressions().length == 1) {
                    PsiElement onSuccessDataEntity =
                            getDataEntityFromSource(addListenerCallExp,
                                    addListenerCallExp.getArgumentList().getExpressions().length == 2 ? 1: 0,
                                    onSuccessListenerTypeCanonicalText, onSuccessCallbackName,
                                    0);
                    PsiElement onCompleteDataEntity =
                            getDataEntityFromSource(addListenerCallExp,
                                    addListenerCallExp.getArgumentList().getExpressions().length == 2 ? 1: 0,
                                    onCompleteListenerTypeCanonicalText, onCompleteCallbackName,
                                    0);
                    if (onSuccessDataEntity != null) {
                        return onSuccessDataEntity;
                    } else if (onCompleteDataEntity != null) {
                        return onCompleteDataEntity;
                    }
                }
            }
            addListenerCallExp = PsiTreeUtil.getParentOfType(addListenerCallExp, PsiMethodCallExpression.class);
        }
        return null;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
