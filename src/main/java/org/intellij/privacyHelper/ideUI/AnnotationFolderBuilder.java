package org.intellij.privacyHelper.ideUI;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnnotationFolderBuilder extends FoldingBuilderEx implements DumbAware {
    static final private List<String> COCONUT_ANNOTATIONS = Arrays.asList(
            CoconutAnnotationType.DataAccess.toString(),
            CoconutAnnotationType.DataTransmission.toString()
    );

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        Collection<PsiAnnotation> annotations =
                PsiTreeUtil.findChildrenOfType(root, PsiAnnotation.class);

        for(final PsiAnnotation annotation : annotations) {
            if(isCoconutAnnotation(annotation.getText())) {
                descriptors.add(new FoldingDescriptor(annotation.getNode(), annotation.getTextRange()));
            }
        }

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    private boolean isCoconutAnnotation(String annotationText) {
        for(final String annotation : COCONUT_ANNOTATIONS) {
            if(annotationText.contains(annotation)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        String retText = "...";
        if (node.getPsi() instanceof PsiAnnotation) {
            PsiAnnotation annotation = (PsiAnnotation) node.getPsi();
            AnnotationHolder annotationHolder = CodeInspectionUtil.parseAnnotation(annotation);
            if (annotationHolder.mAnnotationType == CoconutAnnotationType.DataAccess ||
                    annotationHolder.mAnnotationType == CoconutAnnotationType.DataTransmission) {
                return annotationHolder.getAnnotationShortSummary(annotation.getProject(), false);
            }
        }
        return retText;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return true;
    }
}