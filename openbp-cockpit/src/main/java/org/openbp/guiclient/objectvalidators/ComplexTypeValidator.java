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

import java.util.HashMap;
import java.util.List;

import org.openbp.common.string.StringUtil;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.editor.PropertyEditor;

/**
 * Object validator for the {@link ComplexTypeItem} class.
 * Generates a bean class name and a xml name from the object name.
 *
 * @author Heiko Erhardt
 */
public class ComplexTypeValidator extends ModelObjectValidator
{
	/**
	 * Validates the entire object before it will be saved.
	 * Ensures uniqueness of the data member names.
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
		if (! super.validateObject(editedObject, pb))
			return false;

		return validateMemberNames((ComplexTypeItem) editedObject);
	}

	/**
	 * Property validator method.
	 * Generates an XML name for the data type and automatically adds a package name
	 * if a default package is defined for the model the type belongs to.
	 * @copy DisplayObjectValidator.validateProperty
	 */
	public boolean validateProperty(String propertyName, Object propertyValue, Object editedObject, PropertyEditor propertyEditor, boolean complete)
	{
		if (! super.validateProperty(propertyName, propertyValue, editedObject, propertyEditor, complete))
			return false;

		if (complete)
		{
			if (propertyName.equals("Name"))
			{
				ComplexTypeItem type = (ComplexTypeItem) editedObject;

				Model model = type.getModel();
				if (model != null)
				{
					// ClassName = model.defaultPackage + ".data." + name
					// If there is an implementation class name specified...
					String oldName = type.getName();
					String oldClassName = type.getClassName();
					if (oldClassName != null && oldName != null)
					{
						// ...substitute ".OldName" to ".NewName"
						String newName = (String) propertyValue;
						String className = StringUtil.substitute(oldClassName, "." + oldName, "." + newName);

						// Update the object
						type.setClassName(className);
						propertyEditor.getOwner().reloadProperty("ClassName");
					}
				}
			}
		}

		return true;
	}

	/**
	 * Validates the member names of a complex type.
	 *
	 * @param type Type to validate
	 * @return
	 *		true	Errors have been detected
	 *		false	No errors found
	 */
	protected boolean validateMemberNames(ComplexTypeItem type)
	{
		// This list includes the members of base types, too
		List allMembers = type.getAllMemberList();
		List members = type.getMemberList();

		if (members != null)
		{
			// We may remove members from the list, so clone it
			HashMap reported = null;

			int n = members.size();
			for (int i = 0; i < n; ++i)
			{
				DataMember member = (DataMember) members.get(i);
				String name = member.getName();

				if (reported != null && reported.get(name) != null)
				{
					// Already reported
					continue;
				}

				int count = countMembers(name, allMembers);
				if (count > 1)
				{
					type.getModelMgr().getMsgContainer().addMsg(member,
						"The member name conflicts with another member or this type or a member of its base type.");

					// Save this member name to prevent having the same error message twice
					if (reported == null)
						reported = new HashMap();
					reported.put(name, name);
				}
			}
		}

		// Print errors and return if any have been found
		return displayMsgContainer();
	}

	/**
	 * Counts the number of members with the given name.
	 *
	 * @param name Name
	 * @param members Member list to search in
	 * @return The count
	 */
	private int countMembers(String name, List members)
	{
		int count = 0;

		if (members != null)
		{
			int n = members.size();
			for (int i = 0; i < n; ++i)
			{
				DataMember member = (DataMember) members.get(i);

				if (member.getName().equals(name))
					++count;
			}
		}

		return count;
	}
}
