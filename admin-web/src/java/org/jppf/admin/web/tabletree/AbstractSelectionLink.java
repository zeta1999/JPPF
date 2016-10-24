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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.jppf.admin.web.JPPFWebSession;
import org.jppf.ui.treetable.TreeViewType;

/**
 * Abstract class for action links that select nodes in a table tree.
 * @author Laurent Cohen
 */
public abstract class AbstractSelectionLink extends AbstractActionLink {
  /**
   * The type this button is part of.
   */
  private final TreeViewType viewType;

  /**
   *
   * @param id the lnk id.
   * @param viewType the type this button is part of.
   */
  public AbstractSelectionLink(final String id, final TreeViewType viewType) {
    super(id);
    this.viewType = viewType;
  }

  /**
   *
   * @param id the lnk id.
   * @param model the display model.
   * @param viewType the type this button is part of.
   */
  public AbstractSelectionLink(final String id, final IModel<String> model, final TreeViewType viewType) {
    super(id, model);
    this.viewType = viewType;
  }

  @Override
  public void onClick(final AjaxRequestTarget target) {
    JPPFWebSession session = JPPFWebSession.get();
    TableTreeData data = session.getSessionData().getData(viewType);
    onClick(target, data);
    data.selectionChanged(data.getSelectionHandler());
    Page page = target.getPage();
    if (page instanceof TableTreeHolder) target.add(((TableTreeHolder) page).getTableTree());
    if (getParent() != null) target.add(getParent());
  }

  /**
   * Perform the selectiona action.
   * @param target the ajax target.
   * @param data the tree table data.
   */
  protected abstract void onClick(final AjaxRequestTarget target, final TableTreeData data);
}
