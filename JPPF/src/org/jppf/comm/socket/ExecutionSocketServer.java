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

import java.net.Socket;
import org.jppf.task.*;

/**
 * Instances of this class listen on a configured port for incoming execution requests,
 * and delegate them to an execution service.
 * This class acts as the entry point when invoking a socket-based remote execution service.
 * A socket server can be terminated by invoking its {@link org.jppf.comm.socket.ExecutionSocketServer#end() end()} method.
 * This method can be invoked safely from any thread.
 * @author Laurent Cohen
 */
public class ExecutionSocketServer extends AbstractSocketServer
{
	/**
	 * Initialize this socket server with a specified execution service and port number.
	 * @param execService the execution service to which execution requests are delegated.
	 * @param port the port this socket server is listening to.
	 * @throws ExecutionServiceException if the underlying server socket can't be opened.
	 */
	public ExecutionSocketServer(ExecutionService execService, int port) throws ExecutionServiceException
	{
		super(execService, port);
	}
	
	/**
	 * Instanciate a socket handler that will handle the socket connection obtained through
	 * this socket server. 
	 * @param socket the socket conneciton to handle.
	 * @return an <code>JPPFServerConnection</code> instance.
	 * @throws ExecutionServiceException if an error occurs while instanciating the socket handler.
	 * @see org.jppf.classloader.ClassServer#createHandler(java.net.Socket)
	 */
	protected AbstractSocketHandler createHandler(Socket socket) throws ExecutionServiceException
	{
		ExecutionSocketHandler handler = new ExecutionSocketHandler(socket, execService);
		return handler;
	}
}
