package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class RootNode extends BaseNode {
    protected RootNode(Project project, Object o, AbstractTreeBuilder builder) {
        super(project, o, builder);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<>();

        childrenNodes.add(new PlainTextNode(myProject,
                "Follow the steps below to create a data safety label for your app.", myBuilder));
        childrenNodes.add(new CollectedByAppNode(myProject, DataPracticeGroup.DETECTED_SOURCE, myBuilder));
        childrenNodes.add(new CollectedByAppNode(myProject, DataPracticeGroup.MANUALLY_ADDED_SOURCE, myBuilder));
        childrenNodes.add(new CollectedByAppNode(myProject, DataPracticeGroup.DETECTED_SINK, myBuilder));
        childrenNodes.add(new CollectedByAppNode(myProject, DataPracticeGroup.MANUALLY_ADDED_SINK, myBuilder));

        childrenNodes.add(new CollectedByLibTypeNode(myProject, CollectionType.COLLECTED_BY_LIB, myBuilder));
        childrenNodes.add(new PlainTextNode(myProject,
                "6. Preview the label under the \"Label Preview\" tab", myBuilder));
        childrenNodes.add(new PlainTextNode(myProject,
                "7. Click the \"Generate Data Safety Section CSV\" button to generate the csv file and upload it on the Google Play console.", myBuilder));
        return childrenNodes;
    }

    @Override
    protected void update(PresentationData presentationData) {

    }
}
