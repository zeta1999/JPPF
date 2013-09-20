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
package org.jppf.server.protocol;

import java.io.Serializable;

import org.jppf.classloader.AbstractJPPFClassLoader;
import org.jppf.node.protocol.Task;
import org.jppf.scheduling.JPPFSchedule;
import org.jppf.task.storage.DataProvider;
import org.jppf.utils.*;

/**
 * Abstract superclass for all tasks submitted to the execution server.
 * This class provides the basic facilities to handle data shared among tasks, handling of task execution exception,
 * and handling of the execution results.<p>
 * JPPF clients have to extend this class and must implement the <code>run</code> method. In the
 * <code>run</code> method the task calculations are performed, and the result of the calculations
 * is set with the {@link #setResult(Object)} method:
 * <pre>
 * class MyTask extends JPPFTask {
 *   public void run() {
 *     // do the calculation ...
 *     setResult(myResult);
 *   }
 * }
 * </pre>
 * @author Laurent Cohen
 */
public abstract class JPPFTask implements Task<Object>
{
  /**
   * Explicit serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * The position of this task at the submission time.
   */
  private int position;
  /**
   * The result of the task execution.
   */
  private Object result = null;
  /**
   * The exception that was raised by this task's execution.
   */
  private Exception exception = null;
  /**
   * The provider of shared data for this task.
   */
  private transient DataProvider dataProvider = null;
  /**
   * The task timeout schedule configuration.
   */
  private JPPFSchedule timeoutSchedule = null;
  /**
   * Determines whether this task is executing within a node, or locally on the client side.
   */
  private boolean inNode = false;

  /**
   * Default constructor.
   */
  public JPPFTask()
  {
  }

  /**
   * A user-assigned id for this task.
   */
  private String id = null;

  @Override
  public Object getResult()
  {
    return result;
  }

  @Override
  public void setResult(final Object  result)
  {
    this.result = result;
  }

  @Override
  public Exception getException()
  {
    return exception;
  }

  @Override
  public void setException(final Exception exception)
  {
    this.exception = exception;
  }

  @Override
  public DataProvider getDataProvider()
  {
    return dataProvider;
  }

  @Override
  public void setDataProvider(final DataProvider dataProvider)
  {
    this.dataProvider = dataProvider;
  }

  @Override
  public final int getPosition()
  {
    return position;
  }

  @Override
  public final void setPosition(final int position)
  {
    this.position = position;
  }

  /**
   * Add a connection status listener to this connection's list of listeners.
   * @param listener the listener to add to the list.
   * @deprecated this method does nothing. Sending task notifications via this means has very little, if any, usefulness,
   * as only the latest notification is kept and processed. In a multithreaded/parallel context,
   * this doesn't make any sense. It is much better to use the approach described in the <a href="http://www.jppf.org/samples-pack/TaskNotifications/Readme.php">Tasks Notifications sample</a>
   * @exclude
   */
  public synchronized void addJPPFTaskListener(final JPPFTaskListener listener)
  {
  }

  /**
   * Remove a connection status listener from this connection's list of listeners.
   * @param listener the listener to remove from the list.
   * @deprecated this method does nothing. Sending task notifications via this means has very little, if any, usefulness,
   * as only the latest notification is kept and processed. In a multithreaded/parallel context,
   * this doesn't make any sense. It is much better to use the approach described in the <a href="http://www.jppf.org/samples-pack/TaskNotifications/Readme.php">Tasks Notifications sample</a>
   * @exclude
   */
  public synchronized void removeJPPFTaskListener(final JPPFTaskListener listener)
  {
  }

  /**
   * Notify all listeners that an event has occurred within this task.
   * @param source an object describing the event, must be serializable.
   * @deprecated this method does nothing. Sending task notifications via this means has very little, if any, usefulness,
   * as only the latest notification is kept and processed. In a multithreaded/parallel context,
   * this doesn't make any sense. It is much better to use the approach described in the <a href="http://www.jppf.org/samples-pack/TaskNotifications/Readme.php">Tasks Notifications sample</a>
   * @exclude
   */
  public synchronized void fireNotification(final Serializable source)
  {
  }

