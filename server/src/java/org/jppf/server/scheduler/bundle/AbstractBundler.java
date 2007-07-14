/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jppf.server.scheduler.bundle;

/**
 * Abstract implementation of the bundler interface.
 * @author Laurent Cohen
 */
public abstract class AbstractBundler implements Bundler
{
	/**
	 * Count of the bundlers used to generate a readable unique id.
	 */
	protected static int bundlerCount = 0;
	/**
	 * The bundler number for this bundler.
	 */
	protected int bundlerNumber = incBundlerCount();

	/**
	 * The creation timestamp for this bundler.
	 */
	protected long timestamp = System.currentTimeMillis();
	/**
	 * The override indicator.
	 */
	protected boolean override = false;

	/**
	 * Increment the bundlers count by one.
	 * @return the new count as an int value.
	 */
	private static synchronized int incBundlerCount()
	{
		return ++bundlerCount;
	}

	/**
	 * This method does nothing and should be overriden in subclasses.
	 * @param bundleSize not used.
	 * @param totalTime not used.
	 * @see org.jppf.server.scheduler.bundle.Bundler#feedback(int, double)
	 */
	public void feedback(int bundleSize, double totalTime)
	{
	}

	/**
	 * Get the timestamp at which this bundler was created.
	 * This is used to enable node channels to know when the bundler settings have changed.
	 * @return the timestamp as a long value.
	 * @see org.jppf.server.scheduler.bundle.Bundler#getTimestamp()
	 */
	public long getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Get the  override indicator.
	 * @return true if the settings were overriden by the node, false otherwise.
	 * @see org.jppf.server.scheduler.bundle.Bundler#isOverride()
	 */
	public boolean isOverride()
	{
		return override;
	}

	/**
	 * Set the  override indicator.
	 * @param override true if the settings were overriden by the node, false otherwise.
	 */
	public void setOverride(boolean override)
	{
		this.override = override;
	}
}
