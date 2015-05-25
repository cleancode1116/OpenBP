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
package org.openbp.server.test.script;

import java.util.HashMap;
import java.util.Map;

import org.openbp.server.context.TokenContext;
import org.openbp.server.test.base.TestCaseBase;

/**
 * Test case for script engine integration.
 *
 * @author Heiko Erhardt
 */
public class ScriptEngineTest extends TestCaseBase
{
	public static final String STARTREF = "/TestCase/BeanShellScriptTest.Start";
	public static final Integer STARTVALUE = Integer.valueOf(100);
	public static final Integer EXTVALUE = Integer.valueOf(200);
	public static final Integer ENDVALUE = Integer.valueOf(300);

	public ScriptEngineTest()
	{
	}

	public void performTest()
		throws Exception
	{
		// Prepare the input parameters of the process
		Map<String, Object> inputParams = new HashMap<String, Object>(); 
		inputParams.put("InputValue", STARTVALUE);

		// Create the process context and start the process
		TokenContext token = createToken();
		token.createProcessVariable("ExternalValue", true);
		token.setProcessVariableValue("ExternalValue", EXTVALUE);
		getProcessFacade().startToken(token, STARTREF, inputParams);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		getProcessFacade().executePendingContextsInThisThread();

		// The first workflow node made us stop here
		assertCurrentNode(token, "End");

		Map<String, Object> outputParams = new HashMap<String, Object>(); 
		getProcessFacade().retrieveOutputParameters(token, outputParams);

		Integer endValue = (Integer) outputParams.get("OutputValue");
		assertEquals(ENDVALUE, endValue);
	}
}
