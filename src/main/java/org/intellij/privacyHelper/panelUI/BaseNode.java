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


import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;

/**
 * Created by tianshi on 1/9/17.
 */
public abstract class BaseNode<Value> extends AbstractTreeNode<Value> {
  protected final AbstractTreeBuilder myBuilder;

  protected BaseNode(Project project, Value value, AbstractTreeBuilder builder) {
    super(project, value);
    myBuilder = builder;
  }
}
