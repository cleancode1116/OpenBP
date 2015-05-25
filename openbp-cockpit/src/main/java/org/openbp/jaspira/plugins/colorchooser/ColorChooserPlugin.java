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
package org.openbp.jaspira.plugins.colorchooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.openbp.common.icon.MultiIcon;
import org.openbp.common.icon.MultiImageIcon;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.swing.layout.UnitLayout;
import org.openbp.swing.layout.VerticalFlowLayout;
import org.openbp.swing.plaf.sky.ShadowBorder;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Simple color chooser plugin which selects a Color and supports drag and drop of the color.
 *
 * @author Jens Ferchland
 */
public class ColorChooserPlugin extends AbstractVisiblePlugin
	implements HierarchyListener
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Dimension of the color palette in pixels */
	private static final int PALETTE_DIMENSION = 200;

	/** Size of the preview panel */
	private static final Dimension PREVIEW_SIZE = new Dimension(50, PALETTE_DIMENSION + 3);

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Color chooser component */
	private JColorChooser chooser;

	/** Preview panel */
	private DefaultPreviewPanel preview;

	/** Help text to be displayed below chooser */
	private JTextArea helpTextArea;

	/** The panel containing all components of this plugin. */
	private JPanel mainPanel;

	/** Help text */
	private String helpText;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	protected void initializeComponents()
	{
		// We will initialize the chooser components when the plugin is visible in order to work around
		// a JColorChooser redraw bug (the color chooser repaints the HSB chooser panel in a separate thread,
		// which may occur also if the chooser plugin is not displayed currently).
	}

	/**
	 * Performs the initialization of the chooser components.
	 */
	protected void doInit()
	{
		if (chooser != null && getContentPane().isShowing())
			return;

		// Set up the color chooser component
		chooser = new JColorChooser();

		// Remove all chooser panel except the HSB panel
		AbstractColorChooserPanel [] panels = chooser.getChooserPanels();
		for (int i = 0; i < panels.length; i++)
		{
			if (!panels [i].getClass().getName().endsWith("HSBChooserPanel"))
			{
				chooser.removeChooserPanel(panels [i]);
			}
		}

		// The chooser doesn't center itself correctly, so move it down a little by force
		chooser.setBorder(new EmptyBorder(7, 0, 0, 0));

		// Set up the preview component
		preview = new DefaultPreviewPanel();
		preview.setMinimumSize(PREVIEW_SIZE);
		preview.setMinimumSize(PREVIEW_SIZE);
		preview.setMaximumSize(PREVIEW_SIZE);
		preview.setPreferredSize(PREVIEW_SIZE);
		preview.setSize(PREVIEW_SIZE);
		preview.setBorder(new ShadowBorder());

		// Connect the preview panel to the chooser; it's important to do this
		// before adding the preview to the plugin
		chooser.setPreviewPanel(preview);

		// Create the help text area (two rows high)
		helpTextArea = new JTextArea(2, 0);
		helpTextArea.setEditable(false);
		helpTextArea.setLineWrap(true);
		helpTextArea.setWrapStyleWord(true);
		helpTextArea.setBorder(new EmptyBorder(0, 10, 10, 5));
		helpTextArea.setText(helpText);

		// The preview component will be displayed centered in order to match the color chooser's palette position
		JPanel previewPanel = new JPanel(new UnitLayout(UnitLayout.CENTER, UnitLayout.TOP));
		previewPanel.setBorder(new EmptyBorder(19, 5, 5, 5));
		previewPanel.add(preview);

		// Add preview to the left, chooser to fill
		JPanel chooserPanel = new JPanel(new BorderLayout());
		chooserPanel.add(BorderLayout.CENTER, chooser);
		chooserPanel.add(BorderLayout.WEST, previewPanel);

		// The width of the help text should not be more than the chooser width.
		// The height is computed by the JTextArea based on the number of rows needed for the text.
		Dimension chooserSize = chooserPanel.getPreferredSize();
		helpTextArea.setPreferredSize(new Dimension(chooserSize.width, 0));

		mainPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
		mainPanel.add(chooserPanel);
		mainPanel.add(helpTextArea);
		setComponentBackground(mainPanel, SkyTheme.COLOR_BACKGROUND_LIGHT);

		// Wrap it all in a scroll pane because the chooser requires at least a height of 256
		final JScrollPane sp = new JScrollPane(mainPanel);
		getContentPane().add(sp);

		// Position to start of page
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				sp.getViewport().setViewPosition(new Point(0, 0));
			}
		});
	}

	/**
	 * Called after the plugin has been displayed.
	 */
	public void pluginShown()
	{
		super.pluginShown();

		doInit();
	}

	/**
	 * Sets the background color for the given component and all its sub components.
	 *
	 * @param comp Component
	 * @param background Background color to set
	 */
	private void setComponentBackground(Component comp, Color background)
	{
		comp.setBackground(background);

		if (comp instanceof Container)
		{
			Component [] comps = ((Container) comp).getComponents();
			for (int i = 0; i < comps.length; ++i)
			{
				setComponentBackground(comps [i], background);
			}
		}
	}

	/**
	 * Sets the help text to be displayed below chooser.
	 * @nowarn
	 */
	public void setHelpText(String helpText)
	{
		this.helpText = helpText;
		if (helpTextArea != null)
		{
			helpTextArea.setText(helpText);
		}
	}

	/**
	 * Event module.
	 */
	public class Events extends EventModule
	{
		public String getName()
		{
			return "colorchooser";
		}

		/**
		 * Event handler: Set the current color of the chooser.
		 *
		 * @event colorchooser.setcolor
		 * @eventobject Color to set (java.awt.Color)
		 *
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode setcolor(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof Color)
			{
				Color color = (Color) o;
				if (chooser != null)
				{
					chooser.setColor(color);
				}

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: Set the help text of the chooser.
		 *
		 * @event colorchooser.sethelptext
		 * @eventobject Text to set or null to clear (String)
		 *
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode sethelptext(JaspiraEvent je)
		{
			Object o = je.getObject();

			if (o instanceof String)
			{
				setHelpText((String) o);

				return EVENT_CONSUMED;
			}

			return EVENT_IGNORED;
		}
	}

	/**
	 * Creates a new drag icon based on the given color.
	 *
	 * @param color Icon color
	 *
	 * @return The new multi icon
	 */
	public static MultiIcon createColorDragIcon(final Color color)
	{
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();

		if (g instanceof Graphics2D)
		{
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		g.setColor(color);
		g.fillRoundRect(0, 0, 31, 31, 16, 16);
		g.setColor(Color.BLACK);
		g.drawRoundRect(0, 0, 31, 31, 16, 16);

		return new MultiImageIcon(new ImageIcon(image));
	}
}
