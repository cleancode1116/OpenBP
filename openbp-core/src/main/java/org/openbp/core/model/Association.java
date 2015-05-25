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
package org.openbp.core.model;

import java.io.Serializable;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.util.ToStringHelper;
import org.openbp.core.MimeTypes;

/**
 * An association is a connection from an object to a related object.
 * This class is actually a container to transport information about
 * an association from the server to the client (see the
 * {@link ModelObject#getAssociations} method).
 *
 * Associations depend on the object type and can be e. g. source files,
 * xml files, templates items, visual files, icon items, icon files etc.
 * The type of the association specifies the associated object. The object
 * type is specified by its MIME type.
 *
 * Multiple association types can be specified for an association.
 * The association types should be sorted using a hierarchy, i. e. the most
 * specialized association type should be placed before more generic types.
 * See the {@link MimeTypes} class for possible association types.<br>
 * In example, for a Java source file the association types
 * { MimeTypes.JAVA_SOURCE_FILE, MimeTypes.SOURCE_FILE, MimeTypes.TEXT_FILE }
 * could be used.
 *
 * Instead of the associated object, the {@link #setAssociatedObject} may also
 * contain an Exception. The exception denotes that the association is
 * possible, but no object has been associated with the object.
 * The exception message will give a hint on how to create this association.
 *
 * @author Heiko Erhardt
 */
public class Association extends DisplayObjectImpl
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Association priority: Primary association (= open by double-click) */
	public static final int PRIMARY = 1;

	/** Association priority: Regular association (= open by context menu) */
	public static final int NORMAL = 0;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Association MIME types */
	private String [] associationTypes;

	/** Association priority */
	private int associationPriority;

	/** Associated object */
	private Object associatedObject;

	/** Underlying object */
	private Object underlyingObject;

	/** Hint msg if no actual association is present */
	private String hintMsg;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Association()
	{
	}

	public boolean equals(Object otherObject)
	{
		if (! (otherObject instanceof Association))
			return false;

		Association other = (Association) otherObject;
		if (! CommonUtil.equalsNull(getAssociationTypes(), other.getAssociationTypes()))
			return false;
		if (! CommonUtil.equalsNull(getAssociatedObject(), other.getAssociatedObject()))
			return false;
		if (! CommonUtil.equalsNull(getUnderlyingObject(), other.getUnderlyingObject()))
			return false;
		if (! CommonUtil.equalsNull(getHintMsg(), other.getHintMsg()))
			return false;

		return true;
	}

	/**
	 * Default constructor.
	 *
	 * In Comparison to the object that shall be opened the underlying object
	 * contains the environmental information of former.
	 *
	 * @param displayName Display name of the association
	 * @param associatedObject Associated object
	 * @param underlyingObject Object underlying on the associated object (i.e. item)
	 * @param associationTypes Association MIME types; must contain at least one type
	 * @param associationPriority Association priority
	 * @param hintMsg Hint msg if no actual association is present
	 */
	public Association(String displayName, Object associatedObject, Object underlyingObject, String [] associationTypes, int associationPriority, String hintMsg)
	{
		setDisplayName(displayName);
		this.associatedObject = associatedObject;
		this.underlyingObject = underlyingObject;
		this.associationTypes = associationTypes;
		this.associationPriority = associationPriority;
		this.hintMsg = hintMsg;
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return ToStringHelper.toString(this, "associationTypes", "associatedObject");
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the association MIME types.
	 * @return See the {@link MimeTypes} class for possible association types<br>
	 * Must contain at least one type
	 */
	public String [] getAssociationTypes()
	{
		return associationTypes;
	}

	/**
	 * Sets the association MIME types.
	 * @param associationTypes See the {@link MimeTypes} class for possible association types<br>
	 * Must contain at least one type
	 */
	public void setAssociationTypes(String [] associationTypes)
	{
		this.associationTypes = associationTypes;
	}

	/**
	 * Gets the association priority.
	 * @nowarn
	 */
	public int getAssociationPriority()
	{
		return associationPriority;
	}

	/**
	 * Sets the association priority.
	 * @nowarn
	 */
	public void setAssociationPriority(int associationPriority)
	{
		this.associationPriority = associationPriority;
	}

	/**
	 * Gets the associated object.
	 * @return The object or null. In the latter case, a hint message should be present.
	 */
	public Object getAssociatedObject()
	{
		return associatedObject;
	}

	/**
	 * Sets the associated object.
	 * @param associatedObject The object or null. In the latter case, a hint message should be present.
	 */
	public void setAssociatedObject(Object associatedObject)
	{
		this.associatedObject = associatedObject;
	}

	/**
	 * Gets the hint msg if no actual association is present.
	 * @nowarn
	 */
	public String getHintMsg()
	{
		return hintMsg;
	}

	/**
	 * Sets the hint msg if no actual association is present.
	 * @nowarn
	 */
	public void setHintMsg(String hintMsg)
	{
		this.hintMsg = hintMsg;
	}

	/**
	 * Gets the underlying object.
	 *
	 * @nowarn
	 */
	public Object getUnderlyingObject()
	{
		return underlyingObject;
	}

	/**
	 * Sets the underlying object
	 *
	 * @nowarn
	 */
	public void setUnderlyingObject(Object underlyingObject)
	{
		this.underlyingObject = underlyingObject;
	}
}
