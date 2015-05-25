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
package org.openbp.server.test.engine;

import java.util.Iterator;

import org.openbp.server.context.TokenContext;
import org.openbp.server.test.base.TestCaseBase;
import org.openbp.server.test.base.TestCaseSyncMgr;

/**
 * Test case that tests the capabilities of the ThreadPoolTaskExecutor to run a particular number of threads in parallel w/o queuing them.
 *
 * @author Heiko Erhardt
 */
public class PriorityTest extends TestCaseBase
{
	public PriorityTest()
	{
	}

	public void performTest()
		throws Exception
	{
		// Create 3 processes with different priorities
		startSimpleSignalSetProcess("Sync1", "Sig 1", 2);
		startSimpleSignalSetProcess("Sync2", "Sig 2", 1);
		startSimpleSignalSetProcess("Sync3", "Sig 3", 3);

		Iterator it = getProcessServer().getTokenContextService().getExecutableContexts(10);

		TokenContext tc;

		tc = (TokenContext) it.next();
		getProcessFacade().executeContextInThisThread(tc);
		assertEquals(TestCaseSyncMgr.getInstance().getSignal(tc, "Sync2"), "Sig 2");

		tc = (TokenContext) it.next();
		getProcessFacade().executeContextInThisThread(tc);
		assertEquals(TestCaseSyncMgr.getInstance().getSignal(tc, "Sync1"), "Sig 1");

		tc = (TokenContext) it.next();
		getProcessFacade().executeContextInThisThread(tc);
		assertEquals(TestCaseSyncMgr.getInstance().getSignal(tc, "Sync3"), "Sig 3");
	}
}
