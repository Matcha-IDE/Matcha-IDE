/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.privacyHelper.panelUI;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.xmlb.annotations.Attribute;
import org.intellij.privacyHelper.panelUI.safetySectionPreview.SafetySectionPreviewPanel;
import org.intellij.privacyHelper.panelUI.safetySectionTasks.SafetySectionTasksPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by tianshi on 1/9/17.
 */

@State(
  name = "Privacy",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)

public class PrivacyCheckerView implements PersistentStateComponent<PrivacyCheckerView.State>, Disposable {
  private final Project myProject;

  private ContentManager myContentManager;
  private SafetySectionTasksPanel mySafetySectionTasksPanel;
  private SafetySectionPreviewPanel mySafetySectionPreviewPanel;

  private State state = new State();

  PrivacyCheckerView(@NotNull Project project) {
    myProject = project;

  }

  static class State {
    @Attribute(value = "selected-index")
    public int selectedIndex;
  }

  @Override
  public void dispose() {

  }

  @Nullable
  @Override
  public PrivacyCheckerView.State getState() {
    if (myContentManager != null) {
      // all panel were constructed
      Content content = myContentManager.getSelectedContent();
      state.selectedIndex = myContentManager.getIndexOfContent(content);
    }
    return state;
  }

  @Override
  public void loadState(State state) {
    this.state = state;
  }


  public void initToolWindow(@NotNull ToolWindow toolWindow) {
    // Create panels
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

//
//    Content leakOverviewContent = contentFactory.createContent(null, "Personal Data Leak Overview", false);
//    myLeakOverviewPanel = new PersonalDataUsageOverviewPanel(myProject, CoconutPanelType.PersonalDataLeakOverview);
//    leakOverviewContent.setComponent(myLeakOverviewPanel);
//    Disposer.register(this, myLeakOverviewPanel);

    Content safetySectionTasksContent = contentFactory.createContent(null, "Tasks", false);
    mySafetySectionTasksPanel = new SafetySectionTasksPanel(myProject);
    safetySectionTasksContent.setComponent(mySafetySectionTasksPanel);
    Disposer.register(this, mySafetySectionTasksPanel);

    Content safetySectionPreviewContent = contentFactory.createContent(null, "Label Preview", false);
    mySafetySectionPreviewPanel = new SafetySectionPreviewPanel(myProject);
    safetySectionPreviewContent.setComponent(mySafetySectionPreviewPanel);
    Disposer.register(this, mySafetySectionPreviewPanel);

    myContentManager = toolWindow.getContentManager();

//    myContentManager.addContent(leakOverviewContent);
    myContentManager.addContent(safetySectionTasksContent);
    myContentManager.addContent(safetySectionPreviewContent);

//    leakOverviewContent.setCloseable(false);
    safetySectionTasksContent.setCloseable(false);
    safetySectionPreviewContent.setCloseable(false);

    Content content = myContentManager.getContent(state.selectedIndex);
    myContentManager.setSelectedContent(content == null ? safetySectionTasksContent : content);
  }


}
