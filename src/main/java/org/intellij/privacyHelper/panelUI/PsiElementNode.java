package org.intellij.privacyHelper.panelUI;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.HighlightedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class PsiElementNode extends BaseNode implements HighlightedRegionProvider {
    private final ArrayList<HighlightedRegion> myHighlightedRegions;
    private String mSummary;

    public PsiElementNode(Project project, SmartPsiElementPointer smartPsiElementPointer,
                             AbstractTreeBuilder builder, String summary) {
        super(project, smartPsiElementPointer, builder);
        mSummary = summary;
        myHighlightedRegions = new ArrayList<>();
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(PresentationData presentationData) {
        presentationData.setPresentableText(mSummary);
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }
}
