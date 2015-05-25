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
package org.openbp.jaspira.option.widget;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.jaspira.propertybrowser.ObjectChangeListener;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;

/**
 * The tree table widget provides to manage more than one option
 * with one component.
 *
 * @author Andreas Putz
 */
public class PropertyBrowserWidget extends OptionWidget
	implements ObjectChangeListener
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Component that will be returned */
	private JScrollPane scrollPane;

	/** Property browser as widget component */
	private PropertyBrowserImpl propertyBrowser;

	/** Number of levels that should be expanded */
	private int expandLevels;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param option Option the widget refers to
	 * @param resourceCollection Resource to use for the property browser or null for the property browser's default resource
	 * @param expandLevels Tree level to expand automatically
	 */
	public PropertyBrowserWidget(Option option, ResourceCollection resourceCollection, int expandLevels)
	{
		super(option);
		this.expandLevels = expandLevels;

		propertyBrowser = new PropertyBrowserImpl(null, resourceCollection);
		propertyBrowser.setPreferredScrollableViewportSize(new Dimension(100, 200));
		propertyBrowser.addObjectChangeListener(this);

		scrollPane = new JScrollPane(propertyBrowser);
	}

	/**
	 * @see org.openbp.jaspira.propertybrowser.ObjectChangeListener#objectChanged(Object, Object)
	 */
	public void objectChanged(Object original, Object modified)
	{
		notifyOptionMgrOfOptionChange();
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Set the dimension of the widget.
	 *
	 * @param dimension The dimension
	 */
	public void setSize(Dimension dimension)
	{
		propertyBrowser.setPreferredScrollableViewportSize(dimension);
	}

	//////////////////////////////////////////////////
	// @@ OptionWidget implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.option.OptionWidget#getValue()
	 */
	public Object getValue()
	{
		Object retVal = propertyBrowser.getModifiedObject();
		if (retVal == null)
			retVal = propertyBrowser.getObject();
		return retVal;
	}

	/**
	 * @see org.openbp.jaspira.option.OptionWidget#setValue(Object)
	 */
	public void setValue(Object o)
	{
		try
		{
			propertyBrowser.setObject(o, false);
			propertyBrowser.expandTreeLevels(true, expandLevels);
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		catch (XMLDriverException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see org.openbp.jaspira.option.OptionWidget#getWidgetComponent()
	 */
	public JComponent getWidgetComponent()
	{
		return scrollPane;
	}
}
