package org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import com.intellij.usageView.UsageTreeColors;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class SearchKeywordsNode extends BaseNode implements HighlightedRegionProvider {
    public String keywords;
    public String searchText;
    public String definitionText;

    private final ArrayList<HighlightedRegion> myHighlightedRegions;

    protected SearchKeywordsNode(Project project, String keywords,
                                 AbstractTreeBuilder builder, String searchText, String definitionText) {
        super(project, keywords, builder);
        this.keywords = keywords;
        this.searchText = searchText;
        this.definitionText = definitionText;
        myHighlightedRegions = new ArrayList<>();
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        myHighlightedRegions.clear();

        myHighlightedRegions.add(new HighlightedRegion(0, searchText.length(), new TextAttributes()));
        myHighlightedRegions.add(new HighlightedRegion(searchText.length() + 1,
                searchText.length() + 1 + definitionText.length(),
                UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()));

        presentation.setPresentableText(String.format("%s %s", searchText, definitionText));
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }
}
