/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005-2009 JPPF Team.
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

package org.jppf.timeout;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Instances of this class contain data used to setup a timeout.
 * This includes duration, date, date format.
 * @author Laurent Cohen
 */
public class JPPFTimeoutConfiguration implements Serializable
{
	/**
	 * Time in milliseconds, after which this task will be aborted.<br>
	 * A value of 0 or less indicates this task never times out.
	 */
	private long timeout = 0L;
	/**
	 * Timeout date as a string.
	 */
	private String date = null;
	/**
	 * Format describing the timeout date.
	 */
	private SimpleDateFormat dateFormat = null;

	/**
	 * Initialize this timeout configuration with the specified duration.
	 * @param duration the duration in milliseconds.
	 */
	public JPPFTimeoutConfiguration(long duration)
	{
		this.timeout = duration;
	}

	/**
	 * Initialize this timeout configuration with the specified duration.
	 * @param date the tiemout date provided as a string.
	 * @param dateFormat the format in which the date is expressed (including locale and time zone information).
	 */
	public JPPFTimeoutConfiguration(String date, SimpleDateFormat dateFormat)
	{
		this.date = date;
		this.dateFormat = dateFormat;
	}

	/**
	 * Get the timeout for this task.
	 * @return the timeout in milliseconds.
	 */
	public long getTimeout()
	{
		return timeout;
	}

	/**
	 * Set the timeout for this task.
	 * @param timeout the timeout in milliseconds.
	 */
	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
		this.date = null;
		this.dateFormat = null;
	}

	/**
	 * Get the timeout date for this task.
	 * @return the date in string format.
	 */
	public String getTimeoutDate()
	{
		return date;
	}

	/**
	 * Get the format of timeout date for this task.
	 * @return a <code>SimpleDateFormat</code> instance.
	 */
	public SimpleDateFormat getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * Set the timeout date for this task.<br>
	 * Calling this method will reset the timeout value, as both timeout duration and timeout date
	 * are mutually exclusive.
	 * @param date the date to set in string representation.
	 * @param dateFormat the format of of the date to set.
	 * @see java.text.SimpleDateFormat
	 */
	public void setDate(String date, SimpleDateFormat dateFormat)
	{
		this.timeout = 0L;
		this.date = date;
		this.dateFormat = dateFormat;
	}
}
