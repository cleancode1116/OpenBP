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

import java.util.HashMap;
import java.util.Iterator;

import org.openbp.common.CollectionUtil;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.context.WorkflowTaskCriteria;
import org.openbp.server.test.base.DatabaseTestCaseBase;

/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class WorkflowTaskTest extends DatabaseTestCaseBase
{
	public static final String STARTREF = "/TestCase/WorkflowTaskTest.Start";

	private int startValue;

	public WorkflowTaskTest()
	{
	}

	/**
	 * Template method for creating a test business object.
	 * Override for O/R-mapper specific object classes.
	 * @return The new object
	 */
	public PersistedComplexParam createPersistedComplexParam()
	{
		PersistedComplexParam ret = new PojoPersistedComplexParam();
		ret.setTitle("Workflow task test 1");
		return ret;
	}

	public void performTest()
		throws Exception
	{
		int nContexts = checkNumberOfContexts("Init", - 1);
		Object tokenId = doStart();
		checkNumberOfContexts("Start", nContexts + 1);

		doWorkflowTask(tokenId);

		doFinish(tokenId);
		checkNumberOfContexts("End", nContexts);
	}

	protected Object doStart()
	{
		// Prepare the input parameters of the process
		HashMap inputParams = new HashMap();

		PersistedComplexParam cp = createPersistedComplexParam();
		inputParams.put("ComplexParam", cp);

		startValue = 99;
		inputParams.put("NumValue", Integer.valueOf(startValue));

		// Create the process context and start the process
		TokenContext token = createToken();
		token.createProcessVariable("externalValue", true);
		token.setProcessVariableValue("externalValue", Integer.valueOf(199));
		getProcessFacade().startToken(token, STARTREF, inputParams);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		getProcessFacade().executePendingContextsInThisThread();

		Object tokenId = token.getId();

		// The first workflow node made us stop here
		assertCurrentNode(token, "Workflow");

		return tokenId;
	}

	protected void doWorkflowTask(final Object tokenId)
	{
		TokenContext token = getProcessFacade().getTokenById(tokenId);

		// The any workflow tasks that are associated with our process.
		WorkflowTaskCriteria criteria = new WorkflowTaskCriteria();
		criteria.setTokenContext(token);
		Iterator it = getProcessFacade().getworkflowTasks(criteria);
		assertTrue(it.hasNext());
		WorkflowTask task = (WorkflowTask) it.next();

		// Check the process variables of the token context
		Object p = token.getProcessVariableValue("globalObject");
		assertTrue(p instanceof PersistedComplexParam);
		PersistedComplexParam rcp = (PersistedComplexParam) p;
		int nRet = rcp.getResult();
		assertEquals(startValue + 1, nRet);

		// Notify the process that we want to resume the workflow with the default node
		getProcessFacade().resumeWorkflow(task, null, "TestUser");
	}

	protected void doFinish(final Object tokenId)
	{
		TokenContext token = getProcessFacade().getTokenById(tokenId);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		getProcessFacade().executePendingContextsInThisThread();

		assertCurrentNode(token, "End");

		// Finally, get the output parameters from the process context and check them
		HashMap outputParams = new HashMap();
		getProcessFacade().retrieveOutputParameters(token, outputParams);

		Object r = outputParams.get("r");
		assertTrue(r instanceof PersistedComplexParam);
		PersistedComplexParam rcp = (PersistedComplexParam) r;
		int nRet = rcp.getResult();
		assertEquals(startValue + 1, nRet);

		Object n = outputParams.get("n");
		assertTrue(n instanceof Integer);
		assertEquals(startValue, ((Integer) n).intValue());
	}

	boolean throwException = false;

	protected int checkNumberOfContexts(String stepName, int expected)
	{
		int n = 0;
		for (Iterator it = CollectionUtil.iterator(getProcessFacade().getTokens(null, 0)); it.hasNext();)
		{
			TokenContext token = (TokenContext) it.next();
			if (token.getCurrentSocket() == null)
			{
				errOut("Test context check " + stepName + ": Socket of context " + token + " is null.");
			}
			++n;
		}
		if (expected >= 0 && n != expected)
			errOut("Test context check " + stepName + ": Invalid number of tokens in database (expected: " + expected + ", got: " + n + ".");
		return n;
	}

	protected void errOut(String msg)
	{
		if (throwException)
			throw new RuntimeException(msg);
		else
			System.err.println(msg);
	}
}
