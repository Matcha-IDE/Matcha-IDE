package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;

public class SafetySectionPreviewTreeBuilder extends AbstractTreeBuilder {
    protected final Project myProject;

    public SafetySectionPreviewTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
        super(tree, treeModel, null, SafetySectionPreviewTreeBuilder.MyComparator.ourInstance, false);

        myProject = project;
    }

    public final void init() {
        SafetySectionPreviewTreeStructure safetySectionPreviewTreeStructure = createTreeStructure();
        setTreeStructure(safetySectionPreviewTreeStructure);
        safetySectionPreviewTreeStructure.setTreeBuilder(this);

        initRootNode();
    }

    @NotNull
    protected SafetySectionPreviewTreeStructure createTreeStructure() {
        return new SafetySectionPreviewTreeStructure(myProject);
    }

    public void collapseAll() {
        int row = getTree().getRowCount() - 1;
        while (row > 0) {
            getTree().collapseRow(row);
            row--;
        }
    }

    private static final class MyComparator implements Comparator<NodeDescriptor> {
        public static final Comparator<NodeDescriptor> ourInstance = new SafetySectionPreviewTreeBuilder.MyComparator();

        @Override
        public int compare(NodeDescriptor descriptor1, NodeDescriptor descriptor2) {
            int weight1 = descriptor1.getWeight();
            int weight2 = descriptor2.getWeight();
            if (weight1 != weight2) {
                return weight1 - weight2;
            }
            else {
                return descriptor1.getIndex() - descriptor2.getIndex();
            }
        }
    }

}
