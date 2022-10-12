package org.intellij.privacyHelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.intellij.privacyHelper.codeInspection.utils.quickfixDialogs.DataTransmissionSelectorDialog;
import org.intellij.privacyHelper.codeInspection.utils.quickfixDialogs.DataTypeSelectorDialog;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.DataAccess;
import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.DataTransmission;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 12/27/17.
 */
public class AddPreFilledAnnotationByTypeQuickfix implements LocalQuickFix {
    private final String ADD_ANNOTATION_QUICKFIX;
    private AnnotationHolder preFilledAnnotation;
    private final SmartPsiElementPointer<PsiElement> targetVariablePointer;

    public AddPreFilledAnnotationByTypeQuickfix(SmartPsiElementPointer<PsiElement> targetVariablePointer,
                                                AnnotationHolder preFilledAnnotation) {
        this.targetVariablePointer = targetVariablePointer;
        this.preFilledAnnotation = preFilledAnnotation;
//        assert preFilledAnnotation.mAnnotationType == DataTransmission || preFilledAnnotation.mAnnotationType == DataAccess;
        this.ADD_ANNOTATION_QUICKFIX = String.format("Annotate data %s behavior",
                preFilledAnnotation.mAnnotationType == DataAccess ? "access" : "transmission");
    }


    @Nls
    @NotNull
    @Override
    public String getName() {
        return ADD_ANNOTATION_QUICKFIX;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "personal data API inspection quickfixes";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        if (targetVariablePointer.getElement() == null) {
            return;
        }
        // show a dialog GUI showing a list of data types to choose from
        // if the user chooses one, add the annotation to the modifier list
        if (preFilledAnnotation.mAnnotationType == DataAccess) {
            PersonalDataGroup[] personalDataGroups = Arrays.stream(preFilledAnnotation.getDataTypes())
                    .map(PersonalDataGroup::valueOf)
                    .toArray(PersonalDataGroup[]::new);
            DataTypeSelectorDialog dialog = new DataTypeSelectorDialog(project, personalDataGroups,
                    new DataTypeSelectorDialog.Callback() {
                @Override
                public void onOk(PersonalDataGroup[] selectedDataTypes) {
                    if (selectedDataTypes.length == 0) {
                        preFilledAnnotation =
                                CodeInspectionUtil.createEmptyAnnotationHolderByType(
                                        CoconutAnnotationType.NotPersonalDataAccess);
                    } else {
                        preFilledAnnotation =
                                CodeInspectionUtil.createEmptyAnnotationHolderByType(
                                        DataAccess);
                        preFilledAnnotation.clear(fieldDataAccessDataType);
                        for (PersonalDataGroup dataType : selectedDataTypes) {
                            preFilledAnnotation.add(fieldDataAccessDataType, String.format("DataType.%s", dataType));
                        }
                    }
                    WriteCommandAction.runWriteCommandAction(project, () -> completeFix());
                }

                @Override
                public void onCancel() {
                    return;
                }
            });
            ApplicationManager.getApplication().invokeLater(dialog::showAndGet);
        } else if (preFilledAnnotation.mAnnotationType == DataTransmission) {
            DataTransmissionSelectorDialog dialog = new DataTransmissionSelectorDialog(project, preFilledAnnotation,
                    new DataTransmissionSelectorDialog.Callback() {
                        @Override
                        public void onOk(AnnotationHolder filledAnnotation) {
                            preFilledAnnotation = filledAnnotation;
                            WriteCommandAction.runWriteCommandAction(project, () -> completeFix());
                        }
                        @Override
                        public void onCancel() {
                            return;
                        }
                    });
            ApplicationManager.getApplication().invokeLater(dialog::showAndGet);
        }
    }

