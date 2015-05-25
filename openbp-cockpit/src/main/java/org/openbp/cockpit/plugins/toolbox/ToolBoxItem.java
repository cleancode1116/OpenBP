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
package org.openbp.cockpit.plugins.toolbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.icon.MultiIcon;
import org.openbp.common.icon.MultiImageIcon;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraPopupMenu;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.gui.interaction.BreakoutBoxEntry;
import org.openbp.jaspira.gui.interaction.DragInitiator;
import org.openbp.jaspira.gui.interaction.DragOrigin;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.swing.plaf.sky.SimpleBorder;

/**
 * This is a ToolBoxItem which is displayed in a ToolBoxPlugin.
 *
 * The Item implements starts Drag and Drop with the given TransferHandler.
 *
 * @author Jens Ferchland
 */
public class ToolBoxItem extends JPanel
	implements DragOrigin, BreakoutBoxEntry
{
	//////////////////////////////////////////////////
	// @@ memebers
	//////////////////////////////////////////////////

	/** Transfer object of the tool bar item */
	private Transferable transferable;

	/** Item title */
	private String title;

	/** Icon */
	private Icon icon;

	/** Key for retrieval of the icon from the icon model (in case of skin changes) */
	private String iconModelKey;

	/** Importer that will be used to import the item into the drag target */
	private Importer importer;

	/** Rectangle of the item cirecle in the breakout menu */
	private Rectangle rect;

	/** Toolbox this item belongs to */
	private ToolBoxPlugin toolbox;

	/** Fixed size of the item */
	private static final Dimension FIXEDSIZE = new Dimension(40, 40);

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * @param title Item title
	 * @param iconModelKey Key for retrieval of the icon from the icon model (in case of skin changes)
	 * @param description Description text of the item
	 * @param transferable Transfer object of the tool bar item
	 */
	public ToolBoxItem(String title, String iconModelKey, String description, Transferable transferable)
	{
		this(title, ItemIconMgr.getInstance().getIcon(null, iconModelKey, FlexibleSize.UNDETERMINED), description, transferable);
		this.iconModelKey = iconModelKey;
	}

	/**
	 * Constructor.
	 * @param title Item title
	 * @param icon Icon representing the item
	 * @param description Description text of the item
	 * @param transferable Transfer object of the tool bar item
	 */
	public ToolBoxItem(String title, Icon icon, String description, Transferable transferable)
	{
		this.title = title;
		this.icon = icon;
		this.transferable = transferable;

		setLayout(new BorderLayout());
		setBorder(SimpleBorder.getStandardBorder());

		setMinimumSize(FIXEDSIZE);
		setMaximumSize(FIXEDSIZE);
		setPreferredSize(FIXEDSIZE);

		DragInitiator.makeDraggable(this, this);

		addMouseListener(new TBMouseListener());

		initComponent();

		// Build the tooltip text
		StringBuffer output = new StringBuffer("<html>");
		if (title != null)
		{
			output.append("<b>");
			output.append(title);
			output.append("</b>");
		}
		if (description != null)
		{
			if (title != null)
			{
				output.append("<br><hr>");
			}

			for (StringTokenizer tok = new StringTokenizer(description, "\n"); tok.hasMoreTokens();)
			{
				output.append(tok.nextToken());

				if (tok.hasMoreElements())
				{
					output.append("<br>");
				}
			}
		}
		output.append("</html>");
		setToolTipText(output.toString());
	}

	/**
	 * Returns a string representation of this object.
	 * @nowarn
	 */
	public String toString()
	{
		return super.toString() + " (title = " + title + ")";
	}

	/**
	 * Initializes the component with the icon and text.
	 */
	public void initComponent()
	{
		removeAll();

		if (icon instanceof MultiIcon)
		{
			((MultiIcon) icon).setIconSize(FlexibleSize.MEDIUM);
		}
		add(new JLabel(icon));

		JLabel label = new JLabel(title, JLabel.CENTER);
		Font font = label.getFont();
		font = font.deriveFont(9f);
		label.setFont(font);

		add(label, BorderLayout.SOUTH);
	}

	/**
	 * Updates the icon of the toolbox item if it has been retrieved from the icon model in the case of skin changes.
	 *
	 * @param skinName Skin name
	 */
	public void updateModelIcon(String skinName)
	{
		if (iconModelKey != null)
		{
			icon = ItemIconMgr.getInstance().getIcon(skinName, iconModelKey, FlexibleSize.UNDETERMINED);
			initComponent();
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the transfer object of the tool bar item.
	 * @nowarn
	 */
	public Transferable getTransferable()
	{
		return transferable;
	}

	/**
	 * Gets the toolbox this item belongs to.
	 * @nowarn
	 */
	public ToolBoxPlugin getToolbox()
	{
		return toolbox;
	}

	/**
	 * Sets the toolbox this item belongs to.
	 * @nowarn
	 */
	public void setToolbox(ToolBoxPlugin toolbox)
	{
		this.toolbox = toolbox;
	}

	/**
	 * Gets the key for retrieval of the icon from the icon model (in case of skin changes).
	 * @nowarn
	 */
	public String getIconModelKey()
	{
		return iconModelKey;
	}

	/**
	 * Sets the key for retrieval of the icon from the icon model (in case of skin changes).
	 * @nowarn
	 */
	public void setIconModelKey(String iconModelKey)
	{
		this.iconModelKey = iconModelKey;
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Popupmenu
	/////////////////////////////////////////////////////////////////////////

	/**
	 * MouseListener to intercept popup requests and doubleclicks.
	 */
	public class TBMouseListener extends MouseAdapter
	{
		/**
		 * Checks if the pressed mouse button is the right button, if so initiate popup menu.
		 * @nowarn
		 */
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger() && !(toolbox instanceof StandardToolBoxPlugin))
			{
				InteractionEvent iae = new InteractionEvent(toolbox, InteractionEvent.POPUP, transferable);
				toolbox.fireEvent(iae);
				JaspiraPopupMenu menu = iae.createPopupMenu();

				if (toolbox.acceptDrop())
				{
					// Add the 'Remove entry' action
					if (menu == null)
					{
						menu = new JaspiraPopupMenu(title);
					}
					else
					{
						menu.addSeparator();
					}

					menu.add(new JaspiraAction(toolbox, "toolbox.remove")
					{
						public void actionPerformed(ActionEvent ae)
						{
							toolbox.removeToolBoxItem(ToolBoxItem.this);
							toolbox.refreshContent();
						}
					});
				}

				menu.show(ToolBoxItem.this, e.getPoint().x, e.getPoint().y);
			}
		}

		/**
		 * Checks if we have a double click, if so invoke the association mechanism.
		 * @nowarn
		 */
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				JaspiraEventMgr.fireGlobalEvent("plugin.association.open", transferable);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////
	// @@ Drag Support
	/////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#canDrag()
	 */
	public boolean canDrag()
	{
		return true;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropAccepted(Transferable t)
	 */
	public void dropAccepted(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropCanceled(Transferable t)
	 */
	public void dropCanceled(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#dropPerformed(Transferable t)
	 */
	public void dropPerformed(Transferable t)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#getTranferableAt(Point)
	 */
	public Transferable getTranferableAt(Point p)
	{
		return getTransferable();
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.DragOrigin#getDragImage()
	 */
	public MultiIcon getDragImage()
	{
		return getIcon();
	}

	//////////////////////////////////////////////////
	// @@ implementation of BreakoutBoxEntry
	//////////////////////////////////////////////////

	private static Color transred = new Color(0f, 0f, 1f, 0.2f);

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#draw(Graphics)
	 */
	public void draw(Graphics g)
	{
		g.setColor(transred);
		g.fillOval(rect.x - 5, rect.y - 5, rect.width + 10, rect.height + 10);

		Icon icon = getIcon().getIcon(FlexibleSize.MEDIUM);
		int iconWidth = icon.getIconWidth();
		int iconHeight = icon.getIconHeight();

		icon.paintIcon(null, g, rect.x + (rect.width - iconWidth) / 2, rect.y + (rect.height - iconHeight) / 2);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#getDescription()
	 */
	public String getDescription()
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#getIcon()
	 */
	public MultiIcon getIcon()
	{
		if (icon instanceof MultiIcon)
		{
			return (MultiIcon) icon;
		}
		else if (icon instanceof ImageIcon)
		{
			return new MultiImageIcon((ImageIcon) icon);
		}
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#getImporter()
	 */
	public Importer getImporter()
	{
		return importer;
	}

	/**
	 * Sets the Importer of this BreakoutBoxEntry
	 */
	public void setImporter(Importer importer)
	{
		this.importer = importer;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#getTitle()
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#importData(Point)
	 */
	public boolean importData(Point dropPoint)
	{
		if (importer != null)
		{
			return importer.importData(getTransferable(), dropPoint);
		}
		return false;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#reactsOn(int, int)
	 */
	public boolean reactsOn(int x, int y)
	{
		return rect.contains(x, y);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#setLocationOnGlassPanel(Rectangle)
	 */
	public void setLocationOnGlassPanel(Rectangle r)
	{
		rect = r;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutBoxEntry#setLocationOnGlassPanel(Rectangle)
	 */
	public Rectangle getLocationOnGlassPanel()
	{
		return rect;
	}
}
