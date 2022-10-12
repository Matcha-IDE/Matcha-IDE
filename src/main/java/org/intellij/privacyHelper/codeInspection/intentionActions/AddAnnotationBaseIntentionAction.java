package org.intellij.privacyHelper.codeInspection.intentionActions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil;
import org.intellij.privacyHelper.codeInspection.quickfixes.AddPreFilledAnnotationByTypeQuickfix;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.intellij.privacyHelper.codeInspection.utils.quickfixDialogs.DataTransmissionSelectorDialog;
import org.intellij.privacyHelper.codeInspection.utils.quickfixDialogs.DataTypeSelectorDialog;
import org.jetbrains.annotations.NotNull;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.DataAccess;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataAccessDataType;

public abstract class AddAnnotationBaseIntentionAction extends PsiElementBaseIntentionAction {
    AnnotationHolder preFilledAnnotationHolder;

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiIdentifier) {
            element = element.getParent();
        }
        PsiModifierList modifierList = getModifierList(element);
        if (modifierList != null) {
            if (preFilledAnnotationHolder.mAnnotationType == DataAccess) {
                final AnnotationHolder[] annotationHolder = new AnnotationHolder[1];
                DataTypeSelectorDialog dialog = new DataTypeSelectorDialog(project, PersonalDataGroup.values(),
                        new DataTypeSelectorDialog.Callback() {
                            @Override
                            public void onOk(PersonalDataGroup[] selectedDataTypes) {
                                if (selectedDataTypes.length == 0) {
                                    annotationHolder[0] =
                                            CodeInspectionUtil.createEmptyAnnotationHolderByType(
                                                    CoconutAnnotationType.NotPersonalDataAccess);
                                } else {
                                    annotationHolder[0] =
                                            CodeInspectionUtil.createEmptyAnnotationHolderByType(
                                                    DataAccess);
                                    annotationHolder[0].clear(fieldDataAccessDataType);
                                    for (PersonalDataGroup dataType : selectedDataTypes) {
                                        annotationHolder[0].add(fieldDataAccessDataType, String.format("DataType.%s", dataType));
                                    }
                                }
                                WriteCommandAction.runWriteCommandAction(project,
                                        () -> {
                                            AddPreFilledAnnotationByTypeQuickfix.applyFixToModifierList(
                                                modifierList, annotationHolder[0]);
                                            AddPreFilledAnnotationByTypeQuickfix.navigateToCodeAndShowTooltip(
                                                    annotationHolder[0], modifierList, modifierList);
                                        });
                            }

                            @Override
                            public void onCancel() {
                                return;
                            }
                        });
                ApplicationManager.getApplication().invokeLater(dialog::showAndGet);
            } else {
                DataTransmissionSelectorDialog dialog = new DataTransmissionSelectorDialog(project, preFilledAnnotationHolder,
                        new DataTransmissionSelectorDialog.Callback() {
                            @Override
                            public void onOk(AnnotationHolder filledAnnotation) {
                                WriteCommandAction.runWriteCommandAction(project,
                                        () -> {
                                            AddPreFilledAnnotationByTypeQuickfix.applyFixToModifierList(
                                                    modifierList, filledAnnotation);
                                            AddPreFilledAnnotationByTypeQuickfix.navigateToCodeAndShowTooltip(
                                                    filledAnnotation, modifierList, modifierList);
                                        });
                            }
                            @Override
                            public void onCancel() {
                                return;
                            }
                        });
                ApplicationManager.getApplication().invokeLater(dialog::showAndGet);
            }
        }
    }

    private PsiModifierList getModifierList(PsiElement element) {
        PsiModifierList modifierList = PsiTreeUtil.getChildOfType(element, PsiModifierList.class);
        if (modifierList == null) {
            PsiElement resolvedElement = TargetVariableTrackerUtil.getResolvedVariable(element);
            if (resolvedElement == null) {
                return null;
            }
            modifierList = PsiTreeUtil.getChildOfType(resolvedElement, PsiModifierList.class);
        }
        return modifierList;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        if (element instanceof PsiIdentifier) {
            element = element.getParent();
        }
        PsiModifierList modifierList = getModifierList(element);
        if (modifierList == null) {
            return false;
        }
        setText(String.format("Annotate data %s behavior",
                preFilledAnnotationHolder.mAnnotationType == DataAccess ? "access" : "transmission"));
        return true;
    }
}
