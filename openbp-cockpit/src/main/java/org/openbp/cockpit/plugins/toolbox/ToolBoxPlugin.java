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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.openbp.cockpit.modeler.Modeler;
import org.openbp.cockpit.modeler.util.ModelerFlavors;
import org.openbp.common.icon.FlexibleSize;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.Item;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraMenuItem;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.interaction.BasicTransferable;
import org.openbp.jaspira.gui.interaction.BreakoutBoxEntry;
import org.openbp.jaspira.gui.interaction.BreakoutEvent;
import org.openbp.jaspira.gui.interaction.BreakoutProvider;
import org.openbp.jaspira.gui.interaction.Importer;
import org.openbp.jaspira.gui.interaction.InteractionClient;
import org.openbp.jaspira.gui.interaction.ViewDropRegion;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugins.colorchooser.ColorChooserPlugin;

/**
 * A generic Plugin that shows ToolBoxItems which can used for
 * Drag and Drop. By default the items can be draged into the box.
 *
 * You can use the box as clipboard.
 *
 * @author Jens Ferchland
 */
public abstract class ToolBoxPlugin extends AbstractVisiblePlugin
	implements InteractionClient, BreakoutProvider
{
	/** Id for DnD */
	private static final String MAINREGION = "main";

	/** Minimum size of the content */
	private static final Dimension FIXEDSIZE = new Dimension(50, 50);

	//////////////////////////////////////////////////
	// @@ members
	//////////////////////////////////////////////////

	/** Panel containing all tool bar items */
	private JPanel toolBoxItemPanel;

	/** message that is shown if the toolbox is empty */
	private JLabel emptyMessage;

	/** Scrollpane for the toolbox item panel - used for drag and drop */
	private JScrollPane scrollPane;

	/** Items contained in this toolbox (contains {@link ToolBoxItem} objects) */
	private List entries;

	/** Name of the toolbox */
	private String toolBoxTitle;

	/** Current skin */
	private String currentSkinName;

	//////////////////////////////////////////////////
	// @@ init
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin#initializeComponents()
	 */
	protected void initializeComponents()
	{
		entries = new ArrayList();

		emptyMessage = new JLabel(getPluginResourceCollection().getRequiredString("toolbox.empty"));

		// Create the toolbox item panel
		toolBoxItemPanel = new JPanel(new ToolBoxLayoutManager());
		toolBoxItemPanel.setOpaque(false);
		toolBoxItemPanel.add(emptyMessage);

		// Mouse listener for popup menu
		toolBoxItemPanel.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					// Create the popup menu for the toolbox
					InteractionEvent ie = new InteractionEvent(ToolBoxPlugin.this, "toolbox", this);
					fireEvent(ie);

					JPopupMenu pop = ie.createPopupMenu();

					if (canTitleChange())
					{
						// Add the 'change toolbox name' action
						if (pop == null)
						{
							pop = new JPopupMenu();
						}
						else
						{
							pop.addSeparator();
						}

						pop.add(new JaspiraMenuItem(new JaspiraAction(ToolBoxPlugin.this, "toolbox.changename")
						{
							public void actionPerformed(ActionEvent e)
							{
								String msg = getActionResource().getRequiredString("toolbox.dialog.changename.text");
								String s = JOptionPane.showInputDialog(null, msg);
								setToolBoxTitle(s);
							}
						}));
					}

					if (pop != null)
					{
						// Show the popup
						pop.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});

		// Add the scroll pane for the toolbox item panel
		scrollPane = new JScrollPane(toolBoxItemPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				toolBoxItemPanel.revalidate();
			}
		});
		getContentPane().add(scrollPane);

		// Display at least 50x50
		getContentPane().setMinimumSize(FIXEDSIZE);

		addStandardToolBoxItems();
	}

	//////////////////////////////////////////////////
	// @@ Toolbox content management
	//////////////////////////////////////////////////

	/**
	 * Adds the standard content of the toolbox.
	 * By default, the toolbox has no content.
	 */
	protected void addStandardToolBoxItems()
	{
	}

	/**
	 * Adds a process object to the toolbox.
	 *
	 * @param mo Object to add
	 * @param tooltipResourceName Tooltip resource name
	 */
	protected void addToolBoxItem(ModelObject mo, String tooltipResourceName)
	{
		if (mo instanceof Item)
		{
			((Item) mo).setRuntimeAttribute(Modeler.ATTRIBUTE_SKELETON, Boolean.TRUE);
		}
		addToolBoxItem(new ToolBoxItem(null, mo.getModelObjectSymbolName(), getPluginResourceCollection().getOptionalString(tooltipResourceName),
			new BasicTransferable(mo)));
	}

	/**
	 * Adds a toolbox item to the toolbox.
	 * @param item Item to add
	 */
	public void addToolBoxItem(ToolBoxItem item)
	{
		if (entries.size() == 0)
		{
			// the toolbox is empty and we display the empty message - remove it!
			toolBoxItemPanel.remove(emptyMessage);
		}

		toolBoxItemPanel.add(item);
		entries.add(item);

		item.setToolbox(this);
	}

	/**
	 * Removes a tool box item from the toolbox.
	 * @param item Item to remove; must have been added using {@link #addToolBoxItem(ToolBoxItem)}
	 */
	public void removeToolBoxItem(ToolBoxItem item)
	{
		toolBoxItemPanel.remove(item);
		entries.remove(item);

		if (entries.size() == 0)
		{
			// The toolbox is now empty - display a massage
			toolBoxItemPanel.add(emptyMessage);
		}

		item.setToolbox(null);
	}

	/**
	 * Clears the toolbox.
	 */
	public void clearToolbox()
	{
		entries.clear();

		// The toolbox is now empty - display a massage
		toolBoxItemPanel.removeAll();
		toolBoxItemPanel.add(emptyMessage);
	}

	/**
	 * Redisplays content.
	 */
	public void refreshContent()
	{
		scrollPane.getViewport().revalidate();
		scrollPane.getViewport().repaint();
	}

	/**
	 * Updates the icons of the toolbox items if they have been retrieved from the icon model in the case of skin changes.
	 */
	public void updateSkinIcons()
	{
		for (Iterator it = entries.iterator(); it.hasNext();)
		{
			ToolBoxItem toolBoxItem = (ToolBoxItem) it.next();
			toolBoxItem.updateModelIcon(currentSkinName);
		}
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the title of the ToolBox.
	 * @nowarn
	 */
	public void setToolBoxTitle(String title)
	{
		this.toolBoxTitle = title;

		updatePluginContainer(false);
	}

	/**
	 * Returns the title of the ToolBox.
	 * @nowarn
	 */
	public String getToolBoxTitle()
	{
		return toolBoxTitle;
	}

	//////////////////////////////////////////////////
	// @@ Plugin overrides
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.plugin.AbstractPlugin#getTitle()
	 */
	public String getTitle()
	{
		String s = super.getTitle();
		String tt = getToolBoxTitle();
		if (tt != null)
		{
			// Append the name of the toolbox
			s = s + ": " + tt;
		}
		return s;
	}

	/**
	 * Determines if the title of the toolbox can be changed by the user.
	 * @nowarn
	 */
	protected boolean canTitleChange()
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ InteractionClient implementation
	//////////////////////////////////////////////////

	/**
	 * If this method returns true all item flavor drops will be accepted.
	 * If you don't want to accept drop events override this method!<br>
	 * The default implementation returns true
	 * @nowarn
	 */
	protected boolean acceptDrop()
	{
		return true;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragActionTriggered(Object, Point)
	 */
	public void dragActionTriggered(Object regionId, Point p)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragEnded(Transferable)
	 */
	public void dragEnded(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#dragStarted(Transferable)
	 */
	public void dragStarted(Transferable transferable)
	{
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllDropRegions(List, Transferable, MouseEvent)
	 */
	public List getAllDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		return getDropRegions(flavors, data, mouseEvent);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getDropRegions(List, Transferable, MouseEvent)
	 */
	public List getDropRegions(List flavors, Transferable data, MouseEvent mouseEvent)
	{
		if (acceptDrop())
		{
			if (flavors.contains(ClientFlavors.ITEM) || flavors.contains(ModelerFlavors.COLOR))
				return Collections.singletonList(new ViewDropRegion(MAINREGION, this, SwingUtilities.getLocalBounds(toolBoxItemPanel),
					toolBoxItemPanel));
		}
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getImportersAt(Point)
	 */
	public List getImportersAt(Point p)
	{
		return null;
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#getAllImportersAt(Point)
	 */
	public List getAllImportersAt(Point p)
	{
		return getImportersAt(p);
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.InteractionClient#importData(Object, Transferable, Point)
	 */
	public boolean importData(Object regionId, Transferable data, Point p)
	{
		if (MAINREGION.equals(regionId))
		{
			DataFlavor[] flavors = data.getTransferDataFlavors();

			for (int i = 0; i < flavors.length; i++)
			{
				if (flavors[i].equals(ClientFlavors.ITEM))
				{
					try
					{
						Object o = data.getTransferData(ClientFlavors.ITEM);

						Item item = null;

						if (o instanceof Item)
						{
							item = (Item) o;

							addToolBoxItem(new ToolBoxItem(item.getDisplayText(), ItemIconMgr.getInstance().getIcon(item, FlexibleSize.MEDIUM), item
								.getDescriptionText(), new BasicTransferable(item)));
							refreshContent();
						}
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}

				if (flavors[i].equals(ModelerFlavors.COLOR))
				{
					try
					{
						Color c = (Color) data.getTransferData(ModelerFlavors.COLOR);

						ResourceCollection res = getPluginResourceCollection();
						StringBuffer description = new StringBuffer();
						description.append(res.getOptionalString("color.red"));
						description.append(' ');
						description.append(c.getRed());
						description.append('\n');
						description.append(res.getOptionalString("color.green"));
						description.append(' ');
						description.append(c.getGreen());
						description.append('\n');
						description.append(res.getOptionalString("color.blue"));
						description.append(' ');
						description.append(c.getBlue());

						addToolBoxItem(new ToolBoxItem(res.getOptionalString("color.title"), ColorChooserPlugin.createColorDragIcon(c).getIcon(
							FlexibleSize.MEDIUM), description.toString(), new BasicTransferable(c)));
						refreshContent();
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determines if the given key is accepted by the toolbox as break out mode key.
	 * @param key Key to check
	 * @return
	 * true: The key will initiate the break out mode for this toolbox.<br>
	 * false: The toolbox does not respond on this key.
	 */
	protected boolean acceptFlyWheelKey(int key)
	{
		return false;
	}

	//////////////////////////////////////////////////
	// @@ Layout manager
	//////////////////////////////////////////////////

	public class ToolBoxLayoutManager extends FlowLayout
	{
		public ToolBoxLayoutManager()
		{
			super(LEFT);
		}

		/**
		 * @see java.awt.FlowLayout#preferredLayoutSize(Container target)
		 */
		public Dimension preferredLayoutSize(Container target)
		{
			Insets ins = target.getInsets();

			Dimension parentDim = target.getParent().getSize();
			int maxWidth = parentDim.width - ins.left - ins.right - 2 * getHgap();

			Dimension d = new Dimension(parentDim.width, 0);

			int currentHeight = 0;
			int currentWidth = 0;
			boolean lineFull = false;

			int count = target.getComponentCount();
			for (int i = 0; i < count; i++)
			{
				Component comp = target.getComponent(i);
				Dimension cd = comp.getPreferredSize();

				lineFull = false;

				if (currentWidth + cd.width > maxWidth)
				{
					d.height += currentHeight + getVgap();
					currentWidth = 0;
					currentHeight = 0;
				}

				currentHeight = Math.max(currentHeight, cd.height);
				currentWidth += cd.width + getVgap();
			}

			if (! lineFull)
			{
				d.height += currentHeight;
			}

			d.height += getVgap();
			d.height = Math.max(d.height, parentDim.height);

			return d;
		}

		/**
		 * @see java.awt.FlowLayout#layoutContainer(Container target)
		 */
		public void layoutContainer(Container target)
		{
			Insets ins = target.getInsets();

			Dimension parentDim = target.getParent().getSize();

			int maxWidth = parentDim.width - ins.left - ins.right - getHgap();
			int currentWidth = getHgap();
			int currentHeight = getVgap();
			int heightdif = 0;

			int count = target.getComponentCount();
			for (int i = 0; i < count; i++)
			{
				Component c = target.getComponent(i);

				Dimension cd = c.getPreferredSize();

				if (currentWidth + cd.width > maxWidth)
				{
					currentWidth = getHgap();
					currentHeight += heightdif + getVgap();
					heightdif = 0;
				}

				c.setLocation(currentWidth, currentHeight);

				currentWidth += cd.width + getHgap();
				heightdif = Math.max(heightdif, cd.height);

				c.setSize(cd.width, cd.height);
			}
		}

		/**
		 * @see java.awt.FlowLayout#minimumLayoutSize(Container target)
		 */
		public Dimension minimumLayoutSize(Container target)
		{
			return preferredLayoutSize(target);
		}
	}

	/**
	 * @see org.openbp.jaspira.gui.interaction.BreakoutProvider#createBreakOutEntries(List)
	 */
	public BreakoutBoxEntry[] createBreakOutEntries(List importers)
	{
		List list = new ArrayList();

		for (Iterator it = importers.iterator(); it.hasNext();)
		{
			Importer importer = (Importer) it.next();

			DataFlavor[] flavors = importer.getFlavors();

			for (Iterator itEntries = entries.iterator(); itEntries.hasNext();)
			{
				ToolBoxItem item = (ToolBoxItem) itEntries.next();

				for (int i = 0; i < flavors.length; ++i)
				{
					if (item.getTransferable().isDataFlavorSupported(flavors[i]))
					{
						item.setImporter(importer);
						list.add(item);
					}
				}
			}
		}

		return (BreakoutBoxEntry[]) list.toArray(new BreakoutBoxEntry[list.size()]);
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
			return "toolbox.standard";
		}

		/**
		 * Event handler: Sets this toolbox as breakout provider.
		 *
		 * @event global.breakout.getprovider
		 * @param boe Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode global_breakout_getprovider(BreakoutEvent boe)
		{
			if (acceptFlyWheelKey(boe.getKey()))
			{
				boe.setProvider(ToolBoxPlugin.this);

				return EVENT_CONSUMED;
			}
			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has become active.
		 *
		 * @event modeler.view.activated
		 * @eventobject Modeler that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_activated(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentSkinName = ((Modeler) o).getDrawing().getProcessSkin().getName();

				updateSkinIcons();
				refreshContent();

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: The skin has been changed.
		 *
		 * @event modeler.view.skinchanged
		 * @eventobject Modeler that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_skinchanged(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentSkinName = ((Modeler) o).getDrawing().getProcessSkin().getName();

				updateSkinIcons();
				refreshContent();

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: A modeler view has been closed.
		 *
		 * @event modeler.view.closed
		 * @eventobject Modeler that owns the view ({@link Modeler})
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode modeler_view_closed(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Modeler)
			{
				currentSkinName = null;

				updateSkinIcons();
				refreshContent();

				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}
}
