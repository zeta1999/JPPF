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

package org.jppf.server.nio.nodeserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jppf.management.JMXNodeConnectionWrapper;
import org.jppf.node.protocol.JPPFNodeConfigSpec;
import org.jppf.server.protocol.ServerJob;
import org.jppf.utils.*;
import org.jppf.utils.collections.*;
import org.jppf.utils.configuration.JPPFProperties;
import org.slf4j.*;

/**
 * Handles the reservation of nodes to jobs that request a specific node configuration.
 * @author Laurent Cohen
 */
public class NodeReservationHandler {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(NodeReservationHandler.class);
  /**
   * Determines whether DEBUG logging level is enabled.
   */
  private static boolean debugEnabled = LoggingUtils.isDebugEnabled(log);
  /**
   * Map of reserved nodes UUIDs to the correspoding job UUIDs.
   */
  private final Map<String, String> pendingMap = new ConcurrentHashMap<>();
  /**
   * Map of reserved nodes UUIDs to the correspoding job UUIDs.
   */
  private final Map<String, String> readyMap = new ConcurrentHashMap<>();
  /**
   * Mapping of jobs to the node(s) reserved for them.
   */
  private final CollectionMap<String, String> jobPendingMap = new SetHashMap<>();
  /**
   * Mapping of jobs to the node(s) reserved for them.
   */
  private final CollectionMap<String, String> jobReadyMap = new SetHashMap<>();
  /**
   * The node server.
   */
  private final NodeNioServer server;

  /**
   * Types of transitions for a reservation, applied to a node.
   */
  enum Transition {
    /**
     * Remove the reservation.
     */
    REMOVE,
    /**
     * No transition.
     */
    KEEP,
    /**
     * Pass the reservation to ready state
     */
    READY
  }

  /**
   * Initialize with the specified node server.
   * @param server the node server.
   */
  NodeReservationHandler(final NodeNioServer server) {
    this.server = server;
  }

  /**
   * Reserve the specified node for the specified job.
   * @param job the job for which the node is to be reserved.
   * @param node the node to restart and reserve.
   */
  void doReservation(final ServerJob job, final AbstractNodeContext node) {
    if (debugEnabled) log.debug(String.format("reserving node %s for job %s", node.getUuid(), job.getUuid()));
    synchronized(this) {
      pendingMap.put(node.getUuid(), job.getUuid());
      jobPendingMap.putValue(job.getUuid(), node.getUuid());
    }
    server.getTransitionManager().submit(new NodeReservationTask(job, node));
  }

  /**
   * Remove the job reservation for the specified job, if any.
   * @param node the node for which to remove the reservation.
   */
  synchronized void removeReservation(final AbstractNodeContext node) {
    if (debugEnabled) log.debug(String.format("removing reservation for node %s", node));
    String jobUuid = pendingMap.remove(node.getUuid());
    if (jobUuid != null) jobPendingMap.removeValue(jobUuid, node.getUuid());
    jobUuid = readyMap.remove(node.getUuid());
    if (jobUuid != null) jobReadyMap.removeValue(jobUuid, node.getUuid());
    node.reservationTansition = Transition.REMOVE;
  }

  /**
   * Remove all the node reservations for the specified job that is being cancelled.
   * @param job the job being cancelled.
   */
  public synchronized void onJobCancelled(final ServerJob job) {
    if (debugEnabled) log.debug(String.format("job %s cancelled, removing all reservations", job.getUuid()));
    Collection<String> nodes = jobPendingMap.removeKey(job.getUuid());
    if (nodes != null) {
      for (String nodeUuid: nodes) pendingMap.remove(nodeUuid);
    }
    nodes = jobReadyMap.removeKey(job.getUuid());
    if (nodes != null) {
      for (String nodeUuid: nodes) readyMap.remove(nodeUuid);
    }
  }

  /**
   * Get the UUID of the job for which the specified node has a pending reservation, if any.
   * @param node the node to check.
   * @return the UUID of the corresponding job, or {@code null} if the node doesn't have a pending reservation.
   */
  synchronized String getPendingJobUUID(final AbstractNodeContext node) {
    return pendingMap.get(node.getUuid());
  }

  /**
   * Get the UUID of the job for which the specified node is reserved, if any.
   * @param node the node to check.
   * @return the UUID of the corresponding job, or {@code null} if the node is not reserved.
   */
  synchronized String getReadyJobUUID(final AbstractNodeContext node) {
    return readyMap.get(node.getUuid());
  }

  /**
   * Determine whether the node with the specified uuid has at leat one reserved node.
   * @param jobUuid the uuid of the job to check.
   * @return {@code true} if the job has a t least one reserved node, {@code false} otherwise.
   */
  synchronized boolean hasPendingNode(final String jobUuid) {
    return jobPendingMap.getValues(jobUuid) != null;
  }

