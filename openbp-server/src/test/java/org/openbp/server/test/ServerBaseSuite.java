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
 * Test suite for tests to be executed using the Cayenne o/r mapper.
 */
public class ServerBaseSuite extends TestCaseBase
{
    public ServerBaseSuite(String testName)
    	throws Exception
	{
        super();
		basicInitialize();
    }

    public static Test suite()
	{
        TestSuite suite = new TestSuite("ServerBaseSuite");
        suite.addTest(new TestSuite(org.openbp.server.test.StartStopTest.class));
        suite.addTest(new TestSuite(org.openbp.server.test.engine.ExecutorTest.class));
        suite.addTest(new TestSuite(org.openbp.server.test.engine.PriorityTest.class));
        suite.addTest(new TestSuite(org.openbp.server.test.engine.ShutdownTest.class));
        suite.addTest(new TestSuite(org.openbp.server.test.script.ScriptEngineTest.class));
        suite.addTest(new TestSuite(org.openbp.server.test.model.modelinspection.ModelInspectorUtilTest.class));
        return suite;
    }
}