  /**
   * Get the timeout for this task.
   * @return the timeout as a long.
   * @deprecated use the {@link JPPFSchedule} object from {@link #getTimeoutSchedule()} instead.
   * @exclude
   */
  public long getTimeout()
  {
    if (timeoutSchedule == null) return 0L;
    return timeoutSchedule.getDuration();
  }

  /**
   * Set the timeout for this task.
   * @param timeout the timeout in milliseconds.
   * @deprecated use a {@link JPPFSchedule} object with {@link #setTimeoutSchedule(JPPFSchedule)} instead.
   * @exclude
   */
  public void setTimeout(final long timeout)
  {
    timeoutSchedule = new JPPFSchedule(timeout);
  }

  /**
   * Get the timeout date for this task.
   * @return the dates in string format.
   * @deprecated use the {@link JPPFSchedule} object from {@link #getTimeoutSchedule()} instead.
   * @exclude
   */
  public String getTimeoutDate()
  {
    if (timeoutSchedule == null) return null;
    return timeoutSchedule.getDate();
  }

  /**
   * Get the format used to express the timeout date.
   * @return a format string using the specifications for <code>SimpleDateFormat</code>.
   * @deprecated use the {@link JPPFSchedule} object from {@link #getTimeoutSchedule()} instead.
   * @exclude
   */
  public String getTimeoutFormat()
  {
    if (timeoutSchedule == null) return null;
    return timeoutSchedule.getFormat();
  }

  /**
   * Set the timeout date for this task.<br>
   * Calling this method will reset the timeout value, as both timeout duration and timeout date are mutually exclusive.
   * @param timeoutDate the date to set in string representation.
   * @param format the format of of the date to set, as described in the specification for {@link java.text.SimpleDateFormat SimpleDateFormat}.
   * @see java.text.SimpleDateFormat
   * @deprecated use a {@link JPPFSchedule} object with {@link #setTimeoutSchedule(JPPFSchedule)} instead.
   * @exclude
   */
  public void setTimeoutDate(final String timeoutDate, final String format)
  {
    timeoutSchedule = new JPPFSchedule(timeoutDate, format);
  }

  @Override
  public String getId()
  {
    return id;
  }

  @Override
  public void setId(final String id)
  {
    this.id = id;
  }

  @Override
  public void onCancel()
  {
  }

  /**
   * {@inheritDoc}
   * @deprecated the task restart feature is inherently unsafe, as it depends on the task
   * having a unique id among all the tasks running in the grid, which cannot be guaranteed.
   * This feature has been removed from the management APIs, with no replacement.
   * @exclude
   */
  @Override
  public void onRestart()
  {
  }

  @Override
  public void onTimeout()
  {
  }

  @Override
  public Object getTaskObject()
  {
    return this;
  }

  @Override
  public JPPFSchedule getTimeoutSchedule()
  {
    return timeoutSchedule;
  }

  @Override
  public void setTimeoutSchedule(final JPPFSchedule timeoutSchedule)
  {
    this.timeoutSchedule = timeoutSchedule;
  }

  @Override
  public boolean isInNode()
  {
    return inNode;
  }

  /**
   * {@inheritDoc}
   * @exclude
   */
  @Override
  public void setInNode(final boolean inNode)
  {
    this.inNode = inNode;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V compute(final JPPFCallable<V> callable) throws Exception
  {
    V result = null;
    if (isInNode())
    {
      ClassLoader cl = callable.getClass().getClassLoader();
      if (cl instanceof AbstractJPPFClassLoader)
      {
        AbstractJPPFClassLoader loader = (AbstractJPPFClassLoader) cl;
        result = loader.computeCallable(callable);
      }
    }
    else result = callable.call();
    return result;
  }
}
