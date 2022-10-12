package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.todo.HighlightedRegionProvider;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HighlightedRegion;
import com.intellij.ui.JBColor;
import com.intellij.usageView.UsageTreeColors;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyHelper.codeInspection.instances.AnnotationInstance;
import org.intellij.privacyHelper.codeInspection.instances.SensitiveAPIInstance;
import org.intellij.privacyHelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyHelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyHelper.panelUI.BaseNode;
import org.intellij.privacyHelper.panelUI.PsiElementNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static org.intellij.privacyHelper.panelUI.safetySectionTasks.DataPracticeGroup.*;

public class AnnotationNode extends BaseNode implements HighlightedRegionProvider {
    public static final String API_CALL_SUMMARY = "Review the related API call";
    public static final String ANNOTATION_SUMMARY = "Review the annotation";

    public final DataPracticeGroup myStage;
    private final ArrayList<HighlightedRegion> myHighlightedRegions;
    public final @Nullable SensitiveAPIInstance myAPICallInstance; // only when myAnnotationInstance == null
    private final @Nullable AnnotationInstance myAnnotationInstance; // only when myAPICallInstance == null

    public AnnotationNode(Project project, SensitiveAPIInstance sensitiveAPIInstance, DataPracticeGroup stage,
                             AbstractTreeBuilder builder) {
        super(project, sensitiveAPIInstance, builder);
        myStage = stage;
        myAPICallInstance = sensitiveAPIInstance;
        myAnnotationInstance = null;
        myHighlightedRegions = new ArrayList<>();
    }

    public AnnotationNode(Project project, AnnotationInstance annotationInstance,
                          DataPracticeGroup stage, AbstractTreeBuilder builder) {
        super(project, annotationInstance, builder);
        myStage = stage;
        myAPICallInstance = null;
        myAnnotationInstance = annotationInstance;
        myHighlightedRegions = new ArrayList<>();
    }

    @Override
    public Iterable<HighlightedRegion> getHighlightedRegions() {
        return myHighlightedRegions;
    }

