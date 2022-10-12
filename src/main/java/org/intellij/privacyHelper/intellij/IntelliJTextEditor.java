package org.intellij.privacyHelper.intellij;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.intellij.privacyHelper.projectsourcefiles.SourceFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Text Editor in IntelliJ, where source code files
 * are loaded and edited. Use this to perform any operations on
 * a source file's contents.
 * */
public class IntelliJTextEditor {
    private static Map<Project, IntelliJTextEditor> textEditor;
    private Project project;

    private IntelliJTextEditor(Project project) {
        this.project = project;
    }

    public static IntelliJTextEditor getInstance(Project project) {
        if(textEditor == null) {
            textEditor = new HashMap<Project, IntelliJTextEditor>();
        }

        if(!textEditor.containsKey(project)) {
            textEditor.put(project, new IntelliJTextEditor(project));
        }

        return textEditor.get(project);
    }

    public void performActionOnFile(SourceFile file,
                                    Runnable action) {
        performActionOnFile(file.toPsiFile(), action);
    }

    public void performActionOnFile(PsiFile file,
                                    Runnable action) {
        DumbService.getInstance(file.getProject()).runWhenSmart(() -> {
            WriteCommandAction.runWriteCommandAction(project,
                    new Runnable() {
                        @Override
                        public void run() {
                            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

                            Document document = documentManager.getDocument(file);
                            while(documentManager.isDocumentBlockedByPsi(document));

                            action.run();

                            documentManager.doPostponedOperationsAndUnblockDocument(document);
                        }
                    });
        });
    }
}