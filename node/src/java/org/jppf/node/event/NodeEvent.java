/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.node.event;

import java.util.EventObject;

/**
 * Intances of this class describe events that occur within a nodes life cycle.
 * @author Laurent Cohen
 */
public class NodeEvent extends EventObject
{
	/**
	 * Enumeration of all possible event types.
	 */
	public enum EventType
	{
		/**
		 * Event type to specify a node is about to attempt connecting to the server. 
		 */
		START_CONNECT,
		/**
		 * Event type to specify a node has successfully connected to the server. 
		 */
		END_CONNECT,
		/**
		 * Event type to specify a node is disconnected from the server. 
		 */
		DISCONNECTED,
		/**
		 * Event type to specify a node is starting to execute one or more tasks,
		 * and is switching from idle to executing state. 
		 */
		START_EXEC,
		/**
		 * Event type to specify a node finished executing one or more tasks,
		 * and is switching from executing to idle state. 
		 */
		END_EXEC,
		/**
		 * Event type to specify a task was executed. 
		 */
		TASK_EXECUTED
	}

	/**
	 * Initialize this event with a specified event source.
	 * @param source the source of the event.
	 */
	public NodeEvent(EventType source)
	{
		super(source);
	}
	
	/**
	 * Get the type of this event.
	 * @return the type of event as an enumerated value.
	 */
	public EventType getType()
	{
		return (EventType) getSource();
	}
}
