package org.intellij.privacyHelper.panelUI.safetySectionTasks;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import org.intellij.privacyHelper.codeInspection.state.PrivacyPracticesHolder;
import org.intellij.privacyHelper.panelUI.PrivacyCheckerCompositeRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class SafetySectionTasksPanel extends SimpleToolWindowPanel implements OccurenceNavigator, DataProvider, Disposable {

    private Project myProject;
    private final Tree myOverviewTree;
    protected final SafetySectionTasksTreeBuilder myOverviewTreeBuilder;
    private final float splitterOverviewProportion = 0.45f;

    private final SafetySectionTaskGuidePanel mySafetySectionTaskGuidePanel;

    public SafetySectionTasksPanel(Project project) {
        super(false, true);
        myProject = project;

        DefaultTreeModel overviewModel = new DefaultTreeModel(new DefaultMutableTreeNode());

        mySafetySectionTaskGuidePanel = new SafetySectionTaskGuidePanel(myProject);

        myOverviewTree = new Tree(overviewModel);
        myOverviewTreeBuilder = new SafetySectionTasksTreeBuilder(myOverviewTree, overviewModel, myProject);
        myOverviewTreeBuilder.init();
        initOverviewUI();
    }

    private void initOverviewUI() {
        UIUtil.setLineStyleAngled(myOverviewTree);
        myOverviewTree.setShowsRootHandles(true);
        myOverviewTree.setRootVisible(false);
        myOverviewTree.setCellRenderer(new PrivacyCheckerCompositeRenderer());
        EditSourceOnDoubleClickHandler.install(myOverviewTree);
        new TreeSpeedSearch(myOverviewTree);

        setContent(createHelpVersionCenterComponent());

        myOverviewTree.getSelectionModel().addTreeSelectionListener(e -> SwingUtilities.invokeLater(() ->
                ApplicationManager.getApplication().runWriteAction(() -> {
            TreePath treePath = myOverviewTree.getSelectionPath();
            if (treePath == null) {
                return;
            }
            final Object object = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
            if (object instanceof CollectedByAppNode || object instanceof CollectedByLibTypeNode) {
                if (object instanceof CollectedByAppNode) {
                    CollectedByAppNode collectedByAppNode = (CollectedByAppNode) object;
                    if (collectedByAppNode.myStage == DataPracticeGroup.DETECTED_SOURCE) {
                        mySafetySectionTaskGuidePanel.updateTreeStatus(true, true, true);
                    } else if (collectedByAppNode.myStage == DataPracticeGroup.MANUALLY_ADDED_SOURCE) {
                        mySafetySectionTaskGuidePanel.updateTreeStatus(true, true, false);
                    } else if (collectedByAppNode.myStage == DataPracticeGroup.DETECTED_SINK) {
                        mySafetySectionTaskGuidePanel.updateTreeStatus(true, false, true);
                    } else if (collectedByAppNode.myStage == DataPracticeGroup.MANUALLY_ADDED_SINK) {
                        mySafetySectionTaskGuidePanel.updateTreeStatus(true, false, false);
                    }
                } else {
                    mySafetySectionTaskGuidePanel.updateTreeStatus(false, false, false);
                }
                setContent(createGuideVersionCenterComponent());
            } else if (object instanceof PlainTextNode) {
                setContent(createHelpVersionCenterComponent());
            }
        })));
    }

    protected JComponent createGuideVersionCenterComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        Splitter splitter = new OnePixelSplitter(false, splitterOverviewProportion);

        splitter.setSecondComponent(mySafetySectionTaskGuidePanel);
        return getPanel(panel, splitter);
    }

    protected JComponent createHelpVersionCenterComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        return getPanel(panel, ScrollPaneFactory.createScrollPane(myOverviewTree));
    }


    private JPanel getSettings() {
        JPanel settings = new JPanel();
        settings.setLayout(new BoxLayout(settings, BoxLayout.LINE_AXIS));
        settings.setAlignmentX(LEFT_ALIGNMENT);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(actionEvent -> {
            mySafetySectionTaskGuidePanel.refreshTreeView(false);
            refreshTreeView(false);
        });
        settings.add(Box.createHorizontalStrut(10));
        settings.add(refreshButton);

        JButton generatePrivacyLabelButton = new JButton("Generate Data Safety Section CSV");
        generatePrivacyLabelButton.addActionListener(actionEvent -> {
            PrivacyPracticesHolder.getInstance(myProject).generateAndWritePrivacyLabel();
        });
        settings.add(Box.createHorizontalStrut(10));
        settings.add(generatePrivacyLabelButton);
        return settings;
    }

    @NotNull
    private JComponent getPanel(JPanel panel, Splitter splitter) {
        splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myOverviewTree));

        panel.add(splitter);
        panel.add(getSettings(), BorderLayout.PAGE_START);
        return panel;
    }

    private JPanel getPanel(JPanel panel, JScrollPane scrollPane) {
        panel.add(scrollPane);
        panel.add(getSettings(), BorderLayout.PAGE_START);

        return panel;
    }

    public void refreshTreeView(boolean initFlowAnalysis) {
        myOverviewTreeBuilder.queueUpdate();
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
    public String getNextOccurenceActionName() {
        return null;
    }

    @Override
    public String getPreviousOccurenceActionName() {
        return null;
    }

    @Override
    public void dispose() {

    }

}
