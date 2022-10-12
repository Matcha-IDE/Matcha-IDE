package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static org.intellij.privacyHelper.codeInspection.utils.Constants.safetySectionCollected;
import static org.intellij.privacyHelper.codeInspection.utils.Constants.safetySectionShared;

public class RootNode extends BaseNode {
    protected RootNode(Project project, Object o, AbstractTreeBuilder builder) {
        super(project, o, builder);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        children.add(new ActionTypeNode(myProject, safetySectionShared, myBuilder));
        children.add(new ActionTypeNode(myProject, safetySectionCollected, myBuilder));
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {

    }
}
