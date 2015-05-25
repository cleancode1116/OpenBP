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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.openbp.core.MimeTypes;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelLocationUtil;
import org.openbp.core.model.item.ItemTypes;

/**
 * ata type class
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
public class ComplexTypeItemImpl extends DataTypeItemImpl
	implements ComplexTypeItem
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** List of data members of this type (contains {@link DataMember} objects) */
	private List memberList;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ComplexTypeItemImpl()
	{
		setItemType(ItemTypes.TYPE);
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		ComplexTypeItemImpl src = (ComplexTypeItemImpl) source;

		if (copyMode == Copyable.COPY_FIRST_LEVEL || copyMode == Copyable.COPY_DEEP)
		{
			memberList = null;

			// Create deep clones of collection members
			for (Iterator it = src.getMembers(); it.hasNext();)
			{
				DataMember member = (DataMember) it.next();
				if (copyMode == Copyable.COPY_DEEP)
					member = (DataMember) member.clone();
				addMember(member);
			}
		}
		else
		{
			// Shallow clone
			memberList = src.memberList;
		}
	}

	/**
	 * Gets the children (direct subordinates) of this object.
	 * The method returns the members of the data type and its base types.
	 *
	 * @return The list of objects or null if the object does not have children
	 */
	public List getChildren()
	{
		return getAllMemberList();
	}

	/**
	 * Determines if this type is a simple type or a complex type.
	 * @return Always false
	 */
	public boolean isSimpleType()
	{
		return false;
	}

	/**
	 * Determines if if this type is a simple type or a complex type is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @return Always false
	 */
	public boolean hasSimpleType()
	{
		return false;
	}

	/**
	 * Ensures that a class name has been defined for this data type.
	 *
	 * @return The class name
	 */
	public String getSafeClassName()
	{
		String clsName = getClassName();

		if (clsName == null)
		{
			String msg = LogUtil.error(getClass(), "Missing class name for type $0.", this.getQualifier());
			throw new EngineException("NoClassNameForType", msg);
		}

		return clsName;
	}

	/**
	 * Gets the native class of this complex type.
	 * This is either the Java class specified by the {@link DataTypeItemImpl#setJavaClass} property or
	 * - if not available - a class generated on the fly by the model class loader.
	 * In the latter case, the {@link DataTypeItemImpl#setJavaClass} property of this class will be set
	 * to the generated class.
	 *
	 * @return The class
	 * @throws ClassNotFoundException If the specified class could not be found
	 */
	public Class getSafeClass()
		throws ClassNotFoundException
	{
		Class typeClass = getJavaClass();

		if (typeClass == null)
		{
			synchronized (this)
			{
				// Re-check due to synchronized statement
				typeClass = getJavaClass();
				if (typeClass != null)
					return typeClass;

				// Bean type does not have a class name assigned, create a default class name
				String className = getSafeClassName();
				if (className == null)
				{
					throw new ClassNotFoundException("No class name specified for type '" + getQualifier() + "'");
				}

				// Use the executing model's class loader to load/create the on-the-fly bean class
				typeClass = getModel().getClassLoader().loadClass(className);

				setJavaClass(typeClass);
			}
		}
		return typeClass;
	}

	//////////////////////////////////////////////////
	// @@ Id member management
	//////////////////////////////////////////////////

	/**
	 * Determines the id member (primary key) of the specified type.
	 * Note: This method assumes that we have a single id member defined for this type.
	 *
	 * @return The member that is defined as identifier of this type or null if no primary key has been defined
	 */
	public DataMember getSingleIdMember()
	{
		List list = getAllMemberList();
		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; ++i)
			{
				DataMember member = (DataMember) list.get(i);
				if (member.isPrimaryKey())
					return member;
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		for (Iterator it = getMembers(); it.hasNext();)
		{
			DataMember member = (DataMember) it.next();

			// Establish back reference
			member.setParentDataType(this);

			member.maintainReferences(flag);
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		String javaSource = null;

		if (getClassName() != null)
		{
			javaSource = ModelLocationUtil.expandModelLocation(getModel(), ModelLocationUtil.DIR_SRC) + StringUtil.FOLDER_SEP + getClassName().replace('.', StringUtil.FOLDER_SEP_CHAR) + ".java";
		}

		// Add 'this' as process item association in order to start the Modeler
		associations = AssociationUtil.addAssociation(associations, -1, "Data Type", this, this, new String [] { MimeTypes.COMPLEX_TYPE_ITEM }, Association.PRIMARY, "");

		associations = AssociationUtil.addAssociation(associations, -1, "Java Implementation", javaSource, this, new String [] { MimeTypes.JAVA_SOURCE_FILE, MimeTypes.SOURCE_FILE, MimeTypes.TEXT_FILE, }, Association.NORMAL, "No class name has been specified for this data type.");

		if (getBaseType() != null)
		{
			associations = AssociationUtil.addAssociation(associations, -1, "Base Type", getBaseType(), this, new String [] { MimeTypes.ITEM }, Association.NORMAL, "This data type does not have a base type.");
		}

		associations = AssociationUtil.addAssociations(associations, -1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the list of data members.
	 * @return An iterator of {@link DataMember} objects
	 */
	public Iterator getMembers()
	{
		if (memberList == null)
			return EmptyIterator.getInstance();
		return memberList.iterator();
	}

	/**
	 * Gets the list of data members of this type and its base types.
	 * @return A list of {@link DataMember} objects
	 */
	public List getAllMemberList()
	{
		if (getBaseType() == null)
			return getMemberList();

		List list = new ArrayList();

		// Iterator over this type and all of its base types
		for (DataTypeItem t = this; t != null; t = t.getBaseType())
		{
			if (t instanceof ComplexTypeItem)
			{
				List members = ((ComplexTypeItem) t).getMemberList();
				if (members != null)
				{
					int n = members.size();
					for (int i = 0; i < n; ++i)
					{
						// Add the data member if no member with this name isn't present yet
						// (this allows overriding of data members!)
						DataMember member = (DataMember) members.get(i);
						if (NamedObjectCollectionUtil.getByName(list, member.getName()) == null)
						{
							list.add(member);
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * Gets the list of data members of this type and its base types.
	 * @return An iterator of {@link DataMember} objects
	 */
	public Iterator getAllMembers()
	{
		List list = getAllMemberList();
		if (list == null)
			return EmptyIterator.getInstance();
		return list.iterator();
	}

	/**
	 * Creates a new member and assigns a unique name to it to.
	 * @return The new member
	 */
	public DataMember createMember()
	{
		DataMember member = new DataMemberImpl();

		String name = NamedObjectCollectionUtil.createUniqueId(memberList, "Member");
		member.setName(name);

		return member;
	}

	/**
	 * Adds a member to the list of data members.
	 * @nowarn
	 */
	public void addMember(DataMember member)
	{
		// We do not use a SortingArrayList here because users may want to provide their own ordering to the member list.
		if (memberList == null)
			memberList = new ArrayList();
		memberList.add(member);

		member.setParentDataType(this);
	}

	/**
	 * Removes a member from the list of data members.
	 * @nowarn
	 */
	public void removeMember(DataMember member)
	{
		if (memberList != null)
		{
			memberList.remove(member);
			if (memberList.size() == 0)
				memberList = null;
		}
	}

	/**
	 * Gets a member by its name.
	 *
	 * @param name Member name
	 * @return The member or null if no such member exists
	 */
	public DataMember getMember(String name)
	{
		DataMember member = (DataMember) NamedObjectCollectionUtil.getByName(memberList, name);

		if (member == null)
		{
			DataTypeItem baseType = getBaseType();
			if (baseType instanceof ComplexTypeItem)
			{
				// Member not found, search the base type
				member = ((ComplexTypeItem) baseType).getMember(name);
			}
		}

		return member;
	}

	/**
	 * Clears the list of data members.
	 */
	public void clearMembers()
	{
		memberList = null;
	}

	/**
	 * Gets the list of data members.
	 * @return A list of {@link DataMember} objects
	 */
	public List getMemberList()
	{
		return memberList;
	}

	/**
	 * Sets the list of data members. This is done by clearing the previous member list, and then
	 * itterating through the member items, and adding each member by the {@link #addMember} method.
	 * @param list The list to be set
	 */
	public void setMemberList(List list)
	{
		clearMembers();

		if (list != null)
		{
			int n = list.size();
			for (int i = 0; i < n; ++i)
			{
				DataMember member = (DataMember) list.get(i);
				addMember(member);
			}
		}
	}
}
