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
package org.openbp.cockpit.generator.type;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorCustomizer;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.cockpit.generator.wizard.GeneratorWizard;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.Copyable;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.guiclient.util.ClassNameBuilderUtil;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.gui.wizard.JaspiraWizardObjectPage;
import org.openbp.jaspira.propertybrowser.PropertyBrowser;
import org.openbp.jaspira.propertybrowser.PropertyBrowserImpl;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.plaf.sky.SkyTheme;

/**
 * Customizer class for visual item generation.
 *
 * @author Heiko Erhardt
 */
public class TypeCustomizer extends GeneratorCustomizer
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Complex type item to operate on */
	private ComplexTypeItem type;

	/** Index of the data type member currently displayed in the member page */
	private int currentMemberIndex;

	/** Data member page */
	public static final String MEMBER_PAGE = "member";

	/** Default member data type */
	public static final String DEFAULT_MEMBER_TYPE = "String";

	/** 'Add'/'Remove' button dimension */
	private static final Dimension BUTTON_DIM = new Dimension(260, 26);

	/** Defines the object members visible in the property browser */
	private final String [] typeVisibleProperties = { "Name", "DisplayName", "Description", "FunctionalGroup", "BaseTypeName", "ClassName", };

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public TypeCustomizer()
	{
	}

	//////////////////////////////////////////////////
	// @@ Overridables
	//////////////////////////////////////////////////

	/**
	 * Returns the class of the generator settings used by this generator.
	 *
	 * @return The class or null if the generator does not require a settings object
	 */
	public Class getSettingsClass()
	{
		return GeneratorSettings.class;
	}

	/**
	 * Template method that is called after the page sequence has been updated.
	 *
	 * @param context Context
	 * @param wizard Generator wizard
	 */
	public void afterPageSequenceUpdate(GeneratorContext context, GeneratorWizard wizard)
	{
		MemberPage memberPage = new MemberPage(wizard);
		wizard.addAndLinkPage(MEMBER_PAGE, memberPage);
		wizard.getManager().setNext(MEMBER_PAGE, MEMBER_PAGE);

		// Customize the property page
		JaspiraWizardObjectPage objectPage = (JaspiraWizardObjectPage) wizard.getPage("property");

		// Constrain the visible element
		PropertyBrowser oe = objectPage.getPropertyBrowser();
		oe.setVisibleMembers(typeVisibleProperties);

		// Add the 'Addd member' button below the property browser
		JPanel buttonPanel = createButtonPanel();
		buttonPanel.add(BorderLayout.CENTER, createAddButton(context, objectPage));
		objectPage.getContentPanel().add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Gets template name.
	 *
	 * @param context Generator context
	 * @return By default, the method returns the template name read from the generator XML file
	 */
	public String getTemplateName(GeneratorContext context)
	{
		determinetype(context);

		if (type.getClassName() != null)
		{
			// Implementation class name specified, generate the type source
			return super.getTemplateName(context);
		}

		// No implementation class name, no type source
		return null;
	}

	/**
	 * Determines if the generator should display a result page.
	 * @param context Generator context
	 * @return
	 * true: If an implementation class name was specified.<br>
	 * false: Otherwise
	 */
	public boolean hasResultPage(GeneratorContext context)
	{
		return getTemplateName(context) != null;
	}

	/**
	 * Processes a wizard event caused by a wizard page.
	 *
	 * @param context Generator context
	 * @param event Event to handle
	 */
	public void processWizardEvent(GeneratorContext context, WizardEvent event)
	{
		determinetype(context);

		String pageName = event.wizard.getManager().getCurrent();

		// After the selection page, update the implementation class name of the type
		if (pageName.equals("property"))
		{
			if (event.eventType == WizardEvent.SHOW)
			{
				if (context.isEmptyItem() && !context.isClassnameSet())
				{
					if (type.getClassName() == null)
					{
						type.setClassName(ClassNameBuilderUtil.constructTypeClassName(type));

						// Redisplay the type in the property browser
						// We have to do this because the event was already handled by the page *before* we get called.
						JaspiraWizardObjectPage page = (JaspiraWizardObjectPage) event.wizard.getPage(pageName);
						page.handleWizardEvent(event);
					}

					context.setClassnameSet(true);
				}

				currentMemberIndex = 0;

				List memberList = type.getMemberList();
				JaspiraWizardObjectPage objectPage = (JaspiraWizardObjectPage) event.wizard.getPage(pageName);
				objectPage.canMoveForward = memberList != null && currentMemberIndex < memberList.size() - 1;
			}
		}

		super.processWizardEvent(context, event);
	}

	//////////////////////////////////////////////////
	// @@ Member page
	//////////////////////////////////////////////////

	/**
	 * Property page for data member objects of the type wizard.
	 * Contains an property browser that is used to edit the properties of the node.
	 */
	private class MemberPage extends JaspiraWizardObjectPage
	{
		/** Defines the object members visible in the property browser */
		private final String [] memberVisibleProperties = { "Name", "DisplayName", "Description", "TypeName", "Length", "Precision", "Required", "PrimaryKey", };

		/**
		 * Default constructor.
		 *
		 * @param wizard Wizard that owns the page
		 */
		public MemberPage(final GeneratorWizard wizard)
		{
			super(wizard);

			PropertyBrowser oe = getPropertyBrowser();
			oe.setVisibleMembers(memberVisibleProperties);

			canFinish = true;

			// Add the 'Add member' and 'Remove member' buttons below the property browser
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.setBorder(new EmptyBorder(5, 4, 5, 5));

			GeneratorContext context = wizard.getContext();

			buttonPanel.add(BorderLayout.CENTER, createAddButton(context, this));
			buttonPanel.add(BorderLayout.EAST, createRemoveButton(context, this));

			getContentPanel().add(buttonPanel, BorderLayout.SOUTH);
		}

		/**
		 * Handles a wizard event caused by this wizard page.
		 *
		 * @param event Event to handle
		 */
		public void handleWizardEvent(WizardEvent event)
		{
			if (event.eventType == WizardEvent.SHOW)
			{
				List memberList = type.getMemberList();

				DataMember member = null;
				if (memberList != null && currentMemberIndex < memberList.size())
				{
					// Current member exists
					member = (DataMember) memberList.get(currentMemberIndex);
				}

				PropertyBrowser oe = getPropertyBrowser();

				// Set the item; note that the 'new' flag must be set to true
				// in order for the ModelObjectValidator properly check name uniqueness.
				try
				{
					oe.setObject(member, true);
				}
				catch (XMLDriverException e)
				{
					// Doesn't happen
				}
				catch (CloneNotSupportedException e)
				{
					// Doesn't happen
				}

				// Make the object modified so validation will occur when pressing 'Next' or 'Finish'
				oe.setObjectModified(true);
				oe.setOriginalObject(member);

				// Update the status of the 'Next' button
				canMoveForward = memberList != null && currentMemberIndex < memberList.size() - 1;
				updateNavigator();
			}
			else if (event.eventType == WizardEvent.FINISH || event.eventType == WizardEvent.NEXT || event.eventType == WizardEvent.BACK)
			{
				PropertyBrowser oe = getPropertyBrowser();
				if (!oe.saveObject())
				{
					// Save failed, don't continue
					event.cancel = true;
					return;
				}

				clearPropertyBrowser(this);

				if (event.eventType == WizardEvent.NEXT)
				{
					// Advance in the member list, creating a new member if necessary
					++currentMemberIndex;

					// Redisplay the current page
					event.wizard.displayPage(null);

					// We have done page navigation processing, so don't continue executing the event
					event.cancel = true;
				}
				else if (event.eventType == WizardEvent.BACK)
				{
					if (currentMemberIndex == 0)
					{
						// We are at the start of the member list, go back to the data type property page
					}
					else
					{
						// Show the previous member
						--currentMemberIndex;

						// Redisplay the current page
						event.wizard.displayPage(null);

						// We have done page navigation processing, so don't continue executing the event
						event.cancel = true;
						return;
					}
				}
			}
			else if (event.eventType == WizardEvent.CANCEL)
			{
				clearPropertyBrowser(this);
			}
		}

		/**
		 * Called by the property browser when the object needs to be saved.
		 *
		 * @param pb Property browser
		 * @return
		 * true: Save successful.<br>
		 * false: Save failed.
		 */
		public boolean executeSave(PropertyBrowser pb)
		{
			DataMember member = (DataMember) type.getMemberList().get(currentMemberIndex);
			DataMember modifiedMember = (DataMember) pb.getModifiedObject();

			try
			{
				member.copyFrom(modifiedMember, Copyable.COPY_FIRST_LEVEL);
			}
			catch (CloneNotSupportedException e)
			{
				ExceptionUtil.printTrace(e);
			}

			return true;
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Clears the property browser.
	 *
	 * @param page Page that contains the property browser
	 */
	public static void clearPropertyBrowser(JaspiraWizardObjectPage page)
	{
		PropertyBrowser oe = page.getPropertyBrowser();
		try
		{
			// Clear the property browser for later use
			oe.setObjectModified(false);
			oe.setObject(null, false);
			oe.setOriginalObject(null);
		}
		catch (XMLDriverException e)
		{
			// Never happens
		}
		catch (CloneNotSupportedException e)
		{
			// Never happens
		}
	}

	/**
	 * Creates the 'Add member' button.
	 *
	 * @param page Wizard page to add the button to
	 * @return The button
	 */
	protected JButton createAddButton(final GeneratorContext context, final JaspiraWizardObjectPage page)
	{
		ResourceCollection res = page.getWizard().getResource();
		JButton btn = createButton(res, "wizard.type.addmemberbutton");

		// When clicking the run button, remove the current member
		btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// First, save the current contents of the property browser
				PropertyBrowserImpl oe = (PropertyBrowserImpl) page.getPropertyBrowser();
				if (!oe.saveObject())
				{
					// Save failed, don't continue
					return;
				}

				// Add a new member to the end of the member list
				DataMember member = type.createMember();
				type.addMember(member);

				// Default: String member with default values provided by the String data type
				member.setTypeName(DEFAULT_MEMBER_TYPE);

				try
				{
					// Try to determine the new data type and perform a default configuration
					// of the member according to its new type
					DataTypeItem dataType = (DataTypeItem) member.getParentDataType().resolveItemRef(DEFAULT_MEMBER_TYPE, ItemTypes.TYPE);
					dataType.performDefaultDataMemberConfiguration(member);
				}
				catch (Exception ex)
				{
					// Ignore any errors
				}

				// Display the new member
				List memberList = type.getMemberList();
				currentMemberIndex = memberList.size() - 1;

				// Member list empty, display the type page
				page.getWizard().displayPage(MEMBER_PAGE);

				// Update the status of the 'Next' button
				page.canMoveForward = currentMemberIndex < memberList.size() - 1;
				page.updateNavigator();
			}
		});

		return btn;
	}

	/**
	 * Creates the 'Remove member' button.
	 *
	 * @param page Wizard page to add the button to
	 * @return The button
	 */
	protected JButton createRemoveButton(final GeneratorContext context, final JaspiraWizardObjectPage page)
	{
		ResourceCollection res = page.getWizard().getResource();
		JButton btn = createButton(res, "wizard.type.removememberbutton");

		// When clicking the run button, remove this member
		btn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clearPropertyBrowser(page);

				// Remove the current member from the member list
				type.removeMember((DataMember) type.getMemberList().get(currentMemberIndex));

				List memberList = type.getMemberList();
				int n = memberList != null ? memberList.size() : 0;

				if (currentMemberIndex == n)
				{
					if (currentMemberIndex > 0)
						--currentMemberIndex;
				}

				if (currentMemberIndex < n)
				{
					// Redisplay the current page for the next/previous member in the list
					page.getWizard().displayPage(null);
				}
				else
				{
					// Member list empty, display the type page
					page.getWizard().displayFirst();
				}
			}
		});

		return btn;
	}

	/**
	 * Creates a button, retrieving the button text from the resource file.
	 *
	 * @param res Resource
	 * @param resourceId Id of the button text resource
	 * @return The button
	 */
	public static JButton createButton(ResourceCollection res, String resourceId)
	{
		// Create the 'Remove member' button
		String text = res.getRequiredString(resourceId);
		JButton btn = new JButton(ActionMgr.getStringWithoutMnemonicDelimiter(text));
		btn.setMnemonic(ActionMgr.getMnemonicChar(text));
		btn.setPreferredSize(BUTTON_DIM);
		btn.setMaximumSize(BUTTON_DIM);
		return btn;
	}

	/**
	 * Creates the button panel.
	 *
	 * @return the panel
	 */
	public static JPanel createButtonPanel()
	{
		JPanel btnPanel = new JPanel();
		btnPanel.setBackground(SkyTheme.COLOR_BACKGROUND_LIGHT);
		btnPanel.setLayout(new BorderLayout());
		btnPanel.setBorder(new EmptyBorder(5, 4, 5, 5));
		return btnPanel;
	}

	private void determinetype(GeneratorContext context)
	{
		type = (ComplexTypeItem) context.getItem();
	}
}