    private void completeFix() {
        PsiModifierList modifierList = PsiTreeUtil.getChildOfType(
                targetVariablePointer.getElement(), PsiModifierList.class);
        if (modifierList == null) {
            PsiElement currentElement = targetVariablePointer.getElement();
            do {
                currentElement = PsiTreeUtil.getPrevSiblingOfType(currentElement, currentElement.getClass());
            } while (PsiTreeUtil.getChildOfType(currentElement, PsiModifierList.class) == null);
            PsiTypeElement psiTypeElement = PsiTreeUtil.getChildOfType(currentElement, PsiTypeElement.class);
            PsiElementFactory psiElementFactory = PsiElementFactory.getInstance(targetVariablePointer.getElement().getProject());
            PsiElement newElement;
            if (targetVariablePointer.getElement() instanceof PsiField) {
                PsiField psiField = psiElementFactory.createField(
                        ((PsiField) targetVariablePointer.getElement()).getNameIdentifier().getText(),
                        psiTypeElement.getType());
                modifierList = psiField.getModifierList();
                modifierList.addAnnotation(preFilledAnnotation.getAnnotationString(""));
                newElement = psiField;
            } else {
                assert targetVariablePointer.getElement() instanceof PsiLocalVariable;
                PsiDeclarationStatement psiDeclarationStatement =
                        psiElementFactory.createVariableDeclarationStatement(
                                ((PsiLocalVariable) targetVariablePointer.getElement()).getNameIdentifier().getText(),
                                psiTypeElement.getType(), null);
                PsiLocalVariable psiLocalVariable = (PsiLocalVariable) psiDeclarationStatement.getDeclaredElements()[0];
                modifierList = psiLocalVariable.getModifierList();
                psiLocalVariable.getModifierList().addAnnotation(preFilledAnnotation.getAnnotationString(""));
                newElement = psiDeclarationStatement;
                currentElement = PsiTreeUtil.getParentOfType(targetVariablePointer.getElement(),
                        PsiDeclarationStatement.class);
            }
            currentElement.getParent().addBefore(newElement, currentElement);
            currentElement.getParent().addBefore(Objects.requireNonNull(CoconutUIUtil.nl(currentElement)), currentElement);
            PsiElement prevSameTypeSibling = PsiTreeUtil.getPrevSiblingOfType(targetVariablePointer.getElement(),
                    targetVariablePointer.getElement().getClass());
            if (targetVariablePointer.getElement().getChildren().length > 1) {
                if (targetVariablePointer.getElement() instanceof PsiField) {
                    targetVariablePointer.getElement().getParent().deleteChildRange(prevSameTypeSibling.getNextSibling(),
                            targetVariablePointer.getElement().getPrevSibling());
                    targetVariablePointer.getElement().deleteChildRange(targetVariablePointer.getElement().getFirstChild(),
                            targetVariablePointer.getElement().getLastChild().getPrevSibling());
                    // FIXME: using this method will generate a new line between the previous variable (the new last var)
                    // and the semicolon.
                } else {
                    targetVariablePointer.getElement().getParent().deleteChildRange(prevSameTypeSibling.getNextSibling(),
                            targetVariablePointer.getElement());
                    // generate the semicolon token because it was removed with the last variable in this group.
                    PsiDeclarationStatement temporaryStatement =
                            psiElementFactory.createVariableDeclarationStatement("i",
                                    psiTypeElement.getType(), null);
                    PsiElement semicolonToken = temporaryStatement.getDeclaredElements()[0].getLastChild();
                    currentElement.getParent().addAfter(semicolonToken, currentElement);
                }
            } else {
                targetVariablePointer.getElement().getParent().deleteChildRange(prevSameTypeSibling.getNextSibling(),
                        targetVariablePointer.getElement());
            }

            navigateToCodeAndShowTooltip(preFilledAnnotation, currentElement, modifierList);

        } else {
            applyFixToModifierList(modifierList, preFilledAnnotation);
            navigateToCodeAndShowTooltip(preFilledAnnotation, modifierList, modifierList);
        }
    }

    public static void applyFixToModifierList(PsiModifierList modifierList, AnnotationHolder preFilledAnnotation) {
        String annotationString = preFilledAnnotation.getAnnotationString("");

        // FIXME: if an annotation already exists, replace the current annotation with a @Multi...annotation
//        ArrayList<String> sourceStringList = new ArrayList<>();
//        for (AnnotationHolder annotationHolder : annotationHolders) {
//            sourceStringList.add("@" + annotationHolder.getAnnotationString(""));
//        }
//        annotationString = String.format("%s.%s({\n%s})", ANNOTATION_PKG, MultipleTransmission,
//                String.join(",\n", sourceStringList));

        if (modifierList != null) {
            modifierList.addAnnotation(annotationString);
            modifierList.addAfter(Objects.requireNonNull(CoconutUIUtil.nl(modifierList)),
                    modifierList.getAnnotations()[0]);
            JavaCodeStyleManager.getInstance(modifierList.getProject())
                    // Tell it to shorten all class references accordingly
                    .shortenClassReferences(modifierList);
        }
    }

