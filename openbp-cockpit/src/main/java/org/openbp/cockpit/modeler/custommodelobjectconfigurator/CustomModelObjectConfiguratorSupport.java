/*
 * Created on 24.08.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 */
package org.openbp.cockpit.modeler.custommodelobjectconfigurator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.util.ModelerUtil;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.jaspira.action.JaspiraAction;

/**
 * Helper class that provides support for custom object configurators.
 */
public class CustomModelObjectConfiguratorSupport
{
	/** List of configurators */
	List<CustomModelObjectConfigurator> configurators;

	/** Modeler */
	private Modeler modeler;

	/** Template */
	private JaspiraAction templateAction;

	/**
	 * Default constructor.
	 *
	 * @param modeler Modeler
	 */
	public CustomModelObjectConfiguratorSupport(Modeler modeler)
	{
		this.modeler = modeler;
		initialize();
	}

	public void initialize()
	{
		configurators = new ArrayList<CustomModelObjectConfigurator>();
		List<String> configuratorClassNames = ModelerUtil.getStringListFromSettings("openbp.cockpit.modelObjectConfigurators");
		for (String className : configuratorClassNames)
		{
			try
			{
				CustomModelObjectConfigurator configurator = (CustomModelObjectConfigurator) ReflectUtil.instantiate(className, CustomModelObjectConfigurator.class, "custom model object configurator");
				configurators.add(configurator);
			}
			catch (Exception e)
			{
				// Log as warning
				LogUtil.error(getClass(), "Error instantiating custom model object configurator $0.", className, e);
			}
		}

		templateAction = new JaspiraAction(modeler, "modeler.popup.custom");
	}

	public void addConfiguratorMenuOptions(final ModelObject mo, final JaspiraAction group)
	{
		for (CustomModelObjectConfigurator configurator : configurators)
		{
			CustomModelObjectConfiguratorDescriptor desc = configurator.checkAppliance(mo);
			if (desc != null)
			{
				final CustomModelObjectConfigurator foundConfigurator = configurator;
				JaspiraAction action = new JaspiraAction(desc.getName(), desc.getDisplayName(), desc.getDescription(), null, null, desc.getPriority(), JaspiraAction.TYPE_ACTION)
				{
					public void actionPerformed(ActionEvent e)
					{
						if (foundConfigurator.displayActivityConfigurationDialog(mo))
						{
							modeler.getDrawing().setModified();
						}
					}
				};
				action.setIcon(templateAction.getIcon());
				group.addMenuChild(action);
			}
		}
	}
}
