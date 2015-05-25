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

import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.DisplayObjectImpl;

/**
 * Property browser container class for model imports.
 * This container class is used by the {@link Model} class because a list of strings cannot be edited directly
 * by the property browser. Instead, a list of these containers is edited.
 *
 * @author Heiko Erhardt
 */
public class ModelImportPBContainer extends DisplayObjectImpl
{
	/**
	 * Default constructor.
	 */
	public ModelImportPBContainer()
	{
	}

	/**
	 * Default constructor.
	 *
	 * @param importedModel Imported model wrapped by this container
	 */
	public ModelImportPBContainer(Model importedModel)
	{
		try
		{
			copyFrom(importedModel, Copyable.COPY_DEEP);
		}
		catch (CloneNotSupportedException e)
		{
			// Never happens
		}

		setName(importedModel.getQualifier().toString());
	}
}
