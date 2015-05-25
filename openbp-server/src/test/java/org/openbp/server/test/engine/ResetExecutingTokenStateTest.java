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

import org.openbp.server.ProcessFacade;
import org.openbp.server.context.LifecycleRequest;
import org.openbp.server.context.LifecycleState;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextService;
import org.openbp.server.engine.DefaultSystemNameProvider;
import org.openbp.server.test.base.TestCaseBase;
import org.openbp.server.test.base.TestCaseSyncMgr;

/**
 * Test case that tests the method {@link ProcessFacade#resetExecutingTokenState}.
 *
 * @author Heiko Erhardt
 */
public class ResetExecutingTokenStateTest extends TestCaseBase
{
	public ResetExecutingTokenStateTest()
	{
	}

	public void performTest()
		throws Exception
	{
		String localHost = new DefaultSystemNameProvider().getSystemName();
		TokenContextService tokenContextService = getProcessServer().getTokenContextService();

		// Create all kinds of contexts and simulate several states

		TokenContext tc1 = startSimpleSignalSetProcess("TestSignal1", "Done", 0);
		tc1.setLifecycleState(LifecycleState.SELECTED);
		tc1.setLifecycleRequest(LifecycleRequest.NONE);
		tc1.setNodeId(localHost);
		tokenContextService.saveContext(tc1);

		TokenContext tc2 = startSimpleSignalSetProcess("TestSignal2", "Done", 0);
		tc2.setLifecycleState(LifecycleState.SELECTED);
		tc2.setLifecycleRequest(LifecycleRequest.NONE);
		tc2.setNodeId("DifferentHost");
		tokenContextService.saveContext(tc2);

		TokenContext tc3 = startSimpleSignalSetProcess("TestSignal3", "Done", 0);
		tc3.setLifecycleState(LifecycleState.RUNNING);
		tc3.setLifecycleRequest(LifecycleRequest.NONE);
		tc3.setNodeId(localHost);
		tokenContextService.saveContext(tc3);

		TokenContext tc4 = startSimpleSignalSetProcess("TestSignal4", "Done", 0);
		tc4.setLifecycleState(LifecycleState.ERROR);
		tc4.setLifecycleRequest(LifecycleRequest.NONE);
		tc4.setNodeId(localHost);
		tokenContextService.saveContext(tc4);

		TokenContext tc5 = startSimpleSignalSetProcess("TestSignal5", "Done", 0);
		tokenContextService.saveContext(tc5);

		tokenContextService.commit();

		// Execute those that can be executed
		getProcessFacade().executePendingContextsInThisThread();

		// Check the state of the executions
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal1"));
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal2"));
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal3"));
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal4"));
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal5"));

		// *** Now reset all contexts that seem to be currently executed by this node ***
		// (i. e. simulate system startup)
		int ret = getProcessFacade().resetExecutingTokenState(null);
		assertEquals(2, ret);

		// Clear cache so we will not be fooled by cached context objects
		tokenContextService.clearCache();

		// Execute those that can be executed
		getProcessFacade().executePendingContextsInThisThread();

		// Check the state of the executions
		// 2 Contexts should still be there (tc2: Different host, tc4: Error state)
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal1"));
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal2"));
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal3"));
		assertEquals(null, TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal4"));
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal5"));

		// Cleanup the last ones
		tc2 = tokenContextService.getContextById(tc2.getId());
		tc2.setLifecycleState(LifecycleState.SUSPENDED);
		tc2.setLifecycleRequest(LifecycleRequest.RESUME);
		tc2.setNodeId(localHost);
		tokenContextService.saveContext(tc2);

		tc4 = tokenContextService.getContextById(tc4.getId());
		tc4.setLifecycleState(LifecycleState.SUSPENDED);
		tc4.setLifecycleRequest(LifecycleRequest.RESUME);
		tokenContextService.saveContext(tc4);

		tokenContextService.commit();

		getProcessFacade().executePendingContextsInThisThread();
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal2"));
		assertEquals("Done", TestCaseSyncMgr.getInstance().getSignal(null, "TestSignal4"));
	}
}
