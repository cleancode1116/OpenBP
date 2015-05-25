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
package org.openbp.model.system.misc;

import org.openbp.server.context.BeanAccessUtil;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Set member value.
 * Implementation of the SetMemberValue activity handler.
 * Sets the value of a member of a bean or arbitrary Java object.
 * 
 * If the bean does not have the specified members, an error will be generated.
 * 
 * If you intend to set multiple member values, see the AssignMemberValues activity.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Bean': Bean
 *     Parameter 'Member': Member
 *     Parameter 'Value': Value
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 *     Parameter 'Bean': Bean
 */
public class SetMemberValueHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter Bean */
	private static final String PARAM_BEAN = "Bean";

	/** Parameter Member */
	private static final String PARAM_MEMBER = "Member";

	/** Parameter Value */
	private static final String PARAM_VALUE = "Value";

	// {{*Custom constants*
	// }}*Custom constants*

	// {{*Custom members*
	// Note: If you define member variables, consider the fact that the same handler instance may be executed
	// by multiple threads in parallel, so you have to make sure that your implementation is thread safe.
	// In general, member variables should be defined for global-like data only.
	// }}*Custom members*

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
		// {{*Handler implementation*
		Object bean = hc.getParam(PARAM_BEAN);
		String memberName = (String) hc.getParam(PARAM_MEMBER);
		Object value = hc.getParam(PARAM_VALUE);

		BeanAccessUtil.setMemberValue(bean, memberName, value);
		hc.setResult(PARAM_BEAN, bean);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*
	// }}*Custom methods*
}

