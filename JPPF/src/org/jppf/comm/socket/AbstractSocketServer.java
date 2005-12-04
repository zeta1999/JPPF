/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005 Laurent Cohen.
 * lcohen@osp-chicago.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.jppf.comm.socket;

import java.io.IOException;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.jppf.classloader.ClassServer;
import org.jppf.task.*;

/**
 * Abstract super class for all socket servers.
 * @author Laurent Cohen
 */
public abstract class AbstractSocketServer extends Thread
{
	/**
	 * Log4j logger for this class.
	 */
	private static Logger log = Logger.getLogger(ClassServer.class);

	/**
	 * A list of the remote connections handled by this socket server.
	 */
	protected List<AbstractSocketHandler> connections = new Vector<AbstractSocketHandler>();
	/**
	 * Server socket listening for requests on the configured port.
	 */
	protected ServerSocket server = null;
	/**
	 * The execution service to which execution requests are delegated.
	 */
	protected ExecutionService execService = null;
	/**
	 * Flag indicating that this socket server is closed.
	 */
	protected boolean stop = false;
	/**
	 * The port this socket server is listening to.
	 */
	protected int port = -1;

	/**
	 * Initialize this socket server with a specified execution service and port number.
	 * @param execService the execution service to which execution requests are delegated.
	 * @param port the port this socket server is listening to.
	 * @throws ExecutionServiceException if the underlying server socket can't be opened.
	 */
	public AbstractSocketServer(ExecutionService execService, int port) throws ExecutionServiceException
	{
		this.port = port;
		this.execService = execService;
		init(port);
	}
	
	/**
	 * Start the underlying server socket by making it accept incoming connections.
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		try
		{
			while (!stop)
			{
				Socket socket = server.accept();
				serve(socket);
			}
			end();
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
			end();
		}
	}
	
	/**
	 * Start serving a new incoming connection.
	 * @param socket the socket connecting with this socket server.
	 * @throws ExecutionServiceException if the new connection can't be initialized.
	 */
	protected void serve(Socket socket) throws ExecutionServiceException
	{
		AbstractSocketHandler handler = createHandler(socket);
		//connections.add(sc);
		handler.start();
	}
	
	/**
	 * Instanciate a wrapper for the socket connection opened by this socket server.
	 * Subclasses must implement this method.
	 * @param socket the socket connection obtained through a call to
	 * {@link java.net.ServerSocket#accept() ServerSocket.accept()}.
	 * @return a <code>JPPFServerConnection</code> instance.
	 * @throws ExecutionServiceException if an exception is raised while creating the socket handler.
	 */
	protected abstract AbstractSocketHandler createHandler(Socket socket) throws ExecutionServiceException;

	/**
	 * Initialize the underlying server socket with a specified port.
	 * @param port the port the underlying server listens to.
	 * @throws ExecutionServiceException if the server socket can't be opened on the specified port.
	 */
	protected void init(int port) throws ExecutionServiceException
	{
		Exception e = null;
		try
		{
			server = new ServerSocket();
			InetSocketAddress addr = new InetSocketAddress(port);
			int size = 32*1024;
			server.setReceiveBufferSize(size);
			server.bind(addr);
		}
		catch(IllegalArgumentException iae)
		{
			e = iae;
		}
		catch(IOException ioe)
		{
			e = ioe;
		}
		if (e != null)
		{
			log.error(e.getMessage(), e);
			throw new ExecutionServiceException(e.getMessage(), e);
		}
	}

	/**
	 * Close the underlying server socket and stop this socket server.
	 */
	public synchronized void end()
	{
		if (!stop)
		{
			try
			{
				stop = true;
				server.close();
				for (AbstractSocketHandler sc: connections) sc.setClosed();
			}
			catch(IOException ioe)
			{
				log.error(ioe.getMessage(), ioe);
			}
		}
	}
}
