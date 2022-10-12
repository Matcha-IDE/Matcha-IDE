package org.intellij.privacyHelper.codeInspection.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;

public class ThirdPartyCustomDataInstance {
    private final SmartPsiElementPointer<PsiElement> psiElementPointer;
    public String libraryName;
    public boolean verified;
    public SafetySectionDataElement safetySectionDataElement;

    public ThirdPartyCustomDataInstance(SmartPsiElementPointer<PsiElement> psiElementPointer, String libraryName,
                                        boolean verified, SafetySectionDataElement safetySectionDataElement) {
        this.psiElementPointer = psiElementPointer;
        this.libraryName = libraryName;
        this.verified = verified;
        this.safetySectionDataElement = safetySectionDataElement;
    }

    public SmartPsiElementPointer<PsiElement> getPsiElementPointer() {
        return psiElementPointer;
    }

    public boolean isValid() {
        // TODO: (double-check) seems not the standard way
        return psiElementPointer.getRange() != null && psiElementPointer.getElement() != null;
    }
}
