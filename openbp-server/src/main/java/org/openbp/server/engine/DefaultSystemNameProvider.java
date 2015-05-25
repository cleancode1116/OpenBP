/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.server.engine;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openbp.core.OpenBPException;

/**
 * Default system name provider that delivers the host name.
 *
 * @author Heiko Erhardt
 */
public class DefaultSystemNameProvider
	implements SystemNameProvider
{
	/**
	 * Gets the local system's name.
	 *
	 * @return InetAddress.getLocalHost().getHostName()
	 */
	public String getSystemName()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			throw new OpenBPException("InvalidHostNameConfiguration", "Cannot determine name of local host", e);
		}
	}
}
