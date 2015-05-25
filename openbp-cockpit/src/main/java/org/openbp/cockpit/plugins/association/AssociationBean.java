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
package org.openbp.cockpit.plugins.association;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.util.CopyUtil;

/**
 * Container bean containing the mime-type associations.
 *
 * @author Andreas Putz
 */
public class AssociationBean extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** List of the mime type associations */
	private List mimeTypes;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public AssociationBean()
	{
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the mime types.
	 *
	 * @return The list of mime types
	 */
	public List getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * Sets the mime types.
	 *
	 * @param mimeTypes Mime types which can interact with this association
	 */
	public void setMimeTypes(List mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	/**
	 * Add a mime type to list.
	 *
	 * @param mimeType Mime type which can interact with this association
	 */
	public void addMimeType(MimeTypeAssociation mimeType)
	{
		if (mimeTypes == null)
			mimeTypes = new ArrayList();
		mimeTypes.add(mimeType);
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Gets the MIME type association for the given MIME type.
	 *
	 * @param mimeType Mime type
	 * @return The association or null if no such association exists
	 */
	public MimeTypeAssociation getMimeTypeAssociation(String mimeType)
	{
		if (mimeTypes != null)
		{
			int n = mimeTypes.size();
			for (int i = 0; i < n; ++i)
			{
				MimeTypeAssociation a = (MimeTypeAssociation) mimeTypes.get(i);
				if (mimeType.equals(a.getName()))
					return a;
			}
		}

		return null;
	}

	//////////////////////////////////////////////////
	// @@ Copyable implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.common.generic.Copyable#copyFrom(Object, int)
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;

		AssociationBean sourceBean = (AssociationBean) source;

		mimeTypes = (List) CopyUtil.copyCollection(sourceBean.getMimeTypes(), copyMode);
	}

	//////////////////////////////////////////////////
	// @@ Serialize and deserialize methods
	//////////////////////////////////////////////////

	/**
	 * Serializes the association bean.
	 *
	 * @return The serialized object
	 */
	String serialize()
	{
		StringBuffer serialized = new StringBuffer(200);

		serialized.append("AssociationBean#");

		if (mimeTypes != null)
		{
			Iterator it = mimeTypes.iterator();
			while (it.hasNext())
			{
				serialized.append(((MimeTypeAssociation) it.next()).serialize());
				serialized.append('*');
			}
		}

		serialized.append('#');

		return serialized.toString();
	}

	/**
	 * Deserializes a string to an association bean.
	 *
	 * @param serialized The serialized object as string
	 *
	 * @return The deserialized object
	 */
	static AssociationBean deserialize(String serialized)
	{
		AssociationBean retVal = null;

		if (serialized != null && serialized.startsWith("AssociationBean#"))
		{
			// Creates the association object
			retVal = new AssociationBean();
			serialized = serialized.substring(16, serialized.length() - 2);

			StringTokenizer st = new StringTokenizer(serialized, "*");
			while (st.hasMoreTokens())
			{
				MimeTypeAssociation mta = MimeTypeAssociation.deserialize(st.nextToken());
				if (mta != null)
					retVal.addMimeType(mta);
			}
		}

		return retVal;
	}
}
