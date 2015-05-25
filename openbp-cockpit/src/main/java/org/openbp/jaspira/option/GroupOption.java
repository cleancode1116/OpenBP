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
package org.openbp.jaspira.option;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.string.StringUtil;

/**
 * An empty option containing several other options.
 *
 * @author Jens Ferchland
 */
public class GroupOption extends Option
{
	/** Border for spacing the option components from other components */
	private static final Border BORDER_OUTER = new EmptyBorder(5, 5, 5, 5);

	/** Border for spacing the option components from their title */
	private static final Border BORDER_INNER = new EmptyBorder(3, 5, 5, 5);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 */
	public GroupOption(ResourceCollection res, String optionName)
	{
		super(res, optionName, null);
	}

	/**
	 * Value constructor.
	 *
	 * @param optionName Name of the option
	 * @param displayName Display name of the option
	 * @param description Description of the option
	 * @param parent Option parent or null
	 * @param prio Priority of the option
	 */
	public GroupOption(String optionName, String displayName, String description, Option parent, int prio)
	{
		super(optionName, displayName, description, null, TYPE_GROUP, parent, prio);
	}

	/**
	 * @see org.openbp.jaspira.option.Option#createOptionWidget()
	 */
	public OptionWidget createOptionWidget()
	{
		return new GroupWidget();
	}

	/**
	 * @see org.openbp.jaspira.option.Option#saveToString()
	 */
	public String saveToString()
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.option.Option#loadFromString(String)
	 */
	public Object loadFromString(String cryptString)
	{
		return null;
	}

	//////////////////////////////////////////////////
	// @@ Private methods
	//////////////////////////////////////////////////

	/**
	 * Returns a description of all option settings in this page.
	 *
	 * @return String
	 */
	public String getDescription()
	{
		StringBuffer description = new StringBuffer(200);
		boolean group = getType().equals(TYPE_GROUP);

		if (group)
		{
			StringUtil.append(description, "<html>", "<head>", "<style type=\"text/css\">", "<!-- body {font-family:Helvetica,Arial,sans-serif;} -->");
			StringUtil.append(description, "</style>", "</head>", "<body>");
			StringUtil.append(description, "<b> ", getDisplayName(), "</b><br>\n", "<table>\n");
		}
		else
		{
			StringUtil.append(description, "<b> ", getDisplayName(), "</b><br>\n", "<table>\n");
		}

		for (Iterator it = getOptionChildren().iterator(); it.hasNext();)
		{
			Option opt = (Option) it.next();
			if (opt.getType().equals(TYPE_OPTION))
			{
				StringUtil.append(description, " <tr><td> ", opt.getDisplayName(), "</td> <td>", opt.getDescription(), " </td></tr>\n");
			}
			else
			{
				StringUtil.append(description, " <tr>", opt.getDescription(), " </td></tr>\n");
			}
		}

		if (group)
		{
			StringUtil.append(description, "</table>\n", "</body>", "</html>\n");
		}
		else
		{
			description.append("</table>\n");
		}

		return description.toString();
	}

	//////////////////////////////////////////////////
	// @@ Option widget of the group option
	//////////////////////////////////////////////////

	public class GroupWidget extends OptionWidget
	{
		/** Widget component to be returned */
		private Box box;

		/**
		 * Constructor.
		 */
		public GroupWidget()
		{
			super(GroupOption.this);
		}

		/**
		 * @see org.openbp.jaspira.option.OptionWidget#getValue()
		 */
		public Object getValue()
		{
			return null;
		}

		/**
		 * @see org.openbp.jaspira.option.OptionWidget#setValue(java.lang.Object)
		 */
		public void setValue(Object o)
		{
		}

		/**
		 * Creates the component that will be used as page for the options of this group.
		 * @see org.openbp.jaspira.option.OptionWidget#getWidgetComponent()
		 */
		public JComponent getWidgetComponent()
		{
			if (box == null)
			{
				// Vertically oriented box component
				box = Box.createVerticalBox();

				// Add all options of this group
				for (Iterator it = getOptionChildren().iterator(); it.hasNext();)
				{
					Option opt = (Option) it.next();

					OptionWidget widget = opt.getCachedOptionWidget();
					if (widget == null)
						continue;

					JComponent comp = widget.getWidgetComponent();
					if (comp == null)
						continue;

					comp.setOpaque(false);

					JPanel panel = new JPanel(new BorderLayout());
					panel.setOpaque(false);
					if (getType().equals(TYPE_SUB_GROUP))
					{
						panel.setBorder(BORDER_OUTER);
					}
					else
					{
						panel.setBorder(new CompoundBorder(BORDER_OUTER, new TitledBorder(opt.getDisplayName())));
					}

					comp.setBorder(new CompoundBorder(BORDER_INNER, comp.getBorder()));

					panel.add(comp);

					Dimension d = new Dimension(panel.getMaximumSize().width, panel.getMinimumSize().height);
					panel.setMaximumSize(d);

					box.add(panel);

					box.add(Box.createVerticalStrut(5));
				}

				box.add(Box.createVerticalGlue());
			}

			return box;
		}
	}
}
