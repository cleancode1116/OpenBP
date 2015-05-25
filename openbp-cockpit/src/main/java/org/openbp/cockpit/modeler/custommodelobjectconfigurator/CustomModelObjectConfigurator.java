/*
 * Created on 24.08.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 */
package org.openbp.cockpit.modeler.custommodelobjectconfigurator;

import org.openbp.core.model.ModelObject;

/**
 * Interface that allows for custom activity node configuration dialogs in the openBP Cockpit.
 * The activity configurator is specified for a particular type of activity.
 * It may display a configuration dialog that is activated on an activity node
 * in a process in the OpenBP modeler by selecting the appropriate menu option
 * from the context menu of the activity node.
 */
public interface CustomModelObjectConfigurator
{
	/**
	 * Checks if this configurator applies for the given model object
	 *
	 * @param mo Model object to edit
	 * @return The descriptor that specifies the popup menu option to create
	 * or null if the configurator does not apply to this type of model object
	 */
	public CustomModelObjectConfiguratorDescriptor checkAppliance(ModelObject mo);

	/**
	 * Displays the activity configuration dialog.
	 * This method will be called if the user opts to execute the configuration menu option provided by this configurator.
	 *
	 * @param mo Model object to edit
	 * @return true if the method has modified some property of the activity, false otherwise.
	 * In the former case, the process will be marked as 'modified'.
	 */
	public boolean displayActivityConfigurationDialog(ModelObject mo);
}
