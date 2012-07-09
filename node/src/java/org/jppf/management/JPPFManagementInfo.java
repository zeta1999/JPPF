/*
 * JPPF.
 * Copyright (C) 2005-2012 JPPF Team.
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

import java.io.Serializable;


/**
 * Instances of this class encapsulate the information required to access
 * the JMX server of a node.
 * @author Laurent Cohen
 */
public class JPPFManagementInfo implements Serializable, Comparable<JPPFManagementInfo>
{
  /**
   * Explicit serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * DRIVER information type.
   */
  public static final byte DRIVER = 0;
  /**
   * Node information type.
   */
  public static final byte NODE = 1;
  /**
   * The host on which the node is running.
   */
  private String host = null;
  /**
   * The port on which the node's JMX server is listening.
   */
  private int port = 11198;
  /**
   * Unique id for the node.
   */
  private String id = null;
  /**
   * The type of component this info is for, must be one of {@link #NODE} or {@link #DRIVER}.
   */
  private byte type = NODE;
  /**
   * Determines whether communication with the node or driver should be secure, i.e. via SSL/TLS.
   */
  private boolean secure = false;
  /**
   * The system information associated with the node at the time of the initial connection.
   */
  private transient JPPFSystemInformation systemInfo = null;

  /**
   * Initialize this information with the specified parameters, using {@link #NODE NODE} as type.
   * @param host the host on which the node is running.
   * @param port the port on which the node's JMX server is listening.
   * @param id unique id for the node's mbean server.
   */
  public JPPFManagementInfo(final String host, final int port, final String id)
  {
    this(host, port, id, NODE, false);
  }

  /**
   * Initialize this information with the specified parameters.
   * @param host the host on which the node is running.
   * @param port the port on which the node's JMX server is listening.
   * @param id unique id for the node's mbean server.
   * @param type the type of component this info is for, must be one of {@link #NODE NODE} or {@link #DRIVER DRIVER}.
   */
  public JPPFManagementInfo(final String host, final int port, final String id, final int type)
  {
    this(host, port, id, type, false);
  }

  /**
   * Initialize this information with the specified parameters.
   * @param host the host on which the node is running.
   * @param port the port on which the node's JMX server is listening.
   * @param id unique id for the node's mbean server.
   * @param type the type of component this info is for, must be one of {@link #NODE NODE} or {@link #DRIVER DRIVER}.
   * @param secure sepecifies whether communication with the node or driver should be secure, i.e. via SSL/TLS.
   */
  public JPPFManagementInfo(final String host, final int port, final String id, final int type, final boolean secure)
  {
    this.host = host;
    this.port = port;
    this.id = id;
    this.type = (byte) type;
    this.secure = secure;
  }

  /**
   * Get the host on which the node is running.
   * @return the host as a string.
   */
  public synchronized String getHost()
  {
    return host;
  }

  /**
   * Get the port on which the node's JMX server is listening.
   * @return the port as an int.
   */
  public synchronized int getPort()
  {
    return port;
  }

  /**
   * Get the hashcode for this instance.
   * @return the hashcode as an int.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (id == null) ? 0 : id.hashCode();
  }

  /**
   * Compare this object with another for equality.
   * @param obj the object to compare to.
   * @return true if the two objects are equal, false otherwise.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final JPPFManagementInfo other = (JPPFManagementInfo) obj;
    if (other.id == null) return id == null;
    return (id != null) && id.equals(other.id);
  }

  /**
   * Compare this object with an other.
   * @param o the other object to compare to.
   * @return a negative number if this object is less than the other, 0 if they are equal, a positive number otherwise.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final JPPFManagementInfo o)
  {
    if (o == null) return 1;
    if (this.equals(o)) return 0;
    // we want ascending alphabetical order
    int n = -1 * host.compareTo(o.getHost());
    if (n != 0) return n;

    if(port > o.getPort()) return +1;
    if(port < o.getPort()) return -1;
    return 0;
  }

  /**
   * Get a string representation of this node information.
   * @return a string with the host:port format.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(host).append(':').append(port);
    sb.append(", type=").append(type);
    sb.append(", id=").append(id);
    return sb.toString();
  }

  /**
   * Get the system information associated with the node at the time of the initial connection.
   * @return a <code>JPPFSystemInformation</code> instance.
   */
  public synchronized JPPFSystemInformation getSystemInfo()
  {
    return systemInfo;
  }

  /**
   * Set the system information associated with the node at the time of the initial connection.
   * @param systemInfo a <code>JPPFSystemInformation</code> instance.
   */
  public synchronized void setSystemInfo(final JPPFSystemInformation systemInfo)
  {
    this.systemInfo = systemInfo;
  }

  /**
   * Get the unique id for the node's mbean server.
   * @return the id as a string.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Get the type of component this info is for.
   * @return one of {@link #NODE NODE} or {@link #DRIVER DRIVER}.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Determine whether this information represents a real node.
   * @return true if this information represents a node, false otherwise.
   */
  public boolean isNode()
  {
    return type == NODE;
  }

  /**
   * Determine whether this information represents a driver, connected as a peer to the
   * driver from which this information is obtained.
   * @return true if this information represents a driver, false otherwise.
   */
  public boolean isDriver()
  {
    return type == DRIVER;
  }

  /**
   * Determine whether communication with the node or driver is be secure, i.e. via SSL/TLS.
   * @return <code>true</code> if the connection is secure, <code>false</code> otherwise.
   */
  public boolean isSecure()
  {
    return secure;
  }
}
