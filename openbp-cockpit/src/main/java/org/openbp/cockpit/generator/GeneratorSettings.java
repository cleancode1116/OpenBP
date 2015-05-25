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
package org.openbp.cockpit.generator;

import org.openbp.common.generic.Copyable;
import org.openbp.common.generic.description.Validatable;
import org.openbp.common.generic.msgcontainer.MsgContainer;
import org.openbp.core.model.Model;

/**
 * General settings object for a generator.
 * This object may hold various information that is edited using a custom property browser page.
 *
 * @author Heiko Erhardt
 */
public class GeneratorSettings
	implements Cloneable, Copyable, Validatable
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Generator name */
	private String generatorName;

	/** Model that will be used to resolve item references (e\.g\. data types) */
	private Model model;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public GeneratorSettings()
	{
	}

	/**
	 * Creates a clone of this object.
	 * @return The clone (a deep copy of this object)
	 * @throws CloneNotSupportedException If the cloning of one of the contained members failed
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		GeneratorSettings clone = (GeneratorSettings) super.clone();

		// Perform a deep copy
		clone.copyFrom(this, Copyable.COPY_DEEP);

		return clone;
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

		GeneratorSettings src = (GeneratorSettings) source;

		generatorName = src.generatorName;
		model = src.model;
	}

	/**
	 * This template method is called before the settings object is being serialized.
	 * It can be overridden to implement custom operations.
	 */
	public void beforeSerialization()
	{
	}

	/**
	 * This template method is called after the settings object has been deserialized.
	 * It can be overridden to implement custom initializations.
	 *
	 * @param msgs Container for error messages
	 */
	public void afterDeserialization(MsgContainer msgs)
	{
	}

	//////////////////////////////////////////////////
	// @@ Validatable implementation
	//////////////////////////////////////////////////

	/**
	 * Checks if the object is valid.
	 * @param msgContainer Any errors will be logged to this message container
	 * @return
	 * true: The object is valid.<br>
	 * false: Errors were found within the object or its sub objects.
	 */
	public boolean validate(MsgContainer msgContainer)
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the generator name.
	 * @nowarn
	 */
	public String getGeneratorName()
	{
		return generatorName;
	}

	/**
	 * Sets the generator name.
	 * @nowarn
	 */
	public void setGeneratorName(String generatorName)
	{
		this.generatorName = generatorName;
	}

	/**
	 * Gets the model that will be used to resolve item references (e\.g\. data types).
	 * @nowarn
	 */
	public Model getModel()
	{
		return model;
	}

	/**
	 * Sets the model that will be used to resolve item references (e\.g\. data types).
	 * @nowarn
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}
}
