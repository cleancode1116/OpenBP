/*
 * Created on 24.08.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 */
package org.openbp.cockpit.modeler.custommodelobjectconfigurator;

/**
 * Descriptor class for custom model object configurators.
 */
public class CustomModelObjectConfiguratorDescriptor
{
	/** Name */
	private String name;

	/** Display name */
	private String displayName;

	/** Description */
	private String description;

	/** Priority */
	private int priority;

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
	 * Gets the display name.
	 * @nowarn
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * Sets the display name.
	 * @nowarn
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * Gets the description.
	 * @nowarn
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description.
	 * @nowarn
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the priority.
	 * @nowarn
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Sets the priority.
	 * @nowarn
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
}
