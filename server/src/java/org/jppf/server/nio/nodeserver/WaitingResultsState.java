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

package org.jppf.server.nio.nodeserver;

import static org.jppf.server.nio.nodeserver.NodeTransition.*;
import static org.jppf.server.protocol.BundleParameter.*;

import java.util.List;

import org.jppf.JPPFException;
import org.jppf.io.DataLocation;
import org.jppf.management.JPPFSystemInformation;
import org.jppf.server.nio.*;
import org.jppf.server.protocol.*;
import org.jppf.server.scheduler.bundle.*;
import org.jppf.utils.Pair;
import org.slf4j.*;

/**
 * This class performs performs the work of reading a task bundle execution response from a node.
 * @author Laurent Cohen
 */
class WaitingResultsState extends NodeServerState {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WaitingResultsState.class);
  /**
   * Determines whether DEBUG logging level is enabled.
   */
  private static boolean debugEnabled = log.isDebugEnabled();

  /**
   * Initialize this state.
   * @param server the server that handles this state.
   */
  public WaitingResultsState(final NodeNioServer server) {
    super(server);
  }

  @Override
  public NodeTransition performTransition(final ChannelWrapper<?> channel) throws Exception {
    AbstractNodeContext context = (AbstractNodeContext) channel.getContext();
    if (context.readMessage(channel)) {
      Exception exception = null;
      ServerTaskBundleNode nodeBundle = context.getBundle();
      boolean requeue = false;
      boolean offlineCloseRequest = false;
      try {
        Pair<JPPFTaskBundle, List<DataLocation>> received = context.deserializeBundle();
        JPPFTaskBundle newBundle = received.first();
        if (debugEnabled) log.debug("*** read bundle " + received + " from node " + channel);
        if (offlineCloseRequest = newBundle.getParameter(NODE_OFFLINE_CLOSE_REQUEST, false)) return processOfflineRequest(context);
        Pair<Boolean, Exception> res = processResults(context, received);
        requeue = res.first();
        exception = res.second();
      }
      catch (Throwable t) {
        log.error(t.getMessage(), t);
        exception = (t instanceof Exception) ? (Exception) t : new JPPFException(t);
        nodeBundle.resultsReceived(t);
      } finally {
        if (!offlineCloseRequest) {
          nodeBundle.taskCompleted(exception);
          context.setBundle(null);
        }
      }
      if (requeue) nodeBundle.resubmit();
      // there is nothing left to do, so this instance will wait for a task bundle
      // make sure the context is reset so as not to resubmit the last bundle executed by the node.
      context.setMessage(null);
      return context.isPeer() ? TO_IDLE_PEER : TO_IDLE;
    }
    return TO_WAITING_RESULTS;
  }

  /**
   * Process the results received from the node.
   * @param context the context associated witht he node channel.
   * @param received groups the job header and resuls of the tasks.
   * @return A pairing of a requeue indicator and an eventual exception returned by the node.
   * @throws Exception if any error occurs.
   */
  private Pair<Boolean, Exception> processResults(final AbstractNodeContext context, final Pair<JPPFTaskBundle, List<DataLocation>> received) throws Exception {
    JPPFTaskBundle newBundle = received.first();
    ServerTaskBundleNode nodeBundle = context.getBundle();
    Exception exception = null;
    // if an exception prevented the node from executing the tasks or sending back the results
    Throwable t = newBundle.getParameter(NODE_EXCEPTION_PARAM);
    Bundler bundler = context.getBundler();
    if (t != null) {
      if (debugEnabled) log.debug("node " + context.getChannel() + " returned exception parameter in the header for bundle " + newBundle + " : " + t);
      exception = (t instanceof Exception) ? (Exception) t : new JPPFException(t);
      nodeBundle.resultsReceived(t);
    } else {
      if (debugEnabled) log.debug("*** received bundle with " + received.second().size() + " tasks, taskCount=" + newBundle.getTaskCount() + " : " + received);
      nodeBundle.resultsReceived(received.second());
      long elapsed = System.nanoTime() - nodeBundle.getJob().getExecutionStartTime();
      server.getStatsManager().taskExecuted(newBundle.getTaskCount(), elapsed / 1000000L, newBundle.getNodeExecutionTime() / 1000000L, 
          ((AbstractTaskBundleMessage) context.getMessage()).getLength());
      if (bundler instanceof BundlerEx) {
        long accumulatedTime = newBundle.getParameter(NODE_BUNDLE_ELAPSED_PARAM, -1L);
        ((BundlerEx) bundler).feedback(newBundle.getTaskCount(), elapsed, accumulatedTime, elapsed - newBundle.getNodeExecutionTime());
      } else bundler.feedback(newBundle.getTaskCount(), elapsed);
    }
    boolean requeue = newBundle.isRequeue();
    JPPFSystemInformation systemInfo = newBundle.getParameter(SYSTEM_INFO_PARAM);
    if (systemInfo != null) {
      context.setNodeInfo(systemInfo, true);
      if (bundler instanceof NodeAwareness) ((NodeAwareness) bundler).setNodeConfiguration(systemInfo);
    }
    return new Pair<>(requeue, exception);
  }
  
  /**
   * Process an offline request from the node.
   * @param context the current context assoiated witht he channel.
   * @return a <code>null</code> transition since the channel is closed by this method.
   * @throws Exception if any error occurs.
   */
  private NodeTransition processOfflineRequest(final AbstractNodeContext context) throws Exception {
    if (debugEnabled) log.debug("processing offline request, nodeBundle={} for node={}", context.getBundle(), context.getChannel());
    server.getOfflineNodeHandler().addNodeBundle(context.getBundle());
    context.cleanup(context.getChannel());
    return null;
  }
}
