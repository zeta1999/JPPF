/*
 * Java Parallel Processing Framework.
 * Copyright (C) 2005-2008 JPPF Team.
 * http://www.jppf.org
 *
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

package org.jppf.server.scheduler.bundle.proportional;

import java.util.concurrent.atomic.AtomicReference;

import org.jppf.server.scheduler.bundle.*;
import org.jppf.server.scheduler.bundle.autotuned.AbstractAutoTuneProfile;
import org.jppf.utils.*;

/**
 * Paremeters profile for a proportional bundler. 
 * @author Laurent Cohen
 */
public class ProportionalTuneProfile extends AbstractAutoTuneProfile
{
	/**
	 * A default profile with default parameter values.
	 */
	private static AtomicReference<ProportionalTuneProfile> defaultProfile = new AtomicReference<ProportionalTuneProfile>(new ProportionalTuneProfile());
	/**
	 * The maximum szie of the performance samples cache.
	 */
	private int performanceCacheSize = 2000;
	/**
	 * The propertionality factor.
	 */
	private int proportionalityFactor = 2;

	/**
	 * Initialize this profile with default parameters.
	 */
	public ProportionalTuneProfile()
	{
	}

	/**
	 * Initialize this profile with values read from the configuration file.
	 * @param profileName name of the profile in the configuration file.
	 */
	public ProportionalTuneProfile(String profileName)
	{
		String prefix = "strategy." + profileName + ".";
		TypedProperties props = JPPFConfiguration.getProperties();
		performanceCacheSize = props.getInt(prefix + "performanceCacheSize", 2000);
		proportionalityFactor = props.getInt(prefix + "propertionalityFactor", 2);
	}

	/**
	 * Make a copy of this profile.
	 * @return a new <code>AutoTuneProfile</code> instance.
	 * @see org.jppf.server.scheduler.bundle.AutoTuneProfile#copy()
	 */
	public AutoTuneProfile copy()
	{
		ProportionalTuneProfile other = new ProportionalTuneProfile();
		other.setPerformanceCacheSize(performanceCacheSize);
		other.setProportionalityFactor(proportionalityFactor);
		return other;
	}

	/**
	 * Get the maximum size of the performance samples cache.
	 * @return the cache size as an int.
	 */
	public int getPerformanceCacheSize()
	{
		return performanceCacheSize;
	}

	/**
	 * Set the maximum size of the performance samples cache.
	 * @param performanceCacheSize the cache size as an int.
	 */
	public void setPerformanceCacheSize(int performanceCacheSize)
	{
		this.performanceCacheSize = performanceCacheSize;
	}

	/**
	 * Get the proportionality factor.
	 * @return the factor as an int.
	 */
	public int getProportionalityFactor()
	{
		return proportionalityFactor;
	}

	/**
	 * Set the proportionality factor.
	 * @param proportionalityFactor the factor as an int.
	 */
	public void setProportionalityFactor(int proportionalityFactor)
	{
		this.proportionalityFactor = proportionalityFactor;
	}

	/**
	 * Get the default profile with default parameter values.
	 * @return a <code>ProportionalTuneProfile</code> singleton instance.
	 */
	public static ProportionalTuneProfile getDefaultProfile()
	{
		return defaultProfile.get();
	}
}
