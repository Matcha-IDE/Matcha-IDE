package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;

public class SafetySectionTasksTreeBuilder extends AbstractTreeBuilder {
    protected final Project myProject;

    public SafetySectionTasksTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
        super(tree, treeModel, null, MyComparator.ourInstance, false);

        myProject = project;
    }

    public final void init() {
        SafetySectionTasksTreeStructure safetySectionTasksTreeStructure = createTreeStructure();
        setTreeStructure(safetySectionTasksTreeStructure);
        safetySectionTasksTreeStructure.setTreeBuilder(this);

        initRootNode();
    }

    @NotNull
    protected SafetySectionTasksTreeStructure createTreeStructure() {
        return new SafetySectionTasksTreeStructure(myProject);
    }

    public void collapseAll() {
        int row = getTree().getRowCount() - 1;
        while (row > 0) {
            getTree().collapseRow(row);
            row--;
        }
    }

    private static final class MyComparator implements Comparator<NodeDescriptor> {
        public static final Comparator<NodeDescriptor> ourInstance = new SafetySectionTasksTreeBuilder.MyComparator();

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
