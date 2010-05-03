/*
 * JPPF.
 * Copyright (C) 2005-2010 JPPF Team.
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

package org.jppf.server.nio.nodeserver;

import static java.nio.channels.SelectionKey.*;

import java.io.InputStream;
import java.util.concurrent.atomic.*;

import org.apache.commons.logging.*;
import org.jppf.comm.socket.IOHandler;
import org.jppf.io.*;
import org.jppf.server.nio.*;
import org.jppf.utils.*;

/**
 * Wrapper implementation for a local node's communication channel.
 * @author Laurent Cohen
 */
public class LocalNodeWrapperHandler extends AbstractChannelWrapper<LocalNodeContext> implements IOHandler
{
	/**
	 * Logger for this class.
	 */
	private static Log log = LogFactory.getLog(LocalNodeWrapperHandler.class);
	/**
	 * Determines whether the debug level is enabled in the log configuration, without the cost of a method call.
	 */
	protected static boolean debugEnabled = log.isDebugEnabled();
	/**
	 * This channel's key ops.
	 */
	private int keyOps = 0;
	/**
	 * This channel's ready ops.
	 */
	private int readyOps = 0;
	/**
	 * Determines whether this handler has data to read.
	 */
	private AtomicBoolean readable = new AtomicBoolean(false);
	/**
	 * Position of the next block of data to be read by the node.
	 */
	private int readPosition = 0;
	/**
	 * Position of the next block of data to be written by the node.
	 */
	private int writePosition = 0;
	/**
	 * The current object being read or written.
	 */
	private DataLocation currentLocation = null;
	/**
	 * Count of bytes read or written in the current data location.
	 */
	private int currentCount = 0;

	/**
	 * Initialize this channel wrapper with the specified node context.
	 * @param context the node context used as channel.
	 */
	public LocalNodeWrapperHandler(LocalNodeContext context)
	{
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public NioContext getContext()
	{
		return channel;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int getKeyOps()
	{
		return keyOps;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setKeyOps(int keyOps)
	{
		// to avoid exception when testing isReadable() when channel is write-ready.
		if ((keyOps & OP_WRITE) != 0) readyOps = keyOps & ~OP_READ;
		this.keyOps = keyOps;
		if (getSelector() != null) getSelector().wakeup();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized int getReadyOps()
	{
		return this.readyOps;
	}

	/**
	 * Set the operations for which this channel is ready.
	 * @param readyOps the bitwise operations as an int value.
	 */
	protected synchronized void setReadyOps(int readyOps)
	{
		this.readyOps = readyOps;
		if (getSelector() != null) getSelector().wakeup();
	}

	/**
	 * {@inheritDoc}
	 */
	public void flush() throws Exception
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public JPPFBuffer read() throws Exception
	{
		//while (!readable.get()) goToSleep();
		setReadyOps(OP_WRITE);
		while (readPosition >= channel.getNodeMessage().getLocations().size()) goToSleep();
		readPosition++;
		DataLocation dl = getChannel().getNodeMessage().getLocations().get(readPosition);
		InputStream is = dl.getInputStream();
		return new JPPFBuffer(FileUtils.getInputStreamAsByte(is), dl.getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(byte[] data, int offset, int len) throws Exception
	{
		InputSource is = new ByteBufferInputSource(data, offset, len);
		currentLocation = IOHelper.createDataLocationMemorySensitive(len);
		int n = currentLocation.transferFrom(is, true);
		getChannel().getNodeMessage().addLocation(currentLocation);
		((LocalNodeMessage) getChannel().getNodeMessage()).wakeUp();
		setReadyOps(OP_READ);
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeInt(int value) throws Exception
	{
		currentCount = 0;
	}

	/**
	 * Cause the current thread to wait until notified.
	 */
	public synchronized void goToSleep()
	{
		try
		{
			wait();
		}
		catch(InterruptedException ignored)
		{
		}
	}

	/**
	 * Notify the threads currently waiting on this object that they can resume.
	 */
	public synchronized void wakeUp()
	{
		notifyAll();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		return this.getClass().getSimpleName() + ":" + id;
	}
}
