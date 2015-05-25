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
package org.openbp.common.resource;

import org.openbp.common.util.ToStringHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Loads resources from the classpath.
 *
 * In order to load resources from within an EAR file under SUN application server,
 * an additional classpath statement needs to be present in the EAR's MANIFEST file.
 *	<build>
 *		<plugins>
 *			<plugin>
 *				<groupId>org.apache.maven.plugins</groupId>
 *				<artifactId>maven-ear-plugin</artifactId>
 *				<configuration>
 *				<archive>
 *					<manifestEntries>
 *						<Class-Path>config</Class-Path>
 *					</manifestEntries>
 *					<manifest>
 *						<addClasspath>true</addClasspath>
 *					</manifest>
 *				</archive>
 *			...
 *
 * @author Heiko Erhardt
 */
public class ClasspathResourceProvider extends ResourceProvider
{
	public ClasspathResourceProvider(int priority, ResourceLoader resourceLoader)
	{
		super(priority, resourceLoader);
	}

	public String toString()
	{
		return ToStringHelper.toString(this, "priority");
	}

	public Resource getResource(String resourceLocation) throws ResourceMgrException
	{
		String resourceName = resourceLocation;
		if (!hasPrefix(resourceName))
		{
			resourceName = applyPrefix(resourceName);
		}

		return getResourceLoader().getResource(resourceName);
	}

	public String getPrefix()
	{
		return "classpath:";
	}
}
