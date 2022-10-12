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

public class DataTypeNode extends BaseNode {
    String dataType;
    Set<String> dataUsage;
    HashMap<String, ArrayList<Object>> dataUsagePointerMap;

    protected DataTypeNode(Project project, String dataType, Set<String> dataUsage,
                           HashMap<String, ArrayList<Object>> dataUsagePointerMap, AbstractTreeBuilder builder) {
        super(project, dataType, builder);
        this.dataType = dataType;
        this.dataUsage = dataUsage;
        this.dataUsagePointerMap = dataUsagePointerMap;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        Set<String> purposes = dataUsage.stream().map(s -> s.split("_")[1]).collect(Collectors.toSet());
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        for (String purpose : purposes) {
            Set<String> newDataUsage = new HashSet<>();
            for (String usage : dataUsage) {
                if (usage.contains(purpose)) {
                    newDataUsage.add(usage);
                }
            }
            children.add(new PurposeNode(myProject, purpose, newDataUsage, dataUsagePointerMap, myBuilder));
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        CoconutUIUtil.presentDataWithSource(presentation, dataUsage, dataType);
    }
}
