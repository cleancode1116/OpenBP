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
package org.openbp.core.model.item.type;

import java.util.Iterator;
import java.util.List;

/**
 * OpenBP data type definition object.
 * A type definition object (or short typedef) describes one data object type of an application.
 * Each typedef will be stored in a typedef XML file.
 * All typedefs together make up the OpenBP data dictionary.
 *
 * The name of the type must follow Java naming conventions and must begin with an uppercase letter.
 *
 * The typedef contains information about the data type itself and the properties of the data type
 * (as {@link DataMember} objects).
 *
 * @author Heiko Erhardt
 */
public interface ComplexTypeItem
	extends DataTypeItem
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Ensures that a class name has been defined for this data type.
	 * If there is no class name present, the method will generate an appropiate
	 * bean class name ("databean.'modelname'.'typename'").
	 *
	 * @return The class name
	 */
	public String getSafeClassName();

	/**
	 * Gets the native class of this complex type.
	 * This is either the Java class specified by the {@link #setJavaClass} property or
	 * - if not available - a class generated on the fly by the model class loader.
	 * In the latter case, the {@link #setJavaClass} property of this class will be set
	 * to the generated class.
	 *
	 * @return The class
	 * @throws ClassNotFoundException If the specified class could not be found
	 */
	public Class getSafeClass()
		throws ClassNotFoundException;

	/**
	 * Determines the id member (primary key) of the specified type.
	 * Note: This method assumes that we have a single id member defined for this type.
	 *
	 * @return The member that is defined as identifier of this type or null if no primary key has been defined
	 */
	public DataMember getSingleIdMember();

	/**
	 * Gets the list of data members.
	 * @return An iterator of {@link DataMember} objects
	 */
	public Iterator getMembers();

	/**
	 * Gets the list of data members of this type and its base types.
	 * @return A list of {@link DataMember} objects
	 */
	public List getAllMemberList();

	/**
	 * Gets the list of data members of this type and its base types.
	 * @return An iterator of {@link DataMember} objects
	 */
	public Iterator getAllMembers();

	/**
	 * Creates a new member and assigns a new name to it to the list of data members.
	 * @return The new member
	 */
	public DataMember createMember();

	/**
	 * Adds a member to the list of data members.
	 * @nowarn
	 */
	public void addMember(DataMember member);

	/**
	 * Removes a member from the list of data members.
	 * @nowarn
	 */
	public void removeMember(DataMember member);

	/**
	 * Gets a member by its name.
	 *
	 * @param name Member name
	 * @return The member or null if no such member exists
	 */
	public DataMember getMember(String name);

	/**
	 * Clears the list of data members.
	 */
	public void clearMembers();

	/**
	 * Gets the list of data members.
	 * @return A list of {@link DataMember} objects
	 */
	public List getMemberList();

	/**
	 * Gets the java bean class of this type.
	 * @nowarn
	 */
	public Class getJavaClass();

	/**
	 * Sets the java bean class of this type.
	 * @nowarn
	 */
	public void setJavaClass(Class javaClass);
}
