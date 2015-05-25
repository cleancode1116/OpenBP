/*
 * Created on 24.08.2008
 *
 * Copyright (c) 2005 Giesecke & Devrient GmbH.
 * All rights reserved. Use is subject to licence terms.
 */
package org.openbp.cockpit.modeler;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openbp.cockpit.modeler.custommodelobjectconfigurator.CustomModelObjectConfigurator;
import org.openbp.cockpit.modeler.custommodelobjectconfigurator.CustomModelObjectConfiguratorDescriptor;
import org.openbp.core.CoreConstants;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.process.ActivityNode;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.swing.components.JStandardDialog;
import org.openbp.swing.layout.VerticalFlowLayout;

/**
 * Test sample for an activity configurator.
 * The sample will display two properties that are saved to the design time attributes
 * of the node for any activity node that is not a System model activity.
 */
public class TestActivityConfigurator
	implements CustomModelObjectConfigurator
{
	/**
	 * Default constructor.
	 */
	public TestActivityConfigurator()
	{
	}

	/**
	 * Checks if this configurator applies for the given model object
	 *
	 * @param mo Model object to edit
	 * @return The descriptor that specifies the popup menu option to create
	 * or null if the configurator does not apply to this type of model object
	 */
	public CustomModelObjectConfiguratorDescriptor checkAppliance(ModelObject mo)
	{
		if (mo instanceof ActivityNode)
		{
			HandlerDefinition handler = ((ActivityNode) mo).getActivityHandlerDefinition();

			String className = handler.getHandlerClassName();
			if (className != null && ! className.contains("." + CoreConstants.SYSTEM_MODEL_NAME.toLowerCase() + "."))
			{
				int i = className.lastIndexOf('.');
				if (i >= 0)
				{
					className = className.substring(i + 1);
				}

				CustomModelObjectConfiguratorDescriptor desc = new CustomModelObjectConfiguratorDescriptor();
				desc.setName("testactivityconfigurator");
				desc.setDisplayName(className + " Properties");
				desc.setDescription("Configuration dialog for " + className + " properties");
				return desc;
			}
		}
		return null;
	}

	private static final String TEST_PARAM1 = "TestParam1";
	private static final String TEST_PARAM2 = "TestParam2";

	/**
	 * Displays the activity configuration dialog.
	 * This method will be called if the user opts to execute the configuration menu option provided by this configurator.
	 *
	 * @param mo Model object to edit
	 * @return true if the method has modified some property of the activity, false otherwise.
	 * In the former case, the process will be marked as 'modified'.
	 */
	public boolean displayActivityConfigurationDialog(ModelObject mo)
	{
		JFrame activeFrame = ApplicationUtil.getActiveWindow();
		JStandardDialog dialog = new JStandardDialog(activeFrame, true);
		dialog.setTitle("Activity Configurator Test");

		JPanel p1 = new JPanel(new BorderLayout());
		JLabel l1 = new JLabel("Param 1");
		JTextField t1 = new JTextField();
		t1.setText((String) mo.getDesignTimeAttribute(TEST_PARAM1));
		p1.add(l1, BorderLayout.WEST);
		p1.add(t1, BorderLayout.CENTER);

		JPanel p2 = new JPanel(new BorderLayout());
		JLabel l2 = new JLabel("Param 2");
		JTextField t2 = new JTextField();
		t2.setText((String) mo.getDesignTimeAttribute(TEST_PARAM2));
		p2.add(l2, BorderLayout.WEST);
		p2.add(t2, BorderLayout.CENTER);

		JPanel pMain = new JPanel(new VerticalFlowLayout());
		pMain.add(p1);
		pMain.add(p2);

		dialog.getMainPane().add(pMain);

		dialog.pack();
		dialog.setVisible(true);

		if (! dialog.isCancelled())
		{
			mo.setDesignTimeAttribute(TEST_PARAM1, t1.getText());
			mo.setDesignTimeAttribute(TEST_PARAM2, t2.getText());
			return true;
		}

		return false;
	}
}
