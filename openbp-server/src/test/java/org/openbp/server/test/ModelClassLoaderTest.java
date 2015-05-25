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

import org.openbp.server.context.TokenContext;
import org.openbp.server.test.base.TestCaseBase;

/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class ModelClassLoaderTest extends TestCaseBase
{
	public ModelClassLoaderTest()
	{
	}

	public void performTest()
		throws Exception
	{
		/*
		doStart("/TestCaseM1/ModelClassLoaderTest.StartDirect", "Out", null);
		doStart("/TestCaseM2/ModelClassLoaderTest.StartDirect", "Out", null);
		doStart("/TestCaseM1/ModelClassLoaderTest.StartSubProcess", "Out2", null);
		doStart("/TestCaseM2/ModelClassLoaderTest.StartSubProcess", "Out2", null);
		 */

		doStart("/TestCaseM1/ModelClassLoaderTest.StartDirect", "Out", new String []
		{
			"Implementation", "TestModel1.Activity1",
			"Param", "Param M1",
			"ExecutingModel", "/TestCaseM1",
		});

		doStart("/TestCaseM2/ModelClassLoaderTest.StartDirect", "Out", new String []
		{
			"Implementation", "TestModel2.Activity1",
			"Param", "Param M2",
			"ExecutingModel", "/TestCaseM2",
		});

		doStart("/TestCaseM1/ModelClassLoaderTest.StartSubProcess", "Out2", new String []
		{
			"Implementation", "TestModelSub.Activity1",
			"Param", "Param M1",
			"ExecutingModel", "/TestCaseM1",
		});

		doStart("/TestCaseM2/ModelClassLoaderTest.StartSubProcess", "Out2", new String []
		{
			"Implementation", "TestModelSub.Activity1",
			"Param", "Param M2",
			"ExecutingModel", "/TestCaseM2",
		});
	}

	/*
		result.put("Implementation", "TestModel1.Activity1");
		result.put("Param", param);
		result.put("ExecutingModel", hc.getTokenContext().getExecutingModel().getQualifier().toString());
	 */

	protected void doStart(String startRef, String finalNodeName, String [] expectedResults)
	{
		System.out.println();
		System.out.println("Running " + startRef);

		// Create the process context and start the process
		TokenContext token = createToken();
		getProcessFacade().startToken(token, startRef, null);
		getProcessFacade().executePendingContextsInThisThread();

		// The first workflow node made us stop here
		assertCurrentNode(token, finalNodeName);

		// Finally, get the output parameters from the process context and check them
		HashMap outputParams = new HashMap();
		getProcessFacade().retrieveOutputParameters(token, outputParams);

		HashMap result = (HashMap) outputParams.get("Result");
		assertNotNull(result);
		if (expectedResults != null)
		{
			for (int i = 0; i < expectedResults.length; ++i)
			{
				Object key = expectedResults[i];
				Object expectedValue = expectedResults[++i];
				Object value = result.get(key);
				assertEquals(expectedValue, value);
			}
		}
	}
}
