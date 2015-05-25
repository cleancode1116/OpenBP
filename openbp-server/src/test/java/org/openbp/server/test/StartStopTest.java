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
package org.openbp.server.test;

import junit.framework.TestCase;

import org.openbp.common.application.Application;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;

/**
 * Test case that ensures restartability of the OpenBP server.
 * Starts and stops the server module twice.
 *
 * @author Heiko Erhardt
 */
public class StartStopTest extends TestCase
{
	public StartStopTest()
	{
	}

	public void testMain()
	throws Exception
	{
		performTest();
	}

	public void performTest()
		throws Exception
	{
		Application.setArguments(null);

		ProcessServer processServer = new ProcessServerFactory().createProcessServer();
		processServer.shutdown(true);

		processServer.initialize();
		processServer.shutdown(true);
	}
}
