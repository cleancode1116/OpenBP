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
package org.openbp.swing.components.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Base class for wizard panels.
 *
 * @author Heiko Erhardt
 */
public abstract class WizardPage extends JPanel
	implements WizardValidator, WizardListener
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Wizard that owns the panel */
	private Wizard wizard;

	/** Move forward status flag */
	public boolean canMoveForward;

	/** Move backward status flag */
	public boolean canMoveBackward = true;

	/** Finish status flag */
	public boolean canFinish;

	/** Cancel status flag */
	public boolean canCancel = true;

	/** Title of the page */
	private String title;

	/** Description text */
	private String description;

	/** Wizard image */
	private ImageIcon wizardImage;

	/** Background image */
	private ImageIcon backgroundImage;

	/** Image label */
	private JLabel imageLabel;

	/** Title label */
	private JLabel titleLabel;

	/** Description area */
	private JTextArea descriptionArea;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param wizard Wizard that owns the panel
	 */
	public WizardPage(Wizard wizard)
	{
		this(wizard, null, null, null, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param wizard Wizard that owns the panel
	 * @param title Title of the page
	 */
	public WizardPage(Wizard wizard, String title)
	{
		this(wizard, title, null, null, null);
	}

	/**
	 * Default constructor.
	 *
	 * @param wizard Wizard that owns the panel
	 * @param title Title of the page
	 * @param description Description text
	 */
	public WizardPage(Wizard wizard, String title, String description)
	{
		this(wizard, title, description, null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the panel
	 * @param title Title of the page
	 * @param description Description text
	 * @param wizardImage Wizard image
	 */
	public WizardPage(Wizard wizard, String title, String description, ImageIcon wizardImage)
	{
		this(wizard, title, description, wizardImage, null);
	}

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the panel
	 * @param title Title of the page
	 * @param description Description text
	 * @param wizardImage Wizard image
	 * @param backgroundImage Background image
	 */
	public WizardPage(Wizard wizard, String title, String description, ImageIcon wizardImage, ImageIcon backgroundImage)
	{
		this.wizard = wizard;

		if (wizardImage == null && wizard != null)
			wizardImage = wizard.getDefaultWizardImage();
		if (backgroundImage == null && wizard != null)
			backgroundImage = wizard.getDefaultBackgroundImage();

		setLayout(new BorderLayout());

		JPanel headerPanel = new HeaderPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBorder(new CompoundBorder(new EdgeBorder(SwingConstants.SOUTH), new EmptyBorder(5, 10, 5, 10)));
		headerPanel.setBackground(Color.WHITE);

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BorderLayout());
		titlePanel.setOpaque(false);

		titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
		titleLabel.setOpaque(false);
		titlePanel.add("West", titleLabel);

		if (wizardImage != null)
		{
			imageLabel = new JLabel(wizardImage);
			imageLabel.setOpaque(false);
			imageLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
			titlePanel.add("East", imageLabel);
		}

		descriptionArea = new JTextArea(description);
		descriptionArea.setFont(getFont());
		descriptionArea.setBorder(new EmptyBorder(15, 2, 5, 15));
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setOpaque(false);
		descriptionArea.setCaretPosition(0);

		// Don't request the focus - our first input component should have the focus!
		descriptionArea.setFocusable(false);

		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.getViewport().setOpaque(false);
		descriptionScroll.setBackground(getBackground());
		Dimension size = new Dimension(10, 100);
		descriptionScroll.setMinimumSize(size);
		descriptionScroll.setMaximumSize(size);
		descriptionScroll.setPreferredSize(size);
		descriptionScroll.getViewport().setAutoscrolls(false);

		headerPanel.add("North", titlePanel);
		headerPanel.add("Center", descriptionScroll);

		add("North", headerPanel);
	}

	/**
	 * Updates the navigation bar.
	 * This method should be called if a page that does not make use of the wizard data model
	 * (see {@link #getDataModel}) has changed its status and wants to update the state of the navigation
	 * bar buttons accordingly.
	 */
	public void updateNavigator()
	{
		if (wizard != null)
			wizard.updateNavigator();
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the this page
	 */
	public Component getFocusComponent()
	{
		return this;
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
	}

	//////////////////////////////////////////////////
	// @@ WizardValidator implementation
	//////////////////////////////////////////////////

	/**
	 * Determines if we can advance to the next page.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canMoveForward()
	{
		return canMoveForward;
	}

	/**
	 * Determines if we can return to the previous page.
	 * Default: true.
	 * @nowarn
	 */
	public boolean canMoveBackward()
	{
		return canMoveBackward;
	}

	/**
	 * Determines if we can finish the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canFinish()
	{
		return canFinish;
	}

	/**
	 * Determines if we can cancel the wizard dialog at this point.
	 * Default: false.
	 * @nowarn
	 */
	public boolean canCancel()
	{
		return canCancel;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the title of the page.
	 * @nowarn
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title of the page.
	 * @nowarn
	 */
	public void setTitle(String title)
	{
		this.title = title;
		titleLabel.setText(title);
	}

	/**
	 * Gets the description text.
	 * @nowarn
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description text.
	 * @nowarn
	 */
	public void setDescription(String description)
	{
		this.description = description;
		descriptionArea.setText(description);
		descriptionArea.setCaretPosition(0);
	}

	/**
	 * Gets the wizard image.
	 * @nowarn
	 */
	public ImageIcon getWizardImage()
	{
		return wizardImage;
	}

	/**
	 * Sets the wizard image.
	 * @nowarn
	 */
	public void setWizardImage(ImageIcon wizardImage)
	{
		this.wizardImage = wizardImage;
		imageLabel.setIcon(wizardImage);
	}

	/**
	 * Gets the background image.
	 * @nowarn
	 */
	public ImageIcon getBackgroundImage()
	{
		return backgroundImage;
	}

	/**
	 * Sets the background image.
	 * @nowarn
	 */
	public void setBackgroundImage(ImageIcon backgroundImage)
	{
		this.backgroundImage = backgroundImage;
	}

	/**
	 * Gets the wizard that owns the panel.
	 * @nowarn
	 */
	public Wizard getWizard()
	{
		return wizard;
	}

	/**
	 * Sets the wizard that owns the panel.
	 * @nowarn
	 */
	public void setWizard(Wizard wizard)
	{
		this.wizard = wizard;
	}

	/**
	 * Gets the wizard's data model.
	 * @nowarn
	 */
	public WizardDataModel getDataModel()
	{
		return wizard != null ? wizard.getDataModel() : null;
	}

	/**
	 * Gets the wizard's sequence manager.
	 * @nowarn
	 */
	public SequenceManager getSequenceManager()
	{
		return wizard != null ? wizard.getManager() : null;
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/**
	 * Header panel displaying a background graphics.
	 */
	private class HeaderPanel extends JPanel
	{
		/**
		 * Default constructor.
		 */
		public HeaderPanel()
		{
			setOpaque(false);
		}

		/**
		 * Paints the component.
		 *
		 * @param g Graphics context
		 */
		public void paint(Graphics g)
		{
			ImageIcon bg = backgroundImage;
			if (bg == null && wizard != null)
				bg = wizard.getDefaultBackgroundImage();

			if (bg != null)
			{
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				g.drawImage(bg.getImage(), 0, 0, this);
			}

			super.paint(g);
		}
	}
}
