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

import java.util.Iterator;

import org.openbp.core.OpenBPException;
import org.openbp.core.model.item.process.NodeParam;
import org.openbp.server.context.BeanAccessUtil;
import org.openbp.server.handler.Handler;
import org.openbp.server.handler.HandlerContext;

// {{*Custom imports*
// }}*Custom imports*

/**
 * Assign member values.
 * Implementation of the AssignMemberValues activity handler.
 * Assigns multiple member values to a bean or arbitrary Java object.
 * 
 * Use this activity as follows:
 * On the 'In' socket of the activity node, create a parameter for each member you want to assign.
 * The data type and name of the parameter must match the data type and name of the member you want to modify.
 * 
 * If the bean does not have the specified members, an error will be generated.
 * 
 * If you want to modify a single member value, see also the SetMemberValue activity.
 * 
 * Input sockets/parameter:
 *   Socket 'In'
 *     Parameter 'Bean': Bean
 * 
 * Output sockets/parameter:
 *   Socket 'Out'
 *     Parameter 'Bean': Bean
 */
public class AssignMemberValuesHandler
	// {{*Custom extends*
	// }}*Custom extends*
	// {{*Custom interfaces*
	implements Handler
	// }}*Custom interfaces*
{
	/** Parameter Bean */
	private static final String PARAM_BEAN = "Bean";

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

		hc.setResult(PARAM_BEAN, bean);

		fillBean(bean, hc);

		return true;
		// }}*Handler implementation*
	}

	// {{*Custom methods*

	/**
	 * Assigns member values given as activity node parameters to the bean.
	 *
	 * @param target Object to modify
	 * @param hc Handler context
	 * @throws OpenBPException Any exception that may occur during execution of the activity
	 * should be wrapped into an activity exception that may be handeled by the error process.
	 */
	private void fillBean(Object target, HandlerContext hc)
	{
		// Iterate all entry socket parameters
		for (Iterator params = hc.getCurrentSocket().getParams(); params.hasNext();)
		{
			NodeParam param = (NodeParam) params.next();
			String memberName = param.getName();

			if (memberName.equals(PARAM_BEAN))
			{
				// Skip the TypeName parameter
				continue;
			}

			// Get the parameter value
			Object memberValue = hc.getParam(memberName);

			// Assign it to the target member
			BeanAccessUtil.setMemberValue(target, memberName, memberValue);
		}
	}

	// }}*Custom methods*
}

