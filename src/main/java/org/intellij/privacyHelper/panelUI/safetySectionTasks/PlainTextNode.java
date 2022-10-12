package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class PlainTextNode extends BaseNode {
    String text;

    public PlainTextNode(Project project, String text, AbstractTreeBuilder builder) {
        super(project, text, builder);
        this.text = text;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setPresentableText(text);
    }
}
