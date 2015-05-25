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
package org.openbp.swing.plaf.sky;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.TransferHandler;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorChooserUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

/**
 * The LookAndFeel for a colorchooser. This is simle a copy of the
 * BasicColorChooserUI.
 *
 * This UI is used in the SkyLookAndFeel to fix some jdk2_1.4_1 problems.
 *
 * @author Jens Ferchland
 */
public class SkyColorChooserUI extends ColorChooserUI
{
	//////////////////////////////////////////////////
	// @@ Static members
	//////////////////////////////////////////////////

	/** default TransferHandler for ColorChooser */
	private static TransferHandler defaultTransferHandler = new ColorTransferHandler();

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** ColorChooser of this ui*/
	private JColorChooser chooser;

	/** Panel with all color panels installed at the chooser */
	private JTabbedPane tabbedPane;

	/** Simple panel if only on color panel is installed. */
	private JPanel singlePanel;

	/** Panel with the preview in the south of the chooser */
	private JPanel previewPanelHolder;

	/** the preview component */
	private JComponent previewPanel;

	/** MouseListener for the preview panel */
	private MouseListener previewMouseListener;

	/** all color chooser panels */
	protected AbstractColorChooserPanel [] defaultChoosers;

	/** ChangesListener for the preview componente */
	protected ChangeListener previewListener;

	/** PropertyCangeListener of the chooser */
	protected PropertyChangeListener propertyChangeListener;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Create a new SkyColorChooserUI
	 */
	public SkyColorChooserUI()
	{
		tabbedPane = new JTabbedPane();
		singlePanel = new JPanel(new BorderLayout());
	}

	//////////////////////////////////////////////////
	// @@ UI implementation
	//////////////////////////////////////////////////

	/**
	 * Returns a new SkyColorChooserUI for the given component.
	 * @nowarn
	 */
	public static ComponentUI createUI(JComponent c)
	{
		return new SkyColorChooserUI();
	}

	/**
	 * Creates all default color chooser panels.
	 * @return AbstractColorChooserPanel[] containig 3 panels:
	 *			one with buttons, an hsb panel and a rgb panel.
	 */
	protected AbstractColorChooserPanel [] createDefaultChoosers()
	{
		AbstractColorChooserPanel [] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
		return panels;
	}

	/**
	 * Uninstalls the default color panels.
	 */
	protected void uninstallDefaultChoosers()
	{
		for (int i = 0; i < defaultChoosers.length; i++)
		{
			chooser.removeChooserPanel(defaultChoosers [i]);
		}
	}

