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
package org.openbp.guiclient.event;

import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.plugin.Plugin;

/**
 * This event is used if an object is to be opened in its associated editor.
 *
 * @author Stephan Moritz
 */
public class OpenEvent extends JaspiraEvent
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Underlying object */
	private Object underlyingObject;

	/** true if the object should be opened in read only mode */
	private boolean readonly;

	/** true if a new instance of the object should be create if it is not existend */
	private boolean create;

	/** MIME type(s) of the object */
	private String [] mimeTypes;

	/** Association property (as alternative to a MIME type) */
	private int associationProperty;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * In Comparison to the object that shall be opened the underlying object
	 * contains the environmental information of former.
	 *
	 * @param source Source of the event
	 * @param eventName Name of the event
	 * @param o Object that shall be opened (i.e. FileName)
	 */
	public OpenEvent(Plugin source, String eventName, Object o)
	{
		super(source, eventName, o);
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Gets the flag if the object should be opened in read only mode.
	 * @nowarn
	 */
	public boolean isReadonly()
	{
		return readonly;
	}

	/**
	 * Sets the flag if the object should be opened in read only mode.
	 * @nowarn
	 */
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 * Gets the flag if a new instance of the object should be create if it is not existend.
	 * @nowarn
	 */
	public boolean isCreate()
	{
		return create;
	}

	/**
	 * Sets the flag if a new instance of the object should be create if it is not existend.
	 * @nowarn
	 */
	public void setCreate(boolean create)
	{
		this.create = create;
	}

	/**
	 * Gets the underlying object.
	 * In Comparison to the object that shall be opened the underlying object
	 * contains the environmental information of former.
	 * @nowarn
	 */
	public Object getUnderlyingObject()
	{
		return underlyingObject;
	}

	/**
	 * Sets the underlying object.
	 * @nowarn
	 */
	public void setUnderlyingObject(Object underlyingObject)
	{
		this.underlyingObject = underlyingObject;
	}

	/**
	 * Gets the mIME type(s) of the object.
	 * @nowarn
	 */
	public String [] getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * Sets the mIME type(s) of the object.
	 * @nowarn
	 */
	public void setMimeTypes(String [] mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	/**
	 * Gets the association property (as alternative to a MIME type).
	 * @nowarn
	 */
	public int getAssociationProperty()
	{
		return associationProperty;
	}

	/**
	 * Sets the association property (as alternative to a MIME type).
	 * @nowarn
	 */
	public void setAssociationProperty(int associationProperty)
	{
		this.associationProperty = associationProperty;
	}
}
