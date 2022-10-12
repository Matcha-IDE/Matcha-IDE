package org.intellij.privacyHelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.instances.SensitiveAPIInstance;
import org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

import java.util.*;

/**
 * Created by tianshi on 11/13/17.
 */
public abstract class SensitiveAPI {
    PersonalDataAPIType personalDataAPIType;
    public ArrayList<PersonalDataAPIAnnotationUtil> configs = new ArrayList<>();
    PersonalTargetVariableTracker targetVariableTracker;

    String displayName;
    public String fullAPINamePattern;
    public String returnValueTypeCanonicalTextPattern;
    public String [] parameterTypeCanonicalTextRestriction;
    public String [] parameterValueTextRestriction;

    public AndroidPermission [][] requiredPermissions;
    public AndroidPermission [][] libImplicitVoluntaryPermissions; // will access the data implicitly

    public boolean isThirdPartyAPI;
    public AccessType accessType; // only used by source APIs

    boolean isPersonalDataSource;
    boolean isPersonalDataSink;

    boolean isAlwaysSensitive = true;
    SmartPsiElementPointer<PsiElement> dependentPsiElement;

    SensitiveAPI(PersonalDataAPIType personalDataAPIType,
                 String displayName, String fullAPINamePattern, String returnValueTypeCanonicalTextPattern,
                 String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                 AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                 boolean isThirdPartyAPI,
                 PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                 boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        this.personalDataAPIType = personalDataAPIType;
        this.displayName = displayName;
        this.fullAPINamePattern = fullAPINamePattern;
        this.returnValueTypeCanonicalTextPattern = returnValueTypeCanonicalTextPattern;
        this.parameterTypeCanonicalTextRestriction = parameterTypeCanonicalTextRestriction;
        this.parameterValueTextRestriction = parameterValueTextRestriction;
        this.requiredPermissions = requiredPermissions;
        this.libImplicitVoluntaryPermissions = libImplicitVoluntaryPermissions;
        this.isThirdPartyAPI = isThirdPartyAPI;
        this.configs.add(config);
        this.configs.get(0).setAPI(this);
        this.targetVariableTracker = targetVariableTracker;
        this.isPersonalDataSource = isPersonalDataSource;
        this.isPersonalDataSink = isPersonalDataSink;
        this.accessType = accessType;
    }

    SensitiveAPI(PersonalDataAPIType personalDataAPIType,
                 String displayName, String fullAPINamePattern, String returnValueTypeCanonicalTextPattern,
                 String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                 AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                 boolean isThirdPartyAPI,
                 PersonalDataAPIAnnotationUtil[] configs, PersonalTargetVariableTracker targetVariableTracker,
                 boolean isPersonalDataSource, boolean isPersonalDataSink, AccessType accessType) {
        this.personalDataAPIType = personalDataAPIType;
        this.displayName = displayName;
        this.fullAPINamePattern = fullAPINamePattern;
        this.returnValueTypeCanonicalTextPattern = returnValueTypeCanonicalTextPattern;
        this.parameterTypeCanonicalTextRestriction = parameterTypeCanonicalTextRestriction;
        this.parameterValueTextRestriction = parameterValueTextRestriction;
        this.requiredPermissions = requiredPermissions;
        this.libImplicitVoluntaryPermissions = libImplicitVoluntaryPermissions;
        this.isThirdPartyAPI = isThirdPartyAPI;
        Collections.addAll(this.configs, configs);
        for (PersonalDataAPIAnnotationUtil config : configs) {
            config.setAPI(this);
        }
        this.targetVariableTracker = targetVariableTracker;
        this.isPersonalDataSource = isPersonalDataSource;
        this.isPersonalDataSink = isPersonalDataSink;
        this.accessType = accessType;
    }

    SensitiveAPI(String displayName, String fullAPINamePattern, String returnValueTypeCanonicalTextPattern,
                 String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                 boolean isPersonalDataSource, boolean isPersonalDataSink) {
        this.displayName = displayName;
        this.fullAPINamePattern = fullAPINamePattern;
        this.returnValueTypeCanonicalTextPattern = returnValueTypeCanonicalTextPattern;
        this.parameterTypeCanonicalTextRestriction = parameterTypeCanonicalTextRestriction;
        this.parameterValueTextRestriction = parameterValueTextRestriction;
    }


    public boolean isPersonalDataSource() {
        return isPersonalDataSource;
    }

    public boolean isPersonalDataSink() {
        return isPersonalDataSink;
    }

