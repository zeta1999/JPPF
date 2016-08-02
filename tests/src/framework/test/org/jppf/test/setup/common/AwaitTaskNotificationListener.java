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

package test.org.jppf.test.setup.common;

import javax.management.*;

import org.jppf.client.JPPFClient;
import org.jppf.management.*;
import org.jppf.management.forwarding.JPPFNodeForwardingNotification;

import test.org.jppf.test.setup.BaseSetup;

/**
 * A notification listener that can wait until a specified task notification is sent.
 */
public class AwaitTaskNotificationListener implements NotificationListener {
  /**
   * A message we expect to receive as a notification.
   */
  private final String expectedMessage;
  /**
   *
   */
  private final JMXDriverConnectionWrapper jmx;
  /**
   * Registration ID for this listener.
   */
  private String listenerId;

  /**
   * Intiialize with an expected message.
   * @param client the JPPF client from which to get a JMX connection.
   * @param expectedMessage a message we expect to receive as a notification.
   * @throws Exception if any error occurs.
   */
  public AwaitTaskNotificationListener(final JPPFClient client, final String expectedMessage) throws Exception {
    this.expectedMessage = expectedMessage;
    this.jmx = BaseSetup.getJMXConnection(client);
    listenerId = jmx.registerForwardingNotificationListener(NodeSelector.ALL_NODES, JPPFNodeTaskMonitorMBean.MBEAN_NAME, this, null, null);
  }

  @Override
  public void handleNotification(final Notification notification, final Object handback) {
    JPPFNodeForwardingNotification wrapping = (JPPFNodeForwardingNotification) notification;
    TaskExecutionNotification actualNotif = (TaskExecutionNotification) wrapping.getNotification();
    Object data = actualNotif.getUserData();
    if (expectedMessage.equals(data)) {
      synchronized(this) {
        notifyAll();
      }
    }
  }

  /**
   * Wait for the epxected message to be received.
   * @throws Exception if any error occurs.
   */
  public synchronized void await() throws Exception {
    if (listenerId != null) {
      wait();
      jmx.unregisterForwardingNotificationListener(listenerId);
      listenerId = null;
    }
  }
}