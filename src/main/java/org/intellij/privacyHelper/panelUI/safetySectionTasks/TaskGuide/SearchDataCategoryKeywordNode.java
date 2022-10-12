package org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import com.intellij.usageView.UsageTreeColors;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.codeInspection.utils.Constants;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchDataCategoryKeywordNode extends BaseNode implements HighlightedRegionProvider {
    Map<PersonalDataGroup, Boolean> dataGroupOccurrenceCount;
    ArrayList<PersonalDataGroup> dataGroups;
    String dataCategory;

    private final ArrayList<HighlightedRegion> myHighlightedRegions;

    protected SearchDataCategoryKeywordNode(Project project, String dataCategory, AbstractTreeBuilder builder,
                                            ArrayList<PersonalDataGroup> dataGroups,
                                            Map<PersonalDataGroup, Boolean> dataGroupOccurrenceCount) {
        super(project, dataCategory, builder);
        this.dataCategory = dataCategory;
        this.dataGroups = dataGroups;
        this.dataGroupOccurrenceCount = dataGroupOccurrenceCount;
        myHighlightedRegions = new ArrayList<>();
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> children = new ArrayList<>();
        for (PersonalDataGroup dataGroup : dataGroups) {
            String dataType = CoconutUIUtil.prettifyDataTypeString(dataGroup.toString());
            children.add(new SearchKeywordsNode(myProject, CoconutUIUtil.getKeywordRegex(dataGroup), myBuilder,
                    String.format("Search for %s%s", dataType,
                            dataGroupOccurrenceCount.getOrDefault(dataGroup, false) ? " (found keyword matches)" : ""),
                    Constants.DESCRIPTION_MAPPING.get(dataGroup.toString())));
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        myHighlightedRegions.clear();

        boolean hasMatch = dataGroupOccurrenceCount.values().stream().reduce(false, (a, b) -> a || b);
        String searchText = String.format("Search for %s usage%s", dataCategory,
                hasMatch ? " (found keyword matches)" : "");

        String definitionText = String.format("includes: %s",
                dataGroups.stream().map(d -> CoconutUIUtil.prettifyDataTypeString(d.toString()))
                        .collect(Collectors.joining(",")));
        myHighlightedRegions.add(new HighlightedRegion(0, searchText.length(), new TextAttributes()));
        myHighlightedRegions.add(new HighlightedRegion(
                searchText.length() + 1, searchText.length() + 1 + definitionText.length(),
                UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()));

        presentation.setPresentableText(String.format("%s %s", searchText, definitionText));
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }
}