    public AnnotationSpeculation[] createAnnotationInferencesFromSource(PsiElement source) {
        ArrayList<AnnotationSpeculation> annotationHolders = new ArrayList<>();
        for (PersonalDataAPIAnnotationUtil config : configs) {
            Collections.addAll(annotationHolders, config.createAnnotationInferences(source));
        }
        return annotationHolders.toArray(AnnotationSpeculation[]::new);
    }

    public PsiElement getTargetVariable(PsiElement source) {
        return targetVariableTracker == null ? null : targetVariableTracker.getTargetVariable(source);
    }

    public PsiElement getResolvedTargetVariable(PsiElement source) {
        return targetVariableTracker.getResolvedTargetVariable(source);
    }

    public void setAlwaysSensitive(boolean isAlwaysSensitive) {
        this.isAlwaysSensitive = isAlwaysSensitive;
    }

    public void setDependentPsiElement(PsiElement psiElement) {
        if (psiElement != null) {
            dependentPsiElement = SmartPointerManager.createPointer(psiElement);
        }
    }

    public boolean isDependentPsiElementActive() {
        return dependentPsiElement != null && dependentPsiElement.getElement() != null;
    }

    public boolean permissionUsedInAPI (AndroidPermission permission) {
        return explicitlyUsedRequiredPermissionInAPI(permission) || implicitlyUsedVoluntaryPermissionInAPI(permission);
    }

    public boolean explicitlyUsedRequiredPermissionInAPI(AndroidPermission permission) {
        for (AndroidPermission [] requriedPermissionGroup : requiredPermissions) {
            for (AndroidPermission requiredPermission : requriedPermissionGroup) {
                if (permission.equals(requiredPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean implicitlyUsedVoluntaryPermissionInAPI(AndroidPermission permission) {
        for (AndroidPermission [] permissionGroup : libImplicitVoluntaryPermissions) {
            for (AndroidPermission permission1 : permissionGroup) {
                if (permission.equals(permission1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Compares a PsiElement in the form of a method call to this object and determines if they are the
     * same method call.
     *
     * @param dataElement The method call we're attempting check
     * @return boolean - true if they are the same method, false otherwise
     */
    public boolean psiElementMethodCallMatched(PsiElement dataElement) {
        //Checks to see if PsiElement is a method call
        if (dataElement instanceof PsiMethodCallExpression) {
            if (this instanceof TargetVariableInMethodDefinitionParameterAPI) {
                return false;
            }
            //If so parses to PsiMethodCallExpression
            PsiMethodCallExpression expression = (PsiMethodCallExpression) dataElement;
            //Uses another method to determine if the method call matches
            return CodeInspectionUtil.checkMatchedMethodCall(expression, returnValueTypeCanonicalTextPattern, fullAPINamePattern,
                    parameterTypeCanonicalTextRestriction, parameterValueTextRestriction);
        } else if (dataElement instanceof PsiMethod) {
            if (! (this instanceof TargetVariableInMethodDefinitionParameterAPI)) {
                return false;
            }
            PsiMethod method = (PsiMethod) dataElement;
            return CodeInspectionUtil.checkMatchedMethod(method, returnValueTypeCanonicalTextPattern, fullAPINamePattern,
                    parameterTypeCanonicalTextRestriction);
        } {
            // If either the dataElement is not a method call, method call does
            // not match this API, or the annotation is not correct, we return false
            return false;
        }
    }

    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<SensitiveAPIInstance> instances,
                                                                  PsiNameValuePair nameValuePair,
                                                                  ArrayList<String> targetFieldValue) {
        ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
        for (PersonalDataAPIAnnotationUtil config : configs) {
            Collections.addAll(quickFixes,
                    config.getModifyFieldValueAndCodeQuickfixList(instances, nameValuePair, targetFieldValue));
        }
        return quickFixes.toArray(LocalQuickFix[]::new);
    }

    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression,
                                                                  HashMap<PsiNameValuePair, ArrayList<String>>
                                                                          annotationFieldChangeMap) {
        ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
        for (PersonalDataAPIAnnotationUtil config : configs) {
            Collections.addAll(quickFixes,
                    config.getModifyFieldValueAndCodeQuickfixList(methodCallExpression, annotationFieldChangeMap));
        }
        return quickFixes.toArray(LocalQuickFix[]::new);
    }

    public boolean targetVariableFromCallback() {
        return (personalDataAPIType == PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK);
    }

    public boolean targetVariableFromIntent() {
        return (personalDataAPIType == PersonalDataAPIType.PERSONAL_DATA_FROM_INTENT);
    }

    public boolean isValid() {
        return isAlwaysSensitive || isDependentPsiElementActive();
    }
}

