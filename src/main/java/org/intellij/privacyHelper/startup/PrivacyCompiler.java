package org.intellij.privacyHelper.startup;

import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.intellij.privacyHelper.codeInspection.inspections.*;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.codeInspection.utils.ThirdPartySafetySectionInfo;
import org.intellij.privacyHelper.gradle.AppBuildFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;

import java.util.*;
import java.util.stream.Collectors;

public class PrivacyCompiler implements StartupActivity {

    private final LocalInspectionTool[] javaPrivacyInspections = {
            new PersonalDataSourceAPIInspection(),
            new PersonalDataSinkAPIInspection(),
            new AnnotationScanInspection(),
            new AnnotationTooltipInspection()
    };

    private final LocalInspectionTool[] groovyPrivacyInspections = {
            new ThirdPartyLibImportInspection()
    };

    private final LocalInspectionTool[] xmlPrivacyInspections = {
            new LibraryConfigXmlInspection()
    };

    private List<PsiFile> computeFilesWithErrors(Project project) {
        List<PsiFile> fileWithErrors = new LinkedList<>();

        try {
            fileWithErrors = ReadAction.compute(() -> {
                double filesProcessed = 0.0;
                List<PsiFile> errs = new LinkedList<PsiFile>();
                Collection<VirtualFile> javaProjectFiles = getJavaProjectFiles(project);
                Collection<VirtualFile> groovyProjectFiles = getGroovyProjectFiles(project);
                int totalFileCount = javaProjectFiles.size() * 2 + groovyProjectFiles.size() + 1;

                // Must run groovy file inspection before xml because the xml tag validity test depends on the gradle
                // file analysis results
                for (final VirtualFile f : groovyProjectFiles) {
                    PsiFile fileToInspect = PsiManager.getInstance(project).findFile(f);

                    if (fileHasErrors(project, fileToInspect, false, true, false, false)) {
                        errs.add(fileToInspect);
                    }

                    double percentComplete = ((++filesProcessed) / totalFileCount);
                    updateProgressBar(project, percentComplete);
                }

                {
                    HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap = new HashMap<>();
                    CoconutUIUtil.updateLibraryMaps(project, libraryDataMap);

                    if (!CoconutUIUtil.checkLibraryConfigFileCompletenessAndUpdate(project, libraryDataMap, false)) {
                        errs.add(null);
                    }
                    ApplicationManager.getApplication().invokeLater(
                            ()->ApplicationManager.getApplication().runWriteAction(
                                    (ThrowableComputable<Boolean, RuntimeException>) () ->
                                            CoconutUIUtil.checkLibraryConfigFileCompletenessAndUpdate(
                                                    project, libraryDataMap, true)));
                    double percentComplete = ((++filesProcessed) / totalFileCount);
                    updateProgressBar(project, percentComplete);
                }

                // Double it because the counting of access ID values takes one round and the second round will have the
                // correct values
                for (int i = 0 ; i < 2 ; ++i) {
                    for (final VirtualFile f : javaProjectFiles) {
                        PsiFile fileToInspect = PsiManager.getInstance(project).findFile(f);

                        if (fileHasErrors(project, fileToInspect, true, false, false,
                                i != 1)) {
                            errs.add(fileToInspect);
                        }

                        double percentComplete = ((++filesProcessed) / totalFileCount);
                        updateProgressBar(project, percentComplete);
                    }
                }

                return errs;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileWithErrors;
    }

    private class LoadFileTask implements Runnable {

        @Override
        public void run() {
        }
    }

    private class PrivacyCheckTask implements Runnable {
        private final Project project;

        public PrivacyCheckTask(final Project project) { this.project = project; }

        public void run() {
            List<PsiFile> filesWithErrors = new LinkedList<PsiFile>();

            try {
                filesWithErrors = ProgressManager.getInstance()
                        .runProcessWithProgressSynchronously(
                                () -> computeFilesWithErrors(project),
                                "Checking for privacy tasks",
                                true,
                                project
                        );
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            if(filesWithErrors.size() > 0) {
                notifyUserOfErrors(project);
            }
        }
    }

    public void runActivity(@NotNull Project project) {
        DumbService.getInstance(project).runWhenSmart(new LoadFileTask());
        DumbService.getInstance(project).runWhenSmart(new PrivacyCheckTask(project));
    }

    private Collection<VirtualFile> getJavaProjectFiles(@NotNull final Project project) {
        Collection<VirtualFile> projectFiles = new LinkedList<VirtualFile>();
        Collection<VirtualFile> allJavaFiles = FileBasedIndex.getInstance()
                                                         .getContainingFiles(FileTypeIndex.NAME,
                                                                             JavaFileType.INSTANCE,
                                                                             GlobalSearchScope.projectScope(project));

        for(final VirtualFile file : allJavaFiles) {
            if(!file.getName().equals("R.java") &&
               !file.getName().equals("BuildConfig.java")) {
                    projectFiles.add(file);
            }
        }
        return projectFiles;
    }

    private Collection<VirtualFile> getGroovyProjectFiles(@NotNull final Project project) {
        // FIXME: the getContainingFiles API doesn't return any files, so I use the ImportAnnotationLibTask.getBuildFile
        // to at least get the build.gradle file when the main module name is "app", which should cover the majority of
        // cases but is not a complete solution.
        Collection<VirtualFile> allGroovyFiles = new ArrayList<>(FileBasedIndex.getInstance()
                .getContainingFiles(FileTypeIndex.NAME, GroovyFileType.GROOVY_FILE_TYPE, GlobalSearchScope.projectScope(project)));
        AppBuildFile appBuildFile = ImportAnnotationLibTask.getBuildFile(project);
        if (appBuildFile != null) {
            allGroovyFiles.add(appBuildFile.getVirtualFile());
        }

        return new LinkedList<>(allGroovyFiles);
    }

    private Collection<VirtualFile> getXmlProjectFiles(@NotNull final Project project) {
        Collection<VirtualFile> allXmlFiles = FileBasedIndex.getInstance()
                .getContainingFiles(FileTypeIndex.NAME, XmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));

        return new LinkedList<>(allXmlFiles);
    }

    private boolean fileHasErrors(@NotNull final Project project,
                                  final PsiFile file, boolean isJava, boolean isGroovy, boolean isXml,
                                  boolean skipError) {
        if (file != null) {
            InspectionProfileImpl profile = createInspectionProfile(project);
            GlobalInspectionContext context = createInspectionContext(project);

            boolean hasIssues = false;
            LocalInspectionTool[] privacyInspections;
            if (isJava) {
                privacyInspections = javaPrivacyInspections;
            } else if (isGroovy) {
                privacyInspections = groovyPrivacyInspections;
            } else if (isXml) {
                privacyInspections = xmlPrivacyInspections;
            } else {
                return false;
            }
            for (final LocalInspectionTool inspection : privacyInspections) {
                InspectionToolWrapper wrapper = profile.getInspectionTool(inspection.getShortName(), project);

                if (wrapper != null) {
                    List<ProblemDescriptor> issues = InspectionEngine.runInspectionOnFile(file, wrapper, context);
                    List<ProblemDescriptor> severe_issues = issues.stream().filter(issue ->
                            issue.getHighlightType() == ProblemHighlightType.GENERIC_ERROR ||
                                    issue.getHighlightType() == ProblemHighlightType.ERROR).collect(Collectors.toList());

                    if(!skipError && !severe_issues.isEmpty()) {
                        hasIssues = true;
                    }
                }
            }

            return hasIssues;
        }

        return false;
    }

    private InspectionProfileImpl createInspectionProfile(@NotNull final Project project) {
        final InspectionProfile profile = InspectionProjectProfileManager.getInstance(project).getInspectionProfile();
        return (InspectionProfileImpl)profile;
    }

    private GlobalInspectionContext createInspectionContext(@NotNull final Project project) {
        InspectionManagerEx inspectionManager = (InspectionManagerEx)InspectionManager.getInstance(project);
        return inspectionManager.createNewGlobalContext(false);
    }

    private void notifyUserOfErrors(@NotNull final Project project) {
//        logAnalysisResults(project);
        CoconutUIUtil.pushNotification(project, "Incomplete info for generating privacy label",
                "Please open the tool window \"Privacy Labels\" to complete privacy label tasks.", NotificationType.ERROR);
    }

    private void updateProgressBar(@NotNull final Project project,
                                   final double percentComplete) {
        ProgressManager.getInstance().getProgressIndicator().setFraction(percentComplete);
    }
}