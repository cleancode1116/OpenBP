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
package org.openbp.server;

import org.openbp.common.logger.LogUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Factory class that creates a process server.
 *
 * @author Heiko Erhardt
 */
public class ProcessServerFactory
{
	/** OpenBP server Spring framework configuration file ("OpenBP-Server.spring.xml") */
	public static final String SERVER_SPRING_RESOURCE = "OpenBP-Server.spring.xml";

	/**
	 * Default constructor.
	 */
	public ProcessServerFactory()
	{
	}

	/**
	 * Creates a process server using the standard configuration.
	 * Uses the standard Spring configuration file {@link #SERVER_SPRING_RESOURCE}.
	 *
	 * @return The new process server
	 */
	public ProcessServer createProcessServer()
	{
		return createProcessServer((String) null);
	}

	/**
	 * Creates a process server using the provided configuration.
	 *
	 * @param springConfigFileName Name of the OpenBP spring configuration file or null for "OpenBP.spring.xml"
	 * @return The new process server
	 */
	public ProcessServer createProcessServer(String springConfigFileName)
	{
		if (springConfigFileName == null)
		{
			springConfigFileName = SERVER_SPRING_RESOURCE;
		}

		LogUtil.info(getClass(), "Loading Spring framework resource $0.", springConfigFileName);

		AbstractApplicationContext springContext = new ClassPathXmlApplicationContext(springConfigFileName);
		return createProcessServer(springContext);
	}

	/**
	 * Creates a process server using the provided configuration.
	 *
	 * @param springContext Spring bean factory (e. g. a Spring ApplicationContext)
	 * @return The new process server
	 */
	public ProcessServer createProcessServer(final BeanFactory springContext)
	{
		ProcessServer processServer = (ProcessServer) springContext.getBean("processServerBean");
		processServer.initialize();
		return processServer;
	}
}
