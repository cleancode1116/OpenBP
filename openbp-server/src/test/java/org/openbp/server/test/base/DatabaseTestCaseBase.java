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

import org.openbp.server.persistence.PersistenceContext;

/**
 * Base class for database-related test cases.
 * Clears the openbptokencontext and openbpworkflowtask tables.
 *
 * @author Heiko Erhardt
 */
public abstract class DatabaseTestCaseBase extends TestCaseBase
{
	public DatabaseTestCaseBase()
	{
	}

	public void initializeTest()
		throws Exception
	{
		super.initializeTest();

		quietTableDelete("OPENBPWORKFLOWTASK");
		quietTableDelete("OPENBPTOKENCONTEXT");

		/*
		quietTableDelete("QRTZ_JOB_LISTENERS");
		quietTableDelete("QRTZ_TRIGGER_LISTENERS");
		quietTableDelete("QRTZ_FIRED_TRIGGERS");
		quietTableDelete("QRTZ_PAUSED_TRIGGER_GRPS");
		quietTableDelete("QRTZ_SCHEDULER_STATE");
		quietTableDelete("QRTZ_LOCKS");
		quietTableDelete("QRTZ_SIMPLE_TRIGGERS");
		quietTableDelete("QRTZ_CRON_TRIGGERS");
		quietTableDelete("QRTZ_BLOB_TRIGGERS");
		quietTableDelete("QRTZ_TRIGGERS");
		quietTableDelete("QRTZ_JOB_DETAILS");
		quietTableDelete("QRTZ_CALENDARS");
		 */
	}

	private void quietTableDelete(String tableName)
	{
		PersistenceContext pc = getProcessServer().getEngine().getPersistenceContextProvider().obtainPersistenceContext();
		try
		{
			pc.executeUpdateOrDelete("DELETE FROM " + tableName);
			pc.commitTransaction();
		}
		catch (Exception e)
		{
			// Ignore any
		}
	}
}
