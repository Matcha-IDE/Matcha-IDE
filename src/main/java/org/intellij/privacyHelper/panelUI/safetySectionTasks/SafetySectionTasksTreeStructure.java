package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SafetySectionTasksTreeStructure extends AbstractTreeStructureBase {
    protected SafetySectionTasksTreeBuilder myBuilder;
    protected AbstractTreeNode myRootElement;

    protected SafetySectionTasksTreeStructure(Project project) {
        super(project);
    }

    final void setTreeBuilder(SafetySectionTasksTreeBuilder builder) {
        myBuilder = builder;
        myRootElement = createRootElement();
    }

    public AbstractTreeNode createRootElement() {
        return new RootNode(myProject, new Object(), myBuilder);
    }

    @Override
    public @Nullable List<TreeStructureProvider> getProviders() {
        return null;
    }

    @Override
    public Object getRootElement() {
        return myRootElement;
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }
}
