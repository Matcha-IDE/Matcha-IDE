package org.intellij.privacyHelper.ideUI;

import com.intellij.openapi.application.ApplicationManager;

public class IdeUI {
    public static void submitTask(Runnable task) {
        ApplicationManager.getApplication().invokeLater(task);
    }
}

