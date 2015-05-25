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
package org.openbp.cockpit.plugins.infopanel;

import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.common.string.TextUtil;
import org.openbp.core.model.ModelObject;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;

/**
 * Info panel plugin.
 * Displays HTML text in a browser panel, just like a tooltip window.
 *
 * @author Heiko Erhardt
 */
public class InfoPanelPlugin extends AbstractVisiblePlugin
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Text panel */
	private JEditorPane panel;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * Constructor.
	 */
	public InfoPanelPlugin()
	{
	}

	protected void initializeComponents()
	{
		panel = new JEditorPane();
		panel.setContentType("text/html");
		panel.setEditable(false);

		JLabel label = new JLabel("Dummy");
		Font font = label.getFont();
		font = font.deriveFont(10f);
		panel.setFont(font);

		JScrollPane scrollPane = new JScrollPane(panel);
		getContentPane().add(scrollPane);
	}

	//////////////////////////////////////////////////
	// @@ Event module
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "plugin.infopanel";
		}

		//////////////////////////////////////////////////
		// @@ Global event handlers
		//////////////////////////////////////////////////

		/**
		 * Event method: Sets the text to be displayed (or null to clear).
		 *
		 * @event plugin.infopanel.setinfotext
		 * @param je The event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode setinfotext(JaspiraEvent je)
		{
			Object o = je.getObject();

			String text = null;

			if (o instanceof DisplayObject)
			{
				DisplayObject dob = (DisplayObject) o;

				String typeName = null;
				if (o instanceof ModelObject)
				{
					typeName = ((ModelObject) o).getModelObjectTypeName();
					if (typeName != null)
					{
						typeName = " (" + typeName + ")";
					}
				}

				String description = dob.getDescription();
				if (description != null)
				{
					description = "&nbsp;\n" + description;
				}

				text = TextUtil.convertToHTML(new String [] { dob.getDisplayText(), typeName, description }, true, -1, -1);
			}
			else if (o instanceof String)
			{
				text = (String) o;
			}

			if (text != null)
			{
				int i1 = text.indexOf('>');
				int i2 = text.lastIndexOf('<');
				if (i1 > 0 && i2 > i1)
				{
					StringBuffer sb = new StringBuffer();
					sb.append("<html>");

					sb.append("<head>");

					sb.append("<style type=\"text/css\">");

					sb.append("body ");
					sb.append("{");
					sb.append("font-family : Arial, Helvetica, sans-serif;");
					sb.append("font-size : 10px;");
					sb.append("}");

					sb.append("td");
					sb.append("{");
					sb.append("font-family : Arial, Helvetica, sans-serif;");
					sb.append("font-size : 10px;");
					sb.append("}");

					sb.append("ul");
					sb.append("{");
					sb.append("margin-left : 0.4cm");
					sb.append("}");

					sb.append("H1 ");
					sb.append("{");
					sb.append("font-size : 18px;");
					sb.append("font-style : italic;");
					sb.append("color : #FF6E15;");
					sb.append("}");

					sb.append("H2 ");
					sb.append("{");
					sb.append("font-size : 15px;");
					sb.append("font-style : italic;");
					sb.append("color : #0080ff");
					sb.append("}");

					sb.append("H3 ");
					sb.append("{");
					sb.append("font-size : 15px;");
					sb.append("font-style : italic;");
					sb.append("color : #FF6E15;");
					sb.append("}");

					sb.append("</style>");

					sb.append("</head>");

					sb.append("<body>");

					sb.append(text);

					sb.append("</body>");
					sb.append("</html>");
					text = sb.toString();
				}
			}

			final String panelText = text;
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{

					panel.setText(panelText);
					panel.setCaretPosition(0);
				}
			});

			return EVENT_CONSUMED;
		}

		/**
		 * Event method: Clears the info text.
		 *
		 * @event plugin.infopanel.clearinfotext
		 * @param je The event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode clearinfotext(JaspiraEvent je)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					// Issue a warning message that the Cockpit needs to be restarted
					if (!panel.isFocusOwner())
					{
						panel.setText(null);
					}
				}
			});

			return EVENT_CONSUMED;
		}
	}
}
