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


/**
 * Workflow test that suspends and resumes a workflow that contains a complex parameter that is managed by Hibernate.
 *
 * @author Heiko Erhardt
 */
public class MultiThreadWorkflowTaskTest extends WorkflowTaskTest
{
	public MultiThreadWorkflowTaskTest()
	{
	}

	public void performTest()
		throws Exception
	{
		final Object tokenId = doStart();

		executeWait("t1done", new Runnable ()
		{
			public void run()
			{
				doWorkflowTask(tokenId);
			}
		});

		executeWait("t2done", new Runnable ()
		{
			public void run()
			{
				doFinish(tokenId);
			}
		});
		
		System.out.println("Ready.");
	}
}
