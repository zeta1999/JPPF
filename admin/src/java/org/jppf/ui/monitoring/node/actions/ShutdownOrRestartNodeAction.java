/*
 * JPPF.
 * Copyright (C) 2005-2019 JPPF Team.
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
package org.jppf.ui.monitoring.node.actions;

import java.awt.event.ActionEvent;
import java.util.*;

import org.jppf.client.monitoring.topology.TopologyDriver;
import org.jppf.management.*;
import org.jppf.management.forwarding.NodeForwardingMBean;
import org.jppf.utils.LoggingUtils;
import org.jppf.utils.collections.CollectionMap;
import org.slf4j.*;

/**
 * This action stops a node.
 */
public class ShutdownOrRestartNodeAction extends AbstractTopologyAction {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ShutdownOrRestartNodeAction.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static boolean debugEnabled = LoggingUtils.isDebugEnabled(log);
  /**
   * Whether to restart the nodes or to shut them down.
   */
  private final boolean restart;
  /**
   * Whether to restart/shutdown immediately or wait until each node are idle.
   */
  private final boolean interruptIfRunning;

  /**
   * Initialize this action.
   * @param restart whether to restart the nodes or to shut them down.
   * @param interruptIfRunning whether to restart/shutdown immediately or wait until each node are idle.
   * @param name the name of the corresponding UI element.
   */
  public ShutdownOrRestartNodeAction(final boolean restart, final boolean interruptIfRunning, final String name) {
    this.restart = restart;
    this.interruptIfRunning = interruptIfRunning;
    if (name != null) setupNameAndTooltip(name);
  }

  /**
   * Update this action's enabled state based on a list of selected elements.
   * @param selectedElements a list of objects selected in the tre table.
   */
  @Override
  public void updateState(final List<Object> selectedElements) {
    super.updateState(selectedElements);
    setEnabled(dataArray.length > 0);
  }

  /**
   * Perform the action.
   * @param event not used.
   */
  @Override
  public void actionPerformed(final ActionEvent event) {
    final Runnable r = new Runnable() {
      @Override
      public void run() {
        final CollectionMap<TopologyDriver, String> map = getDriverMap();
        for (final Map.Entry<TopologyDriver, Collection<String>> entry: map.entrySet()) {
          try {
            final NodeForwardingMBean forwarder = entry.getKey().getForwarder();
            if (forwarder == null) continue;
            if (debugEnabled) log.debug("invoking {} with interrupt={} for the nodes: {}", (restart ? "restart()" : "shutdown()"), interruptIfRunning, entry.getValue());
            final NodeSelector selector = new UuidSelector(entry.getValue());
            if (restart) forwarder.restart(selector, interruptIfRunning);
            else forwarder.shutdown(selector, interruptIfRunning);
          } catch (final Exception e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    };
    runAction(r);
  }
}
