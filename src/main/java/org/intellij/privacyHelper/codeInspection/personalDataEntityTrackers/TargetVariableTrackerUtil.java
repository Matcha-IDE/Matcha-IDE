package org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.regex.Pattern;

/**
 * Created by tianshi on 1/21/18.
 */
public class TargetVariableTrackerUtil {
    static PsiExpression getListener(PsiElement source, int listenerParameterPosition) {
        if (!(source instanceof PsiMethodCallExpression)) {
            return null;
        }
        PsiMethodCallExpression expression = (PsiMethodCallExpression) source;
        if (expression.getArgumentList().getExpressions().length <= listenerParameterPosition) {
            return null;
        }
        return expression.getArgumentList().getExpressions()[listenerParameterPosition];
    }

    static PsiClass getCallbackContainingClass(PsiExpression listenerExpression) {
        PsiClass psiClass = null;
        if (listenerExpression instanceof PsiThisExpression) {
            psiClass = PsiTreeUtil.getParentOfType(listenerExpression, PsiClass.class);
        } else if (listenerExpression instanceof  PsiNewExpression) {
            psiClass = ((PsiNewExpression) listenerExpression).getAnonymousClass();
        } else {
            if (listenerExpression.getReference() == null) {
                return null;
            }
            PsiElement listenerDeclaration = listenerExpression.getReference().resolve();
            PsiNewExpression newExpression = PsiTreeUtil.getChildOfType(listenerDeclaration, PsiNewExpression.class);
            if (newExpression != null && PsiTreeUtil.getChildOfType(newExpression, PsiAnonymousClass.class) != null) {
                assert PsiTreeUtil.getChildrenOfType(newExpression, PsiAnonymousClass.class).length == 1;
                psiClass = PsiTreeUtil.getChildOfType(newExpression, PsiAnonymousClass.class);
            }
            if (psiClass == null && listenerDeclaration != null) {
                PsiReference[] references = ReferencesSearch.search(listenerDeclaration).findAll().toArray(new PsiReference[0]);
                for (PsiReference reference : references) {
                    PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiAssignmentExpression.class);
                    if (assignmentExpression != null) {
                         if (assignmentExpression.getRExpression() instanceof PsiNewExpression) {
                             newExpression = (PsiNewExpression) assignmentExpression.getRExpression();
                             if (PsiTreeUtil.getChildOfType(newExpression, PsiAnonymousClass.class) != null) {
                                 assert PsiTreeUtil.getChildrenOfType(newExpression, PsiAnonymousClass.class).length == 1;
                                 psiClass = PsiTreeUtil.getChildOfType(newExpression, PsiAnonymousClass.class);
                                 break;
                             }
                         }
                    }
                }
            }
        }
        if (psiClass == null) {
            if (listenerExpression.getType() == null) {
                return null;
            }
            psiClass = JavaPsiFacade.getInstance(listenerExpression.getProject()).findClass(
                    listenerExpression.getType().getCanonicalText(), listenerExpression.getResolveScope());
        }
        return psiClass;
    }

    /**
     * This method searches a PsiClass for a specific callback and returns the data within that callback.
     *
     * @param psiClass The class the API should be in
     * @param targetListenerType The full API name for the listener we are looking at
     * @param callbackName The name of the callback we are looking for
     * @param dataInCallbackParameterPosition The parameter number of the data we are concerned with within the callback
     * @return A PsiElement of data from one of the callback parameters
     */
    static PsiElement getDataEntityFromClass(PsiClass psiClass, String targetListenerType, String callbackName, int dataInCallbackParameterPosition) {
        while (true) {
            if (psiClass == null) {
                return null;
            }
            String psiClassTypeCanonicalText = JavaPsiFacade.getElementFactory(psiClass.getProject()).createType(psiClass).getCanonicalText();
            boolean match = Pattern.compile(targetListenerType, Pattern.DOTALL).matcher(psiClassTypeCanonicalText).matches();
            for (PsiType type : psiClass.getSuperTypes()) {
                if (targetListenerType.equals(type.getCanonicalText())) {
                    match = true;
                }
            }
            if (match) {
                break;
            }
            psiClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class);
        }
        // TODO (double-check) figure out what the second (boolean) parameter means
        //Elijah's comment - for the boolean - if true, the methods are also searched in the base classes of the class.
        PsiMethod[] callbackMethod = psiClass.findMethodsByName(callbackName, false);
        for (PsiMethod method : callbackMethod) {
            //TODO: This method of iterating through the methods currently only allows us to compare methods by their name and number of parameters. It would be more effective to compare the methods as we do in the standard inspection methods, but this works for known cases right now.
            PsiParameterList parameterList = method.getParameterList();
            //If we have multiple methods with the same name, this prevents us from encountering errors by trying to access data that doesn't exist
            if(parameterList.getParametersCount() <= dataInCallbackParameterPosition) continue;
            //Returns the data in the parameter list
            return parameterList.getParameters()[dataInCallbackParameterPosition];
        }
        //Reaching this statement means there were no matching methods within the PsiClass with the data we were looking for
        return null;
    }

    static PsiElement getDataEntityFromSource(PsiElement source, int listenerParameterPosition,
                                              String targetListenerType, String callbackName,
                                              int dataInCallbackParameterPosition) {
        PsiExpression listenerExpression = TargetVariableTrackerUtil.getListener(source, listenerParameterPosition);
        if (listenerExpression instanceof PsiLambdaExpression) {
            PsiLambdaExpression lambdaExpression = (PsiLambdaExpression) listenerExpression;
            return lambdaExpression.getParameterList().getParameter(0);
        }
        PsiClass psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(listenerExpression);
        if (psiClass != null) {
            return getDataEntityFromClass(psiClass, targetListenerType, callbackName, dataInCallbackParameterPosition);
        }
        return null;
    }

    public static PsiElement getResolvedVariable(PsiElement element) {
        PsiElement resolvedVariable = null;
        if (element instanceof PsiParameter || element instanceof PsiLocalVariable || element instanceof PsiField) {
            resolvedVariable = element;
        } else if (element instanceof PsiReferenceExpression) {
            resolvedVariable = ((PsiReferenceExpression)element).resolve();
        } else if (PsiTreeUtil.getParentOfType(element, PsiStatement.class) != null) {
            PsiStatement statement = PsiTreeUtil.getParentOfType(element, PsiStatement.class);
            if (statement instanceof PsiDeclarationStatement) {
                assert PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class) != null;
                resolvedVariable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
            } else if (statement instanceof PsiExpressionStatement) {
                if (PsiTreeUtil.getParentOfType(element, PsiAssignmentExpression.class) != null) {
                    PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(element, PsiAssignmentExpression.class);
                    assert PsiTreeUtil.getChildOfType(assignmentExpression, PsiReferenceExpression.class) != null;
                    //noinspection ConstantConditions
                    resolvedVariable = PsiTreeUtil.getChildOfType(assignmentExpression, PsiReferenceExpression.class).resolve();
                }
            }
        } else if (PsiTreeUtil.getParentOfType(element, PsiField.class) != null) {
            resolvedVariable = PsiTreeUtil.getParentOfType(element, PsiField.class);
        }
        return resolvedVariable;
    }
}
