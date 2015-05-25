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

import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.ObserverEvent;
import org.openbp.core.model.item.process.RollbackDataBehavior;
import org.openbp.core.model.item.process.RollbackPositionBehavior;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.Engine;
import org.openbp.server.engine.EngineExceptionHandlerEvent;
import org.openbp.server.engine.StandardRollbackProcessor;
import org.openbp.server.test.base.SimpleDatabaseTestCaseBase;

/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class RollbackLocalErrorHandlerTest extends SimpleDatabaseTestCaseBase
	implements EventObserver
{
	public RollbackLocalErrorHandlerTest()
	{
		setStartRef("/TestCase/RollbackLocalErrorHandlerTest.Start");
	}

	public void performTest()
		throws Exception
	{
		if (getStartRef() == null)
			throw new RuntimeException("StartRef not set in test case class derived from SimpleDatabaseTestCaseBase");

		// Create the process context and start the process
		TokenContext token = createToken();
		token.registerObserver(this, new String[] { EngineExceptionHandlerEvent.HANDLE_EXCEPTION });

		getProcessFacade().startToken(token, getStartRef(), null);

		Engine engine = this.getProcessServer().getEngine();
		engine.executeContext(token);
	}

	/**
	 * Template method that will be called whenever an event occurs the observer is interested in.
	 * @param e Event
	 */
	public void observeEvent(ObserverEvent e)
	{
		EngineExceptionHandlerEvent ee = (EngineExceptionHandlerEvent) e;

		// Perform rollback
		StandardRollbackProcessor rollbackProcessor = new StandardRollbackProcessor();
		rollbackProcessor.setRollbackDataBehavior(RollbackDataBehavior.RESTORE_VARIABLES);
		rollbackProcessor.setRollbackPositionBehavior(RollbackPositionBehavior.MAINTAIN_POSITION);
		rollbackProcessor.performRollback(ee);

		// Continue at the error socket of the current node
		ee.setHandlingOption(EngineExceptionHandlerEvent.HANDLING_OPTION_ERROR_SOCKET);

		ee.skipSubsequentObservers();
	}
	// @param handlingOption {@link #HANDLING_OPTION_ERROR_SOCKET} | {@link #HANDLING_OPTION_CONTINUE} | {@link #HANDLING_OPTION_RETHROW}
}
