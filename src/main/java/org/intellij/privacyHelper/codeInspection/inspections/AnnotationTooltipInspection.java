package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyHelper.codeInspection.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.fieldDataTransmissionAccessIdList;

/**
 * Created by tianshi on 1/18/18.
 */
public class AnnotationTooltipInspection extends LocalInspectionTool {
    @NotNull
    @Override
    public String getShortName() { return "AnnotationTooltipInspection"; }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                if (!CodeInspectionUtil.isPrivacyAnnotation(annotation)) {
                    return;
                }
                Project openProject = annotation.getProject();

                CoconutAnnotationType annotationType = CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation);
                if (annotationType == CoconutAnnotationType.DataAccess || annotationType == CoconutAnnotationType.DataTransmission) {
                    CodeInspectionUtil.checkAnnotationCorrectnessByType(annotation, null);
                }
                for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                    if (nameValuePair == null || nameValuePair.getNameIdentifier() == null || nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                        continue;
                    }
                    PsiExpression[] expressionList;
                    if (nameValuePair.getValue() instanceof PsiExpression) {
                        expressionList = new PsiExpression[]{(PsiExpression) nameValuePair.getValue()};
                    } else {
                        expressionList = PsiTreeUtil.getChildrenOfType(nameValuePair.getValue(), PsiExpression.class);
                    }
                    if (expressionList != null) {
                        for (PsiExpression exp : expressionList) {
                            String [] expChunks = exp.getText().split("\\.");
                            for (Map.Entry<String, String> entry : Constants.DESCRIPTION_MAPPING.entrySet()) {
                                boolean match = false;
                                for (String expChunk : expChunks) {
                                    if (expChunk.equals(entry.getKey())) {
                                        match = true;
                                        break;
                                    }
                                }
                                if (match) {
                                    holder.registerProblem(exp, entry.getValue(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
                                    break;
                                }
                            }
                        }
                    }
                    if (Constants.DESCRIPTION_MAPPING.containsKey(nameValuePair.getName())) {
                        String message;
                        if (fieldDataTransmissionAccessIdList.equals(nameValuePair.getName())) {
                            message = String.format(Constants.DESCRIPTION_MAPPING.get(nameValuePair.getName()),
                                    String.join(", ", CodeInspectionUtil.getAllUniqueSourceIds(annotation.getProject())));
                        } else {
                            message = Constants.DESCRIPTION_MAPPING.get(nameValuePair.getName());
                        }
                        String tooltipMessage = String.format("How to fill out %s: %s", nameValuePair.getName(), message);
                        if (PrivacyPracticesHolder.getInstance(openProject).getAnnotationFieldIsCorrect(nameValuePair.getNameIdentifier())) {
                            holder.registerProblem(nameValuePair.getNameIdentifier(), tooltipMessage, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
                        } else {
                            // If this field is incomplete, then we should set the error severity here to the same level as the incomplete annotation error, which is ProblemHighlightType.GENERIC_ERROR
                            holder.registerProblem(nameValuePair.getNameIdentifier(), tooltipMessage, ProblemHighlightType.GENERIC_ERROR, null);
                        }
                    }
                }
            }
        };
    }
}
