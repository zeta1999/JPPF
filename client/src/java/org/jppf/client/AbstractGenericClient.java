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
package org.jppf.client;

import java.util.*;
import java.util.concurrent.*;

import org.jppf.client.balancer.*;
import org.jppf.client.balancer.queue.JPPFPriorityQueue;
import org.jppf.client.event.*;
import org.jppf.comm.discovery.*;
import org.jppf.discovery.*;
import org.jppf.queue.*;
import org.jppf.startup.JPPFClientStartupSPI;
import org.jppf.utils.*;
import org.jppf.utils.configuration.*;
import org.jppf.utils.hooks.HookFactory;
import org.slf4j.*;

/**
 * This class provides an API to submit execution requests and administration commands,
 * and request server information data.<br>
 * It has its own unique identifier, used by the nodes, to determine whether classes from
 * the submitting application should be dynamically reloaded or not, depending on whether
 * the uuid has changed or not.
 * @author Laurent Cohen
 */
public abstract class AbstractGenericClient extends AbstractJPPFClient implements QueueListener<ClientJob, ClientJob, ClientTaskBundle> {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(AbstractGenericClient.class);
  /**
   * Determines whether debug-level logging is enabled.
   */
  private static boolean debugEnabled = LoggingUtils.isDebugEnabled(log);
  /**
   * Constant for JPPF automatic connection discovery
   */
  static final String VALUE_JPPF_DISCOVERY = "jppf_discovery";
  /**
   * The pool of threads used for submitting execution requests.
   */
  private ThreadPoolExecutor executor = null;
  /**
   * The JPPF configuration properties.
   */
  private TypedProperties config;
  /**
   * Performs server discovery.
   */
  private JPPFMulticastReceiverThread receiverThread = null;
  /**
   * The job manager.
   */
  private JobManager jobManager;
  /**
   * Handles the class loaders used for inbound class loading requests from the servers.
   */
  private final ClassLoaderRegistrationHandler classLoaderRegistrationHandler;
  /**
   * The list of listeners on the queue associated with this client.
   */
  private final List<ClientQueueListener> queueListeners = new CopyOnWriteArrayList<>();
  /**
   * Supports built-in and custom discovery mechanisms.
   */
  final DriverDiscoveryHandler<ClientConnectionPoolInfo> discoveryHandler = new DriverDiscoveryHandler(ClientDriverDiscovery.class);
  /**
   * Listens to new connection pool notifications from {@link DriverDiscovery} instances.
   */
  private final ClientDriverDiscoveryListener discoveryListener;

  /**
   * Initialize this client with a specified application UUID.
   * @param uuid the unique identifier for this local client.
   * @param configuration the object holding the JPPF configuration.
   * @param listeners the listeners to add to this JPPF client to receive notifications of new connections.
   */
  public AbstractGenericClient(final String uuid, final TypedProperties configuration, final ConnectionPoolListener... listeners) {
    super(uuid);
    this.classLoaderRegistrationHandler = new ClassLoaderRegistrationHandler();
    if ((listeners != null) && (listeners.length > 0)) for (ConnectionPoolListener listener : listeners) addConnectionPoolListener(listener);
    discoveryListener = new ClientDriverDiscoveryListener(this);
    init(configuration);
  }

