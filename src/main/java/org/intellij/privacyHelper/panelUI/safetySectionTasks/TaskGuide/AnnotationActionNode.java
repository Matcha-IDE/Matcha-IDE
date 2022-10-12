package org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.instances.SensitiveAPIInstance;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.codeInspection.utils.PersonalDataGroup;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.AnnotationNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.DataPracticeGroup;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil.NetworkKeywordRegex;
import static org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil.ReviewCompleteAnnotationText;

public class AnnotationActionNode extends BaseNode {
    public final DataPracticeGroup myStage;
    public boolean todo;
    boolean hasMatch = false;

    Set<String> unusedDataCategories;
    Set<PersonalDataGroup> unusedDataGroups;
    Map<PersonalDataGroup, Boolean> dataGroupOccurrenceCount;

    protected AnnotationActionNode(Project project, int typeId, DataPracticeGroup stage, AbstractTreeBuilder builder, boolean todo) {
        super(project, typeId, builder);
        myStage = stage;
        this.todo = todo;
    }

    protected AnnotationActionNode(Project project, int typeId, DataPracticeGroup stage, AbstractTreeBuilder builder,
                                   boolean todo, boolean hasMatch) {
        super(project, typeId, builder);
        myStage = stage;
        this.todo = todo;
        this.hasMatch = hasMatch;
    }

    protected AnnotationActionNode(Project project, int typeId, DataPracticeGroup stage, AbstractTreeBuilder builder,
                                   boolean todo, Set<String> unusedDataCategories,
                                   Set<PersonalDataGroup> unusedDataGroups,
                                   Map<PersonalDataGroup, Boolean> dataGroupOccurrenceCount) {
        super(project, typeId, builder);
        myStage = stage;
        this.todo = todo;
        this.unusedDataCategories = unusedDataCategories;
        this.unusedDataGroups = unusedDataGroups;
        this.dataGroupOccurrenceCount = dataGroupOccurrenceCount;
        // aggregate all the booleans of dataGroupOccurrenceCount by and them
        this.hasMatch = dataGroupOccurrenceCount.values().stream().reduce(false, (a, b) -> a || b);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<>();
        if (myStage == DataPracticeGroup.DETECTED_SOURCE || myStage == DataPracticeGroup.DETECTED_SINK) {
            Vector<SensitiveAPIInstance> sensitiveAPIInstances;
            if (myStage == DataPracticeGroup.DETECTED_SOURCE) {
                sensitiveAPIInstances = PrivacyPracticesHolder.getInstance(getProject()).getSourceAPICallInstances();
            } else {
                sensitiveAPIInstances = PrivacyPracticesHolder.getInstance(getProject()).getSinkAPICallInstances();
            }
            for (SensitiveAPIInstance dataInstance : sensitiveAPIInstances) {
                if (dataInstance.hasError && todo || !dataInstance.hasError && !todo) {
                    childrenNodes.add(new AnnotationNode(myProject, dataInstance, myStage, myBuilder));
                }
            }
        } else {
            Vector<AnnotationInstance> annotationInstances;
            if (todo) {
                if (myStage == DataPracticeGroup.MANUALLY_ADDED_SOURCE) {
                    for (String category : unusedDataCategories) {
                        Map<PersonalDataGroup, Boolean> occurrenceMap = new HashMap<>();
                        ArrayList<PersonalDataGroup> dataGroups = new ArrayList<>();
                        for (PersonalDataGroup dataGroup : unusedDataGroups) {
                            if (category.equals(CoconutUIUtil.prettifyDataCategoryString(dataGroup.toString()))) {
                                occurrenceMap.put(dataGroup,
                                        dataGroupOccurrenceCount.getOrDefault(dataGroup, false));
                                dataGroups.add(dataGroup);
                            }
                        }
                        childrenNodes.add(new SearchDataCategoryKeywordNode(myProject, category, myBuilder, dataGroups,
                                occurrenceMap));
                    }
                } else {
                    childrenNodes.add(new SearchKeywordsNode(myProject, NetworkKeywordRegex, myBuilder,
                            String.format("Search for possible data transmissions%s",
                                    hasMatch ? " (found keyword matches)" : ""),
                            ""));
                }
            } else {
                if (myStage == DataPracticeGroup.MANUALLY_ADDED_SOURCE) {
                    annotationInstances = PrivacyPracticesHolder.getInstance(getProject())
                            .getManuallyAddedAnnotationsByType(CoconutAnnotationType.DataAccess);
                } else {
                    annotationInstances = PrivacyPracticesHolder.getInstance(getProject())
                            .getManuallyAddedAnnotationsByType(CoconutAnnotationType.DataTransmission);
                }
                for (AnnotationInstance annotationInstance : annotationInstances) {
                    childrenNodes.add(new AnnotationNode(myProject, annotationInstance, myStage, myBuilder));
                }
            }
        }
        return childrenNodes;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        switch (myStage) {
            case DETECTED_SOURCE:
            case DETECTED_SINK:
                if (todo) {
                    String text = "Add required annotations";
                    int childrenSize = getChildren().size();
                    String todo = String.format("(%d %s)", childrenSize, childrenSize > 1 ? "todos" : "todo");
                    presentation.setPresentableText(String.format("%s %s", text, todo));
                } else {
                    presentation.setPresentableText(ReviewCompleteAnnotationText);
                }
                break;
            case MANUALLY_ADDED_SOURCE:
                if (todo) {
                    presentation.setPresentableText(String.format("Annotate other data access variables;%s",
                            hasMatch ? " (found keyword matches)" : ""));
                } else {
                    presentation.setPresentableText(ReviewCompleteAnnotationText);
                }
                break;
            case MANUALLY_ADDED_SINK:
                if (todo) {
                    presentation.setPresentableText(String.format("Annotate other data transmission variables;%s",
                            hasMatch ? " (found keyword matches)" : ""));
                } else {
                    presentation.setPresentableText(ReviewCompleteAnnotationText);
                }
                break;
        }
    }
}
