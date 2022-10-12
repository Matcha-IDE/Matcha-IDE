package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.AnnotationNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.LibraryNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType.DataAccess;
import static org.intellij.privacyHelper.panelUI.safetySectionTasks.DataPracticeGroup.ANY_SINK;
import static org.intellij.privacyHelper.panelUI.safetySectionTasks.DataPracticeGroup.ANY_SOURCE;

public class PurposeNode extends BaseNode {
    String purpose;
    Set<String> dataUsage;
    List<Object> dataUsagePointerList;

    protected PurposeNode(Project project, String purpose, Set<String> dataUsage,
                          HashMap<String, ArrayList<Object>> dataUsagePointerMap, AbstractTreeBuilder builder) {
        super(project, purpose, builder);
        this.purpose = purpose;
        this.dataUsage = dataUsage;
        dataUsagePointerList = dataUsage.stream().map(dataUsagePointerMap::get).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        for (Object pointer : dataUsagePointerList) {
            if (pointer instanceof AnnotationInstance) {
                AnnotationInstance annotationInstance = (AnnotationInstance) pointer;
                children.add(new AnnotationNode(myProject, annotationInstance,
                        annotationInstance.getAnnotationType() == DataAccess ? ANY_SOURCE : ANY_SINK, myBuilder));
            } else if (pointer instanceof String) {
                String libName = (String) pointer;
                children.add(new LibraryNode(myProject, libName, myBuilder));
            }
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        CoconutUIUtil.presentDataWithSource(presentation, dataUsage, purpose);
    }

}
