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
package org.jppf.process;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Wrapper around an external process started with {@link java.lang.ProcessBuilder ProcessBuilder}.
 * Instances of this class read the output ands error streams generated by the process and provide
 * a notification mechanism with separate events for the respective streams.
 * @author Laurent Cohen
 */
public final class ProcessWrapper {
  /**
   * The process to handle.
   */
  private Process process;
  /**
   * The list of registered listeners.
   */
  protected final List<ProcessWrapperEventListener> eventListeners = new CopyOnWriteArrayList<>();
  /**
   * A name given to this object.
   */
  private String name;

  /**
   * Initialize this process handler.
   */
  public ProcessWrapper() {
  }

  /**
   * Initialize this process handler with the specified process.
   * @param process the process to handle.
   */
  public ProcessWrapper(final Process process) {
    setProcess(process);
  }

  /**
   * Get the process to handle.
   * @return a <code>Process</code> instance.
   */
  public Process getProcess() {
    return process;
  }

  /**
   * Set the process to handle.
   * If the process has already been set through this setter or the corresponding constructor, this method does nothing.
   * @param process - a <code>Process</code> instance.
   */
  public void setProcess(final Process process) {
    if (this.process == null) {
      this.process = process;
      if (name == null) {
        new StreamHandler(process.getInputStream(), true).start();
        new StreamHandler(process.getErrorStream(), false).start();
      } else {
        new StreamHandler(name + "-out", process.getInputStream(), true).start();
        new StreamHandler(name + "-err", process.getErrorStream(), false).start();
      }
    }
  }

  /**
   * Add a listener to the list of listeners.
   * @param listener the listener to add to the list.
   */
  public void addListener(final ProcessWrapperEventListener listener) {
    eventListeners.add(listener);
  }

  /**
   * Remove a listener from the list of listeners.
   * @param listener the listener to remove from the list.
   */
  public void removeListener(final ProcessWrapperEventListener listener) {
    eventListeners.remove(listener);
  }

  /**
   * Notify all listeners that a stream event has occurred.
   * @param output true if the event is for the output stream, false if it is for the error stream.
   * @param content the text that written to the stream.
   */
  protected synchronized void fireStreamEvent(final boolean output, final String content) {
    final ProcessWrapperEvent event = new ProcessWrapperEvent(content);
    for (ProcessWrapperEventListener listener: eventListeners) {
      if (output) listener.outputStreamAltered(event);
      else listener.errorStreamAltered(event);
    }
  }

  /**
   * Give a name to this object.
   * @param name the object's name.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Used to empty the standard or error output of a process, so as not to block the process.
   */
  private class StreamHandler extends Thread {
    /**
     * Flag to determine whether the event is for output or error stream.
     */
    private boolean output = true;
    /**
     * The stream to get the output from.
     */
    private InputStream is = null;

    /**
     * Initialize this handler with the specified stream and buffer receiving its content.
     * @param is the stream where output is taken from.
     * @param output true if this event is for an output stream, false for an error stream.
     */
    public StreamHandler(final InputStream is, final boolean output) {
      this.is = is;
      this.output = output;
    }

    /**
     * Initialize this handler with the specified stream and buffer receiving its content.
     * @param name a name to give tot he thread.
     * @param is the stream where output is taken from.
     * @param output true if this event is for an output stream, false for an error stream.
     */
    public StreamHandler(final String name, final InputStream is, final boolean output) {
      super(name);
      this.is = is;
      this.output = output;
    }

    /**
     * Monitor the stream for available data and write that data to the buffer.
     */
    @Override
    public void run() {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final boolean end = false;
        final int bufferSize = 8*1024;
        StringBuilder sb = new StringBuilder(bufferSize);
        while (!end) {
          final int c = reader.read();
          if (c == -1) break;      // end of file (the process has exited)
          if (c == '\r') continue; // skip the line feed
          sb.append((char) c);
          if ((sb.length() >= bufferSize) || (c == '\n')) {
            fireStreamEvent(output, sb.toString());
            sb = new StringBuilder(bufferSize);
          }
        }
      } catch(@SuppressWarnings("unused") final IOException ignore) {
      } catch(final Throwable t) {
        t.printStackTrace();
      }
    }
  }
}