    private String getDataUseDescriptionFromAnnotation() {
        AnnotationMetaData[] annotationMetaDataList;
        if (myAPICallInstance == null) {
            annotationMetaDataList = new AnnotationMetaData[]{
                    new AnnotationMetaData(Objects.requireNonNull(myAnnotationInstance).getAnnotationHolder(), null,
                            myAnnotationInstance.getAnnotationSmartPointer())};
        } else {
            annotationMetaDataList = myAPICallInstance.getAnnotationMetaDataList();
        }

        ArrayList<String> descriptions = new ArrayList<>();
        if (myStage == DETECTED_SOURCE || myStage == MANUALLY_ADDED_SOURCE || myStage == ANY_SOURCE) {
            for (AnnotationMetaData annotationMetaData : annotationMetaDataList) {
                if (annotationMetaData.annotationInstance != null) {
                    if (annotationMetaData.annotationInstance.mAnnotationType
                            == CoconutAnnotationType.NotPersonalDataAccess) {
                        descriptions.add("Not personal data.");
                    } else {
                        descriptions.add(annotationMetaData.annotationInstance.getAnnotationShortSummary(
                                myProject, false));
                    }
                }
            }
        } else {
            for (AnnotationMetaData annotationMetaData : annotationMetaDataList) {
                if (annotationMetaData.annotationInstance != null) {
                    if (annotationMetaData.annotationInstance.mAnnotationType
                            == CoconutAnnotationType.NotPersonalDataAccess) {
                        descriptions.add("Not personal data.");
                    } else {
                        if (myAPICallInstance != null) {
                            switch (myAPICallInstance.sensitiveAPI.accessType) {
                                case SENT_OFF_DEVICE:
                                    descriptions.add(String.format("(sent off device) %s.",
                                            annotationMetaData.annotationInstance.getAnnotationShortSummary(
                                                    myProject, false)));
                                    break;
                                case STORED_ON_DEVICE:
                                    descriptions.add(String.format("(stored on device) %s.",
                                            annotationMetaData.annotationInstance.getAnnotationShortSummary(
                                                    myProject, false)));
                                    break;
                                case SHARE_ON_DEVICE:
                                    descriptions.add(String.format("(shared on device) %s.",
                                            annotationMetaData.annotationInstance.getAnnotationShortSummary(
                                                    myProject, false)));
                                case STORED_ON_CLOUD:
                                    descriptions.add(String.format("(stored on cloud) %s.",
                                            annotationMetaData.annotationInstance.getAnnotationShortSummary(
                                                    myProject, false)));
                                    break;
                            }
                        } else {
                            descriptions.add(annotationMetaData.getTransmissionAttributeSummary().toLowerCase());
                        }
                    }
                }
            }
        }
        return String.join(" ", descriptions);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode> getChildren() {
        ArrayList<PsiElementNode> children = new ArrayList<>();

        if (myAPICallInstance == null) {
            children.add(new PsiElementNode(myProject,
                    Objects.requireNonNull(myAnnotationInstance).getAnnotationSmartPointer(),
                    myBuilder, ANNOTATION_SUMMARY));
        } else {
            children.add(new PsiElementNode(myProject, myAPICallInstance.psiElementPointer, myBuilder,
                    API_CALL_SUMMARY));
            if (myAPICallInstance.annotationMetaDataList != null && myAPICallInstance.annotationMetaDataList.length > 0) {
                if (myAPICallInstance.annotationMetaDataList[0].psiAnnotationPointer != null) {
                    children.add(new PsiElementNode(myProject, myAPICallInstance.annotationMetaDataList[0].psiAnnotationPointer,
                            myBuilder, ANNOTATION_SUMMARY));
                }
            }
        }
        return children;
    }

    @Override
    protected void update(@NotNull PresentationData presentationData) {
        myHighlightedRegions.clear();

        TextAttributes redText = new TextAttributes();
        redText.setForegroundColor(JBColor.RED);

        TextAttributes annotationSummaryText = new TextAttributes();
        annotationSummaryText.setForegroundColor(new JBColor(new Color(12, 44, 160), new Color(12, 44, 160)));

        if (myAPICallInstance == null) {
            String statusTag = getDataUseDescriptionFromAnnotation();

            String basePath = Objects.requireNonNull(myAnnotationInstance).getAnnotationSmartPointer().getProject().getBasePath();
            assert basePath != null;
            String filePath = Objects.requireNonNull(myAnnotationInstance).getAnnotationSmartPointer().getVirtualFile().getPath().substring(
                    basePath.length() + 1);
            if (myAnnotationInstance.getAnnotationSmartPointer().getElement() == null) {
                return;
            }

            String lineNumber = String.valueOf(CodeInspectionUtil.getElementLineNumber(
                    Objects.requireNonNull(myAnnotationInstance).getAnnotationSmartPointer().getElement()));
            String fileLocation = String.format("(%s:%s)", filePath, lineNumber);

            myHighlightedRegions.add(new HighlightedRegion(0, statusTag.length(), annotationSummaryText));
            myHighlightedRegions.add(new HighlightedRegion(
                    statusTag.length() + 1,
                    statusTag.length() + 1 + fileLocation.length(),
                    UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()));
            presentationData.setPresentableText(String.format("%s %s", statusTag, fileLocation));
        } else {
            String basePath = myAPICallInstance.getPsiElementPointer().getProject().getBasePath();
            assert basePath != null;
            String filePath = myAPICallInstance.getPsiElementPointer().getVirtualFile().getPath().substring(
                    basePath.length() + 1);
            if (myAPICallInstance.getPsiElementPointer().getElement() == null) {
                return;
            }
            String statusTag = myAPICallInstance.hasError ? String.format("Todo: %s", myAPICallInstance.todoDescription)
                    : getDataUseDescriptionFromAnnotation();
            String lineNumber = String.valueOf(CodeInspectionUtil.getElementLineNumber(myAPICallInstance.getPsiElementPointer().getElement()));
            String ApiCallText = CodeInspectionUtil.getApiCallInstanceText(myAPICallInstance);
            String fileLocation = String.format("(%s:%s)", filePath, lineNumber);

            myHighlightedRegions.add(new HighlightedRegion(
                    0,
                    statusTag.length(),
                    myAPICallInstance.hasError ? redText : annotationSummaryText));
            myHighlightedRegions.add(new HighlightedRegion(
                    statusTag.length() + 1,
                    statusTag.length() + 1 + ApiCallText.length(),
                    new TextAttributes()));
            myHighlightedRegions.add(new HighlightedRegion(
                    statusTag.length() + 1 + ApiCallText.length() + 1,
                    statusTag.length() + 1 + ApiCallText.length() + 1 + fileLocation.length(),
                    UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()));

            presentationData.setPresentableText(String.format("%s %s %s", statusTag, ApiCallText, fileLocation));
        }
    }
}
