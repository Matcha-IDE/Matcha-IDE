package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class CollectedByAppNode extends BaseNode implements HighlightedRegionProvider {
    private final ArrayList<HighlightedRegion> myHighlightedRegions = new ArrayList<>();
    public final DataPracticeGroup myStage;

    CollectedByAppNode(Project project, DataPracticeGroup stage, AbstractTreeBuilder builder) {
        super(project, stage, builder);
        myStage = stage;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AnnotationNode> annotationNodes = new ArrayList<>();
        return annotationNodes;
    }

    @Override
    protected void update(PresentationData presentationData) {
        myHighlightedRegions.clear();
        String name;
        String todo;
        String all;
        long todoCount;
        switch (myStage) {
            case DETECTED_SOURCE:
                name = "1. Annotate detected API calls that access sensitive data types: ";
                todoCount = PrivacyPracticesHolder.getInstance(getProject())
                        .getSourceAPICallInstances().stream().filter(i -> i.hasError).count();
                todo = String.format("%d incomplete", todoCount);
                all = String.format(" out of %d detected.",
                        PrivacyPracticesHolder.getInstance(getProject()).getSourceAPICallInstances().size());
                CoconutUIUtil.labelErrorText(myHighlightedRegions, name, todo, all, todoCount > 0);
                presentationData.setPresentableText(String.format("%s%s%s", name, todo, all));
                break;
            case MANUALLY_ADDED_SOURCE:
                name = "2. Find and annotate other accesses to sensitive data types. (Caution: This step can be slow for large apps)";
                presentationData.setPresentableText(name);
                break;
            case DETECTED_SINK:
                name = "3. Annotate detected API calls that send data off the device/to other apps: ";
                todoCount = PrivacyPracticesHolder.getInstance(getProject())
                        .getSinkAPICallInstances().stream().filter(i -> i.hasError).count();
                todo = String.format("%d incomplete", todoCount);
                all = String.format(" out of %d detected.",
                        PrivacyPracticesHolder.getInstance(getProject()).getSinkAPICallInstances().size());
                CoconutUIUtil.labelErrorText(myHighlightedRegions, name, todo, all, todoCount > 0);
                presentationData.setPresentableText(String.format("%s%s%s", name, todo, all));
                break;
            case MANUALLY_ADDED_SINK:
                name = "4. Find and annotate other code that sends data off the device/to other apps. ";
                presentationData.setPresentableText(name);
                break;
        }
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }
}
