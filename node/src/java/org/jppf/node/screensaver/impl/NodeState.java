/*
 * JPPF.
 * Copyright (C) 2005-2013 JPPF Team.
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
package org.jppf.node.screensaver.impl;

import org.jppf.node.event.*;
import org.jppf.node.screensaver.*;

/**
 * Instances of this class represent information about a node.
 */
public class NodeState implements NodeLifeCycleListener, JPPFScreenSaverHolder
{
  /**
   * 
   */
  NodePanel nodePanel = null;

  /**
   * 
   */
  public NodeState()
  {
  }

  @Override
  public void nodeStarting(final NodeLifeCycleEvent event)
  {
    nodePanel.updateConnectionStatus(true);
    nodePanel.updateExecutionStatus(false);
  }

  @Override
  public void nodeEnding(final NodeLifeCycleEvent event)
  {
    nodePanel.updateConnectionStatus(false);
    nodePanel.updateExecutionStatus(false);
  }

  @Override
  public void jobHeaderLoaded(final NodeLifeCycleEvent event)
  {
  }

  @Override
  public void jobStarting(final NodeLifeCycleEvent event)
  {
    nodePanel.updateExecutionStatus(true);
  }

  @Override
  public void jobEnding(final NodeLifeCycleEvent event)
  {
    nodePanel.updateExecutionStatus(false);
    nodePanel.incTaskCount(event.getTasks().size());
    
  }

  @Override
  public void setScreenSaver(final JPPFScreenSaver screensaver)
  {
    this.nodePanel = ((JPPFScreenSaverImpl) screensaver).getNodePanel();
  }
}