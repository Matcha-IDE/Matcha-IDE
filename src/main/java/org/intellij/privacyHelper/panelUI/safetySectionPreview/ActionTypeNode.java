package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.instances.ThirdPartyDependencyInstance;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.*;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil.DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.*;

public class ActionTypeNode extends BaseNode {

    String[] actionType;
    Set<String> dataCategories;
    HashMap<String, Set<String>> dataUsageMap;
    HashMap<String, ArrayList<Object>> dataUsagePointerMap;

    protected ActionTypeNode(Project project, String[] actionType, AbstractTreeBuilder builder) {
        super(project, actionType, builder);
        this.actionType = actionType;
    }

    private void updateWithDataElementPractice(Map.Entry<String[], Set<String[]>> data, boolean byApp,
                                               boolean forCollection, Object pointer) {
        PersonalDataGroup dataGroup = DATA_TYPE_PRIVACY_LABEL_REVERSE_MAPPING.get(data.getKey());
        String dataCategory = CoconutUIUtil.prettifyDataCategoryString(dataGroup.toString());
        String dataType = CoconutUIUtil.prettifyDataTypeString(dataGroup.toString());
        dataCategories.add(dataCategory);
        if (!dataUsageMap.containsKey(dataCategory)) {
            dataUsageMap.put(dataCategory, new HashSet<>());
        }
        ArrayList<String> purposes = new ArrayList<>();
        if (forCollection) {
            if (data.getValue().contains(safetySectionCollectionAppFunctionality)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForAppFunctionality.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionAdvertising)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForAdvertisingOrMarketing.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionAnalytics)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForAnalytics.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionDevCommunications)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForDeveloperCommunications.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionFraudPrevention)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForFraudPreventionAndSecurityAndCompliance.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionAccountManagement)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForAccountManagement.toString()));
            }
            if (data.getValue().contains(safetySectionCollectionPersonalization)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        CollectionAttribute.ForPersonalization.toString()));
            }
        } else {
            if (data.getValue().contains(safetySectionSharingAppFunctionality)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForAppFunctionality.toString()));
            }
            if (data.getValue().contains(safetySectionSharingAdvertising)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForAdvertisingOrMarketing.toString()));
            }
            if (data.getValue().contains(safetySectionSharingAnalytics)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForAnalytics.toString()));
            }
            if (data.getValue().contains(safetySectionSharingDevCommunications)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForDeveloperCommunications.toString()));
            }
            if (data.getValue().contains(safetySectionSharingFraudPrevention)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForFraudPreventionAndSecurityAndCompliance.toString()));
            }
            if (data.getValue().contains(safetySectionSharingAccountManagement)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForAccountManagement.toString()));
            }
            if (data.getValue().contains(safetySectionSharingPersonalization)) {
                purposes.add(CoconutUIUtil.prettifyCamelCaseString(
                        SharingAttribute.ForPersonalization.toString()));
            }
        }
        String usageString = String.format("%s_%s_%s", dataType,
                String.join(", ", purposes).replace(".", " "), byApp ? "byApp" : "byLibrary");
        dataUsageMap.get(dataCategory).add(usageString);
        if (!dataUsagePointerMap.containsKey(usageString)) {
            dataUsagePointerMap.put(usageString, new ArrayList<>());
        }
        dataUsagePointerMap.get(usageString).add(pointer);
    }

    private void updateWithDataElement(SafetySectionDataElement dataElement, boolean byApp, Object pointer) {
        for (Map.Entry<String[], Set<String[]>> data : dataElement.dataPractices.entrySet()) {
            if (Arrays.equals(safetySectionShared, actionType) && data.getValue().contains(safetySectionShared)) {
                updateWithDataElementPractice(data, byApp, false, pointer);
            } else if (Arrays.equals(safetySectionCollected, actionType) &&
                    data.getValue().contains(safetySectionCollected) &&
                    !data.getValue().contains(safetySectionEphemeral)) {
                updateWithDataElementPractice(data, byApp, true, pointer);
            }
        }
    }

    private void updateData() {
        dataCategories = new HashSet<>();
        dataUsageMap = new HashMap<>();
        dataUsagePointerMap = new HashMap<>();
        AnnotationInstance[] annotationInstances =
                PrivacyPracticesHolder.getInstance(myProject).getAnnotationInstances();
        for (AnnotationInstance annotationInstance : annotationInstances) {
            SafetySectionDataElement appDataElement = annotationInstance.getSafetySectionDataElement(myProject);
            if (appDataElement == null) {
                continue;
            }
            updateWithDataElement(appDataElement, true, annotationInstance);
        }
        ThirdPartyDependencyInstance[] dependencyInstances =
                PrivacyPracticesHolder.getInstance(myProject).getThirdPartyDependencyInstances();
        for (ThirdPartyDependencyInstance dependencyInstance : dependencyInstances) {
            if (dependencyInstance.getDependencyInfo() == null) {
                continue;
            }
            ThirdPartyDependencyInfo dependencyInfo = dependencyInstance.getDependencyInfo();
            updateWithDataElement(dependencyInfo.getSynthesizedDefaultDataPractices(), false, dependencyInfo.libName);
            ThirdPartyCustomDataInstance customDataInstance =
                    PrivacyPracticesHolder.getInstance(myProject).getThirdPartyCustomDataInstance(
                            dependencyInfo.libName);
            if (customDataInstance != null) {
                updateWithDataElement(customDataInstance.safetySectionDataElement, false, dependencyInfo.libName);
            }
        }
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        updateData();
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        for (String dataCategory : dataCategories) {
            children.add(new DataCategoryNode(myProject, dataCategory, dataUsageMap.get(dataCategory),
                    dataUsagePointerMap, myBuilder));
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        if (Arrays.equals(safetySectionShared, actionType)) {
            presentation.setPresentableText("Shared Data (Shared with other companies and organizations)");
        } else {
            presentation.setPresentableText("Collected Data (Collected by the app from your device)");
        }
    }
}
