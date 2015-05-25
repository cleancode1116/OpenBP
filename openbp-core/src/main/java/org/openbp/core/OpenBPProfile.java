/*
 *   Copyright 2007 skynamics AG
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openbp.core;

import java.io.IOException;
import java.util.Properties;

import org.openbp.common.application.ProductProfile;

/**
 * Profile class for the OpenBP framework.
 * This is an implementation of a product profile.
 *
 * @author Andreas Putz
 */
public class OpenBPProfile
	implements ProductProfile
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	private String version;

	private String build;
	
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Construction.
	 */
	public OpenBPProfile()
	{
		initialize();
	}

	//////////////////////////////////////////////////
	// @@ Profile implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the short name of a company for internal use.
	 * @nowarn
	 */
	public final String getShortCompanyName()
	{
		return "OpenBP";
	}

	/**
	 * Gets the full company name for display in the user interface.
	 * @nowarn
	 */
	public final String getFullCompanyName()
	{
		return "OpenBP AG";
	}

	/**
	 * Gets the short name for internal use.
	 * @return "OpenBP"
	 */
	public String getShortProductName()
	{
		return "OpenBP";
	}

	/**
	 * @copy ProductProfile.getFullProductName()
	 */
	public String getFullProductName()
	{
		return getShortProductName() + " (R)";
	}

	/**
	 * @copy ProductProfile.getVersion()
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @copy ProductProfile.getBuildNumber()
	 */
	public String getBuildNumber()
	{
		return build;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Determines the build number and the version number from the tag name.
	 * The tag name should be composed by the following schema:
	 *    openbp-[MajorVersion].[MinorVersion]-[BuildNumber]
	 */
	private void initialize()
	{
		Properties versionProperties = new Properties();
		try {
			versionProperties.load(this.getClass().getResourceAsStream("version.properties"));
		} catch (IOException e) {
			// ignore exceptions, use default values
		}
		
		version = versionProperties.getProperty("version", "<unknown>");
		build = versionProperties.getProperty("build", "???");
	}
}
