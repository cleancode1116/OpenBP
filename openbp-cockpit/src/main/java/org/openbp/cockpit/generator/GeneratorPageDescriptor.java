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

import org.openbp.common.generic.description.DisplayObjectImpl;

/**
 * Descriptor class for a custom wizard page of a generator process.
 * The class may either contain a reference to a class that can be used
 * directly as wizard page or a class that defines an object that should
 * be edited using the generator's default property browser page.
 *
 * This class is a subclass of DisplayObject. The name is also the sequence
 * name of the wizard page. This name can be used from within the template
 * to access the object instance edited by this page.<br>
 * The display name will be used as title of the wizard page and must be present.
 * If there is no display name, the display name
 * of the {@link Generator} object that owns this page descriptor will be used.<br>
 * The description property is used as page description.
 *
 * @author Heiko Erhardt
 */
public class GeneratorPageDescriptor extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Page class name */
	private String pageClassName;

	/** Object class name */
	private String objectClassName;

	/** Finish wizard flag */
	private boolean finish;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Page class name */
	private Class pageClass;

	/** Object class name */
	private Class objectClass;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public GeneratorPageDescriptor()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the page class name.
	 * @nowarn
	 */
	public String getPageClassName()
	{
		return pageClassName;
	}

	/**
	 * Sets the page class name.
	 * @nowarn
	 */
	public void setPageClassName(String pageClassName)
	{
		this.pageClassName = pageClassName;
	}

	/**
	 * Gets the object class name.
	 * @nowarn
	 */
	public String getObjectClassName()
	{
		return objectClassName;
	}

	/**
	 * Sets the object class name.
	 * @nowarn
	 */
	public void setObjectClassName(String objectClassName)
	{
		this.objectClassName = objectClassName;
	}

	/**
	 * Gets the page class name.
	 * @nowarn
	 */
	public Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * Sets the page class name.
	 * @nowarn
	 */
	public void setPageClass(Class pageClass)
	{
		this.pageClass = pageClass;
	}

	/**
	 * Gets the object class name.
	 * @nowarn
	 */
	public Class getObjectClass()
	{
		return objectClass;
	}

	/**
	 * Sets the object class name.
	 * @nowarn
	 */
	public void setObjectClass(Class objectClass)
	{
		this.objectClass = objectClass;
	}

	/**
	 * Gets the finish wizard flag.
	 * @nowarn
	 */
	public boolean isFinish()
	{
		return finish;
	}

	/**
	 * Sets the finish wizard flag.
	 * @nowarn
	 */
	public void setFinish(boolean finish)
	{
		this.finish = finish;
	}
}
