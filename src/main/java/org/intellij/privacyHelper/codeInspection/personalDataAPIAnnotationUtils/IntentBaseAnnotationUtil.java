package org.intellij.privacyHelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;

import java.util.ArrayList;

public abstract class IntentBaseAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    int intentPosition;

    boolean checkCreateChooserAPI(ArrayList<String> statements, PsiMethodCallExpression intentMethodCallExp) {
        PsiExpression intentExpression;
        PsiIdentifier currentMethodIdentifier =
                PsiTreeUtil.getChildOfType(intentMethodCallExp.getMethodExpression(), PsiIdentifier.class);
        if (currentMethodIdentifier != null &&
                "createChooser".equals(currentMethodIdentifier.getText())) {
            intentExpression = intentMethodCallExp.getArgumentList().getExpressions()[0];
            statements.clear();
            traceAllIntentOccurrences(intentExpression, statements, intentMethodCallExp);
            return true;
        } else {
            return false;
        }
    }

    void traceAllIntentOccurrences(PsiExpression intentExpression, ArrayList<String> statements,
                                           PsiMethodCallExpression methodCallExpression) {
        if (intentExpression instanceof PsiReferenceExpression) {
            ArrayList<PsiElement> intentOccurrences = CodeInspectionUtil.getGlobalAndLocalRefExpsBeforeMethodExp(
                    (PsiReferenceExpression) intentExpression, methodCallExpression);
            for (PsiElement intentOccurrence : intentOccurrences) {
                boolean isChooserIntent = false;
                if (intentOccurrence instanceof PsiLocalVariable) {
                    if (((PsiLocalVariable) intentOccurrence).getInitializer() instanceof PsiMethodCallExpression) {
                        isChooserIntent = checkCreateChooserAPI(statements,
                                (PsiMethodCallExpression) ((PsiLocalVariable) intentOccurrence).getInitializer());
                    }
                } else if (intentOccurrence instanceof PsiField) {
                    if (((PsiField) intentOccurrence).getInitializer() instanceof PsiMethodCallExpression) {
                        isChooserIntent = checkCreateChooserAPI(statements,
                                (PsiMethodCallExpression) ((PsiField) intentOccurrence).getInitializer());
                    }
                } else {
                    PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(intentOccurrence, PsiAssignmentExpression.class);
                    PsiDeclarationStatement declarationStatement = PsiTreeUtil.getParentOfType(intentOccurrence, PsiDeclarationStatement.class);
                    if (assignmentExpression != null && intentOccurrence.equals(assignmentExpression.getLExpression()) &&
                            assignmentExpression.getRExpression() != null && assignmentExpression.getRExpression() instanceof PsiMethodCallExpression) {
                        if (checkCreateChooserAPI(statements, (PsiMethodCallExpression) assignmentExpression.getRExpression())) {
                            isChooserIntent = true;
                        }
                    }
                    if (declarationStatement != null) {
                        for (PsiElement element : declarationStatement.getDeclaredElements()) {
                            if (intentOccurrence.equals(element)) {
                                PsiMethodCallExpression[] children =
                                        PsiTreeUtil.getChildrenOfType(declarationStatement, PsiMethodCallExpression.class);
                                if (children != null) {
                                    for (PsiMethodCallExpression child : children) {
                                        if (checkCreateChooserAPI(statements, child)) {
                                            isChooserIntent = true;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (!isChooserIntent) {
                    PsiStatement statement = PsiTreeUtil.getParentOfType(intentOccurrence, PsiStatement.class);
                    if (statement != null) {
                        statements.add(statement.getText());
                    }
                }
            }
        } else if (intentExpression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression intentMethodCallExp = (PsiMethodCallExpression) intentExpression;
            checkCreateChooserAPI(statements, intentMethodCallExp);
        } else if (intentExpression instanceof PsiNewExpression) {
            PsiStatement statement = PsiTreeUtil.getParentOfType(intentExpression, PsiStatement.class);
            if (statement != null) {
                statements.add(statement.getText());
            }
        }
    }
}
