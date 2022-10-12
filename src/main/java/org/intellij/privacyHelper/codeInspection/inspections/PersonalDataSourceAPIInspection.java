package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculationLevel;
import org.intellij.privacyHelper.codeInspection.quickfixes.AddPreFilledAnnotationByTypeQuickfix;
import org.intellij.privacyHelper.codeInspection.quickfixes.DeclareVariableQuickfix;
import org.intellij.privacyHelper.codeInspection.quickfixes.NavigateToCodeQuickfix;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.*;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;


/**
 * Created by tianshi on 4/10/17.
 */
// Only for internal
public class PersonalDataSourceAPIInspection extends LocalInspectionTool {

    static private ArrayList<SensitiveAPI> APIList = new ArrayList<>();

    /**
     * Default constructor for objects of type PersonalDataSourceAPIInspection
     */
    public PersonalDataSourceAPIInspection() {
        super();
        //Get an array of APIs that are not third party and are part of the personal data groups in the parameter.
        if (APIList.isEmpty()) {
            APIList.addAll(Arrays.asList(PersonalDataSourceAPIList.getAPIList(false)));
        }
    }

    @NotNull
    @Override
    public String getShortName() { return "PersonalDataSourceAPIInspection"; }

    /**
     *
     *
     * @param holder
     * @param element
     */
    synchronized
    static public void MyVisitFunction(@Nullable ProblemsHolder holder, PsiElement element) {
        if (CodeInspectionUtil.isInTestFile(element)) {
            return;
        }

        Project openProject = element.getProject();
        for (SensitiveAPI api : APIList) {
            if (api.psiElementMethodCallMatched(element)) {
                boolean hasError = false;
                boolean hasWarning = false;
                String todoDescription = "";
                // First try to extract the target variable that receives the return value
                PsiElement resolvedTargetVariable = api.getResolvedTargetVariable(element);
                PsiElement targetVariable = api.getTargetVariable(element);
                AnnotationSpeculation [] annotationsSpeculatedFromAPICall =
                        api.createAnnotationInferencesFromSource(element);

                if (annotationsSpeculatedFromAPICall.length == 0) {
                    // If there isn't an annotation expected for this API, it means this API doesn't collect any
                    // personal data. This may happen for APIs like startActivityForResult that are not always used for
                    // collecting personal data.
                    return;
                }

                PsiElement warningElement = null;
                if (element instanceof PsiMethod) {
                    warningElement = ((PsiMethod) element).getNameIdentifier();
                } else if (element instanceof PsiMethodCallExpression) {
                    warningElement = ((PsiMethodCallExpression) element).getMethodExpression();
                }
                if (warningElement == null) {
                    warningElement = element;
                }

                ArrayList<PsiAnnotation> annotationInstanceArrayList = new ArrayList<>();
                ArrayList<AnnotationHolder> annotationHolderInstanceArrayList = new ArrayList<>();
                ArrayList<AnnotationSpeculation> annotationSpeculationArrayList = new ArrayList<>();

                // If the target variable does not exist, require the developer to create one, or use the quickfixes to generate one.
                if (resolvedTargetVariable == null) {
                    if (!api.targetVariableFromCallback() && !api.targetVariableFromIntent()) {
                        // This is only possible when the data is used in an expression
                        if (targetVariable instanceof PsiExpression) {
                            hasError = true;
                            todoDescription = String.format("Define a variable to hold \"%s\" for annotating data access by API call ",
                                    targetVariable.getText());
                            if (holder != null) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR,
                                        new DeclareVariableQuickfix("temp_var", (PsiExpression) targetVariable));
                            }
                        } else {
                            hasError = true;
                            todoDescription = String.format("Define a variable to hold \"%s\" for annotating data access by API call ",
                                    targetVariable.getText());
                            if (holder != null) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, null);
                            }
                        }
                    }
                    CodeInspectionUtil.fillAnnotationList(
                            annotationHolderInstanceArrayList, annotationInstanceArrayList,
                            annotationSpeculationArrayList, null, null,
                            annotationsSpeculatedFromAPICall);