  /**
   * Determine whether the node with the specified uuid has at leat one reserved node.
   * @param jobUuid the uuid of the job to check.
   * @return {@code true} if the job has a t least one reserved node, {@code false} otherwise.
   */
  synchronized boolean hasReadyNode(final String jobUuid) {
    return jobReadyMap.getValues(jobUuid) != null;
  }

  /**
   * Get the nodes for which the specified job is ready for dispatch.
   * @param jobUuid the uuid of the job to check.
   * @return a collection of node uuids, or {@code null} if there is no ready node for the job.
   */
  synchronized Collection<String> getReadyNodes(final String jobUuid) {
    return jobReadyMap.getValues(jobUuid);
  }

  /**
   * Get the number of nodes reserved for the specified job.
   * @param jobUuid the uuid of the jkob to check.
   * @return the total number of jobs reserved for the job.
   */
  synchronized int getNbReservedNodes(final String jobUuid) {
    int result = 0;
    Collection<String> nodes = jobPendingMap.getValues(jobUuid);
    if (nodes != null) result += nodes.size();
    nodes = jobReadyMap.getValues(jobUuid);
    if (nodes != null) result += nodes.size();
    return result;
  }

  /**
   *
   * @param node .
   * @return .
   */
  boolean transitionReservation(final AbstractNodeContext node) {
    TypedProperties config = node.getSystemInformation().getJppf();
    String reservedUuid = config.get(JPPFProperties.NODE_RESERVED_JOB);
    if (reservedUuid != null) {
      if (debugEnabled) log.debug(String.format("node %s is reserved for job %s", node.getUuid(), reservedUuid));
      String oldNodeUuid = config.get(JPPFProperties.NODE_RESERVED_UUID);
      synchronized(this) {
        String pendingJobUuid = pendingMap.get(oldNodeUuid);
        if (debugEnabled) log.debug(String.format("node %s previous uuid was %s and was reserved for job %s", node.getUuid(), oldNodeUuid, pendingJobUuid));
        if (reservedUuid.equals(pendingJobUuid)) {
          if (debugEnabled) log.debug(String.format("oldNodeUuid=%s, nodeUuid=%s, jobUuid=%s", oldNodeUuid, node.getUuid(), pendingJobUuid));
          pendingMap.remove(oldNodeUuid);
          readyMap.put(node.getUuid(), pendingJobUuid);
          jobPendingMap.removeValue(pendingJobUuid, oldNodeUuid);
          jobReadyMap.putValue(pendingJobUuid, node.getUuid());
          node.reservationTansition = Transition.REMOVE;
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get the uuids of the jobs for which at least oone node is reserved.
   * @return a set of job uuids.
   */
  public synchronized Set<String> getReservedJobs() {
    Set<String> set = new HashSet<>(jobPendingMap.keySet());
    set.addAll(jobReadyMap.keySet());
    return set;
  }

  /**
   * Get the uuids of the reserved nodes.
   * @return a set of node uuids.
   */
  public synchronized Set<String> getReservedNodes() {
    Set<String> set = new HashSet<>(pendingMap.keySet());
    set.addAll(readyMap.keySet());
    return set;
  }

  /**
   * This task performs the reservation of a node for a job, providing optional
   * configuration overrides and optionally restarting the node to re-configure it.
   */
  private class NodeReservationTask implements Runnable {
    /**
     * The job for which the node is to be reserved.
     */
    private final ServerJob job;
    /**
     * The node to restart and reserve.
     */
    private final AbstractNodeContext node;

    /**
     * Initialize this task with the specified node for the specified job.
     * @param job the job for which the node is to be reserved.
     * @param node the node to restart and reserve.
     */
    public NodeReservationTask(final ServerJob job, final AbstractNodeContext node) {
      this.job = job;
      this.node = node;
    }

    @Override
    public void run() {
      final JPPFNodeConfigSpec spec =  job.getSLA().getDesiredNodeConfiguration();
      final TypedProperties desiredConfig = spec.getConfiguration();
      TypedProperties config = new TypedProperties(desiredConfig)
        .set(JPPFProperties.NODE_RESERVED_JOB, job.getUuid())
        .set(JPPFProperties.NODE_RESERVED_UUID, node.getUuid());
      JMXNodeConnectionWrapper jmx = node.getJmxConnection();
      if ((jmx != null) && jmx.isConnected()) {
        try {
          if (debugEnabled) log.debug(String.format("about to restart node %s reserved for job %s with config=%s", node.getUuid(), job.getUuid(), config));
          boolean restart =  spec.isForceRestart() || (node.reservationScore > 0);
          node.reservationTansition = Transition.KEEP;
          jmx.updateConfiguration(config, restart);
          // if node is not restarted, synchronize server version of the node's config
          if (!restart) {
            node.getSystemInformation().getJppf().putAll(config);
            transitionReservation(node);
          }
        } catch (Exception e) {
          log.error(String.format("error reserving node %s for job %s", node, job));
        }
      }
    }
  }
}
