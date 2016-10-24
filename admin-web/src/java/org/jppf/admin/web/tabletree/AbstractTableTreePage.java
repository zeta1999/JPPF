/*
 * JPPF.
 * Copyright (C) 2005-2016 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jppf.admin.web.tabletree;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.time.Duration;
import org.jppf.admin.web.*;
import org.jppf.ui.treetable.*;
import org.slf4j.*;

/**
 * Abstract super class for pages holding a toolbar and a table tree view.
 * @author Laurent Cohen
 */
public abstract class AbstractTableTreePage extends TemplatePage implements TableTreeHolder {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(AbstractTableTreePage.class);
  /**
   * Determines whether the debug level is enabled in the log configuration, without the cost of a method call.
   */
  private static boolean debugEnabled = log.isDebugEnabled();
  /**
   * The tree table component.
   */
  protected transient JPPFTableTree tableTree;
  /**
   * The tree table model.
   */
  protected transient AbstractJPPFTreeTableModel treeModel;
  /**
   * Handles the selection of rows in the tree table.
   */
  protected transient SelectionHandler selectionHandler;
  /**
   * The toolbar.
   */
  protected transient Form<String> toolbar;
  /**
   * The behavior that periodically refreshes the toolbar and table tree.
   */
  protected final transient AjaxSelfUpdatingTimerBehavior refreshTimer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5));
  /**
   * The type of tree this page holds.
   */
  protected final TreeViewType viewType;
  /**
   * The name prefix for the toolbar and table tree.
   */
  protected final String namePrefix;

  /**
   * 
   * @param viewType the type of tree this page holds.
   * @param namePrefix the name prefix for the toolbar and table tree.
   */
  public AbstractTableTreePage(final TreeViewType viewType, final String namePrefix) {
    this.viewType = viewType;
    this.namePrefix = namePrefix;
    add(getOrCreateToolbar());
    TableTreeData data = JPPFWebSession.get().getTableTreeData(viewType);
    selectionHandler = data.getSelectionHandler();
    tableTree = createTableTree();
    tableTree.add(new WindowsTheme()); // adds windows-style handles on nodes with children
    tableTree.add(refreshTimer);
    tableTree.addUpdateTarget(toolbar);
    data.selectionChanged(selectionHandler);
    if (debugEnabled) log.debug("table tree created");
    add(tableTree);
    if (debugEnabled) log.debug("table tree added to page");
  }

  @Override
  public JPPFTableTree getTableTree() {
    return tableTree;
  }

  @Override
  public Form<String> getToolbar() {
    return toolbar;
  }

  @Override
  public AjaxSelfUpdatingTimerBehavior getRefreshTimer() {
    return refreshTimer;
  }

  /**
   * Create the tree table.
   * @return a {@link JPPFTableTree} instance.
   */
  protected JPPFTableTree createTableTree() {
    if (debugEnabled) log.debug("getting tree model for {}", viewType);
    createTreeTableModel();
    TableTreeData data = JPPFWebSession.get().getTableTreeData(viewType);
    JPPFTableTree tree = new JPPFTableTree(
      viewType, namePrefix + ".table.tree", createColumns(), treeModel, Integer.MAX_VALUE, selectionHandler, TableTreeHelper.newTreeNodeRenderer(viewType), data.getExpansionModel());
    DataTable<DefaultMutableTreeNode, String> table = tree.getTable();
    HeadersToolbar<String> header = new HeadersToolbar<>(table, null);
    table.addTopToolbar(header);
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
    if (data.isFirstExpansion()) {
      data.setFirstExpansion(false);
      TableTreeHelper.expand(tree, root);
    }
    if (debugEnabled) log.debug("tree created");
    return tree;
  }

  /**
   * Get the tolbar and create it if necessary.
   * @return the toolbar.
   */
  private Form<String> getOrCreateToolbar() {
    TableTreeData data = JPPFWebSession.get().getTableTreeData(viewType);
    ActionHandler actionHandler = data.getActionHandler();
    if (actionHandler == null) {
      actionHandler = new ActionHandler();
      data.setActionHandler(actionHandler);
    }
    if (toolbar == null) {
      toolbar = new Form<>(namePrefix + ".toolbar");
      createActions();
    }
    return toolbar;
  }

  /**
   * Create and initialize the tree table model.
   */
  protected abstract void createTreeTableModel();

  /**
   * @return the list of columns.
   */
  protected abstract List<? extends IColumn<DefaultMutableTreeNode, String>> createColumns();

  /**
   * 
   */
  protected abstract void createActions();
}
