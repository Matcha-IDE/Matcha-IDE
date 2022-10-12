package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.psi.PsiMethodCallExpression;

/**
 * Created by tianshi on 3/11/18.
 */
public interface ChangeCodeFunction {
    void change(PsiMethodCallExpression methodCallExpression);
}
