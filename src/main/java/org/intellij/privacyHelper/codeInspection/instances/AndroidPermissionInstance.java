package org.intellij.privacyHelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class AndroidPermissionInstance {
    public SmartPsiElementPointer<PsiElement> psiElementPointer;
    public AndroidPermission permission;

    public AndroidPermissionInstance(SmartPsiElementPointer<PsiElement> psiElementPointer,
                                     AndroidPermission permission) {
        this.psiElementPointer = psiElementPointer;
        this.permission = permission;
    }

    public boolean isValid() {
        // TODO (double-check) What's the right way to check the validity?
        return psiElementPointer.getElement() != null;
    }
}