    public static void navigateToCodeAndShowTooltip(AnnotationHolder preFilledAnnotation,
                                                    PsiElement navigationTargetElement, PsiModifierList modifierList) {
        CoconutUIUtil.navigateMainEditorToPsiElement(Objects.requireNonNull(navigationTargetElement), ()-> {
            if (preFilledAnnotation.mAnnotationType == DataAccess) {
                String accessText = "Data access is not considered data collection or sharing until the data is later " +
                        "transmitted out of the device or shared with another app.";
                String idHeader = "Pick a unique id for this access";
                String idText = "You need to create a resource string as a unique id because you will refer to this " +
                        "access later. Complete the name (e.g., R.string.privacy_location_for_weather) and then " +
                        "define the string value using quickfix.";
                PsiAnnotation annotation = null;
                PsiNameValuePair idAttribute = null;
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(psiAnnotation) == DataAccess) {
                        annotation = psiAnnotation;
                        break;
                    }
                }
                // find the attribute of annotation with the name "id"
                if (annotation != null) {
                    for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                        if (fieldDataAccessId.equals(nameValuePair.getName())) {
                            idAttribute = nameValuePair;
                            break;
                        }
                    }
                }
                CoconutUIUtil.showTooltip(new TooltipInfo[] {
                        new TooltipInfo("matcha.features.access_overview_test", "", accessText,
                                Balloon.Position.atLeft, modifierList.getProject(),
                                annotation == null ? modifierList : annotation),
                        new TooltipInfo("matcha.features.access_id_test", idHeader, idText,
                                Balloon.Position.atLeft, modifierList.getProject(),
                                idAttribute == null ? modifierList : idAttribute)
                });
            } else if (preFilledAnnotation.mAnnotationType == DataTransmission) {
                String transmissionText = "Data transmission may be considered data collection or data sharing. This " +
                        "annotation will guide Matcha to check whether certain exemption rules apply.";
                String accessIdHeader = "Access id of data sources";
                String accessIdText = "Access ids of the data source that you are transmitting " +
                        "data from. This helps Matcha determine what types of data are collected/shared here.";
                String collectionHeader = "Fill in collection attributes";
                String dataCollectionText = "Details about data collection. " +
                        "This helps Matcha determine whether this transmission can be exempt from data collection. ";
                String sharingHeader = "Fill in sharing attributes";
                String dataSharingText = "Details about data sharing. " +
                        "This helps Matcha determine whether this transmission can be exempt from data sharing. ";
                PsiAnnotation annotation = null;
                PsiNameValuePair idListAttribute = null;
                PsiNameValuePair collectionAttribute = null;
                PsiNameValuePair sharingAttribute = null;
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(psiAnnotation) == DataTransmission) {
                        annotation = psiAnnotation;
                        break;
                    }
                }
                // find the attribute of annotation with the name "id"
                if (annotation != null) {
                    for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                        if (fieldDataTransmissionAccessIdList.equals(nameValuePair.getName())) {
                            idListAttribute = nameValuePair;
                        }
                        if (fieldDataTransmissionCollectionAttributeList.equals(nameValuePair.getName())) {
                            collectionAttribute = nameValuePair;
                        }
                        if (fieldDataTransmissionSharingAttributeList.equals(nameValuePair.getName())) {
                            sharingAttribute = nameValuePair;
                        }
                    }
                }
                CoconutUIUtil.showTooltip(new TooltipInfo[] {
                        new TooltipInfo("matcha.features.transmission_overview_test", "", transmissionText,
                                Balloon.Position.atLeft,
                                modifierList.getProject(), annotation == null ? modifierList : annotation),
                        new TooltipInfo("matcha.features.transmission_id_test", accessIdHeader, accessIdText,
                                Balloon.Position.atLeft,
                                modifierList.getProject(),
                                idListAttribute == null ? modifierList : idListAttribute),
                        new TooltipInfo("matcha.features.transmission_collection_test", collectionHeader,
                                dataCollectionText,
                                Balloon.Position.atLeft,
                                modifierList.getProject(),
                                collectionAttribute == null ? modifierList : collectionAttribute),
                        new TooltipInfo("matcha.features.transmission_sharing_test", sharingHeader, dataSharingText,
                                Balloon.Position.atLeft,
                                modifierList.getProject(),
                                sharingAttribute == null ? modifierList : sharingAttribute)
                });
            }
        });
    }
}
