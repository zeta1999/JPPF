/*
 * JPPF.
 * Copyright (C) 2005-2011 JPPF Team.
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

package org.jppf.server.protocol;

import java.io.Serializable;

import org.jppf.node.policy.ExecutionPolicy;
import org.jppf.node.protocol.JobSLA;
import org.jppf.scheduling.JPPFSchedule;

/**
 * This class represents the Service Level Agreement Between a JPPF job and a server.
 * It determines the state, conditions and order in which a job will be executed. 
 * @author Laurent Cohen
 */
public class JPPFJobSLA implements Serializable, JobSLA
{
	/**
	 * Explicit serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The tasks execution policy.
	 */
	private ExecutionPolicy executionPolicy = null;
	/**
	 * The priority of this job, used by the server to prioritize queued jobs.
	 */
	private int priority = 0;
	/**
	 * The maximum number of nodes this job can run on.
	 */
	private int maxNodes = Integer.MAX_VALUE;
	/**
	 * Determines whether this job is initially suspended.
	 * If it is, it will have to be resumed, using either the admin console or the JMX APIs.
	 */
	private boolean suspended = false;
	/**
	 * The job start schedule configuration.
	 */
	private JPPFSchedule jobSchedule = null;
	/**
	 * The job expiration schedule configuration.
	 */
	private JPPFSchedule jobExpirationSchedule = null;
	/**
	 * Specifies whether the job is a broadcast job.
	 */
	private boolean broadcastJob = false;
	/**
	 * Determines whether the job should be canceled by the driver if the client gets disconnected.
	 */
	private boolean cancelUponClientDisconnect = true;

	/**
	 * Default constructor.
	 */
	public JPPFJobSLA()
	{
	}

	/**
	 * Initialize this job SLA with the specified execution policy.
	 * @param policy the tasks execution policy.
	 */
	public JPPFJobSLA(ExecutionPolicy policy)
	{
		this(policy, 0, Integer.MAX_VALUE, false);
	}

	/**
	 * Initialize this job SLA with the specified execution policy and priority.
	 * @param policy the tasks execution policy.
	 * @param priority the priority of this job.
	 */
	public JPPFJobSLA(ExecutionPolicy policy, int priority)
	{
		this(policy, priority, Integer.MAX_VALUE, false);
	}

	/**
	 * Initialize this job SLA with the specified execution policy, priority, max number of nodes and suspended indicator.
	 * @param policy the tasks execution policy.
	 * @param priority the priority of this job.
	 * @param maxNodes the maximum number of nodes this job can run on. A value <= 0 means no limit on the number of nodes.
	 * @param suspended determines whether this job is initially suspended.
	 */
	public JPPFJobSLA(ExecutionPolicy policy, int priority, int maxNodes, boolean suspended)
	{
		this.executionPolicy = policy;
		this.priority = priority;
		this.maxNodes = maxNodes > 0 ? maxNodes : Integer.MAX_VALUE;
		this.suspended = suspended;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionPolicy getExecutionPolicy()
	{
		return executionPolicy;
	}

	/**
	 * Set the tasks execution policy.
	 * @param executionPolicy an <code>ExecutionPolicy</code> instance.
	 */
	public void setExecutionPolicy(ExecutionPolicy executionPolicy)
	{
		this.executionPolicy = executionPolicy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Set the priority of this job.
	 * @param priority the priority as an int.
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxNodes()
	{
		return maxNodes;
	}

	/**
	 * Set the maximum number of nodes this job can run on.
	 * @param maxNodes the number of nodes as an int value. A value <= 0 means no limit on the number of nodes.
	 */
	public void setMaxNodes(int maxNodes)
	{
		this.maxNodes = maxNodes > 0 ? maxNodes : Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSuspended()
	{
		return suspended;
	}

	/**
	 * Specify whether this job is initially suspended.
	 * @param suspended true if the job is suspended, false otherwise.
	 */
	public void setSuspended(boolean suspended)
	{
		this.suspended = suspended;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPPFSchedule getJobSchedule()
	{
		return jobSchedule;
	}

	/**
	 * Set the job schedule.
	 * @param jobSchedule a <code>JPPFSchedule</code> instance. 
	 */
	public void setJobSchedule(JPPFSchedule jobSchedule)
	{
		this.jobSchedule = jobSchedule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPPFSchedule getJobExpirationSchedule()
	{
		return jobExpirationSchedule;
	}

	/**
	 * Set the job expiration schedule configuration.
	 * @param jobExpirationSchedule a {@link JPPFSchedule} instance.
	 */
	public void setJobExpirationSchedule(JPPFSchedule jobExpirationSchedule)
	{
		this.jobExpirationSchedule = jobExpirationSchedule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBroadcastJob()
	{
		return broadcastJob;
	}

	/**
	 * Specify whether the job is a broadcast job.
	 * @param broadcastJob true for a broadcast job, false otherwise.
	 */
	public void setBroadcastJob(boolean broadcastJob)
	{
		this.broadcastJob = broadcastJob;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelUponClientDisconnect()
	{
		return cancelUponClientDisconnect;
	}

	/**
	 * Specifiy whether the job should be canceled by the driver if the client gets disconnected.
	 * @param cancelUponClientDisconnect <code>true</code> if the job should be canceled, <code>false</code> otherwise.
	 */
	public void setCancelUponClientDisconnect(boolean cancelUponClientDisconnect)
	{
		this.cancelUponClientDisconnect = cancelUponClientDisconnect;
	}

	/**
	 * Create a copy of this job SLA.
	 * @return a {@link JPPFJobSLA} instance.
	 */
	public JPPFJobSLA copy()
	{
		JPPFJobSLA sla = new JPPFJobSLA();
		sla.setBroadcastJob(broadcastJob);
		sla.setExecutionPolicy(executionPolicy);
		sla.setJobExpirationSchedule(jobExpirationSchedule);
		sla.setJobSchedule(jobSchedule);
		sla.setMaxNodes(maxNodes);
		sla.setPriority(priority);
		sla.setSuspended(suspended);
		sla.setCancelUponClientDisconnect(cancelUponClientDisconnect);
		return sla;
	}
}