                    PrivacyPracticesHolder.getInstance(openProject).addSensitiveAPIInstance(element,
                            annotationInstanceArrayList.toArray(new PsiAnnotation[0]),
                            annotationsSpeculatedFromAPICall,
                            annotationHolderInstanceArrayList.toArray(new AnnotationHolder[0]),
                            api, hasError, hasWarning, todoDescription);
                    return;
                }

                SmartPsiElementPointer<PsiElement> resolvedTargetVariablePointer =
                        SmartPointerManager.createPointer(resolvedTargetVariable);
                PsiAnnotation [] annotations = CodeInspectionUtil.getAllAnnotations(resolvedTargetVariable);
                List<LocalQuickFix> allAddAnnotationQuickfixes = new ArrayList<>();
                Set<String> dataGroupSuggestions = new HashSet<>();
                for (AnnotationSpeculation annotationSpeculation : annotationsSpeculatedFromAPICall) {
                    for (AnnotationHolder speculationHolder : annotationSpeculation.getSpeculations()) {
                        dataGroupSuggestions.addAll(
                                Arrays.asList(Arrays.stream(speculationHolder.getDataTypes()).map(
                                        dataType -> String.format("DataType.%s", dataType)).toArray(String[]::new)));
                    }
                }
                AnnotationHolder preFilledAnnotation =
                        CodeInspectionUtil.createEmptyAnnotationHolderByType(DataAccess);
                preFilledAnnotation.clear(fieldDataAccessDataType);
                for (String dataGroupSuggestion : dataGroupSuggestions) {
                    preFilledAnnotation.add(fieldDataAccessDataType, dataGroupSuggestion);
                }
                allAddAnnotationQuickfixes.add(new AddPreFilledAnnotationByTypeQuickfix(
                        SmartPointerManager.createPointer(resolvedTargetVariable),
                        preFilledAnnotation));


                if (CodeInspectionUtil.containsConflictAnnotation(annotations)) {
                    CodeInspectionUtil.fillAnnotationList(
                            annotationHolderInstanceArrayList, annotationInstanceArrayList,
                            annotationSpeculationArrayList, null, null,
                            annotationsSpeculatedFromAPICall);
                    hasError = true;
                    todoDescription = String.format("Fix the conflict annotations",
                            targetVariable.getText());
                    Objects.requireNonNull(holder).registerProblem(warningElement,
                            "Has conflict annotations",
                            ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                } else if (annotations.length > 0) {
                    PsiAnnotation sourceAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, CoconutAnnotationType.DataAccess);
                    PsiAnnotation multiSourceAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, CoconutAnnotationType.MultipleAccess);
                    PsiAnnotation noPersonalDataCollectedAnnotation =
                            CodeInspectionUtil.getAnnotationByType(resolvedTargetVariable, CoconutAnnotationType.NotPersonalDataAccess);

                    if (sourceAnnotation != null && multiSourceAnnotation != null) {
                        // request developers to fix format first
                        CodeInspectionUtil.fillAnnotationList(
                                annotationHolderInstanceArrayList, annotationInstanceArrayList,
                                annotationSpeculationArrayList, null, null,
                                annotationsSpeculatedFromAPICall);

                        hasError = true;
                        todoDescription = String.format("Merge multiple @%s into a @%s",
                                DataTransmission, MultipleTransmission);
                        if (holder != null) {
                            holder.registerProblem(warningElement,
                                    String.format("Multiple @%s should be merged into @%s", DataAccess, MultipleAccess),
                                    ProblemHighlightType.GENERIC_ERROR, (LocalQuickFix) null);
                        }
                    } else if (sourceAnnotation != null || multiSourceAnnotation != null) {
                        ArrayList<PsiAnnotation> sourceAnnotations = new ArrayList<>();
                        if (sourceAnnotation != null) {
                            sourceAnnotations.add(sourceAnnotation);
                        } else {
                            sourceAnnotations.addAll(CodeInspectionUtil.unpackListAnnotation(multiSourceAnnotation));
                        }

                        for (PsiAnnotation annotationInstance : sourceAnnotations) {
                            boolean matchAnySpeculation = false;
                            for (AnnotationSpeculation annotationSpeculation : annotationsSpeculatedFromAPICall) {
                                for (AnnotationHolder speculationHold : annotationSpeculation.getSpeculations()) {
                                    if (CodeInspectionUtil.isMatchedSource(speculationHold, annotationInstance)) {
                                        matchAnySpeculation = true;
                                        break;
                                    }
                                }
                            }
                            if (!matchAnySpeculation) {
                                annotationHolderInstanceArrayList.add(
                                        CodeInspectionUtil.parseAnnotation(annotationInstance));
                                annotationInstanceArrayList.add(annotationInstance);
                                annotationSpeculationArrayList.add(null);
                            }
                        }

                        boolean hasAddAnnotationQuickfix = false;

                        for (AnnotationSpeculation annotationSpeculation : annotationsSpeculatedFromAPICall) {
                            List<PsiAnnotation> matchedAnnotationInstanceList = new ArrayList<>();
                            List<AnnotationHolder> matchedAnnotationInstanceHolderList = new ArrayList<>();
                            for (PsiAnnotation annotationInstance : sourceAnnotations) {
                                for (AnnotationHolder speculationHold : annotationSpeculation.getSpeculations()) {
                                    if (CodeInspectionUtil.isMatchedSource(speculationHold, annotationInstance)) {
                                        matchedAnnotationInstanceList.add(annotationInstance);
                                        matchedAnnotationInstanceHolderList.add(
                                                CodeInspectionUtil.parseAnnotation(annotationInstance));
                                        break;
                                    }
                                }
                            }

                            if (matchedAnnotationInstanceHolderList.isEmpty() &&
                                    annotationSpeculation.getSpeculationLevel() ==
                                            AnnotationSpeculationLevel.AT_LEAST_ONE_REQUIRED) {
                                annotationHolderInstanceArrayList.add(null);
                                annotationInstanceArrayList.add(null);
                                annotationSpeculationArrayList.add(annotationSpeculation);
                                // If one type of annotation in the speculation is missing, then this is treated as an error (Because we can have very high confidence in our speculation for third party libraries)
                                hasError = true;
                                todoDescription = String.format(CoconutUIUtil.AddAnnotationTodoTemplate,
                                        CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable));
                                if (holder != null) {
                                    if (!hasAddAnnotationQuickfix) {
                                        holder.registerProblem(warningElement,
                                                String.format(
                                                        SOURCE_ANNOTATION_REQUIRED, CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable),
                                                        annotationSpeculation.getAccessDataTypeDescription()),
                                                ProblemHighlightType.GENERIC_ERROR,
                                                allAddAnnotationQuickfixes.toArray(LocalQuickFix[]::new));
                                        hasAddAnnotationQuickfix = true;
                                    } else {
                                        holder.registerProblem(warningElement,
                                                String.format(
                                                        SOURCE_ANNOTATION_REQUIRED, CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable),
                                                        annotationSpeculation.getAccessDataTypeDescription()),
                                                ProblemHighlightType.GENERIC_ERROR,
                                                (LocalQuickFix) null);
                                    }
                                }
                            } else {
                                for (int i = 0 ; i < matchedAnnotationInstanceList.size() ; i++) {
                                    PsiAnnotation psiAnnotation = matchedAnnotationInstanceList.get(i);
                                    AnnotationHolder annotationHolder = matchedAnnotationInstanceHolderList.get(i);

                                    annotationHolderInstanceArrayList.add(annotationHolder);
                                    annotationInstanceArrayList.add(psiAnnotation);
                                    annotationSpeculationArrayList.add(annotationSpeculation);

                                    boolean correctSourceAnnotation =
                                            CodeInspectionUtil.checkAnnotationCorrectnessByType(psiAnnotation, null);
                                    SmartPsiElementPointer<PsiElement> annotationInstanceOfTypePointer =
                                            SmartPointerManager.createPointer(psiAnnotation);

                                    if (!correctSourceAnnotation) {
                                        hasError = true;
                                        todoDescription = String.format("Fix errors in @%s",
                                                annotationHolder.mAnnotationType);
                                        if (holder != null) {
                                            holder.registerProblem(warningElement, INCORRECT_ANNOTATION,
                                                    ProblemHighlightType.GENERIC_ERROR,
                                                    new NavigateToCodeQuickfix(annotationInstanceOfTypePointer,
                                                            annotationHolder.getAnnotationShortSummary(
                                                                    psiAnnotation.getProject(), true)));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (noPersonalDataCollectedAnnotation != null) {
                        CodeInspectionUtil.fillAnnotationList(
                                annotationHolderInstanceArrayList, annotationInstanceArrayList,
                                annotationSpeculationArrayList,
                                CodeInspectionUtil.parseAnnotation(noPersonalDataCollectedAnnotation),
                                noPersonalDataCollectedAnnotation, annotationsSpeculatedFromAPICall);
                    } else {
                        // no @DataAccess, @MultipleAccess or @NotPersonalDataAccess detected
                        hasError = true;
                        todoDescription = String.format(CoconutUIUtil.AddAnnotationTodoTemplate,
                                CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable));
                        missingSourceAnnotation(annotationHolderInstanceArrayList, annotationInstanceArrayList,
                                annotationSpeculationArrayList, annotationsSpeculatedFromAPICall,
                                holder, warningElement, resolvedTargetVariable,
                                allAddAnnotationQuickfixes.toArray(LocalQuickFix[]::new));
                    }
                } else {
                    // no annotation detected
                    hasError = true;
                    todoDescription = String.format(CoconutUIUtil.AddAnnotationTodoTemplate,
                            CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable));
                    missingSourceAnnotation(annotationHolderInstanceArrayList, annotationInstanceArrayList,
                            annotationSpeculationArrayList, annotationsSpeculatedFromAPICall,
                            holder, warningElement, resolvedTargetVariable,
                            allAddAnnotationQuickfixes.toArray(LocalQuickFix[]::new));
                }

                PrivacyPracticesHolder.getInstance(openProject).addSensitiveAPIInstance(element,
                        annotationInstanceArrayList.toArray(new PsiAnnotation[0]),
                        annotationSpeculationArrayList.toArray(new AnnotationSpeculation[0]),
                        annotationHolderInstanceArrayList.toArray(new AnnotationHolder[0]),
                        api, hasError, hasWarning, todoDescription);
            }
        }

    }

    private static void missingSourceAnnotation(ArrayList<AnnotationHolder> annotationHolderInstanceArrayList,
                                                ArrayList<PsiAnnotation> annotationInstanceArrayList,
                                                ArrayList<AnnotationSpeculation> annotationSpeculationArrayList,
                                                AnnotationSpeculation[] annotationsSpeculatedFromAPICall,
                                                @Nullable ProblemsHolder holder, PsiElement warningElement,
                                                PsiElement resolvedTargetVariable, LocalQuickFix [] quickFixes) {
        CodeInspectionUtil.fillAnnotationList(
                annotationHolderInstanceArrayList, annotationInstanceArrayList, annotationSpeculationArrayList,
                null, null, annotationsSpeculatedFromAPICall);
        ArrayList<String> missingData = new ArrayList<>();
        for (AnnotationSpeculation speculatedAnnotation : annotationSpeculationArrayList) {
            missingData.add(speculatedAnnotation.getAccessDataTypeDescription());
        }
        if (holder != null) {
            holder.registerProblem(warningElement,
                    String.format(SOURCE_ANNOTATION_REQUIRED,
                            CodeInspectionUtil.getTargetVariableNameString(resolvedTargetVariable),
                            String.join("; ", missingData)),
                    ProblemHighlightType.GENERIC_ERROR, quickFixes);
        }
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethod(PsiMethod method) {
                MyVisitFunction(holder, method);
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                MyVisitFunction(holder, expression);
            }
        };
    }
}
