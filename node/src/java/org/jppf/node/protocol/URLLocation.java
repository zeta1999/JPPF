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

package org.jppf.node.protocol;

import java.io.*;
import java.net.*;

import org.jppf.utils.ExceptionUtils;
import org.slf4j.*;

/**
 * Wrapper for manipulating data from a URL.
 * This implementation of the {@link Location} interface allows writing to and reading from a URL.
 * @author Laurent Cohen
 */
public class URLLocation extends AbstractLocation<URL>
{
  /**
   * Explicit serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(URLLocation.class);
  /**
   * Determines whether the debug level is enabled in the logging configuration, without the cost of a method call.
   */
  private static boolean debugEnabled = log.isDebugEnabled();
  /**
   * The size of the artifact pointed to by this URL location.
   * We attempt to cache it to avoid looking it up by opening a connection every time.
   */
  private long size =-1L;
  /**
   * Determines whether an attempt to obtain the size of the content has already been made.
   * This attribute is transient so that it is "reset" whenever it is deserialized in a remote JVM.
   * This forces a new attempt to be made to get the size, in case the URL was reachable from where this location was created.
   */
  private transient boolean sizeAttemptMade = false;

  /**
   * Initialize this location with the specified file path.
   * @param url a {@link URL}.
   */
  public URLLocation(final URL url)
  {
    super(url);
  }

  /**
   * Initialize this location with the specified file path.
   * @param url a URL in string format.
   * @throws MalformedURLException if the url is malformed.
   */
  public URLLocation(final String url) throws MalformedURLException
  {
    super(new URL(url));
  }

  @Override
  public InputStream getInputStream() throws Exception
  {
    return path.openStream();
  }

  @Override
  public OutputStream getOutputStream() throws Exception
  {
    URLConnection conn = path.openConnection();
    conn.setDoOutput(true);
    return conn.getOutputStream();
  }

  /**
   * This method attemps to get the size of the content pointed to by the URL. It may not always be possible,
   * depending on the protocol and what's at the other end.
   * @return the content size if it is available, or -1 if it isn't.
   */
  @Override
  public long size()
  {
    if ((size < 0L) && !sizeAttemptMade)
    {
      URLConnection c = null;
      try
      {
        c = path.openConnection();
        c.connect();
        size = c.getContentLengthLong();
      }
      catch (Exception e)
      {
        String msg = "Error while trying to get the content length of {} : {}";
        if (debugEnabled) log.debug(msg, this, ExceptionUtils.getStackTrace(e));
        else log.warn(msg, this, ExceptionUtils.getMessage(e));
      }
      finally
      {
        sizeAttemptMade = true;
      }
    }
    return size;
  }
}