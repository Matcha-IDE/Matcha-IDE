package org.intellij.privacyHelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.jetbrains.annotations.Nullable;

/**
 * Created by tianshi on 2/3/18.
 */
public class AnnotationMetaData {
    public AnnotationHolder annotationInstance;
    public AnnotationSpeculation annotationSpeculation;
    public SmartPsiElementPointer<PsiElement> psiAnnotationPointer;

    public AnnotationMetaData(AnnotationHolder annotationInstance, @Nullable AnnotationSpeculation annotationSpeculation,
                              SmartPsiElementPointer<PsiElement> psiAnnotationPointer) {
        this.annotationInstance = annotationInstance;
        this.annotationSpeculation = annotationSpeculation;
        this.psiAnnotationPointer = psiAnnotationPointer;
    }

    @Nullable
    public String getDataType() {
        if (annotationInstance != null) {
            return annotationInstance.getDataType();
        } else {
            return null;
        }
    }

    public String getTransmissionAttributeSummary() {
        if (annotationInstance != null) {
            return annotationInstance.getTransmissionAttributeSummary();
        } else {
            return "";
        }
    }
}
