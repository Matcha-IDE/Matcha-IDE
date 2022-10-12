package org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.LibraryNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class LibNode extends BaseNode {
    ArrayList<String> libs;
    String text;

    protected LibNode(Project project, ArrayList<String> libs, AbstractTreeBuilder builder, String text) {
        super(project, libs, builder);
        this.libs = libs;
        this.text = text;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<>();
        for (String libName : libs) {
            childrenNodes.add(new LibraryNode(myProject, libName, myBuilder));
        }
        return childrenNodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setPresentableText(text);
    }
}
