package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SafetySectionPreviewTreeStructure extends AbstractTreeStructureBase {
    protected SafetySectionPreviewTreeBuilder myBuilder;
    protected AbstractTreeNode myRootElement;

    protected SafetySectionPreviewTreeStructure(Project project) {
        super(project);
    }

    final void setTreeBuilder(SafetySectionPreviewTreeBuilder builder) {
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
