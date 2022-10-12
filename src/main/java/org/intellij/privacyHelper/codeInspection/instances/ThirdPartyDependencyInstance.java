package org.intellij.privacyHelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.utils.ThirdPartyDependencies;
import org.intellij.privacyHelper.codeInspection.utils.ThirdPartyDependencyInfo;

import java.util.regex.Pattern;

/**
 * Created by tianshi on 5/11/17.
 */
public class ThirdPartyDependencyInstance {
    private final SmartPsiElementPointer<PsiElement> psiElementPointer;
    ThirdPartyDependencyInfo dependencyInfo = null;

    public ThirdPartyDependencyInstance(SmartPsiElementPointer<PsiElement> smartPsiElementPointer,
                                        String dependencyString) {
        this.psiElementPointer = smartPsiElementPointer;
        for (ThirdPartyDependencyInfo info : ThirdPartyDependencies.getThirdPartyLibList(smartPsiElementPointer.getProject())) {
            for (String gradlePublicDependencyPattern : info.gradlePublicDependencyPatterns) {
                if (Pattern.matches(gradlePublicDependencyPattern, dependencyString)) {
                    dependencyInfo = info;
                    break;
                }
            }
            if (dependencyInfo != null) {
                break;
            }
        }
    }
    // TODO: also check manual dependency and import statement

    public ThirdPartyDependencyInfo getDependencyInfo() {
        return dependencyInfo;
    }

    public SmartPsiElementPointer<PsiElement> getPsiElementPointer() {
        return psiElementPointer;
    }

    public boolean isValid() {
        // TODO: (double-check) seems not the standard way
        return psiElementPointer.getRange() != null && psiElementPointer.getElement() != null;
    }
}
