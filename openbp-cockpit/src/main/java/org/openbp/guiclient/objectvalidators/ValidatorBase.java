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
package org.openbp.guiclient.objectvalidators;

import java.util.Iterator;

import org.openbp.common.generic.msgcontainer.MsgItem;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.logger.LogLevel;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.jaspira.propertybrowser.ObjectValidator;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;
import org.openbp.swing.components.JMsgBox;

/**
 * Base class for object validators.
 *
 * @author Heiko Erhardt
 */
public class ValidatorBase
	implements ObjectValidator
{
	/**
	 * Validates an edited property.
	 *
	 * @param propertyName Name of the edited property
	 * @param propertyValue Current value of the property
	 * @param editedObject Edited object that contains the property
	 * @param propertyEditor Property editor that edits the property value
	 * @param complete
	 *		true	The value has been completely entered. This is the case if the user wishes to leave the field.<br>
	 *		false	The value is being typed/edited.
	 * @return
	 *		true	The property value is valid.<br>
	 *		false	The property value is invalid. In this case, the focus should not be set
	 *				to the next component.
	 */
	public boolean validateProperty(String propertyName, Object propertyValue, Object editedObject, PropertyEditor propertyEditor, boolean complete)
	{
		return true;
	}

	/**
	 * Validates the entire object before it will be saved.
	 *
	 * @param editedObject Edited object that contains the property
	 * @param pb Property browser that edits the object
	 * @return
	 *		true	The object value is valid and can be saved.<br>
	 *		false	The object value is invalid. In this case, the save operation should be aborted and
	 *				the focus should not be set outside the property browser.
	 */
	public boolean validateObject(Object editedObject, PropertyBrowser pb)
	{
		return true;
	}

	/**
	 * Outputs an error message in a message box.
	 *
	 * @param msg The message
	 */
	public void displayErrorMsg(final String msg)
	{
		// When the validator is being called during e. g. a 'focus lost' event,
		// displaying a message box will stall AWT's focus handling.
		// So we defer the message box display after the current event has been processed
		// by using JMsgBox.TYPE_OKLATER.
		JMsgBox.show(null, msg, JMsgBox.ICON_ERROR | JMsgBox.TYPE_OKLATER);
	}

	/**
	 * Prints the contents of the CoreModule's message container - if any - in a message box.
	 *
	 * @return
	 *		true	The message container is null, does not contain any messages or only warning messages (which have been displayed)
	 *		false	The message container contains error messages
	 */
	public boolean displayMsgContainer()
	{
		StandardMsgContainer msgContainer = ModelConnector.getInstance().getMsgContainer();

		if (! msgContainer.isEmpty())
		{
			// Show the errors to the user
			boolean ret = true;
			StringBuffer sb = new StringBuffer();

			for (Iterator it = msgContainer.getMsgs(); it.hasNext();)
			{
				MsgItem error = (MsgItem) it.next();

				if (error.getMsgType().equals(LogLevel.ERROR))
				{
					// No warning, so validation failed
					ret = false;
				}

				String errorMsg = error.toString();
				if (sb.length() != 0)
					sb.append("\n");
				sb.append(errorMsg);
			}

			msgContainer.clearMsgs();
			displayErrorMsg(sb.toString());

			return ret;
		}

		return true;
	}
}
