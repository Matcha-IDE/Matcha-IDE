package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiEmptyExpressionImpl;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.quickfixes.AddPreFilledAnnotationByTypeQuickfix;
import org.intellij.privacyHelper.codeInspection.quickfixes.DeclareVariableQuickfix;
import org.intellij.privacyHelper.codeInspection.quickfixes.NavigateToCodeQuickfix;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.DataTransmission;
import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.MultipleTransmission;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 2/2/18.
 */
public class PersonalDataSinkAPIInspection extends LocalInspectionTool {
    static private final ArrayList<SensitiveAPI> APIList = new ArrayList<>();

    public PersonalDataSinkAPIInspection () {
        super();
        if (APIList.isEmpty()) {
            APIList.addAll(Arrays.asList(PersonalDataSinkAPIList.getAPIList()));
        }
    }

    @NotNull
    @Override
    public String getShortName() { return "PersonalDataSinkAPIInspection"; }

    static public void MyVisitMethodCallExpression(@Nullable ProblemsHolder holder, PsiMethodCallExpression expression) {
        // 1. check if the current API call instance is in the global data holder, if not then update the global data
        // holder. Note that we don't need to parse the actual values of annotations until the current API call is in the
        // global data holder and data flow analysis suggests there might be data reaching here.
        // 2. If the current API call instance is in the global data holder, then check whether any source data reaches
        // this point.
        // 3. If so, check whether there are all the expected annotation types, and if all the data source IDs are
        // included in either the @LocalOnly annotation or the corresponding annotation type. If not, then register
        // errors to the problemsholder.
        if (CodeInspectionUtil.isInTestFile(expression)) {
            return;
        }

        PsiElement warningElement = expression.getMethodExpression();

        ArrayList<AnnotationSpeculation> annotationSpeculationArrayList = new ArrayList<>();
        ArrayList<AnnotationHolder> annotationHolderInstanceArrayList = new ArrayList<>();
        ArrayList<PsiAnnotation> annotationInstanceArrayList = new ArrayList<>();

        ArrayList<SensitiveAPI> copyOfAPIList = new ArrayList<>(APIList);
        for (SensitiveAPI api : copyOfAPIList) {
            if (!api.isValid()) {
                return;
            }
            if (api.psiElementMethodCallMatched(expression)) {
                boolean hasError = false;
                boolean hasWarning = false;
                String todoDescription = "";
                Project openProject = expression.getProject();

                PsiElement targetVariable = api.getTargetVariable(expression);
                AnnotationSpeculation [] annotationsSpeculatedFromAPICall = api.createAnnotationInferencesFromSource(expression);

                if (annotationsSpeculatedFromAPICall.length == 0) {
                    // If there isn't an annotation expected for this API, it means this API doesn't collect any
                    // personal data. This may happen for APIs like startActivityForResult that are not always used for
                    // collecting personal data.
                    return;
                }

                if (targetVariable instanceof PsiLiteralExpression) {
                    return;
                }

                PsiElement resolvedTargetVariable = api.getResolvedTargetVariable(expression);
                if (resolvedTargetVariable == null) {
                    if (targetVariable == null) {
                        // When there is no target variable available, which is usually because the corresponding parameter has not been passed in the API call, ignore and return directly.
                        return;
                    }
                    try {
                        if (targetVariable instanceof PsiExpression) {
                            hasError = true;
                            todoDescription = String.format("Define a variable to hold \"%s\" for annotating data transmission by API call ",
                                    targetVariable.getText());
                            if (holder != null && !(targetVariable instanceof PsiEmptyExpressionImpl)) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE,
                                        ProblemHighlightType.GENERIC_ERROR,
                                        new DeclareVariableQuickfix("temp_var", (PsiExpression) targetVariable));
                            }
                        } else {
                            hasError = true;
                            todoDescription = String.format("Define a variable to hold \"%s\" for annotating data transmission by API call ",
                                    targetVariable.getText());
                            if (holder != null) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE,
                                        ProblemHighlightType.GENERIC_ERROR, null);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    PrivacyPracticesHolder.getInstance(openProject).addSensitiveAPIInstance(expression, api, hasError,
                            hasWarning, todoDescription);
                    return;
                }
                ArrayList<AddPreFilledAnnotationByTypeQuickfix> addAnnotationQuickfixes = new ArrayList<>();
                SmartPsiElementPointer<PsiElement> resolvedTargetVariablePointer =
                        SmartPointerManager.createPointer(resolvedTargetVariable);
                for (AnnotationSpeculation annotationSpeculation : annotationsSpeculatedFromAPICall) {
                    for (AnnotationHolder speculationHolder : annotationSpeculation.getSpeculations()) {
                        addAnnotationQuickfixes.add(new AddPreFilledAnnotationByTypeQuickfix(
                                resolvedTargetVariablePointer, speculationHolder));
                    }
                }
                addAnnotationQuickfixes.add(new AddPreFilledAnnotationByTypeQuickfix(resolvedTargetVariablePointer,
                        CodeInspectionUtil.createEmptyAnnotationHolderByType(
                                CoconutAnnotationType.NotPersonalDataTransmission)));

                PsiAnnotation [] annotations = CodeInspectionUtil.getAllAnnotations(resolvedTargetVariable);
                if (CodeInspectionUtil.containsConflictAnnotation(annotations)) {
                    CodeInspectionUtil.fillAnnotationList(
                            annotationHolderInstanceArrayList, annotationInstanceArrayList, annotationSpeculationArrayList,
                            null, null, annotationsSpeculatedFromAPICall);
                    hasError = true;
                    todoDescription = String.format("Fix the conflict annotations",
                            targetVariable.getText());
                    Objects.requireNonNull(holder).registerProblem(warningElement, "Has conflict annotations",
                            ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                } else if (annotations.length > 0) {
                    PsiAnnotation sinkAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, DataTransmission);
                    PsiAnnotation multiSinkAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, MultipleTransmission);
                    PsiAnnotation noPersonalDataTransmittedAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, CoconutAnnotationType.NotPersonalDataTransmission);

                    if (sinkAnnotation != null && multiSinkAnnotation != null) {
                        // request developers to fix format first
                        CodeInspectionUtil.fillAnnotationList(
                                annotationHolderInstanceArrayList, annotationInstanceArrayList, annotationSpeculationArrayList,
                                null, null, annotationsSpeculatedFromAPICall);

                        hasError = true;
                        todoDescription = String.format("Merge multiple @%s into a @%s",
                                DataTransmission, MultipleTransmission);
                        if (holder != null) {
                            holder.registerProblem(warningElement,
                                    String.format("Multiple @%s should be merged into @%s", DataTransmission,
                                            MultipleTransmission),
                                    ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                        }
                    } else if (sinkAnnotation != null || multiSinkAnnotation != null) {
                        ArrayList<PsiAnnotation> sinkAnnotations = new ArrayList<>();
                        if (sinkAnnotation != null) {
                            sinkAnnotations.add(sinkAnnotation);
                        } else {
                            sinkAnnotations.addAll(CodeInspectionUtil.unpackListAnnotation(multiSinkAnnotation));
                        }
                        for (PsiAnnotation annotation : sinkAnnotations) {
                            AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotation);
                            annotationHolderInstanceArrayList.add(annotationHolder);
                            annotationInstanceArrayList.add(annotation);
                            // FIXME: correct the assumption that annotationsSpeculatedFromAPICall only has one element
                            annotationSpeculationArrayList.add(annotationsSpeculatedFromAPICall[0]);
                            boolean correctSinkAnnotation = CodeInspectionUtil.checkAnnotationCorrectnessByType(
                                    annotation, null);
                            SmartPsiElementPointer<PsiElement> annotationSmartPsiElementPointer =
                                    SmartPointerManager.createPointer(annotation);

                            if (!correctSinkAnnotation) {
                                hasError = true;
                                todoDescription = String.format("Fix errors in @%s", annotationHolder.mAnnotationType);
                                if (holder != null) {
                                    holder.registerProblem(warningElement, INCORRECT_ANNOTATION,
                                            ProblemHighlightType.GENERIC_ERROR,
                                            new NavigateToCodeQuickfix(annotationSmartPsiElementPointer,
                                                    CHECK_THE_ANNOTATION));
                                }
                            }
                        }
                    } else if (noPersonalDataTransmittedAnnotation != null) {
                        CodeInspectionUtil.fillAnnotationList(
                                annotationHolderInstanceArrayList, annotationInstanceArrayList,
                                annotationSpeculationArrayList,
                                CodeInspectionUtil.parseAnnotation(noPersonalDataTransmittedAnnotation),
                                noPersonalDataTransmittedAnnotation, annotationsSpeculatedFromAPICall);
                    } else {
                        CodeInspectionUtil.fillAnnotationList(
                                annotationHolderInstanceArrayList, annotationInstanceArrayList, annotationSpeculationArrayList,
                                null, null, annotationsSpeculatedFromAPICall);

                        hasError = true;
                        todoDescription = String.format(CoconutUIUtil.AddAnnotationTodoTemplate,
                                CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable));
                        if (holder != null) {
                            holder.registerProblem(warningElement, String.format(SINK_ANNOTATION_REQUIRED,
                                    CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable)),
                                    ProblemHighlightType.GENERIC_ERROR,
                                    addAnnotationQuickfixes.toArray(new AddPreFilledAnnotationByTypeQuickfix[0]));
                        }
                    }
                } else {
                    CodeInspectionUtil.fillAnnotationList(
                            annotationHolderInstanceArrayList, annotationInstanceArrayList, annotationSpeculationArrayList,
                            null, null, annotationsSpeculatedFromAPICall);

                    hasError = true;
                    todoDescription = String.format(CoconutUIUtil.AddAnnotationTodoTemplate,
                            CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable));
                    if (holder != null) {
                        holder.registerProblem(warningElement, String.format(SINK_ANNOTATION_REQUIRED,
                                CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable)),
                                ProblemHighlightType.GENERIC_ERROR, addAnnotationQuickfixes.toArray(
                                        new AddPreFilledAnnotationByTypeQuickfix[0]));
                    }
                }

                // Finally, update the API call instance in the global data structure
                PrivacyPracticesHolder.getInstance(openProject).addSensitiveAPIInstance(expression,
                        annotationInstanceArrayList.toArray(new PsiAnnotation[0]),
                        annotationSpeculationArrayList.toArray(new AnnotationSpeculation[0]),
                        annotationHolderInstanceArrayList.toArray(new AnnotationHolder[0]), api, hasError, hasWarning,
                        todoDescription);
            }
        }
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                MyVisitMethodCallExpression(holder, expression);
            }
        };
    }
}
