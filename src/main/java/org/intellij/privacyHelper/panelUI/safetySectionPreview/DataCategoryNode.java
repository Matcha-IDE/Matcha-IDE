package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DataCategoryNode extends BaseNode {
    String dataCategory;
    Set<String> dataUsage;
    HashMap<String, ArrayList<Object>> dataUsagePointerMap;

    protected DataCategoryNode(Project project, String dataCategory, Set<String> dataUsage,
                               HashMap<String, ArrayList<Object>> dataUsagePointerMap,
                               AbstractTreeBuilder builder) {
        super(project, dataCategory, builder);
        this.dataCategory = dataCategory;
        this.dataUsage = dataUsage;
        this.dataUsagePointerMap = dataUsagePointerMap;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        Set<String> dataTypes = dataUsage.stream().map(s -> s.split("_")[0]).collect(Collectors.toSet());
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        for (String dataType : dataTypes) {
            Set<String> newDataUsage = new HashSet<>();
            for (String usage : dataUsage) {
                if (usage.contains(dataType)) {
                    newDataUsage.add(usage);
                }
            }
            children.add(new DataTypeNode(myProject, dataType, newDataUsage, dataUsagePointerMap, myBuilder));
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        CoconutUIUtil.presentDataWithSource(presentation, dataUsage, dataCategory);
    }
}
