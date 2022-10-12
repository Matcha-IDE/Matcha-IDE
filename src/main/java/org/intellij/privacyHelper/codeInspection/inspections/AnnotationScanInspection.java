package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElementVisitor;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.NotNull;

public class AnnotationScanInspection extends LocalInspectionTool {
    @NotNull
    @Override
    public String getShortName() { return "AnnotationScanInspection"; }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotation);
                if (annotationHolder.mAnnotationType != CoconutAnnotationType.DataAccess &&
                        annotationHolder.mAnnotationType != CoconutAnnotationType.DataTransmission) {
                    return;
                }
                Project openProject = annotation.getProject();
                PrivacyPracticesHolder.getInstance(openProject).addAnnotationInstance(
                        annotation, annotationHolder);
                CodeInspectionUtil.checkAnnotationCorrectnessByType(annotation, holder);
            }
        };
    }
}
