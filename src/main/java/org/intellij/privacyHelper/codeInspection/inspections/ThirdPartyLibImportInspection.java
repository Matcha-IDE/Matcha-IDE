package org.intellij.privacyHelper.codeInspection.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.GroovyLocalInspectionTool;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;

public class ThirdPartyLibImportInspection extends GroovyLocalInspectionTool {
    @Override
    public @NonNls @NotNull String getShortName() {
        return "ThirdPartyLibImportInspection";
    }

    public ThirdPartyLibImportInspection() {
        super();
    }

    @NotNull
    @Override
    public GroovyElementVisitor buildGroovyVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new GroovyElementVisitor() {
            @Override
            public void visitApplicationStatement(@NotNull GrApplicationStatement applicationStatement) {
                super.visitApplicationStatement(applicationStatement);
                if (applicationStatement.getCallReference() == null) {
                    return;
                }
                if (!applicationStatement.getContainingFile().getName().equals("build.gradle")) {
                    return;
                }
                GrMethodCallExpression topParentMethodCallExpression =
                        PsiTreeUtil.getTopmostParentOfType(applicationStatement, GrMethodCallExpression.class);
                if (topParentMethodCallExpression == null || topParentMethodCallExpression.getCallReference() == null ||
                        !"dependencies".equals(topParentMethodCallExpression.getCallReference().getMethodName())) {
                    return;
                }
                Project openProject = applicationStatement.getProject();
                String methodName = applicationStatement.getCallReference().getMethodName();
                if ("implementation".equals(methodName) || "compile".equals(methodName)) {
                    GrCommandArgumentList argumentList = applicationStatement.getArgumentList();
                    if (argumentList.getAllArguments().length == 1) {
                        if (argumentList.getAllArguments()[0] instanceof GrLiteral) {
                            PrivacyPracticesHolder.getInstance(openProject).addThirdPartyDependencyInstance(
                                    applicationStatement, argumentList.getAllArguments()[0].getText());
                        }
                    }
                }
            }
        };
    }
}
