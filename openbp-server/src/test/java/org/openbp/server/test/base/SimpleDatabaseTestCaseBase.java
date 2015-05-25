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
package org.openbp.server.test.base;

import org.openbp.server.context.TokenContext;
import org.openbp.server.test.PersistedComplexParam;
import org.openbp.server.test.PojoPersistedComplexParam;

/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public abstract class SimpleDatabaseTestCaseBase extends DatabaseTestCaseBase
{
	/** Start reference of the process to start */
	private String startRef;

	public SimpleDatabaseTestCaseBase()
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
		ret.setTitle("Complex Param 1");
		return ret;
	}

	public void performTest()
		throws Exception
	{
		if (getStartRef() == null)
			throw new RuntimeException("StartRef not set in test case class derived from SimpleDatabaseTestCaseBase");

		// Create the process context and start the process
		TokenContext token = createToken();
		getProcessFacade().startToken(token, getStartRef(), null);

		// Note: This should be done by some worker thread; for the test case, we do it right here.
		getProcessFacade().executePendingContextsInThisThread();
	}

	/**
	 * Gets the start reference of the process to start.
	 * @nowarn
	 */
	public String getStartRef()
	{
		return startRef;
	}

	/**
	 * Sets the start reference of the process to start.
	 * @nowarn
	 */
	public void setStartRef(String startRef)
	{
		this.startRef = startRef;
	}
}