	/**
	 * Installs the UI at the component.
	 * @nowarn
	 */
	public void installUI(JComponent c)
	{
		chooser = (JColorChooser) c;

		super.installUI(c);

		installDefaults();
		installListeners();

		chooser.setLayout(new BorderLayout());

		defaultChoosers = createDefaultChoosers();
		chooser.setChooserPanels(defaultChoosers);

		previewPanelHolder = new JPanel(new BorderLayout());
		chooser.add(previewPanelHolder, BorderLayout.SOUTH);

		previewMouseListener = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (chooser.getDragEnabled())
				{
					TransferHandler th = chooser.getTransferHandler();
					th.exportAsDrag(chooser, e, TransferHandler.COPY);
				}
			}
		};

		installPreviewPanel();
		chooser.applyComponentOrientation(c.getComponentOrientation());
	}

	/**
	 * Uninstalls the UI from the component.
	 * @nowarn
	 */
	public void uninstallUI(JComponent c)
	{
		chooser.remove(tabbedPane);
		chooser.remove(singlePanel);
		chooser.remove(previewPanelHolder);

		uninstallListeners();
		uninstallDefaultChoosers();
		uninstallDefaults();

		previewPanelHolder.remove(previewPanel);
		if (previewPanel instanceof UIResource)
		{
			chooser.setPreviewPanel(null);
		}

		previewPanelHolder = null;
		previewPanel = null;
		defaultChoosers = null;
		chooser = null;
		tabbedPane = null;
	}

	/**
	 * Installs the previewpanel.
	 */
	protected void installPreviewPanel()
	{
		if (previewPanel != null)
		{
			previewPanelHolder.remove(previewPanel);
			previewPanel.removeMouseListener(previewMouseListener);
		}

		previewPanel = chooser.getPreviewPanel();
		if (previewPanel == null || previewPanel instanceof UIResource)
		{
			previewPanel = ColorChooserComponentFactory.getPreviewPanel();

			// get from table?
			chooser.setPreviewPanel(previewPanel);
		}
		previewPanel.setForeground(chooser.getColor());
		previewPanelHolder.add(previewPanel);
		previewPanel.addMouseListener(previewMouseListener);
	}

	/**
	 * Installs all defaults of the UI.
	 * The defaults are:
	 * <li> colors
	 * <li> font
	 * <li> handling (Transferhandler)
	 */
	protected void installDefaults()
	{
		LookAndFeel.installColorsAndFont(chooser, "ColorChooser.background", "ColorChooser.foreground", "ColorChooser.font");
		TransferHandler th = chooser.getTransferHandler();
		if (th == null || th instanceof UIResource)
		{
			chooser.setTransferHandler(defaultTransferHandler);
		}
	}

	/**
	 * Uninstalls the defaults. (Remove the Teransferhandler)
	 */
	protected void uninstallDefaults()
	{
		if (chooser.getTransferHandler() instanceof UIResource)
		{
			chooser.setTransferHandler(null);
		}
	}

	/**
	 * Installs all Listeners.
	 */
	protected void installListeners()
	{
		propertyChangeListener = createPropertyChangeListener();
		chooser.addPropertyChangeListener(propertyChangeListener);

		previewListener = new PreviewListener();
		chooser.getSelectionModel().addChangeListener(previewListener);
	}

	/**
	 * Creates the PropertyChangeListener for the chooser.
	 *
	 * @nowarn
	 */
	protected PropertyChangeListener createPropertyChangeListener()
	{
		return new PropertyHandler();
	}

	/**
	 * Uninstalls all Listeners.
	 */
	protected void uninstallListeners()
	{
		chooser.removePropertyChangeListener(propertyChangeListener);
		chooser.getSelectionModel().removeChangeListener(previewListener);
		previewPanel.removeMouseListener(previewMouseListener);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * A simple changelistener for the previepanel.
	 * Just to set the right color.
	 */
	class PreviewListener
		implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			ColorSelectionModel model = (ColorSelectionModel) e.getSource();
			if (previewPanel != null)
			{
				previewPanel.setForeground(model.getSelectedColor());
				previewPanel.repaint();
			}
		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug.
	 * This class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of <Foo>.
	 */
	public class PropertyHandler
		implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent e)
		{
			if (e.getPropertyName().equals(JColorChooser.CHOOSER_PANELS_PROPERTY))
			{
				AbstractColorChooserPanel [] oldPanels = (AbstractColorChooserPanel []) e.getOldValue();
				AbstractColorChooserPanel [] newPanels = (AbstractColorChooserPanel []) e.getNewValue();

				for (int i = 0; i < oldPanels.length; i++)
				{
					// remove old panels
					Container wrapper = oldPanels [i].getParent();
					if (wrapper != null)
					{
						Container parent = wrapper.getParent();
						if (parent != null)
							parent.remove(wrapper); // remove from hierarchy
						oldPanels [i].uninstallChooserPanel(chooser);

						// uninstall
					}
				}

				int numNewPanels = newPanels.length;
				if (numNewPanels == 0)
				{
					// removed all panels and added none
					chooser.remove(tabbedPane);
					return;
				}
				else if (numNewPanels == 1)
				{
					// one panel case
					chooser.remove(tabbedPane);
					JPanel centerWrapper = new JPanel(new BorderLayout());
					centerWrapper.add(newPanels [0]);
					singlePanel.add(centerWrapper, BorderLayout.CENTER);
					chooser.add(singlePanel);
				}
				else
				{
					// multi-panel case
					if (oldPanels.length < 2)
					{
						// moving from single to multiple
						chooser.remove(singlePanel);
						chooser.add(tabbedPane, BorderLayout.CENTER);
					}

					for (int i = 0; i < newPanels.length; i++)
					{
						JPanel centerWrapper = new JPanel(new BorderLayout());
						String name = newPanels [i].getDisplayName();
						int mnemonic = newPanels [i].getMnemonic();
						centerWrapper.add(newPanels [i]);
						tabbedPane.addTab(name, centerWrapper);
						if (mnemonic > 0)
						{
							tabbedPane.setMnemonicAt(i, mnemonic);
							tabbedPane.setDisplayedMnemonicIndexAt(i, newPanels [i].getDisplayedMnemonicIndex());
						}
					}
				}

				chooser.applyComponentOrientation(chooser.getComponentOrientation());
				for (int i = 0; i < newPanels.length; i++)
				{
					newPanels [i].installChooserPanel(chooser);
				}
			}

			if (e.getPropertyName().equals(JColorChooser.PREVIEW_PANEL_PROPERTY))
			{
				if (e.getNewValue() != previewPanel)
				{
					installPreviewPanel();
				}
			}
			if (e.getPropertyName().equals("componentOrientation"))
			{
				ComponentOrientation o = (ComponentOrientation) e.getNewValue();
				JColorChooser cc = (JColorChooser) e.getSource();
				if (o != (ComponentOrientation) e.getOldValue())
				{
					cc.applyComponentOrientation(o);
					cc.updateUI();
				}
			}
		}
	}

	/**
	 * Transferhandler for colors.
	 */
	static class ColorTransferHandler extends TransferHandler
		implements UIResource
	{
		ColorTransferHandler()
		{
			super("color");
		}
	}
}
