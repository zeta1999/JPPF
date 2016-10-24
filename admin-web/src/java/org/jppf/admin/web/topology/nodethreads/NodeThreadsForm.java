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

package org.jppf.admin.web.topology.nodethreads;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.jppf.admin.web.AbstractModalForm;
import org.jppf.utils.TypedProperties;

/**
 *
 * @author Laurent Cohen
 */
public class NodeThreadsForm extends AbstractModalForm {
  /**
   * Text field for the number of threads.
   */
  private TextField<Integer> nbThreadsField;
  /**
   * Text field for the number of threads.
   */
  private TextField<Integer> priorityField;

  /**
   * @param modal the modal window.
   * @param okAction the ok action.
   */
  public NodeThreadsForm(final ModalWindow modal, final Runnable okAction) {
    super("node_threads", modal, okAction);
  }

  @Override
  protected void createFields() {
    add(nbThreadsField = new TextField<>(prefix + ".nb_threads.field", Model.of(1)));
    add(priorityField = new TextField<>(prefix + ".priority.field", Model.of(Thread.NORM_PRIORITY)));
  }

  /**
   * @return the number of threads.
   */
  public int getNbThreads() {
    return (Integer) nbThreadsField.getDefaultModelObject();
  }

  /**
   * Set the number of slaves.
   * @param nbThreads the number of threads to set.
   */
  public void setNbThreads(final int nbThreads) {
    nbThreadsField.setModel(Model.of(nbThreads));
  }

  /**
   * @return the threads priority.
   */
  public int getPriority() {
    return (Integer) priorityField.getDefaultModelObject();
  }

  /**
   * Set the threads priority.
   * @param priority the threads priority to set.
   */
  public void setPriority(final int priority) {
    priorityField.setModel(Model.of(priority));
  }

  @Override
  protected void loadSettings(final TypedProperties props) {
    setNbThreads(props.getInt(nbThreadsField.getId(), Runtime.getRuntime().availableProcessors()));
    setPriority(props.getInt(priorityField.getId(), Thread.NORM_PRIORITY));
  }

  @Override
  protected void saveSettings(final TypedProperties props) {
    props.setInt(nbThreadsField.getId(), getNbThreads()).setInt(priorityField.getId(), getPriority());
  }
}
