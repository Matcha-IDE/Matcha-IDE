package org.intellij.privacyHelper.gradle;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.intellij.IntelliJTextEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.path.GrMethodCallExpressionImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AppBuildFile {
    private VirtualFile buildFile;
    private PsiFile psiBuildFile;
    private Project buildFileProject;

    private AppBuildFile(@NotNull VirtualFile buildFile,
                         @NotNull Project buildFileProject) {
        this.buildFile = buildFile;
        this.buildFileProject = buildFileProject;
        psiBuildFile = PsiManager.getInstance(buildFileProject).findFile(buildFile);
    }

    public VirtualFile getVirtualFile() {
        return buildFile;
    }

    public static AppBuildFile getBuildFileForProject(@NotNull Project project)
                                                      throws FileNotFoundException {
        File file = new File(String.format("%s/app/build.gradle", project.getBasePath()));
        VirtualFile buildFile = LocalFileSystem.getInstance().findFileByIoFile(file);

        if(buildFile == null) {
            throw new FileNotFoundException("Cannot find the build.gradle file for this project");
        }

        return new AppBuildFile(buildFile, project);
    }

    public void addDependencyIfNotPresent(@NotNull String dependency)
                                          throws IllegalStateException {
        if(!dependencyExists(dependency)) {
            dependency = "implementation \'" + dependency + "\'";
            GroovyPsiElementFactory groovyFactory = GroovyPsiElementFactory.getInstance(buildFileProject);
            PsiElement newLib = groovyFactory.createStatementFromText(dependency);
            List<GrApplicationStatement> dependencies = getDependencies();
            int lastIndex = (dependencies.size() - 1);

            IntelliJTextEditor.getInstance(buildFileProject)
                              .performActionOnFile(psiBuildFile,
                                      () -> {
                                          dependencies.get(lastIndex)
                                                      .getParent()
                                                      .addBefore(newLib, dependencies.get(lastIndex));
                                      });
        }

    }

    public boolean dependencyExists(@NotNull String dependency) {
        for(GrApplicationStatement statement : getDependencies()) {
            if(statement.getText().contains(dependency)) { return true; }
        }

        return false;
    }

    public List<GrApplicationStatement> getDependencies() throws IllegalStateException {
        PsiFile psiFile = PsiManager.getInstance(buildFileProject).findFile(buildFile);
        GrMethodCallExpressionImpl[] grMethodCallExpressions = PsiTreeUtil.getChildrenOfType(psiFile, GrMethodCallExpressionImpl.class);

        for(GrMethodCallExpression methodCallExpression : grMethodCallExpressions) {
            if(Pattern.compile(".*dependencies.*", Pattern.DOTALL)
                      .matcher(methodCallExpression.getText()).matches()) {
                GrApplicationStatement[] statements = PsiTreeUtil.getChildrenOfType(methodCallExpression.getClosureArguments()[0],
                                                                                    GrApplicationStatement.class);

                if(statements == null || statements.length == 0) { continue; }
                else { return Arrays.asList(statements); }
            }
        }

        throw new IllegalStateException("build.gradle file does not have any dependencies");
    }
}