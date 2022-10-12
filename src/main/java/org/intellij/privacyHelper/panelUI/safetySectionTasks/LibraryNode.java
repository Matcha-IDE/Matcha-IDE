package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyHelper.codeInspection.instances.ThirdPartyDependencyInstance;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.PsiElementNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class LibraryNode extends BaseNode {
    String libName;
    ArrayList<SmartPsiElementPointer<PsiElement>> libIntegrationPointerList;
    SafetySectionDataElement libDefaultDataUsage;
    public ThirdPartyCustomDataInstance customDataInstance;

    public LibraryNode(Project project, String libName, AbstractTreeBuilder builder) {
        super(project, libName, builder);
        this.libName = libName;
        updateList();
    }

    private void updateList() {
        libIntegrationPointerList = new ArrayList<>();
        libDefaultDataUsage = new SafetySectionDataElement();
        ThirdPartyDependencyInstance[] dependencyInstances =
                PrivacyPracticesHolder.getInstance(myProject).getThirdPartyDependencyInstances(libName);
        for (ThirdPartyDependencyInstance dependencyInstance : dependencyInstances) {
            libIntegrationPointerList.add(dependencyInstance.getPsiElementPointer());
            ThirdPartySafetySectionInfo[] safetySectionInfoList =
                    dependencyInstance.getDependencyInfo().getDataPractices(true, false);
            for (ThirdPartySafetySectionInfo safetySectionInfo : safetySectionInfoList) {
                libDefaultDataUsage.combine(safetySectionInfo.safetySectionDataElement);
            }
        }
        customDataInstance =
                PrivacyPracticesHolder.getInstance(myProject).getThirdPartyCustomDataInstance(libName);
    }

    private String generateDataUsageString(SafetySectionDataElement dataElement) {
        HashMap<String, ArrayList<String>> collectedDataPurposesMap = new HashMap<>();
        HashMap<String, ArrayList<String>> sharedDataPurposesMap = new HashMap<>();
        for (Map.Entry<String[], Set<String[]>> entry : dataElement.dataPractices.entrySet()) {
            String dataGroup = DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING.get(entry.getKey()).toString();
            if (entry.getValue().contains(safetySectionCollected)) {
                if (!collectedDataPurposesMap.containsKey(dataGroup)) {
                    collectedDataPurposesMap.put(dataGroup, new ArrayList<>());
                }
                if (entry.getValue().contains(safetySectionCollectionAppFunctionality)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForAppFunctionality.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionAdvertising)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForAdvertisingOrMarketing.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionAnalytics)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForAnalytics.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionDevCommunications)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForDeveloperCommunications.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionFraudPrevention)) {
                    collectedDataPurposesMap.get(dataGroup).add(
                            CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionAccountManagement)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForAccountManagement.toString());
                }
                if (entry.getValue().contains(safetySectionCollectionPersonalization)) {
                    collectedDataPurposesMap.get(dataGroup).add(CollectionAttribute.ForPersonalization.toString());
                }
            }
            if (entry.getValue().contains(safetySectionShared)) {
                if (!sharedDataPurposesMap.containsKey(dataGroup)) {
                    sharedDataPurposesMap.put(dataGroup, new ArrayList<>());
                }
                if (entry.getValue().contains(safetySectionSharingAppFunctionality)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForAppFunctionality.toString());
                }
                if (entry.getValue().contains(safetySectionSharingAdvertising)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForAdvertisingOrMarketing.toString());
                }
                if (entry.getValue().contains(safetySectionSharingAnalytics)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForAnalytics.toString());
                }
                if (entry.getValue().contains(safetySectionSharingDevCommunications)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForDeveloperCommunications.toString());
                }
                if (entry.getValue().contains(safetySectionSharingFraudPrevention)) {
                    sharedDataPurposesMap.get(dataGroup).add(
                            SharingAttribute.ForFraudPreventionAndSecurityAndCompliance.toString());
                }
                if (entry.getValue().contains(safetySectionSharingAccountManagement)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForAccountManagement.toString());
                }
                if (entry.getValue().contains(safetySectionSharingPersonalization)) {
                    sharedDataPurposesMap.get(dataGroup).add(SharingAttribute.ForPersonalization.toString());
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Collected: ");
        if (collectedDataPurposesMap.isEmpty()) {
            stringBuilder.append("none");
        } else {
            for (Map.Entry<String, ArrayList<String>> entry : collectedDataPurposesMap.entrySet()) {
                stringBuilder.append(String.format("%s (purposes: %s) ",
                        CoconutUIUtil.prettifyDataTypeString(entry.getKey()),
                        entry.getValue().stream()
                                .map(CoconutUIUtil::prettifyDataTypeString)
                                .collect(Collectors.joining(", ")).toLowerCase()));
            }
        }
        stringBuilder.append("; Shared: ");
        if (sharedDataPurposesMap.isEmpty()) {
            stringBuilder.append("none");
        } else {
            for (Map.Entry<String, ArrayList<String>> entry : sharedDataPurposesMap.entrySet()) {
                stringBuilder.append(String.format("%s (purposes: %s) ",
                        CoconutUIUtil.prettifyDataTypeString(entry.getKey()),
                        entry.getValue().stream().map(CoconutUIUtil::prettifyDataTypeString)
                                .collect(Collectors.joining(", "))
                                .toLowerCase()));
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        updateList();
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        if (customDataInstance == null || customDataInstance.verified) {
            children.add(new PlainTextNode(myProject,
                    String.format("Default usage: %s", generateDataUsageString(libDefaultDataUsage)), myBuilder));
            if (customDataInstance != null && customDataInstance.verified) {
                children.add(new PlainTextNode(myProject,
                        String.format("Custom usage: %s",
                                generateDataUsageString(customDataInstance.safetySectionDataElement)), myBuilder));
            }
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        ArrayList<String> integrationPoints = new ArrayList<>();
        for (SmartPsiElementPointer<PsiElement> libIntegrationPointer : libIntegrationPointerList) {
            if (libIntegrationPointer.getElement() != null) {
                integrationPoints.add(libIntegrationPointer.getElement().getText());
            }
        }
        if (customDataInstance != null) {
            String statusTag = customDataInstance.verified ? "" : "(Unverified) ";
            presentation.setPresentableText(String.format("%s%s (%s)",statusTag, libName,
                    String.join(", ", integrationPoints)));
        } else {
            presentation.setPresentableText(String.format("%s (%s)", libName,
                    String.join(", ", integrationPoints)));
        }
    }
}
