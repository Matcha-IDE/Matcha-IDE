package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.find.FindModel;
import com.intellij.find.findInProject.FindInProjectManager;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DataManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.codeInspection.utils.TooltipInfo;
import org.intellij.privacyHelper.panelUI.PrivacyCheckerCompositeRenderer;
import org.intellij.privacyHelper.panelUI.PsiElementNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide.GuideRootNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.TaskGuide.SearchKeywordsNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SafetySectionTaskGuidePanel extends SimpleToolWindowPanel implements OccurenceNavigator, DataProvider, Disposable {
    private Project myProject;

    private final Tree myTaskGuideTree;
    private final MyGuideTreeExpander myTaskGuideTreeExpander;
    protected final SafetySectionTaskGuideTreeBuilder myTaskGuideTreeBuilder;
    private final float splitterOverviewProportion = 0.65f;

    private final HelpMessagePanel detectedAccessAnnotationHelpMessagePanel;
    private final HelpMessagePanel additionalAccessAnnotationHelpMessagePanel;
    private final HelpMessagePanel detectedTransmissionAnnotationHelpMessagePanel;
    private final HelpMessagePanel additionalTransmissionAnnotationHelpMessagePanel;
    private final HelpMessagePanel libraryHelpMessagePanel;
    private Splitter splitter;

    public SafetySectionTaskGuidePanel(Project project) {
        super(false, true);
        myProject = project;

        detectedAccessAnnotationHelpMessagePanel = new DetectedAccessAnnotationHelpPanel();
        detectedTransmissionAnnotationHelpMessagePanel = new DetectedTransmissionAnnotationHelpPanel();
        additionalAccessAnnotationHelpMessagePanel = new AdditionalAccessAnnotationHelpPanel();
        additionalTransmissionAnnotationHelpMessagePanel = new AdditionalTransmissionAnnotationHelpPanel();
        libraryHelpMessagePanel = new LibHelpPanel();

        DefaultTreeModel guideModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        myTaskGuideTree = new Tree(guideModel);
        myTaskGuideTreeBuilder = new SafetySectionTaskGuideTreeBuilder(myTaskGuideTree, guideModel, myProject);
        myTaskGuideTreeBuilder.init();
        myTaskGuideTreeExpander = new MyGuideTreeExpander();
        initGuideTree();

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup rightGroup = new DefaultActionGroup();
        AnAction expandAllAction = CommonActionsManager.getInstance().createExpandAllAction(myTaskGuideTreeExpander, this);
        rightGroup.add(expandAllAction);

        AnAction collapseAllAction = CommonActionsManager.getInstance().createCollapseAllAction(myTaskGuideTreeExpander, this);
        rightGroup.add(collapseAllAction);

        toolBarPanel.add(
                ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, rightGroup, false).getComponent());

        setToolbar(toolBarPanel);

        JPanel panel = new JPanel(new BorderLayout());
        splitter = new OnePixelSplitter(false, splitterOverviewProportion);

        setupHelpPanel(detectedAccessAnnotationHelpMessagePanel);
        setupHelpPanel(additionalAccessAnnotationHelpMessagePanel);
        setupHelpPanel(detectedTransmissionAnnotationHelpMessagePanel);
        setupHelpPanel(additionalTransmissionAnnotationHelpMessagePanel);
        setupHelpPanel(libraryHelpMessagePanel);

        splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myTaskGuideTree));
        splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(detectedAccessAnnotationHelpMessagePanel));

        panel.add(splitter);
        setContent(panel);
    }

    private void setupHelpPanel(HelpMessagePanel helpMessagePanel) {
        helpMessagePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        helpMessagePanel.update();
    }

    private void initGuideTree() {
        UIUtil.setLineStyleAngled(myTaskGuideTree);
        myTaskGuideTree.setShowsRootHandles(true);
        myTaskGuideTree.setRootVisible(false);
        myTaskGuideTree.setCellRenderer(new PrivacyCheckerCompositeRenderer());
        EditSourceOnDoubleClickHandler.install(myTaskGuideTree);
        new TreeSpeedSearch(myTaskGuideTree);

        myTaskGuideTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() > 1) {
                    SwingUtilities.invokeLater(() ->
                      ApplicationManager.getApplication().runWriteAction(() -> {
                          TreePath treePath = myTaskGuideTree.getSelectionPath();
                          if (treePath == null) {
                              return;
                          }
                          final Object object = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
                          if (object instanceof SearchKeywordsNode) {
                              final SearchKeywordsNode searchNode = (SearchKeywordsNode) object;
                              FindInProjectManager findManager = FindInProjectManager.getInstance(myProject);

                              FindModel findModel = CoconutUIUtil.getFindModel(searchNode.keywords);
                              findManager.findInProject(DataManager.getInstance().getDataContext(SafetySectionTaskGuidePanel.this), findModel);
                          }
                      }));
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        myTaskGuideTree.getSelectionModel().addTreeSelectionListener(e -> SwingUtilities.invokeLater(() ->
                ApplicationManager.getApplication().runWriteAction(() -> {
                    TreePath treePath = myTaskGuideTree.getSelectionPath();
                    if (treePath == null) {
                        return;
                    }
                    final Object object = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
                    if (!(object instanceof PsiElementNode) && !(object instanceof LibraryNode) && !(object instanceof AnnotationNode)) {
                        return;
                    }
                    if (object instanceof AnnotationNode) {
                        final AnnotationNode annotationNode = (AnnotationNode) object;
                        if (annotationNode.myAPICallInstance != null) {
                            if (annotationNode.myAPICallInstance.annotationMetaDataList != null
                                    && annotationNode.myAPICallInstance.annotationMetaDataList.length > 0) {
                                if (annotationNode.myAPICallInstance.annotationMetaDataList[0].psiAnnotationPointer
                                        != null) {
                                    CoconutUIUtil.navigateMainEditorToPsiElement(
                                            annotationNode.myAPICallInstance.annotationMetaDataList[0]
                                                    .psiAnnotationPointer);
                                } else {
                                    String id;
                                    String header;
                                    String text;
                                    if (annotationNode.myAPICallInstance.getSensitiveAPI().isPersonalDataSource()) {
                                        id = "matcha.features.panel.step_required_access";
                                        header = "Use quickfix to add your first @DataAccess";
                                        text = "This API call may access user data that needs to be reported in the " +
                                                "data safety label.";
                                    } else {
                                        id = "matcha.features.panel.step_required_transmission";
                                        header = "Use quickfix to add your first @DataTransmission";
                                        text = "This API call may send user data out of your app that needs to be " +
                                                "reported as data collection/sharing in the data safety label.";
                                    }
                                    CoconutUIUtil.navigateMainEditorToPsiElement(
                                            annotationNode.myAPICallInstance.psiElementPointer, () -> {
                                                if (annotationNode.myAPICallInstance
                                                        .psiElementPointer.getElement() != null) {
                                                    CoconutUIUtil.showTooltip(new TooltipInfo[]{
                                                            new TooltipInfo(id, header, text,
                                                                    Balloon.Position.atLeft,
                                                                    annotationNode.getProject(),
                                                                    annotationNode.myAPICallInstance
                                                                    .psiElementPointer.getElement())
                                                    });
                                                }
                                            });
                                }
                            } else {
                                CoconutUIUtil.navigateMainEditorToPsiElement(
                                        annotationNode.myAPICallInstance.psiElementPointer);
                            }
                        }
                    } else if (object instanceof PsiElementNode) {
                        final PsiElementNode psiElementNode = (PsiElementNode) object;
                        if (psiElementNode.getValue() instanceof SmartPsiElementPointer) {
                            SmartPsiElementPointer smartPsiElementPointer = (SmartPsiElementPointer) psiElementNode.getValue();
                            if (smartPsiElementPointer == null) {
                                return;
                            }
                            CoconutUIUtil.navigateMainEditorToPsiElement(smartPsiElementPointer);
                        } else if (psiElementNode.getValue() instanceof PsiElement) {
                            PsiElement psiElement = (PsiElement) psiElementNode.getValue();
                            if (psiElement == null) {
                                return;
                            }
                            CoconutUIUtil.navigateMainEditorToPsiElement(psiElement);
                        }
                    } else {
                        final LibraryNode libraryNode = (LibraryNode) object;
                        if (libraryNode.customDataInstance == null) {
                            return;
                        }
                        PsiElement psiElement = libraryNode.customDataInstance.getPsiElementPointer().getElement();
                        if (psiElement == null) {
                            return;
                        }
                        String dataTagId = "matcha.features.panel.step_library";
                        String dataTagHeader = "Check 3rd-party SDK custom usage";
                        String dataTagText = "3rd-party SDK's data usage may depend on how your app uses the SDK. Check the " +
                                "data usage condition in each &lt;data&gt; tag; Keep the tag if it matches your app's " +
                                "usage of this SDK; Remove it otherwise.";
                        String verifiedId = "matcha.features.panel.step_verified";
                        String verifiedHeader = "";
                        String verifiedText = "After checking all the &lt;data&gt; tags, set the verified attribute to " +
                                "\"true\".";
                        PsiElement targetDataTagElement = psiElement;
                        XmlTag dataTag = PsiTreeUtil.getChildOfType(psiElement, XmlTag.class);
                        if (dataTag != null) {
                            XmlText textNode = PsiTreeUtil.getChildOfType(dataTag, XmlText.class);
                            if (textNode != null) {
                                PsiWhiteSpace dataTagFirstWhiteSpace =
                                        PsiTreeUtil.getChildOfType(textNode, PsiWhiteSpace.class);
                                if (dataTagFirstWhiteSpace != null && dataTagFirstWhiteSpace.getNextSibling() != null) {
                                    targetDataTagElement = dataTagFirstWhiteSpace.getNextSibling();
                                }
                            }
                        }
                        // get all the attributes of psiElement
                        XmlAttribute[] attributes = PsiTreeUtil.getChildrenOfType(psiElement, XmlAttribute.class);
                        // find the attribute with the name of "verified"
                        PsiElement verifiedTargetElement = psiElement;
                        if (attributes != null) {
                            for (XmlAttribute attribute : attributes) {
                                if ("verified".equals(attribute.getName())) {
                                    verifiedTargetElement = attribute;
                                    break;
                                }
                            }
                        }


                        PsiElement finalDataTagTargetElement = targetDataTagElement;
                        PsiElement finalVerifiedTargetElement = verifiedTargetElement;
                        CoconutUIUtil.navigateMainEditorToPsiElement(
                                psiElement, () -> {
                                    CoconutUIUtil.showTooltip(new TooltipInfo[]{
                                            new TooltipInfo(dataTagId, dataTagHeader, dataTagText,
                                                    Balloon.Position.atLeft,
                                                    psiElement.getProject(), finalDataTagTargetElement),
                                            new TooltipInfo(verifiedId, verifiedHeader, verifiedText,
                                                    Balloon.Position.above,
                                                    psiElement.getProject(), finalVerifiedTargetElement)
                                    });
                                });

                    }
                })));
    }

    public void refreshTreeView(boolean initFlowAnalysis) {
        myTaskGuideTreeBuilder.queueUpdate();
        if (initFlowAnalysis) {
            // TODO: init static flow analysis
        }
    }

    @Override
    public boolean hasNextOccurence() {
        return false;
    }

    @Override
    public boolean hasPreviousOccurence() {
        return false;
    }

    @Override
    public OccurenceInfo goNextOccurence() {
        return null;
    }

    @Override
    public OccurenceInfo goPreviousOccurence() {
        return null;
    }

    @Override
    public @NlsActions.ActionText @NotNull String getNextOccurenceActionName() {
        return null;
    }

    @Override
    public @NlsActions.ActionText @NotNull String getPreviousOccurenceActionName() {
        return null;
    }

    @Override
    public void dispose() {

    }

    public void updateTreeStatus(boolean isAnnotation, boolean isSource, boolean isRequired) {
        GuideRootNode guideRootNode = (GuideRootNode) myTaskGuideTreeBuilder.getRootElement();
        guideRootNode.setCondition(isAnnotation, isSource, isRequired);
        refreshTreeView(false);
        if (isAnnotation) {
            if (isRequired) {
                if (isSource) {
                    splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(detectedAccessAnnotationHelpMessagePanel));
                } else {
                    splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(detectedTransmissionAnnotationHelpMessagePanel));
                }
            } else {
                if (isSource) {
                    splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(additionalAccessAnnotationHelpMessagePanel));
                } else {
                    splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(additionalTransmissionAnnotationHelpMessagePanel));
                }
            }
        } else {
            splitter.setSecondComponent(ScrollPaneFactory.createScrollPane(libraryHelpMessagePanel));
        }
    }

    private final class MyGuideTreeExpander implements TreeExpander {

        MyGuideTreeExpander() {
        }

        @Override
        public boolean canCollapse() {
            return true;
        }

        @Override
        public boolean canExpand() {
            return true;
        }

        @Override
        public void collapseAll() {
            myTaskGuideTreeBuilder.collapseAll();
        }

        @Override
        public void expandAll() {myTaskGuideTreeBuilder.expandAll(null);
        }
    }
}
