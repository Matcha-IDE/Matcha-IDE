package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide.GuideRootNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SafetySectionTaskGuideTreeStructure extends AbstractTreeStructureBase {
    protected SafetySectionTaskGuideTreeBuilder myBuilder;
    protected AbstractTreeNode myRootElement;

    protected SafetySectionTaskGuideTreeStructure(Project project) {
        super(project);
    }

    final void setTreeBuilder(SafetySectionTaskGuideTreeBuilder builder) {
        myBuilder = builder;
        myRootElement = createRootElement();
    }

    public AbstractTreeNode createRootElement() {
        return new GuideRootNode(myProject, new Object(), myBuilder);
    }

    @Override
    public @Nullable List<TreeStructureProvider> getProviders() {
        return null;
    }

    @Override
    public @NotNull Object getRootElement() {
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
