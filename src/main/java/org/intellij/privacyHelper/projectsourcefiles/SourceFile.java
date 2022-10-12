package org.intellij.privacyHelper.projectsourcefiles;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyHelper.intellij.IntelliJTextEditor;
import org.jetbrains.annotations.NotNull;

public class SourceFile {
    private final PsiJavaFile psiFile;

    private SourceFile(@NotNull PsiJavaFile psiFile) {
        this.psiFile = psiFile;
    }

    public static SourceFile getInstance(@NotNull PsiJavaFile sourceFile) {
        return new SourceFile(sourceFile);
    }

    public String fileName() { return psiFile.getName(); }

    public PsiFile toPsiFile() { return psiFile; }

    /* Adding import statements do not work if there are none in the source file */
    public void addImportStatementIfNotPresent(@NotNull String packageOrClass) {
        if(psiFile != null) {
            PsiElementFactory javaFactory = JavaPsiFacade.getInstance(psiFile.getProject()).getElementFactory();
            PsiImportList [] importList = PsiTreeUtil.getChildrenOfType(psiFile, PsiImportList.class);
            PsiImportStatement importStatement = javaFactory.createImportStatementOnDemand(packageOrClass);

            //Mike - idk why no imports in a file give us an empty import list of length 1
            boolean thereAreImports = ((importList != null) &&
                                       (importList.length > 1));

            if(thereAreImports) {
                for(final PsiImportList stmt : importList) {
                    if(stmt.getText().contains(packageOrClass)) { return; }
                }

                PsiImportList firstImportList = importList[0];

                IntelliJTextEditor.getInstance(psiFile.getProject())
                                  .performActionOnFile(psiFile, () -> firstImportList.add(importStatement));
            }
            else {
                PsiPackage packageAtTopOfFile = PsiTreeUtil.getChildOfType(psiFile, PsiPackage.class);
                PsiJavaFile j = (PsiJavaFile)psiFile;

                if(packageAtTopOfFile != null) {
                    System.out.println(packageAtTopOfFile.getText());
                    IntelliJTextEditor.getInstance(psiFile.getProject())
                                      .performActionOnFile(psiFile, () -> psiFile.addAfter(importStatement, j.getPackageStatement()));
                }
                System.out.println("is null");
            }
        }
    }
}