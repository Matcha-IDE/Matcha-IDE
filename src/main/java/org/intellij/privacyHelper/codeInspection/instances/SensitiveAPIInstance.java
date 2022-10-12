package org.intellij.privacyHelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.inspections.PersonalDataSinkAPIInspection;
import org.intellij.privacyHelper.codeInspection.inspections.PersonalDataSourceAPIInspection;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 4/27/17.
 */
public class SensitiveAPIInstance {
    // TODO: (long-term) now we only record the source and destination of personal data, but later we will also record the instances that are along the data path propagating from the source to the destination
    public SmartPsiElementPointer<PsiElement> psiElementPointer;
    public AnnotationMetaData[] annotationMetaDataList = null;
    public SensitiveAPI sensitiveAPI;
    public boolean hasError;
    public boolean hasWarning; // TODO. Warnings can be sink ID suggestions, foreground/background analysis
    public String todoDescription = "";

    @NotNull
    private String elementText = "";
    static private final SensitiveAPI[] sourceAPIList = PersonalDataSourceAPIList.getAPIList(false);
    static private final SensitiveAPI[] sinkAPIList = PersonalDataSinkAPIList.getAPIList();

    public SensitiveAPIInstance(SmartPsiElementPointer<PsiElement> psiElementPointer, SensitiveAPI sensitiveAPI,
                                boolean hasError, boolean hasWarning, String todoDescription) {
        this.psiElementPointer = psiElementPointer;
        this.annotationMetaDataList = new AnnotationMetaData[0];
        this.sensitiveAPI = sensitiveAPI;
        if (psiElementPointer.getElement() != null) {
            this.elementText = psiElementPointer.getElement().getText();
        }
        this.hasError = hasError;
        this.hasWarning = hasWarning;
        this.todoDescription = todoDescription;
    }

    public SensitiveAPIInstance(SmartPsiElementPointer<PsiElement> psiElementPointer,
                                AnnotationMetaData [] metaData,
                                SensitiveAPI sensitiveAPI,
                                boolean hasError, boolean hasWarning, String todoDescription) {
        this.psiElementPointer = psiElementPointer;
        this.annotationMetaDataList = metaData;
        this.sensitiveAPI = sensitiveAPI;
        if (psiElementPointer.getElement() != null) {
            this.elementText = psiElementPointer.getElement().getText();
        }
        this.hasError = hasError;
        this.hasWarning = hasWarning;
        this.todoDescription = todoDescription;
    }


    public boolean isValid() {
        if (psiElementPointer.getElement() == null) {
            return false;
        } else {
            if (elementText.equals(psiElementPointer.getElement().getText())) {
                return true;
            } else {
                boolean findNewMatch = false;
                for (SensitiveAPI api : sourceAPIList) {
                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
                        sensitiveAPI = api;
                        elementText = psiElementPointer.getElement().getText();
                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
                            PersonalDataSourceAPIInspection.MyVisitFunction(null,
                                    psiElementPointer.getElement());
                        }
                        findNewMatch = true;
                    }
                }
                for (SensitiveAPI api : sinkAPIList) {
                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
                        sensitiveAPI = api;
                        elementText = psiElementPointer.getElement().getText();
                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
                            PersonalDataSinkAPIInspection.MyVisitMethodCallExpression(null,
                                    (PsiMethodCallExpression) psiElementPointer.getElement());
                        }
                        findNewMatch = true;
                    }
                }
//                for (SensitiveAPI api : permissionRequestAPIList) {
//                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
//                        sensitiveAPI = api;
//                        elementText = psiElementPointer.getElement().getText();
//                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
//                            PermissionRequestApiInspection.MyVisitMethodCallExpression(null,
//                                    (PsiMethodCallExpression) psiElementPointer.getElement());
//                        }
//                        findNewMatch = true;
//                    }
//                }

                return findNewMatch;
            }
        }
    }

    public SmartPsiElementPointer<PsiElement> getPsiElementPointer() {
        return psiElementPointer;
    }

    public AnnotationMetaData[] getAnnotationMetaDataList() {
        return annotationMetaDataList;
    }

    public SensitiveAPI getSensitiveAPI() {
        return sensitiveAPI;
    }

    public void updateAnnotationInfo(AnnotationMetaData [] annotationMetaDataList) {
        this.annotationMetaDataList = annotationMetaDataList;
    }

}
