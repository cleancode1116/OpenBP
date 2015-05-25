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
package org.openbp.guiclient.model.item.itemfilter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.openbp.common.CollectionUtil;
import org.openbp.core.model.item.Item;

/**
 * Item text filter.
 * The item type filter filters items according to a full text search in the
 * item name, item display text and item description.<br>
 * The user may enter the text to search in a text field of the configuration
 * component of the filter. Each identifier separated by space will be considered
 * a text filter criterium. All criterias will be related by an 'and' condition,
 * i.e. given the search string "array sort" will match all item that have the
 * words "array" and "sort" appear in their description, display text or name.
 * The comparison is case-insensitive.
 *
 * @author Heiko Erhardt
 */
public class ItemTextFilter extends AbstractItemFilter
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** List of full text search strings (contains String objects) */
	private String [] patternList;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Configuration component containing the item type toolbar */
	private JPanel configurationComponent;

	/** Text field for search pattern */
	private JTextField textField;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ItemTextFilter()
	{
	}

	//////////////////////////////////////////////////
	// @@ ItemFilter implementation
	//////////////////////////////////////////////////

	/**
	 * Determines if an item is accepted by this filter.
	 *
	 * @param item Item to check
	 * @return
	 * true: The item is accepted by the filter<br>
	 * false: The filter rejects this item
	 */
	public boolean acceptsItem(Item item)
	{
		if (patternList == null)
		{
			// No filter active, accept all
			return true;
		}

		String name = item.getName();
		String description = item.getDescription();
		String displayName = item.getDisplayName();

		for (int i = 0; i < patternList.length; ++i)
		{
			String pattern = patternList [i];

			if (!match(name, pattern) && !match(description, pattern) && !match(displayName, pattern))
			{
				// Pattern does not match one of the texts of the item
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets the configuration component that can be used to configure the installed item filters.
	 *
	 * @return The component or null if the item filter does not provide one
	 */
	public JComponent getConfigurationComponent()
	{
		if (configurationComponent == null)
		{
			textField = new JTextField();

			textField.addFocusListener(new FocusListener()
			{
				public void focusGained(FocusEvent e)
				{
				}

				public void focusLost(FocusEvent e)
				{
					processFilterSettings();
				}
			});
			textField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					processFilterSettings();
				}
			});

			JLabel label = new JLabel(getItemFilterResource().getRequiredString("pattern"));
			label.setBorder(new EmptyBorder(0, 0, 0, 5));

			configurationComponent = new JPanel(new BorderLayout());

			configurationComponent.add(label, BorderLayout.WEST);
			configurationComponent.add(textField);
		}

		return configurationComponent;
	}

	/**
	 * Checks if the filter is active.
	 * The filter is active if it contains at least one filtered item type.
	 * @nowarn
	 */
	public boolean isActive()
	{
		return patternList != null;
	}

	/**
	 * Activates or deactivates the filter.
	 * @nowarn
	 */
	public void setActive(boolean active)
	{
		if (!active)
		{
			if (patternList != null)
			{
				patternList = null;
				textField.setText(null);
				apply();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Processes the current filter settings.
	 */
	void processFilterSettings()
	{
		patternList = null;

		// Get the filter criteria from the text field
		String text = textField.getText();
		if (text.length() != 0)
		{
			ArrayList list = new ArrayList();
			StringTokenizer st = new StringTokenizer(text);
			while (st.hasMoreTokens())
			{
				String token = st.nextToken();
				list.add(token.toLowerCase());
			}

			patternList = CollectionUtil.toStringArray(list);
		}

		// Re-apply the filter
		apply();
	}

	/**
	 * Checks if the text matches the pattern.
	 *
	 * @param text Text
	 * @param pattern Pattern
	 * @return
	 * true: The pattern appears at some position in the text (lowercase comparison)<br>
	 * false: The text does not match the pattern
	 */
	public boolean match(String text, String pattern)
	{
		if (text == null)
			return false;

		int tl = text.length();
		int pl = pattern.length();
		int n = tl - pl;

		for (int i = 0; i <= n; ++i)
		{
			if (text.regionMatches(true, i, pattern, 0, pl))
				return true;
		}

		return false;
	}
}