  /**
   * Initialize this client with the specified configuration.
   * @param configuration the configuration to use with this client.
   */
  protected void init(final TypedProperties configuration) {
    if (debugEnabled) log.debug("initializing client");
    closed.set(false);
    resetting.set(false);
    this.config = initConfig(configuration);
    try {
      HookFactory.registerSPIMultipleHook(JPPFClientStartupSPI.class, null, null).invoke("run");
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    if (jobManager == null) jobManager = createJobManager();
    Runnable r = new Runnable() {
      @Override
      public void run() {
        initPools(config);
      }
    };
    new Thread(r, "InitPools").start();
  }

  /**
   * Get JPPF configuration properties. These properties are unmodifiable.
   * @return <code>TypedProperties</code> instance. With JPPF configuration.
   */
  public TypedProperties getConfig() {
    return config;
  }

  /**
   * Initialize this client's configuration.
   * @param configuration an object holding the JPPF configuration.
   * @return <code>TypedProperties</code> instance holding JPPF configuration. Never be <code>null</code>.
   * @exclude
   */
  protected TypedProperties initConfig(final Object configuration) {
    if (configuration instanceof TypedProperties) return (TypedProperties) configuration;
    return JPPFConfiguration.getProperties();
  }

  /**
   * {@inheritDoc}
   * @exclude
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void initPools(final TypedProperties config) {
    if (debugEnabled) log.debug("initializing connections");
    int coreThreads = Runtime.getRuntime().availableProcessors();
    BlockingQueue<Runnable> queue = new SynchronousQueue<>();
    executor = new ThreadPoolExecutor(coreThreads, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, queue, new JPPFThreadFactory("JPPF Client"));
    executor.allowCoreThreadTimeOut(true);
    if (config.get(JPPFProperties.LOCAL_EXECUTION_ENABLED)) setLocalExecutionEnabled(true);
    if (config.get(JPPFProperties.REMOTE_EXECUTION_ENABLED)) initRemotePools(config);
    discoveryHandler.register(discoveryListener.open()).start();
  }

  /**
   * Initialize remote connection pools according to configuration.
   * @param config The JPPF configuration properties.
   * @exclude
   */
  protected void initRemotePools(final TypedProperties config) {
    try {
      boolean initPeers;
      if (config.get(JPPFProperties.DISCOVERY_ENABLED)) {
        final int priority = config.get(JPPFProperties.DISCOVERY_PRIORITY);
        boolean acceptMultipleInterfaces = config.get(JPPFProperties.DISCOVERY_ACCEPT_MULTIPLE_INTERFACES);
        if (debugEnabled) log.debug("initializing connections from discovery with priority = {} and acceptMultipleInterfaces = {}", priority, acceptMultipleInterfaces);
        receiverThread = new JPPFMulticastReceiverThread(new JPPFMulticastReceiverThread.ConnectionHandler() {
          @Override
          public void onNewConnection(final String name, final JPPFConnectionInformation info) {
            boolean ssl = config.get(JPPFProperties.SSL_ENABLED);
            if (info.hasValidPort(ssl)) {
              int poolSize = config.get(JPPFProperties.POOL_SIZE);
              int jmxPoolSize = config.get(JPPFProperties.JMX_POOL_SIZE);
              newConnectionPool(name, info, priority, poolSize, ssl, jmxPoolSize);
            } else {
              String type = ssl ? "secure" : "plain";
              String msg = String.format("this client cannot fulfill a %s connection request to %s:%d because the host does not expose that port as a %s port",
                type, info.host, info.getValidPort(ssl), type);
              log.warn(msg);
            }
          }
        }, new IPFilter(config), acceptMultipleInterfaces);
        new Thread(receiverThread).start();
        initPeers = false;
      } else {
        receiverThread = null;
        initPeers = true;
      }

      if (debugEnabled) log.debug("found peers in the configuration");
      String[] names = config.get(JPPFProperties.DRIVERS);
      if (debugEnabled) log.debug("list of drivers: " + Arrays.asList(names));
      for (String name : names) initPeers |= VALUE_JPPF_DISCOVERY.equals(name);

      if (initPeers) {
        List<ClientConnectionPoolInfo> infoList = new ArrayList<>();
        for (String name : names) {
          if (!VALUE_JPPF_DISCOVERY.equals(name)) {
            JPPFConnectionInformation info = new JPPFConnectionInformation();
            boolean ssl = config.getBoolean(name + '.' + JPPFProperties.SSL_ENABLED.getName(), false);
            JPPFProperty<String> serverProp = JPPFProperties.SERVER_HOST;
            String host =  config.getString(name + '.' + serverProp.getName(), serverProp.getDefaultValue());
            info.host = host;
            JPPFProperty<Integer> portProp = JPPFProperties.SERVER_PORT;
            int port = config.getInt(name + '.' + portProp.getName(), portProp.getDefaultValue());
            if (!ssl) info.serverPorts = new int[] { port };
            else info.sslServerPorts = new int[] { port };
            portProp = ssl ? JPPFProperties.MANAGEMENT_SSL_PORT : JPPFProperties.MANAGEMENT_PORT;
            int jmxport = config.getInt(name + '.' + portProp.getName(), -1);
            if (!ssl) info.managementPort = jmxport;
            else info.sslManagementPort = jmxport;
            int priority = config.getInt(name + ".jppf.priority", 0);
            if (receiverThread != null) receiverThread.addConnectionInformation(info);
            int poolSize = config.get(new IntProperty(name + '.' + JPPFProperties.POOL_SIZE.getName(), 1, 1, Integer.MAX_VALUE));
            int jmxPoolSize = config.get(new IntProperty(name + '.' + JPPFProperties.JMX_POOL_SIZE.getName(), 1, 1, Integer.MAX_VALUE));
            infoList.add(new ClientConnectionPoolInfo(name, ssl, host, port, priority, poolSize, jmxPoolSize));
          }
        }
        Collections.sort(infoList, new Comparator<ClientConnectionPoolInfo>() { // order by decreasing priority
          @Override
          public int compare(final ClientConnectionPoolInfo o1, final ClientConnectionPoolInfo o2) {
            int p1 = o1.getPriority(), p2 = o2.getPriority();
            return p1 > p2 ? -1 : (p1 < p2 ? 1 : 0);
          }
        });
        for (ClientConnectionPoolInfo poolInfo: infoList) newConnectionPool(poolInfo);
      }
    } catch(Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Called when a new connection pool is read from the configuration.
   * @param info the information required for the connection to connect to the driver.
   * @exclude
   */
  protected void newConnectionPool(final ClientConnectionPoolInfo info) {
    if (debugEnabled) log.debug("new connection pool: {}", info.getName());
    final int size = info.getPoolSize() > 0 ? info.getPoolSize() : 1;
    Runnable r = new Runnable() {
      @Override public void run() {
        final JPPFConnectionPool pool = new JPPFConnectionPool((JPPFClient) AbstractGenericClient.this, poolSequence.incrementAndGet(), info);
        newConnectionPool(pool, info.getHost(), info.getPort());
      }
    };
    executor.execute(r);
  }

  /**
   * Called when a new connection pool is read from the configuration.
   * @param name the name assigned to the connection pool.
   * @param info the information required for the connection to connect to the driver.
   * @param priority the priority assigned to the connection.
   * @param poolSize the size of the connection pool.
   * @param ssl determines whether the pool is for SSL connections.
   * @param jmxPoolSize the core size of the JMX connections pool.
   * @exclude
   */
  protected void newConnectionPool(final String name, final JPPFConnectionInformation info, final int priority, final int poolSize, final boolean ssl, final int jmxPoolSize) {
    if (debugEnabled) log.debug("new connection pool: {}", name);
    final int size = poolSize > 0 ? poolSize : 1;
    Runnable r = new Runnable() {
      @Override public void run() {
        final JPPFConnectionPool pool = new JPPFConnectionPool((JPPFClient) AbstractGenericClient.this, poolSequence.incrementAndGet(), name, priority, size, ssl, jmxPoolSize);
        newConnectionPool(pool, info.host, ssl ? info.sslServerPorts[0] : info.serverPorts[0]);
      }
    };
    executor.execute(r);
  }

  /**
   * Initialize a new connection pool.
   * @param pool the pool to configure and init.
   * @param host the driver host.
   * @param port the driver port.
   */
  private void newConnectionPool(final JPPFConnectionPool pool, final String host, final int port) {
    pool.setDriverPort(port);
    synchronized(pools) {
      pools.putValue(pool.getPriority(), pool);
    }
    HostIP hostIP = new HostIP(host, host);
    if (JPPFConfiguration.getProperties().get(JPPFProperties.RESOLVE_ADDRESSES)) hostIP = NetworkUtils.getHostIP(host);
    if (debugEnabled) log.debug("'{}' was resolved into '{}'", host, hostIP.hostName());
    pool.setDriverHostIP(hostIP);
    fireConnectionPoolAdded(pool);
    for (int i=1; i<=pool.getSize(); i++) {
      if (isClosed()) return;
      submitNewConnection(pool);
    }
  }

  /**
   * Called to submit the initialization of a new connection.
   * @param pool thez connection pool to which the connection belongs.
   * @exclude
   */
  protected void submitNewConnection(final JPPFConnectionPool pool) {
    AbstractJPPFClientConnection c = createConnection(pool.getName() + "-" + pool.nextSequence(), pool);
    newConnection(c);
  }

  /**
   * Create a new driver connection based on the specified parameters.
   * @param name the name of the connection.
   * @param pool id of the connection pool the connection belongs to.
   * @return an instance of a subclass of {@link AbstractJPPFClientConnection}.
   */
  abstract AbstractJPPFClientConnection createConnection(String name, final JPPFConnectionPool pool);

  @Override
  void newConnection(final AbstractJPPFClientConnection c) {
    if (isClosed()) return;
    log.info("connection [" + c.getName() + "] created");
    c.addClientConnectionStatusListener(this);
    executor.submit(new ConnectionInitializer(c));
    fireConnectionAdded(c);
    if (debugEnabled) log.debug("end of of newConnection({})", c.getName());
  }

  /**
   * Invoked when the status of a connection has changed to <code>JPPFClientConnectionStatus.FAILED</code>.
   * @param connection the connection that failed.
   * @exclude
   */
  @Override
  protected void connectionFailed(final JPPFClientConnection connection) {
    if (debugEnabled) log.debug("Connection [{}] {}", connection.getName(), connection.getStatus());
    JPPFConnectionPool pool = connection.getConnectionPool();
    connection.close();
    boolean poolRemoved = removeClientConnection(connection);
    fireConnectionRemoved(connection);
    if (poolRemoved) {
      fireConnectionPoolRemoved(pool);
      if (receiverThread != null) receiverThread.removeConnectionInformation(connection.getDriverUuid());
      ClientConnectionPoolInfo info = pool.getDiscoveryInfo();
      if (info != null) {
        boolean b = discoveryListener.onPoolRemoved(info);
        if (debugEnabled) log.debug("removal of {} = {}", info, b);
      }
    }
  }

  @Override
  public void close() {
    close(false);
  }

  /**
   * Close this client.
   * @param reset if <code>true</code>, then this client is left in a state where it can be reopened.
   * @exclude
   */
  protected void close(final boolean reset) {
    if (closed.get()) return;
    if (debugEnabled) log.debug("closing JPPF client");
    closed.set(true);
    if (debugEnabled) log.debug("closing discovery handler");
    discoveryListener.close();
    discoveryHandler.stop();
    if (debugEnabled) log.debug("closing broadcast receiver");
    if (receiverThread != null) {
      receiverThread.close();
      receiverThread = null;
    }
    if (debugEnabled) log.debug("unregistering startup classes");
    HookFactory.unregister(JPPFClientStartupSPI.class);
    if (jobManager != null) {
      if (reset) {
        if (debugEnabled) log.debug("resetting job manager");
        jobManager.reset();
      } else {
        if (debugEnabled) log.debug("closing job manager");
        jobManager.close();
        jobManager = null;
      }
    }
    if (debugEnabled) log.debug("closing executor");
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
    if (debugEnabled) log.debug("clearing registered class loaders");
    classLoaderRegistrationHandler.close();
    super.close();
  }

  /**
   * Determine whether local execution is enabled on this client.
   * @return <code>true</code> if local execution is enabled, <code>false</code> otherwise.
   */
  public boolean isLocalExecutionEnabled() {
    JobManager jobManager = getJobManager();
    return (jobManager != null) && jobManager.isLocalExecutionEnabled();
  }

  /**
   * Specify whether local execution is enabled on this client.
   * @param localExecutionEnabled <code>true</code> to enable local execution, <code>false</code> otherwise
   */
  public void setLocalExecutionEnabled(final boolean localExecutionEnabled) {
    JobManager jobManager = getJobManager();
    if (jobManager != null) jobManager.setLocalExecutionEnabled(localExecutionEnabled);
  }

  /**
   * Determine whether there is a client connection available for execution.
   * @return true if at least one connection is available, false otherwise.
   */
  public boolean hasAvailableConnection() {
    JobManager jobManager = getJobManager();
    return (jobManager != null) && jobManager.hasAvailableConnection();
  }

  /**
   * {@inheritDoc}
   * @exclude
   */
  @Override
  public void statusChanged(final ClientConnectionStatusEvent event) {
    super.statusChanged(event);
    JobManager jobManager = getJobManager();
    if(jobManager != null) {
      ClientConnectionStatusListener listener = jobManager.getClientConnectionStatusListener();
      if (listener != null) listener.statusChanged(event);
    }
  }

  /**
   * Get the pool of threads used for submitting execution requests.
   * @return a {@link ThreadPoolExecutor} instance.
   * @exclude
   */
  public ThreadPoolExecutor getExecutor() {
    return executor;
  }

  /**
   * Get the job manager for this JPPF client.
   * @return a <code>JobManager</code> instance.
   * @exclude
   */
  public JobManager getJobManager() {
    return jobManager;
  }

  /**
   * Create the job manager for this JPPF client.
   * @return a <code>JobManager</code> instance.
   */
  abstract JobManager createJobManager();

  /**
   * Cancel the job with the specified id.
   * @param jobId the id of the job to cancel.
   * @throws Exception if any error occurs.
   * @see org.jppf.server.job.management.DriverJobManagementMBean#cancelJob(java.lang.String)
   * @return a <code>true</code> when cancel was successful <code>false</code> otherwise.
   */
  public boolean cancelJob(final String jobId) throws Exception {
    if (jobId == null || jobId.isEmpty()) throw new IllegalArgumentException("jobUUID is blank");
    if (debugEnabled) log.debug("request to cancel job with uuid=" + jobId);
    return getJobManager().cancelJob(jobId);
  }

  /**
   * Get a class loader associated with a job.
   * @param uuid unique id assigned to classLoader. Added as temporary fix for problems hanging jobs.
   * @return a {@code Collection} of {@code RegisteredClassLoader} instances.
   * @exclude
   */
  public Collection<ClassLoader> getRegisteredClassLoaders(final String uuid) {
    return classLoaderRegistrationHandler.getRegisteredClassLoaders(uuid);
  }

  /**
   * Register a class loader for the specified job uuid.
   * @param cl the <code>ClassLoader</code> instance to register.
   * @param uuid the uuid of the job for which the class loader is registered.
   * @return a <code>RegisteredClassLoader</code> instance.
   */
  public ClassLoader registerClassLoader(final ClassLoader cl, final String uuid) {
    return classLoaderRegistrationHandler.registerClassLoader(cl, uuid);
  }

  /**
   * Unregisters the class loader associated with the specified job uuid.
   * @param uuid the uuid of the job the class loaders are associated with.
   * @exclude
   */
  public void unregisterClassLoaders(final String uuid) {
    classLoaderRegistrationHandler.unregister(uuid);
  }

  /**
   * Register the specified listener to receive client queue event notifications.
   * @param listener the listener to register.
   * @since 4.1
   */
  public void addClientQueueListener(final ClientQueueListener listener) {
    queueListeners.add(listener);
  }

  /**
   * Unregister the specified listener.
   * @param listener the listener to unregister.
   * @since 4.1
   */
  public void removeClientQueueListener(final ClientQueueListener listener) {
    queueListeners.remove(listener);
  }

  /**
   * Notify all client queue listeners that a queue event has occurred.
   * @param qEvent the actual event which occurred in the queue.
   * @param jobAdded {@code true} for a job added event, {@code false} for a job removed event.
   * @exclude
   * @since 4.1
   */
  protected void fireQueueEvent(final QueueEvent<ClientJob, ClientJob, ClientTaskBundle> qEvent, final boolean jobAdded) {
    ClientQueueEvent event = new ClientQueueEvent((JPPFClient) this, qEvent.getBundleWrapper().getJob(), (JPPFPriorityQueue) qEvent.getQueue());
    if (jobAdded) {
      for (ClientQueueListener listener: queueListeners) listener.jobAdded(event);
    } else {
      for (ClientQueueListener listener: queueListeners) listener.jobRemoved(event);
    }
  }

  /**
   * {@inheritDoc}
   * @exclude
   * @since 4.1
   */
  @Override
  public void bundleAdded(final QueueEvent event) {
    fireQueueEvent(event, true);
  }

  /**
   * {@inheritDoc}
   * @exclude
   * @since 4.1
   */
  @Override
  public void bundleRemoved(final QueueEvent event) {
    fireQueueEvent(event, false);
  }
}
