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

package org.jppf.server.node.local;

import java.util.List;
import java.util.concurrent.Callable;

import org.jppf.classloader.*;
import org.jppf.comm.socket.IOHandler;
import org.jppf.server.nio.nodeserver.LocalNodeWrapperHandler;
import org.jppf.server.node.*;

/**
 * Local (in-VM) node implementation.
 * @author Laurent Cohen
 */
public class JPPFLocalNode extends JPPFNode
{
	/**
	 * The I/O handler for this node.
	 */
	private LocalNodeWrapperHandler handler = null;
	/**
	 * The I/O handler for the class loader.
	 */
	private IOHandler classLoaderHandler = null;

	/**
	 * Initialize this local node with the specfied I/O handler.
	 * @param handler the I/O handler for this node.
	 * @param classLoaderHandler the I/O handler for the class loader.
	 */
	public JPPFLocalNode(LocalNodeWrapperHandler handler, IOHandler classLoaderHandler)
	{
		this.handler = handler;
		this.classLoaderHandler = classLoaderHandler;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initDataChannel() throws Exception
	{
		nodeIO = new LocalNodeIO(this);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void closeDataChannel() throws Exception
	{
	}

	/**
	 * {@inheritDoc}
	 */
	protected AbstractJPPFClassLoader createClassLoader()
	{
		if (classLoader == null) classLoader = new JPPFLocalClassLoader(classLoaderHandler, this.getClass().getClassLoader());
		return classLoader;
	}

	/**
	 * @param uuidPath the uuid path containing the key to the container.
	 * Instatiate the callback used to create the class loader in each {@link JPPFContainer}.
	 * @return a {@link Callable} instance.
	 */
	protected Callable<AbstractJPPFClassLoader> newClassLoaderCreator(final List<String> uuidPath)
	{
		return new Callable<AbstractJPPFClassLoader>()
		{
			public AbstractJPPFClassLoader call()
			{
				return new JPPFLocalClassLoader(getClass().getClassLoader(), uuidPath);
			}
		};
	}

	/**
	 * Get the I/O handler for this node.
	 * @return a {@link LocalNodeWrapperHandler} instance.
	 */
	public LocalNodeWrapperHandler getHandler()
	{
		return handler;
	}
}
