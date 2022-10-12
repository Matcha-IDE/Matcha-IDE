package org.intellij.privacyHelper.startup;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.intellij.privacyHelper.gradle.AppBuildFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class ImportAnnotationLibTask implements StartupActivity {
    public void runActivity(@NotNull Project project) {
        AppBuildFile buildFile = getBuildFile(project);

        if(buildFile != null) {
            DumbService.getInstance(project).runWhenSmart(() -> {

            });
            addDependencyToBuildFile(buildFile, "io.github.i7mist:privacyannotationlib-android-sdk:1.1.2");
        }
    }

    static public AppBuildFile getBuildFile(Project project) {
        try {
            return AppBuildFile.getBuildFileForProject(project);
        }
        catch(FileNotFoundException fnf) {
            System.out.println("This project does not have a build.grade file");
            fnf.printStackTrace();
        }

        return null;
    }

    private void addDependencyToBuildFile(AppBuildFile buildFile,
                                          String dependency) {
        try {
            buildFile.addDependencyIfNotPresent(dependency);
        }
        catch(IllegalStateException ise) {
            System.out.println("This project does not have any dependencies");
            ise.printStackTrace();
        }
    }
}