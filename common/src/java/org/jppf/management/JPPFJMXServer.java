/*
 * JPPF.
 * Copyright (C) 2005-2019 JPPF Team.
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

package org.jppf.management;

import java.lang.management.ManagementFactory;
import java.util.*;

import org.jppf.jmx.*;
import org.jppf.ssl.SSLHelper;
import org.jppf.utils.*;
import org.jppf.utils.configuration.*;
import org.slf4j.*;

/**
 * Wrapper around the JMXMP remote connector server implementation.
 * @author Laurent Cohen
 * @exclude
 */
public class JPPFJMXServer extends AbstractJMXServer {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(JPPFJMXServer.class);
  /**
   * Determines whether debug log statements are enabled.
   */
  private static final boolean debugEnabled = LoggingUtils.isDebugEnabled(log);
  /**
   * An ordered set of configuration properties to use for looking up the desired management port.
   */
  private final JPPFProperty<Integer> portProperty;

  /**
   * Initialize this JMX server with the specified uuid.
   * @param config the configuration to use.
   * @param id the unique id of the driver or node holding this jmx server.
   * @param ssl specifies whether JMX should be used over an SSL/TLS connection.
   * @param portProperty an ordered set of configuration properties to use for looking up the desired management port.
   * @exclude
   */
  public JPPFJMXServer(final TypedProperties config, final String id, final boolean ssl, final JPPFProperty<Integer> portProperty) {
    super(config);
    this.uuid = id;
    this.ssl = ssl;
    if (portProperty == null) this.portProperty = ssl ? JPPFProperties.MANAGEMENT_SSL_PORT : JPPFProperties.MANAGEMENT_PORT;
    else this.portProperty = portProperty;
    if (debugEnabled) log.debug("initializing with ssl={}, portProperty={}", ssl, this.portProperty);
  }

  /**
   * @exclude
   */
  @Override
  public void start(final ClassLoader cl) throws Exception {
    if (debugEnabled) log.debug("starting remote connector server");
    final ClassLoader tmp = Thread.currentThread().getContextClassLoader();
    lock.lock();
    try {
      Thread.currentThread().setContextClassLoader(cl);
      mbeanServer = ManagementFactory.getPlatformMBeanServer();
      managementPort = config.get(portProperty);
      if (debugEnabled) log.debug("managementPort={}, portProperties={}", managementPort, Arrays.asList(portProperty));
      final Map<String, Object> env = new HashMap<>();
      env.put("jmx.remote.default.class.loader", cl);
      env.put("jmx.remote.protocol.provider.class.loader", cl);
      if (ssl) SSLHelper.configureJMXProperties(JMXHelper.JPPF_JMX_PROTOCOL, env);
      int queueSize = config.get(JPPFProperties.JMX_NOTIF_QUEUE_SIZE);
      if (queueSize <= 0) queueSize = JPPFProperties.JMX_NOTIF_QUEUE_SIZE.getDefaultValue();
      env.put(JPPFJMXProperties.NOTIF_QUEUE_SIZE.getName(), queueSize);
      startConnectorServer(JMXHelper.JPPF_JMX_PROTOCOL, env);
    } finally {
      lock.unlock();
      Thread.currentThread().setContextClassLoader(tmp);
    }
  }
}
