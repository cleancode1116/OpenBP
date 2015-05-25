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
package org.openbp.server.test.activity;

import org.openbp.common.CommonUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.engine.EngineException;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

/**
 * Asserts a given string value for the given process variable.
 * Throws an EngineException if the actual value does not match the expected value.
 */
public class AssertProcessVariableValueActivity
	implements Handler
{
	/**
	 * Executes the handler.
	 *
	 * @param hc Handler context that contains execution parameters
	 * @return true if the handler handled the event, false to apply the default handling to the event
	 * @throws Exception Any exception that may occur during execution of the handler will be
	 * propagated to an exception handler if defined or abort the process execution otherwise.
	 */
	public boolean execute(HandlerContext hc)
		throws Exception
	{
		String variableName = (String) hc.getParam("VariableName");
		Object expectedValue = hc.getParam("ExpectedValue");

		if ("<null>".equals(expectedValue))
		{
			Object actualValue = hc.getTokenContext().getProcessVariableValue(variableName);
			if (actualValue != null)
			{
				String msg = LogUtil.error(getClass(), "Test case assert failed: Expected null value for variable $0, actual value: $1", variableName, actualValue);
				throw new EngineException("TestCaseAssertFailed", msg);
			}
		}
		else if ("<notnull>".equals(expectedValue))
		{
			Object actualValue = hc.getTokenContext().getProcessVariableValue(variableName);
			if (actualValue == null)
			{
				String msg = LogUtil.error(getClass(), "Test case assert failed: Expected non-null value for variable $0, actual value: $1", variableName, actualValue);
				throw new EngineException("TestCaseAssertFailed", msg);
			}
		}
		else if ("<present>".equals(expectedValue))
		{
			if (! hc.getTokenContext().hasProcessVariableValue(variableName))
			{
				String msg = LogUtil.error(getClass(), "Test case assert failed: Process variable $0 is not present, but should be", variableName);
				throw new EngineException("TestCaseAssertFailed", msg);
			}
		}
		else if ("<notpresent>".equals(expectedValue))
		{
			if (hc.getTokenContext().hasProcessVariableValue(variableName))
			{
				String msg = LogUtil.error(getClass(), "Test case assert failed: Process variable $0 is present, but should not be", variableName);
				throw new EngineException("TestCaseAssertFailed", msg);
			}
		}
		else
		{
			Object actualValue = hc.getTokenContext().getProcessVariableValue(variableName);
			if (! CommonUtil.equalsNull(expectedValue, actualValue))
			{
				String msg = LogUtil.error(getClass(), "Test case assert failed: Expected value for variable $0: $1, actual value: $2", variableName, expectedValue, actualValue);
				throw new EngineException("TestCaseAssertFailed", msg);
			}
		}

		return true;
	}
}
