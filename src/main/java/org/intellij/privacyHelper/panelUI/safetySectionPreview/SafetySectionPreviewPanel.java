package org.intellij.privacyHelper.panelUI.safetySectionPreview;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.NlsActions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyHelper.panelUI.PrivacyCheckerCompositeRenderer;
import org.intellij.privacyHelper.panelUI.PsiElementNode;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.LibraryNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class SafetySectionPreviewPanel extends SimpleToolWindowPanel implements OccurenceNavigator, DataProvider, Disposable {

    private Project myProject;
    private final Tree myTree;
    private final MyTreeExpander myTreeExpander;
    protected final SafetySectionPreviewTreeBuilder myTreeBuilder;

    public SafetySectionPreviewPanel(Project project) {
        super(false, true);
        myProject = project;
        DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        myTree = new Tree(model);
        myTreeExpander = new MyTreeExpander();
        initUI();
        myTreeBuilder = new SafetySectionPreviewTreeBuilder(myTree, model, myProject);
        myTreeBuilder.init();
    }

    private void initUI() {
        UIUtil.setLineStyleAngled(myTree);
        myTree.setShowsRootHandles(true);
        myTree.setRootVisible(false);
        myTree.setCellRenderer(new PrivacyCheckerCompositeRenderer());
        EditSourceOnDoubleClickHandler.install(myTree);
        new TreeSpeedSearch(myTree);

        setContent(createCenterComponent());

        myTree.getSelectionModel().addTreeSelectionListener(e -> SwingUtilities.invokeLater(() ->
                ApplicationManager.getApplication().runWriteAction(() -> {
                    TreePath treePath = myTree.getSelectionPath();
                    if (treePath == null) {
                        return;
                    }
                    final Object object = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
                    if (!(object instanceof PsiElementNode) && !(object instanceof LibraryNode)) {
                        return;
                    }
                    if (object instanceof PsiElementNode) {
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
                        CoconutUIUtil.navigateMainEditorToPsiElement(psiElement);
                    }
                })));

        JPanel toolBarPanel = new JPanel(new GridLayout());
        DefaultActionGroup rightGroup = new DefaultActionGroup();
        AnAction expandAllAction = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
        rightGroup.add(expandAllAction);

        AnAction collapseAllAction = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this);
        rightGroup.add(collapseAllAction);
        toolBarPanel.add(
                ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, rightGroup, false).getComponent());

        setToolbar(toolBarPanel);
    }

    protected JComponent createCenterComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(ScrollPaneFactory.createScrollPane(myTree), BorderLayout.CENTER);

        JPanel settings = new JPanel();
        settings.setLayout(new BoxLayout(settings, BoxLayout.LINE_AXIS));
        settings.setAlignmentX(LEFT_ALIGNMENT);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(actionEvent -> {
            refreshTreeView();
        });
        settings.add(Box.createHorizontalStrut(10));
        settings.add(refreshButton);

        JButton generatePrivacyLabelButton = new JButton("Generate Data Safety Section CSV");
        generatePrivacyLabelButton.addActionListener(actionEvent -> {
            PrivacyPracticesHolder.getInstance(myProject).generateAndWritePrivacyLabel();
        });
        settings.add(Box.createHorizontalStrut(10));
        settings.add(generatePrivacyLabelButton);

        panel.add(settings, BorderLayout.PAGE_START);
        return panel;
    }

    private void refreshTreeView() {
        myTreeBuilder.queueUpdate();
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

    private final class MyTreeExpander implements TreeExpander {
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
            myTreeBuilder.collapseAll();
        }

        @Override
        public void expandAll() {
            myTreeBuilder.expandAll(null);
        }
    }

}
