/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005-2007 JPPF Team.
 * http://www.jppf.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package test.client;

import java.util.*;

import org.jppf.process.*;
import org.jppf.process.NodePropertiesBuilder.NodePermission;


/**
 * 
 * @author Laurent Cohen
 */
public class TestMultipleDrivers
{
	/**
	 * Logging level to use in the create processes.
	 */
	private static final String LOGGING_LEVEL = "INFO";

	/**
	 * Create a new node process using default values.
	 * @param nodeConfig JPPF configuration properties for the node.
	 * @param permissions a list of descriptions of the permissions granted to the node.
	 * @param log4jConfig log4j configuration properties for the node.
	 * @return the started node process.
	 * @throws Exception if an error occurs while building the process.
	 */
	public ProcessWrapper startNodeProcess(Properties nodeConfig, List<NodePermission> permissions,
		Properties log4jConfig) throws Exception
	{
		return ProcessCommand.buildNodeProcess("org.jppf.node.NodeLauncher", nodeConfig, permissions, log4jConfig, 64);
	}

	/**
	 * Create a new driver process using default values.
	 * @param driverConfig JPPF configuration properties for the driver.
	 * @param log4jConfig log4j configuration properties for the driver.
	 * @return the started node process.
	 * @throws Exception if an error occurs while building the process.
	 */
	public ProcessWrapper startDriverProcess(Properties driverConfig, Properties log4jConfig) throws Exception
	{
		return ProcessCommand.buildProcess("org.jppf.server.DriverLauncher", driverConfig, log4jConfig, 32);
	}
	
	/**
	 * Create a new matrix sample process using default values and a client connect to 2 drivers.
	 * @param priority1 priority assigned to the connection with the first driver.
	 * @param priority2 priority assigned to the connection with the first driver.
	 * @param matrixSize size of the matrix to use.
	 * @param nbIter number of times the computation will be performed.
	 * @return the started node process.
	 * @throws Exception if an error occurs while building the process.
	 */
	public ProcessWrapper startMatrixSampleProcess(int priority1, int priority2, int matrixSize, int nbIter) throws Exception
	{
		Properties c1 = ClientPropertiesBuilder.buildDriverConnection("driver1", "localhost", 11111, 11112, priority1);
		Properties c2 = null;
		if (priority2 >= 0)
		{
			c2 = ClientPropertiesBuilder.buildDriverConnection("driver2", "localhost", 11121, 11122, priority2);
		}
		Properties clientConfig = null;
		if (priority2 >= 0)
			clientConfig = ClientPropertiesBuilder.buildClientConfig("driver1 driver2", new Properties[] {c1, c2});
		else
			clientConfig = ClientPropertiesBuilder.buildClientConfig("driver1", new Properties[] {c1});
		clientConfig.setProperty("matrix.size", ""+matrixSize);
		clientConfig.setProperty("matrix.iterations", ""+nbIter);
		Properties log4jConfig = ProcessConfig.buildLog4jConfig("matrix.log", true, LOGGING_LEVEL);
		return ProcessCommand.buildProcess("sample.matrix.MatrixRunner", clientConfig, log4jConfig, 64);
	}

	/**
	 * Perform first test.
	 * @throws Exception if any error occurs.
	 */
	public void test1() throws Exception
	{
		Properties log4jConfig = ProcessConfig.buildLog4jConfig("driver1.log", false, LOGGING_LEVEL);
		ProcessWrapper driver1 = startDriverProcess(DriverPropertiesBuilder.DRIVER_1, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("driver2.log", false, LOGGING_LEVEL);
		ProcessWrapper driver2 = startDriverProcess(DriverPropertiesBuilder.DRIVER_2, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("node1.log", false, LOGGING_LEVEL);
		List<NodePermission> permissions = NodePropertiesBuilder.buildBasePermissions();
		ProcessWrapper node1 = startNodeProcess(NodePropertiesBuilder.NODE_1, permissions, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("node2.log", false, LOGGING_LEVEL);
		ProcessWrapper node2 = startNodeProcess(NodePropertiesBuilder.NODE_3, permissions, log4jConfig);
		ProcessWrapper client = startMatrixSampleProcess(10, 10, 300, 10);
	}

	/**
	 * Perform first test.
	 * @throws Exception if any error occurs.
	 */
	public void test2() throws Exception
	{
		Properties log4jConfig = ProcessConfig.buildLog4jConfig("driver1.log", false, LOGGING_LEVEL);
		ProcessWrapper driver1 = startDriverProcess(DriverPropertiesBuilder.DRIVER_1, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("driver2.log", false, LOGGING_LEVEL);
		ProcessWrapper driver2 = startDriverProcess(DriverPropertiesBuilder.DRIVER_2, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("node1.log", false, LOGGING_LEVEL);
		List<NodePermission> permissions = NodePropertiesBuilder.buildBasePermissions();
		ProcessWrapper node1 = startNodeProcess(NodePropertiesBuilder.NODE_1, permissions, log4jConfig);
		log4jConfig = ProcessConfig.buildLog4jConfig("node2.log", false, LOGGING_LEVEL);
		ProcessWrapper node2 = startNodeProcess(NodePropertiesBuilder.NODE_3, permissions, log4jConfig);
		ProcessWrapper client = startMatrixSampleProcess(10, 10, 300, 10);
	}
}
