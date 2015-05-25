/*
 *   Copyright 2008 skynamics AG
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
package org.openbp.server.model.modelmgr;

import org.openbp.core.model.Model;
import org.openbp.server.persistence.PersistentObjectBase;

/**
 * Descriptor object that is used to persist a {@link Model} object from/to the database.
 *
 * @author Heiko Erhardt
 */
public class DbModel extends PersistentObjectBase
{
	/** Name */
	private String name;

	/** Xml */
	private String xml;

	/**
	 * Default constructor.
	 */
	public DbModel()
	{
	}

	/**
	 * Gets the name.
	 * @nowarn
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @nowarn
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the xml.
	 * @nowarn
	 */
	public String getXml()
	{
		return xml;
	}

	/**
	 * Sets the xml.
	 * @nowarn
	 */
	public void setXml(String xml)
	{
		this.xml = xml;
	}
}
