package org.intellij.privacyHelper.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

public class RegisterEventListenersTask implements StartupActivity {
    public void runActivity(@NotNull Project project) {
        VirtualFileManager fileManager = VirtualFileManager.getInstance();

        /*
        * Important note from IntelliJ Documentation:
        * "VFS listeners are application level, and will receive events for
        * changes happening in all the projects opened by the user. You may
        * need to filter out events which aren’t relevant to your task."
        *
        * This might apply to us?
        * http://www.jetbrains.org/intellij/sdk/docs/basics/virtual_file_system.html#virtual-file-system-events
        * */
        fileManager.addVirtualFileListener(fileCreatedListener(project));
    }

    /* Implement this if you need to do something when a file is added to the project */
    private VirtualFileListener fileCreatedListener(Project project) {
        return new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                VirtualFile newlyAddedFile = event.getFile();

                System.out.println("created " + newlyAddedFile.getName());
            }
        };
    }
}