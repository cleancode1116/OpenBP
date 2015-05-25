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

/**
 * Info container that describes an the open event for a particular object.
 * This information is returned by the event handler of a plugin in response to the
 * plugin.association.supports event.
 * It contains the name of the open event, the MIME type of the supported object
 * and a textual description of the plugin that accept the open event.
 *
 * @author Heiko Erhardt
 */
public class OpenEventInfo
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Name of the open event used to openthe object */
	private String eventName;

	/** Mime type */
	private String mimeType;

	/** Description of the plugin */
	private String description;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public OpenEventInfo()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param eventName Name of the open event used to openthe object
	 * @param mimeType Mime type
	 * @param description Description of the plugin
	 */
	public OpenEventInfo(String eventName, String mimeType, String description)
	{
		this.eventName = eventName;
		this.mimeType = mimeType;
		this.description = description;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the name of the open event used to openthe object.
	 * @nowarn
	 */
	public String getEventName()
	{
		return eventName;
	}

	/**
	 * Sets the name of the open event used to openthe object.
	 * @nowarn
	 */
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	/**
	 * Gets the mime type.
	 * @nowarn
	 */
	public String getMimeType()
	{
		return mimeType;
	}

	/**
	 * Sets the mime type.
	 * @nowarn
	 */
	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	/**
	 * Gets the description of the plugin.
	 * @nowarn
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description of the plugin.
	 * @nowarn
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
}
