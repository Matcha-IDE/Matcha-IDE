package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.codeInspection.utils.ThirdPartyCustomDataInstance;
import org.intellij.privacyHelper.codeInspection.utils.ThirdPartySafetySectionInfo;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CollectedByLibTypeNode extends BaseNode implements HighlightedRegionProvider {
    private final ArrayList<HighlightedRegion> myHighlightedRegions = new ArrayList<>();
    HashMap<String, ArrayList<ThirdPartySafetySectionInfo>> libraryDataMap = new HashMap<>();

    public CollectedByLibTypeNode(Project myProject, CollectionType collectedByLib, AbstractTreeBuilder myBuilder) {
        super(myProject, collectedByLib, myBuilder);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        return new ArrayList<>();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        myHighlightedRegions.clear();

        CoconutUIUtil.updateLibraryMaps(myProject, libraryDataMap);

        ApplicationManager.getApplication().invokeLater(
                () -> ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        CoconutUIUtil.checkLibraryConfigFileCompletenessAndUpdate(myProject, libraryDataMap, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

        Set<String> libNames = Arrays.stream(
                PrivacyPracticesHolder.getInstance(myProject).getThirdPartyDependencyInstances())
                .filter(d -> d.getDependencyInfo() != null).map(d -> d.getDependencyInfo().libName)
                .collect(Collectors.toSet());
        String name = "5. Verify library data usage config: ";
        long todoCount = libNames.stream().filter(
                l -> {
                    ThirdPartyCustomDataInstance customDataInstance =
                            PrivacyPracticesHolder.getInstance(myProject).getThirdPartyCustomDataInstance(l);
                    return (customDataInstance != null && !customDataInstance.verified);
                }).count();
        String todo = String.format("%d %s verification", todoCount, todoCount > 1 ? "need" : "needs");
        String all = String.format(" out of %d detected.", libNames.size());
        CoconutUIUtil.labelErrorText(myHighlightedRegions, name, todo, all,todoCount > 0);
        presentation.setPresentableText(String.format("%s%s%s", name, todo, all));
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }
}
