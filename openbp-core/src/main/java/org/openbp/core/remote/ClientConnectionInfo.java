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
package org.openbp.core.remote;

import org.openbp.common.MsgFormat;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.OpenBPException;

/**
 * Identifies a OpenBP server.
 *
 * @property rmiServerPort Port on which the server accepts RMI requests
 * @property rmiServerHost RMI registry host
 *
 * @author Heiko Erhardt
 */
public class ClientConnectionInfo
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** RMI registry host (property in OpenBP-Server\.properties) */
	private static final String REGISTRY_HOST_PROP = "openbp.RMIRegistry.host";

	/** RMI registry port (property in OpenBP-Server\.properties) */
	private static final String REGISTRY_PORT_PROP = "openbp.RMIRegistry.port";

	/** RMI registry enabled flag (property in OpenBP-Server\.properties) */
	private static final String REGISTRY_ENABLED_PROP = "openbp.RMIRegistry.enabled";

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Host for the remote registry */
	private String rmiServerHost;

	/** Port on which the registry accepts requests */
	private int rmiServerPort;

	/** Enabled flag */
	private boolean enabled;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ClientConnectionInfo()
	{
	}

	//////////////////////////////////////////////////
	// @@ Public Methods
	//////////////////////////////////////////////////

	/**
	 * Initialize this object by property-file
	 */
	public void loadFromProperties()
	{
		rmiServerHost = SettingUtil.getStringSetting(REGISTRY_HOST_PROP);
		rmiServerPort = SettingUtil.getIntSetting(REGISTRY_PORT_PROP, 0);
		enabled = SettingUtil.getBooleanSetting(REGISTRY_ENABLED_PROP, false);
	}

	/**
	 * Validates the connection info
	 *
	 * @throws OpenBPException On validation error
	 */
	public void validate()
	{
		if (rmiServerHost == null)
		{
			String message = MsgFormat.format("Missing property $0.", REGISTRY_HOST_PROP);
			throw new OpenBPException("Connection.Properties", message);
		}

		if (rmiServerPort <= 0)
		{
			String message = MsgFormat.format("Missing or invalid property $0.", REGISTRY_PORT_PROP);
			throw new OpenBPException("Connection.Properties", message);
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the rmiServerHost.
	 * @param rmiServerHost The rmiServerHost to set
	 */
	public void setRmiServerHost(String rmiServerHost)
	{
		this.rmiServerHost = rmiServerHost;
	}

	/**
	 * Gets the rmiServerHost.
	 * @return Returns a String
	 */
	public String getRmiServerHost()
	{
		return rmiServerHost;
	}

	/**
	 * Sets the rmiServerPort.
	 * @param rmiServerPort The rmiServerPort to set
	 */
	public void setRmiServerPort(int rmiServerPort)
	{
		this.rmiServerPort = rmiServerPort;
	}

	/**
	 * Gets the rmiServerPort.
	 * @return Returns a int
	 */
	public int getRmiServerPort()
	{
		return rmiServerPort;
	}

	/**
	 * Gets the enabled flag.
	 * @nowarn
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Sets the enabled flag.
	 * @nowarn
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
