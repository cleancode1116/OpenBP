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
package org.openbp.server.model.modelmgr;

import org.openbp.core.model.item.Item;
import org.openbp.server.persistence.PersistentObjectBase;

/**
 * Descriptor object that is used to persist a {@link Item} object from/to the database.
 *
 * @author Heiko Erhardt
 */
public abstract class DbModelJarFile extends PersistentObjectBase
{
	/** Model name */
	private String modelName;

	/** Name of the jar file */
	private String jarFileName;

	/**
	 * Default constructor.
	 */
	public DbModelJarFile()
	{
	}

	/**
	 * Gets the model name.
	 * @nowarn
	 */
	public String getModelName()
	{
		return modelName;
	}

	/**
	 * Sets the model name.
	 * @nowarn
	 */
	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	/**
	 * Gets the name of the jar file.
	 * @nowarn
	 */
	public String getJarFileName()
	{
		return jarFileName;
	}

	/**
	 * Sets the name of the jar file.
	 * @nowarn
	 */
	public void setJarFileName(String jarFileName)
	{
		this.jarFileName = jarFileName;
	}

	/**
	 * Gets the byte code of the jar file.
	 * @nowarn
	 */
	public abstract byte[] getByteCode();

	/**
	 * Sets the byte code of the jar file.
	 * @nowarn
	 */
	public abstract void setByteCode(byte[] byteCode);
}
