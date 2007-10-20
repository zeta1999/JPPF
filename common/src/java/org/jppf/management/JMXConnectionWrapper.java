/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005-2007 JPPF Team.
 * http://www.jppf.org
 *
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

package org.jppf.management;

import java.io.IOException;
import java.util.*;

import javax.management.*;
import javax.management.remote.*;

import org.apache.commons.logging.*;
import org.jppf.utils.ThreadSynchronization;

/**
 * Wrapper around a JMX connection, providing a thread-safe way of handling disconnections and recovery.
 * @author Laurent Cohen
 */
public class JMXConnectionWrapper extends ThreadSynchronization
{
	/**
	 * Logger for this class.
	 */
	private static Log log = LogFactory.getLog(JMXConnectionWrapper.class);
	/**
	 * Determines whether debug log statements are enabled.
	 */
	private static boolean debugEnabled = log.isDebugEnabled();
	/**
	 * URL of the MBean server, in a JMX-compliant format.
	 */
	private JMXServiceURL url = null;
	/**
	 * The JMX client.
	 */
	private JMXConnector jmxc = null;
	/**
	 * A connection to the MBean server.
	 */
	private MBeanServerConnection mbeanConnection = null;
	/**
	 * The host the server is running on.
	 */
	private String host = null;
	/**
	 * The RMI port used by the server.
	 */
	private int port = 0;
	/**
	 * Contains the names of all MBeans registered with the remote MBean server.
	 */
	private Set<ObjectName> allNames = new HashSet();
	/**
	 * The connection thread that performs the connection to the management server.
	 */
	private JMXConnectionThread connectionThread = null;
	/**
	 * A string representing this connection, used for logging purposes.
	 */
	private String idString = null;
  /**
   * Default credentials for authenticating with the MBean server.
   */
  private String[] credentials = { "username" , "password" }; 

	/**
	 * Initialize the connection to the remote MBean server.
	 * @param host the host the server is running on.
	 * @param port the RMI port used by the server.
	 */
	public JMXConnectionWrapper(String host, int port)
	{
		this.host = host;
		this.port = port;
		
		idString = "[" + (host == null ? "_" : host) + ":" + port + "] ";
		try
		{
			url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host+":"+port+"/jppf");
		}
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Initialize the connection to the remote MBean server.
	 */
	public void connect()
	{
		connectionThread = new JMXConnectionThread();
		new Thread(connectionThread).start();
	}

	/**
	 * Initialize the connection to the remote MBean server.
	 * @throws Exception if the connection could not be established.
	 */
	private void performConnection() throws Exception
	{
    synchronized(this)
    {
      HashMap env = new HashMap(); 
      env.put("jmx.remote.credentials", credentials); 
      jmxc = JMXConnectorFactory.connect(url, env);
    	mbeanConnection = jmxc.getMBeanServerConnection();
    	/*
    	Set set = mbeanConnection.queryNames(null, null);
    	for (Object o: set) allNames.add((ObjectName) o);
    	*/
    }
		log.info(getId() + "RMI connection successfully established");
	}

	/**
	 * Close the connection to the remote MBean server.
	 * @throws Exception if the connection could not be closed.
	 */
	public void close() throws Exception
	{
		connectionThread.close();
    jmxc.close();
	}

	/**
	 * Invoke a method on the specified MBean.
	 * @param name the name of the MBean.
	 * @param methodName the name of the method to invoke.
	 * @param params the method parameter values.
	 * @param signature the types of the method parameters.
	 * @return an object or null.
	 * @throws Exception if the invocation failed.
	 */
	public Object invoke(String name, String methodName, Object[] params, String[] signature) throws Exception
	{
		if (connectionThread.isConnecting()) return null;
		Object result = null;
		try
		{
	    ObjectName mbeanName = new ObjectName(name);
  		result = getMbeanConnection().invoke(mbeanName, methodName, params, signature);
	    /*
  		for (ObjectName objectName: allNames)
	    {
	    	if (mbeanName.apply(objectName))
	    	{
	    		result = getMbeanConnection().invoke(objectName, methodName, params, signature);
	    		break;
	    	}
	    }
	    */
		}
		catch(IOException e)
		{
			connectionThread.resume();
			log.info(getId() + e.getMessage(), e);
			//throw e;
		}
		return result;
	}

	/**
	 * This class is intended to be used as a thread that attempts to (re-)connect to
	 * the management server.
	 */
	public class JMXConnectionThread extends ThreadSynchronization implements Runnable
	{
		/**
		 * Determines the suspended state of this connection thread.
		 */
		private boolean suspended = false;
		/**
		 * Determines the connecting state of this connection thread.
		 */
		private boolean connecting = true;

		/**
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			while (!isStopped())
			{
				if (isSuspended())
				{
					if (debugEnabled) log.debug(getId() + "about to go to sleep");
					goToSleep();
					continue;
				}
				if (connecting)
				{
					try
					{
						if (debugEnabled) log.debug(getId() + "about to perform RMI connection attempts");
						performConnection();
						if (debugEnabled) log.debug(getId() + "about to suspend RMI connection attempts");
						wakeUp();
						suspend();
					}
					catch(Exception ignored)
					{
						if (debugEnabled) log.debug(getId(), ignored);
						try
						{
							Thread.sleep(100);
						}
						catch(InterruptedException e)
						{
							log.error(e.getMessage(), e);
						}
					}
				}
			}
		}

		/**
		 * Suspend the current thread.
		 */
		public synchronized void suspend()
		{
			if (debugEnabled) log.debug(getId() + "suspending RMI connection attempts");
			setConnecting(false);
			suspended = true;
		}

		/**
		 * Resume the current thread's execution.
		 */
		public synchronized void resume()
		{
			if (debugEnabled) log.debug(getId() + "resuming RMI connection attempts");
			setConnecting(true);
			suspended = false;
			wakeUp();
		}

		/**
		 * Stop this thread.
		 */
		public synchronized void close()
		{
			setConnecting(false);
			setStopped(true);
		}

		/**
		 * Get the connecting state of this connection thread.
		 * @return true if the connection is established, false otherwise.
		 */
		public synchronized boolean isConnecting()
		{
			return connecting;
		}

		/**
		 * Get the connecting state of this connection thread.
		 * @param connecting true if the connection is established, false otherwise.
		 */
		public void setConnecting(boolean connecting)
		{
			this.connecting = connecting;
		}

		/**
		 * Determines the suspended state of this connection thread.
		 * @return true if the thread is suspended, false otherwise. 
		 */
		public synchronized boolean isSuspended()
		{
			return suspended;
		}
	}

	/**
	 * Get the host the server is running on.
	 * @return the host as a string.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Get the RMI port used by the server.
	 * @return the port as an int.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Get a string describing this connection.
	 * @return a string in the format host:port.
	 */
	public String getId()
	{
		return idString;
	}

	/**
	 * Get the connection to the MBean server.
	 * @return a <code>MBeanServerConnection</code> instance.
	 */
	public synchronized MBeanServerConnection getMbeanConnection()
	{
		return mbeanConnection;
	}
}
