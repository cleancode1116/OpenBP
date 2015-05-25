/*
 *   Copyright 2008 skynamics AG
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openbp.server.test.base.TestCaseBase;

/**
 * Test suite for tests to be executed using the Hibernate o/r mapper.
 */
public class TestSuiteServerHibernate extends TestCaseBase
{
    public TestSuiteServerHibernate()
    	throws Exception
	{
		super();
		basicInitialize();
    }            

    public static Test suite()
    	throws Exception
	{
        TestSuite suite = new TestSuite("ServerHibernateTestSuite");
        suite.addTest(ServerDatabaseSuite.suite());
		suite.addTest(new TestSuite(org.openbp.server.test.WorkflowTaskTest.class));
		// suite.addTest(new TestSuite(org.openbp.server.test.MultiThreadWorkflowTaskTest.class));
        return suite;
    }
}
