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

package org.jppf.jca.work;

import static org.jppf.client.JPPFClientConnectionStatus.*;

import java.net.*;

import org.jppf.JPPFError;
import org.jppf.client.*;
import org.jppf.client.event.*;
import org.jppf.comm.discovery.JPPFConnectionInformation;
import org.jppf.comm.socket.*;
import org.jppf.server.protocol.JPPFTaskBundle;
import org.jppf.utils.JPPFConfiguration;
import org.slf4j.*;

/**
 * This class provides an API to submit execution requests and administration
 * commands, and request server information data.<br>
 * It has its own unique identifier, used by the nodes, to determine whether
 * classes from the submitting application should be dynamically reloaded or not
 * depending on whether the uuid has changed or not.
 * @author Laurent Cohen
 */
public class JPPFJcaClientConnection extends AbstractJPPFClientConnection
{
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(JPPFJcaClientConnection.class);
  /**
   * Determines whether the debug level is enabled in the logging configuration, without the cost of a method call.
   */
  private static boolean debugEnabled = log.isDebugEnabled();

  /**
   * Initialize this client with a specified application UUID.
   * @param client the JPPF client that owns this connection.
   * @param uuid the unique identifier for this local client.
   * @param name configuration name for this local client.
   * @param info the connection properties for this connection.
   * @param ssl determines whether this is an SSL connection.
   */
  public JPPFJcaClientConnection(final JPPFJcaClient client, final String uuid, final String name, final JPPFConnectionInformation info, final boolean ssl)
  {
    this.client = client;
    this.ssl = ssl;
    this.connectionUuid = client.getUuid() + '_' + connectionCount.incrementAndGet();
    this.serializationHelperClassName = JPPFConfiguration.getProperties().getString("jppf.serialization.helper.class", "org.jppf.jca.serialization.JcaSerializationHelperImpl");
    configure(uuid, name, info.host, ssl ? info.sslServerPorts[0] : info.serverPorts[0], 0, ssl);
    status.set(DISCONNECTED);
    jmxPort = ssl ? info.sslManagementPort : info.managementPort;
    initializeJmxConnection();
  }

  /**
   * Initialize this client connection.
   * @see org.jppf.client.JPPFClientConnection#init()
   */
  @Override
  public void init()
  {
    try
    {
      if (closed) throw new IllegalStateException("this client connection is closed");
      try
      {
        //String s = InetAddress.getByName(host).getCanonicalHostName();
        host = InetAddress.getByName(host).getCanonicalHostName();
        displayName = name + '[' + host + ':' + port + ']';
        getJmxConnection().setHost(host);
      }
      catch (UnknownHostException e)
      {
        displayName = name;
      }
      delegate = new JcaClassServerDelegate(this, name, client.getUuid(), host, port);
      delegate.addClientConnectionStatusListener(new ClientConnectionStatusListener()
      {
        @Override
        public void statusChanged(final ClientConnectionStatusEvent event)
        {
          delegateStatusChanged(event);
        }
      });
      taskServerConnection.addClientConnectionStatusListener(new ClientConnectionStatusListener()
      {
        @Override
        public void statusChanged(final ClientConnectionStatusEvent event)
        {
          taskServerConnectionStatusChanged(event);
        }
      });
      connect();
      JPPFClientConnectionStatus status = getStatus();
      if (debugEnabled) log.debug("connection [" + name + "] status=" + status);
      if (client.isClosed()) close();
      else if ((status == ACTIVE) || (status == EXECUTING))
      {
        client.addClientConnection(this);
        if (debugEnabled) log.debug("connection [" + name + "] added to the client pool");
      }
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      setStatus(FAILED);
    }
    catch (JPPFError e)
    {
      setStatus(FAILED);
      throw e;
    }
  }

  /**
   * Connect to the driver.
   * @throws Exception if connection failed.
   */
  protected void connect() throws Exception
  {
    ((JcaClassServerDelegate) delegate).performConnection();
    if (!delegate.isClosed())
    {
      Thread t = new Thread(delegate);
      t.setName('[' + delegate.getName() + " : class delegate]");
      t.start();
      taskServerConnection.init();
    }
  }

  @Override
  public void sendTasks(final ClassLoader cl, final JPPFTaskBundle header, final JPPFJob job) throws Exception
  {
    if (debugEnabled) log.debug("sending tasks bundle with requestUuid=" + header.getRequestUuid());
    ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
    try
    {
      if (cl != null) Thread.currentThread().setContextClassLoader(cl);
      super.sendTasks(cl, header, job);
    }
    catch (Exception e)
    {
      if (debugEnabled) log.debug(e.getMessage(), e);
      throw e;
    }
    catch (Error e)
    {
      if (debugEnabled) log.debug(e.getMessage(), e);
      throw e;
    }
    finally
    {
      if (cl != null) Thread.currentThread().setContextClassLoader(oldCl);
    }
  }

  /**
   * Submit the request to the server.
   * @param job the job to execute remotely.
   * @throws Exception if an error occurs while sending the request.
   * @see org.jppf.client.JPPFClientConnection#submit(org.jppf.client.JPPFJob)
   * @deprecated job submissions should be performed via {@link JPPFClient#submit(JPPFJob)} directly.
   */
  @Override
  public void submit(final JPPFJob job) throws Exception
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a socket initializer.
   * @return an instance of <code>SocketInitializerImpl</code>.
   * @see org.jppf.client.AbstractJPPFClientConnection#createSocketInitializer()
   */
  @Override
  protected SocketInitializer createSocketInitializer()
  {
    //return new JcaSocketInitializer();
    return new SocketInitializerImpl();
  }

  /**
   * Get the JPPF client that manages connections to the JPPF drivers.
   * @return a <code>JPPFJcaClient</code> instance.
   */
  @Override
  public JPPFJcaClient getClient()
  {
    return (JPPFJcaClient) client;
  }
}