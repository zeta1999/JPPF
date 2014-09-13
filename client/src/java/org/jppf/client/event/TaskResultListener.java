/*
 * JPPF.
 * Copyright (C) 2005-2014 JPPF Team.
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
package org.jppf.client.event;

import java.util.EventListener;

/**
 * Listener interface for receiving notifications of task results received from the server.
 * <p>To properly order the results, implementations of this interface should rely on {@link org.jppf.node.protocol.Task#getPosition() Task.getPosition()}.
 * @author Laurent Cohen
 * @deprecated {@code TaskResultListener} and its implementations are no longer exposed as public APIs.
 * {@code JobListener} should be used instead, with the {@code JPPFJob.addJobListener(JobListener)} and {@code JPPFJob.removeJobListener(JobListener)} methods.
 */
@Deprecated
public interface TaskResultListener extends EventListener
{
  /**
   * Called to notify that the results of a number of tasks have been received from the server.
   * @param event the event encapsulating the tasks that were received.
   */
  void resultsReceived(TaskResultEvent event);
}
