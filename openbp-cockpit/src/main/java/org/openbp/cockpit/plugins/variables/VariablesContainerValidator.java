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
package org.openbp.cockpit.plugins.variables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ProcessVariable;
import org.openbp.guiclient.objectvalidators.DisplayObjectValidator;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;

/**
 * ProcessVariables container validator.
 *
 * @author Heiko Erhardt
 */
public class VariablesContainerValidator extends DisplayObjectValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 * Ensures uniqueness of the object name based on the original object
	 * that has been set with the property browser (i. e. this object will be skipped
	 * when checking for objects with the same name).
	 *
	 * @param editedObject Edited object that contains the property
	 * @param pb Property browser that edits the object
	 * @return
	 * true: The object value is valid and can be saved.<br>
	 * false: The object value is invalid. In this case, the save operation should be aborted and the focus should not be set outside the property browser.
	 */
	public boolean validateObject(Object editedObject, PropertyBrowser pb)
	{
		if (! (editedObject instanceof VariablesContainer))
			// Just for safety
			return true;

		VariablesContainer container = (VariablesContainer) editedObject;

		for (Iterator it = container.getProcessVariables(); it.hasNext();)
		{
			ProcessVariable param = (ProcessVariable) it.next();
			param.maintainReferences(ModelObject.VALIDATE_BASIC | ModelObject.RESOLVE_GLOBAL_REFS);
		}

		checkNameUniqueness(container);

		// Print errors and return if any have been found
		if (! displayMsgContainer())
			return false;

		return super.validateObject(editedObject, pb);
	}

	/**
	 * Checks the name uniqueness of the process variables in the container.
	 *
	 * @param container Container
	 */
	protected void checkNameUniqueness(VariablesContainer container)
	{
		List invalidNames = new ArrayList();

		for (Iterator it = container.getProcessVariables(); it.hasNext();)
		{
			ProcessVariable param = (ProcessVariable) it.next();

			String name = param.getName();
			if (name != null && ! invalidNames.contains(name) && countNamedParams(container, name) > 1)
			{
				invalidNames.add(name);
				param.getModelMgr().getMsgContainer().addMsg(param, "An element with this name already exists.");
			}
		}
	}

	/**
	 * Checks the name uniqueness of the process variables in the container.
	 *
	 * @param container Container
	 */
	private int countNamedParams(VariablesContainer container, String name)
	{
		int count = 0;

		for (Iterator it = container.getProcessVariables(); it.hasNext();)
		{
			ProcessVariable param = (ProcessVariable) it.next();

			if (name.equals(param.getName()))
				++count;
		}

		return count;
	}
}
