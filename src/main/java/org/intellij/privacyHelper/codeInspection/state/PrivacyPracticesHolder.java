package org.intellij.privacyHelper.codeInspection.state;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationSpeculation;
import org.intellij.privacyHelper.codeInspection.instances.*;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.intellij.privacyHelper.codeInspection.annotations.AnnotationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 4/26/17.
 */
public class PrivacyPracticesHolder {

    private final Vector<SensitiveAPIInstance> sensitiveAPIInstances = new Vector<>();
    private final Vector<AndroidPermissionInstance> permissionInstances = new Vector<>();
    private final HashMap<SmartPsiElementPointer<PsiElement>, Boolean> annotationFieldIsCorrect = new HashMap<>();
    private final Vector<ThirdPartyDependencyInstance> thirdPartyDependencyInstances = new Vector<>();
    private final Vector<AnnotationInstance> annotationInstances = new Vector<>();
    private final Vector<ThirdPartyCustomDataInstance> thirdPartyCustomDataInstanceInstances = new Vector<>();

    private static Map<String, PrivacyPracticesHolder> ourInstances = new HashMap<>();

    private Project openProject = null;

    static private void addProject(final Project newProject) {
        final String key = getProjectKey(newProject);

        if(!ourInstances.containsKey(key)) {
            ourInstances.put(key, new PrivacyPracticesHolder(newProject));
            newProject.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerAdapter() {
                @Override
                public void projectClosed(Project project) {
                    if (project == newProject) {
                        removeProject(getProjectKey(newProject));
                    }
                }
            });
        }

    }

    private void updateProjectIfDisposed(final Project newProject) {
        if (openProject == null || openProject.isDisposed()) {
            if (openProject != null) {
                removeProject(getProjectKey(openProject));
                addProject(newProject);
            }
            openProject = newProject;
        }
    }

    static public PrivacyPracticesHolder getInstance(final Project holderProject) {
        if (ourInstances == null) {
            ourInstances = new HashMap<String, PrivacyPracticesHolder>();
        }

        addProject(holderProject);
        return ourInstances.get(getProjectKey(holderProject));
    }

    private static String getProjectKey(final Project project) {
        return project.getBasePath() + ":" + project.getName();
    }

    static private void removeProject(String projectKey) {
        ourInstances.remove(projectKey);
    }

    private PrivacyPracticesHolder(final Project project){
        openProject = project;
    }

    private void cleanupInvalidAnnotationFieldIsCorrectInstance() {
        annotationFieldIsCorrect.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().getElement() == null);
    }

    public void setAnnotationFieldIsCorrect(PsiElement element, boolean errorSeverity) {
        cleanupInvalidAnnotationFieldIsCorrectInstance();
        SmartPsiElementPointer<PsiElement> psiElementPointer = SmartPointerManager.createPointer(element);
        annotationFieldIsCorrect.put(psiElementPointer, errorSeverity);
    }

    public boolean getAnnotationFieldIsCorrect(PsiElement element) {
        cleanupInvalidAnnotationFieldIsCorrectInstance();
        SmartPsiElementPointer<PsiElement> psiElementPointer = SmartPointerManager.createPointer(element);
        // If the element is not in the map, we consider it as the lowest severity
        return annotationFieldIsCorrect.getOrDefault(psiElementPointer, true);
    }

    private void cleanupInvalidInstances() {
        try {
            // personal data API calls
            sensitiveAPIInstances.removeIf(personalDataAPICallInstance -> !personalDataAPICallInstance.isValid()
                    || !personalDataAPICallInstance.sensitiveAPI.isValid());
            // permissions
            permissionInstances.removeIf(permissionInstance -> !permissionInstance.isValid());
            // dependencies (in the gradle file)
            thirdPartyDependencyInstances.removeIf(thirdPartyDependencyInstance ->
                    !thirdPartyDependencyInstance.isValid());
            annotationInstances.removeIf(annotationInstance ->
                    !annotationInstance.isValid());
            thirdPartyCustomDataInstanceInstances.removeIf(thirdPartyCustomDataInstance ->
                    !thirdPartyCustomDataInstance.isValid());
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public Vector<SensitiveAPIInstance> getSourceAPICallInstances() {
        cleanupInvalidInstances();
        Vector<SensitiveAPIInstance> instances = new Vector<>();
        for (SensitiveAPIInstance instance : sensitiveAPIInstances) {
            if (instance.getSensitiveAPI().isPersonalDataSource()) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public Vector<SensitiveAPIInstance> getSinkAPICallInstances() {
        cleanupInvalidInstances();
        Vector<SensitiveAPIInstance> instances = new Vector<>();
        for (SensitiveAPIInstance instance : sensitiveAPIInstances) {
            if (instance.getSensitiveAPI().isPersonalDataSink()) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public SensitiveAPIInstance addSensitiveAPIInstance(PsiElement expression, SensitiveAPI api, boolean hasError,
                                                        boolean hasWarning, String todoDescription) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(expression.getProject());

        SmartPsiElementPointer<PsiElement> newAPISmartPointer =
                SmartPointerManager.createPointer(expression);
        for (SensitiveAPIInstance instance : sensitiveAPIInstances) {
            if (instance.psiElementPointer.equals(newAPISmartPointer) && instance.sensitiveAPI.equals(api)) {
                instance.hasError = hasError;
                instance.hasWarning = hasWarning;
                instance.todoDescription = todoDescription;
                return instance;
            }
        }
        SensitiveAPIInstance instance = new SensitiveAPIInstance(
                newAPISmartPointer, api, hasError, hasWarning, todoDescription);
        sensitiveAPIInstances.add(instance);
        return instance;
    }

    public SensitiveAPIInstance addSensitiveAPIInstance(PsiElement expression, PsiAnnotation [] psiAnnotations,
                                                        AnnotationSpeculation[] annotationSpeculationFromAPICalls,
                                                        AnnotationHolder [] annotations,
                                                        SensitiveAPI api, boolean hasError, boolean hasWarning,
                                                        String todoDescription) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(expression.getProject());

        assert (psiAnnotations.length == annotationSpeculationFromAPICalls.length && psiAnnotations.length == annotations.length);
        ArrayList<AnnotationMetaData> metaDataArrayList = new ArrayList<>();
        for (int i = 0 ; i < psiAnnotations.length ; ++i) {
            SmartPsiElementPointer<PsiElement> newAnnotationSmartPointer = null;
            if (psiAnnotations[i] != null) {
                newAnnotationSmartPointer = SmartPointerManager.createPointer(psiAnnotations[i]);
            }
            metaDataArrayList.add(new AnnotationMetaData(annotations[i], annotationSpeculationFromAPICalls[i], newAnnotationSmartPointer));
        }
        SmartPsiElementPointer<PsiElement> newAPISmartPointer =
                SmartPointerManager.createPointer(expression);
        for (SensitiveAPIInstance instance : sensitiveAPIInstances) {
            if (instance.psiElementPointer.equals(newAPISmartPointer) && instance.sensitiveAPI.equals(api)) {
                // Already had the annotation, just update annotation content
                instance.updateAnnotationInfo(metaDataArrayList.toArray(new AnnotationMetaData[0]));
                instance.hasError = hasError;
                instance.hasWarning = hasWarning;
                instance.todoDescription = todoDescription;
                return instance;
            }
        }
        SensitiveAPIInstance instance = new SensitiveAPIInstance(
                newAPISmartPointer, metaDataArrayList.toArray(new AnnotationMetaData[0]),
                api, hasError, hasWarning, todoDescription);
        sensitiveAPIInstances.add(instance);

        return instance;
    }


    public void addPermissionInstance(AndroidPermission permission, PsiElement psiElement) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(psiElement.getProject());

        SmartPsiElementPointer<PsiElement> newPermissionSmartPointer =
                SmartPointerManager.createPointer(psiElement);
        ArrayList<AndroidPermissionInstance> permissionInstancesToRemove = new ArrayList<>();
        for (AndroidPermissionInstance instance : permissionInstances) {
            if (instance.psiElementPointer.equals(newPermissionSmartPointer)) {
                permissionInstancesToRemove.add(instance);
            }
        }
        permissionInstances.removeAll(permissionInstancesToRemove);
        permissionInstances.add(new AndroidPermissionInstance(newPermissionSmartPointer, permission));
    }

    public void addThirdPartyDependencyInstance(GrApplicationStatement applicationStatement, String dependencyString) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(applicationStatement.getProject());

        SmartPsiElementPointer<PsiElement> newApplicationStatementSmartPointer =
                SmartPointerManager.createPointer(applicationStatement);
        ArrayList<ThirdPartyDependencyInstance> instancesToRemove = new ArrayList<>();
        for (ThirdPartyDependencyInstance instance : thirdPartyDependencyInstances) {
            if (instance.getPsiElementPointer().equals(newApplicationStatementSmartPointer)) {
                instancesToRemove.add(instance);
            }
        }
        thirdPartyDependencyInstances.removeAll(instancesToRemove);
        thirdPartyDependencyInstances.add(new ThirdPartyDependencyInstance(
                newApplicationStatementSmartPointer, dependencyString));
    }

    public void addAnnotationInstance(PsiAnnotation annotation, AnnotationHolder annotationHolder) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(annotation.getProject());

        SmartPsiElementPointer<PsiElement> newAnnotationSmartPointer = SmartPointerManager.createPointer(annotation);
        ArrayList<AnnotationInstance> instancesToRemove = new ArrayList<>();
        for (AnnotationInstance instance : annotationInstances) {
            if (instance.getAnnotationSmartPointer().equals(newAnnotationSmartPointer)) {
                instancesToRemove.add(instance);
            }
        }
        annotationInstances.removeAll(instancesToRemove);
        annotationInstances.add(new AnnotationInstance(newAnnotationSmartPointer, annotationHolder));
    }

    public void addThirdPartyCustomDataInstance(PsiElement xmlTag, String libraryName,
                                                boolean verified, SafetySectionDataElement safetySectionDataElement) {
        cleanupInvalidInstances();
        updateProjectIfDisposed(xmlTag.getProject());

        SmartPsiElementPointer<PsiElement> newXmlTagPointer = SmartPointerManager.createPointer(xmlTag);
        ArrayList<ThirdPartyCustomDataInstance> instancesToRemove = new ArrayList<>();
        for (ThirdPartyCustomDataInstance instance : thirdPartyCustomDataInstanceInstances) {
            if (instance.getPsiElementPointer().equals(newXmlTagPointer)) {
                instancesToRemove.add(instance);
            }
        }
        thirdPartyCustomDataInstanceInstances.removeAll(instancesToRemove);
        thirdPartyCustomDataInstanceInstances.add(new ThirdPartyCustomDataInstance(newXmlTagPointer, libraryName,
                verified, safetySectionDataElement));

    }

    public boolean hasPermissionDeclared(AndroidPermission permission) {
        cleanupInvalidInstances();
        if (openProject != null && !openProject.isDisposed()) {
            PsiFile [] manifestFiles = FilenameIndex.getFilesByName(openProject, "AndroidManifest.xml", GlobalSearchScope.allScope(openProject));
            for (PsiFile manifestFile : manifestFiles) {
                if (Pattern.matches(".*app/src/main/AndroidManifest\\.xml", manifestFile.getVirtualFile().getPath())) {
                    manifestFile.accept(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(@NotNull PsiElement element) {
                            super.visitElement(element);
                            if (element instanceof XmlTag) {
                                XmlTag tag = (XmlTag) element;
                                if (tag.getAttributes().length == 0) {
                                    return;
                                }
                                String xmlAttribute = tag.getAttributes()[0].getDisplayValue();
                                if (xmlAttribute == null) {
                                    return;
                                }
                                for (AndroidPermission permission : AndroidPermission.values()) {
                                    if (xmlAttribute.equals("android.permission." + permission.toString())) {
                                        addPermissionInstance(permission, tag);
                                    }
                                }
                            }
                        }
                    });
                }
            }
            for (AndroidPermissionInstance instance : permissionInstances) {
                if (instance.permission.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public SensitiveAPIInstance[] getAllSensitiveApiInstances() {
        cleanupInvalidInstances();
        return sensitiveAPIInstances.toArray(new SensitiveAPIInstance[0]);
    }

    public AnnotationInstance[] getAnnotationInstances() {
        cleanupInvalidInstances();
        return annotationInstances.toArray(new AnnotationInstance[0]);
    }

    public ThirdPartyDependencyInstance[] getThirdPartyDependencyInstances() {
        cleanupInvalidInstances();
        return thirdPartyDependencyInstances.toArray(new ThirdPartyDependencyInstance[0]);
    }

    public ThirdPartyDependencyInstance[] getThirdPartyDependencyInstances(@NotNull String libName) {
        cleanupInvalidInstances();
        ArrayList<ThirdPartyDependencyInstance> dependencyInstances = new ArrayList<>();
        for (ThirdPartyDependencyInstance dependencyInstance : thirdPartyDependencyInstances) {
            if (dependencyInstance.getDependencyInfo() == null) {
                continue;
            }
            if (libName.equals(dependencyInstance.getDependencyInfo().libName)) {
                dependencyInstances.add(dependencyInstance);
            }
        }
        return dependencyInstances.toArray(new ThirdPartyDependencyInstance[0]);
    }

    public ThirdPartyCustomDataInstance getThirdPartyCustomDataInstance(@NotNull String libName) {
        cleanupInvalidInstances();
        for (ThirdPartyCustomDataInstance customDataInstance : thirdPartyCustomDataInstanceInstances) {
            if (libName.equals(customDataInstance.libraryName)) {
                return customDataInstance;
            }
        }
        return null;
    }

    SafetySectionDataElement getSynthesizedSafetySectionDataElements() {
        SafetySectionDataElement safetySectionDataElement = new SafetySectionDataElement();
        for (AnnotationInstance annotationInstance : annotationInstances) {
            if (annotationInstance.getAnnotationType() == CoconutAnnotationType.DataTransmission) {
                safetySectionDataElement.combine(annotationInstance.getSafetySectionDataElement(openProject));
            }
        }
        for (ThirdPartyDependencyInstance dependencyInstance : thirdPartyDependencyInstances) {
            if (dependencyInstance.getDependencyInfo() == null) {
                continue;
            }
            ThirdPartyDependencyInfo dependencyInfo = dependencyInstance.getDependencyInfo();
            safetySectionDataElement.combine(dependencyInfo.getSynthesizedDefaultDataPractices());
            ThirdPartyCustomDataInstance customDataInstance =
                    PrivacyPracticesHolder.getInstance(openProject).getThirdPartyCustomDataInstance(
                            dependencyInfo.libName);
            if (customDataInstance != null) {
                safetySectionDataElement.combine(customDataInstance.safetySectionDataElement);
            }
        }
        return safetySectionDataElement;
    }

    String generateSafetySectionCsvText() {
        StringBuilder safetySectionCsv = new StringBuilder();
        safetySectionCsv.append(safetySectionCsvHeader).append("\n");
        // fill in answers to the overview questions
        SafetySectionDataElement synthesizedDataElement = getSynthesizedSafetySectionDataElements();
        Set<String[]> synthesizedDataTypes = synthesizedDataElement.dataPractices.keySet();

        safetySectionCsv.append(String.join(",", new String[]{safetySectionIsCollectingData[0], "",
                synthesizedDataElement.getIsCollectingDataString() , safetySectionIsCollectingData[1],
                safetySectionIsCollectingData[2]})).append("\n");
        safetySectionCsv.append(String.join(",", new String[]{safetySectionEncryptedInTransit[0], "",
                synthesizedDataElement.getEncryptedInTransitString(), safetySectionEncryptedInTransit[1],
                safetySectionEncryptedInTransit[2]})).append("\n");
        safetySectionCsv.append(String.join(",", new String[]{safetySectionUserRequestDelete[0], "",
                synthesizedDataElement.getUserRequestDeleteString(), safetySectionUserRequestDelete[1],
                safetySectionUserRequestDelete[2]})).append("\n");
        safetySectionCsv.append(String.join(",", new String[]{safetySectionFamilyPolicy[0], "",
                synthesizedDataElement.getAnswerForFamilyPolicyString(), safetySectionFamilyPolicy[1],
                safetySectionFamilyPolicy[2]})).append("\n");
        safetySectionCsv.append(String.join(",", new String[]{safetySectionSecurityReview[0], "",
                synthesizedDataElement.getAnswerForSecurityReviewString(), safetySectionSecurityReview[1],
                safetySectionSecurityReview[2]})).append("\n");

        // fill in answers to the data type questions
        for (String [] dataType : safetySectionDataTypes) {
            if (synthesizedDataTypes.contains(dataType)) {
                safetySectionCsv.append(String.join(",", new String[]{dataType[0], dataType[1], "TRUE", dataType[2],
                        dataType[3]})).append("\n");
            } else {
                safetySectionCsv.append(String.join(",", new String[]{dataType[0], dataType[1], "", dataType[2],
                        dataType[3]})).append("\n");
            }
        }
        // fill in answers to the data practice question
        for (String [] dataType : safetySectionDataTypes) {
            for (String [] usageResponse : safetySectionDataUsageResponses) {
                String dataTypeQuestionId =
                        String.format("PSL_DATA_USAGE_RESPONSES:%s:%s", dataType[1], usageResponse[0]);
                String humanLabel = String.format(usageResponse[3], dataType[3].split(" / ")[1]);
                if (!synthesizedDataTypes.contains(dataType)) {
                    safetySectionCsv.append(String.join(",",
                                        new String[]{dataTypeQuestionId, usageResponse[1], "", usageResponse[2],
                                                humanLabel}))
                                .append("\n");
                } else if (!synthesizedDataElement.dataPractices.get(dataType).contains(usageResponse)) {
                    if ("MAYBE_REQUIRED".equals(usageResponse[2]) || "REQUIRED".equals(usageResponse[2])) {
                        if ("MAYBE_REQUIRED".equals(usageResponse[2]) &&
                                Arrays.equals(safetySectionEphemeral, usageResponse) && !synthesizedDataElement
                                        .dataPractices.get(dataType).contains(safetySectionCollected)) {
                            safetySectionCsv.append(String.join(",",
                                            new String[]{dataTypeQuestionId, usageResponse[1], "", usageResponse[2],
                                                    humanLabel}))
                                    .append("\n");
                        } else {
                            safetySectionCsv.append(String.join(",",
                                            new String[]{dataTypeQuestionId, usageResponse[1], "FALSE", usageResponse[2],
                                                    humanLabel}))
                                    .append("\n");
                        }
                    } else {
                        safetySectionCsv.append(String.join(",",
                                        new String[]{dataTypeQuestionId, usageResponse[1], "", usageResponse[2],
                                                humanLabel}))
                                .append("\n");
                    }
                } else {
                    safetySectionCsv.append(String.join(",",
                                    new String[]{dataTypeQuestionId, usageResponse[1], "TRUE", usageResponse[2],
                                            humanLabel}))
                            .append("\n");
                }
            }
        }
        return safetySectionCsv.toString();
    }

    private void addPrivacyLabelFile(PsiFile privacyLabelFile, String newFileName, PsiDirectory directory) {
        if (directory == null) {
            return;
        }
        for (PsiFile file : directory.getFiles()) {
            if (newFileName.equals(file.getName())) {
                file.delete();
            }

        }
        directory.add(privacyLabelFile);
        CoconutUIUtil.pushNotification(openProject, "Data Safety Section Report Generated",
                String.format("Check the CSV file at %s/%s", directory.getVirtualFile().getPath(), newFileName),
                NotificationType.INFORMATION);
    }

    public void generateAndWritePrivacyLabel() {
        final PsiFileFactory factory = PsiFileFactory.getInstance(openProject);
        String newFileName = "data_safety_form.csv";
        FileType type = FileTypeRegistry.getInstance().getFileTypeByFileName(newFileName);
        PsiFile privacyLabelFile = factory.createFileFromText(newFileName, type, generateSafetySectionCsvText());
        PsiManager manager = PsiManager.getInstance(openProject);
        PsiDirectory directory = manager.findDirectory(openProject.getBaseDir());
        if (directory != null) {
            final PsiDirectory[] subDirectory = {directory.findSubdirectory("safety_section")};
            if (subDirectory[0] == null) {
                ApplicationManager.getApplication().invokeLater(() ->
                        ApplicationManager.getApplication().runWriteAction(()->{
                            subDirectory[0] = directory.createSubdirectory("safety_section");
                        }));
            }
            final Application application = ApplicationManager.getApplication();
            if (application.isDispatchThread()) {
                application.runWriteAction(() -> {
                    addPrivacyLabelFile(privacyLabelFile, newFileName, subDirectory[0]);
                });
            } else {
                application.invokeLater(() -> application.runWriteAction(
                        () -> {
                            addPrivacyLabelFile(privacyLabelFile, newFileName, subDirectory[0]);
                        }));
            }
        }
    }

    private Vector<AnnotationInstance> getAnnotationsByCoverageAndType(CoconutAnnotationType targetType,
                                                                       boolean required) {
        cleanupInvalidInstances();
        Vector<AnnotationInstance> annotationInstances = new Vector<>();
        for (AnnotationInstance annotationInstance : this.annotationInstances) {
            if (annotationInstance.getAnnotationType() != targetType) {
                continue;
            }
            boolean coveredInDetectedApi = false;
            for (SensitiveAPIInstance apiInstance : sensitiveAPIInstances) {
                for (AnnotationMetaData metaData : apiInstance.annotationMetaDataList) {
                    if (annotationInstance.getAnnotationSmartPointer().equals(metaData.psiAnnotationPointer)) {
                        coveredInDetectedApi = true;
                        break;
                    }
                }
            }
            if (coveredInDetectedApi == required) {
                annotationInstances.add(annotationInstance);
            }
        }
        return annotationInstances;

    }

    public Vector<AnnotationInstance> getRequiredAnnotationsByType(CoconutAnnotationType targetType) {
        return getAnnotationsByCoverageAndType(targetType, true);
    }

    public Vector<AnnotationInstance> getManuallyAddedAnnotationsByType(CoconutAnnotationType targetType) {
        return getAnnotationsByCoverageAndType(targetType, false);
    }
}
