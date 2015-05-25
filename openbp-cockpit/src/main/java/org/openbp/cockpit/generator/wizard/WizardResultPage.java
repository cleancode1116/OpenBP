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
package org.openbp.cockpit.generator.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openbp.cockpit.generator.Generator;
import org.openbp.cockpit.generator.GeneratorContext;
import org.openbp.cockpit.generator.GeneratorSettings;
import org.openbp.common.MsgFormat;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.template.TemplateEngineResult;
import org.openbp.common.template.writer.CancelException;
import org.openbp.common.template.writer.MergeModeRequester;
import org.openbp.common.template.writer.TemplateWriter;
import org.openbp.core.MimeTypes;
import org.openbp.core.model.Association;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.gui.wizard.JaspiraWizardResultPage;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;
import org.openbp.swing.components.popupfield.JPopupField;
import org.openbp.swing.components.popupfield.JSelectionField;
import org.openbp.swing.components.wizard.WizardEvent;
import org.openbp.swing.layout.VerticalFlowLayout;

/**
 * The wizard result page performs the generation process while giving a visual feedback to the user.
 * The output of the template-based generator is redirected to the console component of the dialog.
 *
 * @author Heiko Erhardt
 */
public class WizardResultPage extends JaspiraWizardResultPage
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Mode: Start panel displayed */
	public static final int MODE_START = 0;

	/** Mode: Generator running */
	public static final int MODE_RUN = 1;

	/** Mode: Generator successful */
	public static final int MODE_SUCCESS = 2;

	/** Mode: Generator errors */
	public static final int MODE_ERROR = 3;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Start panel */
	private JPanel startPanel;

	/** Settings panel */
	private JPanel settingsPanel;

	/** 'Run generator' button */
	private JButton runButton;

	/** Check box containing the open document flag */
	private JCheckBox openCheckBox;

	/** Wizard resource */
	private ResourceCollection resourceCollection;

	/** Flag if the generator has been run */
	private boolean generatorPerformed;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 *
	 * @param wizard Wizard that owns the page
	 */
	public WizardResultPage(GeneratorWizard wizard)
	{
		super(wizard);

		resourceCollection = wizard.getResource();

		// Construct the UI

		// Instead, we display the template set combo box and the 'Run generator' button
		// at the top of the page.
		addStartPanel();

		// Add the 'Open result file' checkbox to the bottom of the page.
		Generator generator = getContext().getSelectedGenerator();
		if (generator.isShowOpenResultCheckBox())
		{
			addOpenPanel();
		}
	}

	/**
	 * Adds the template set combo box and the 'Run generator' button to the top of the page.
	 */
	private void addStartPanel()
	{
		startPanel = new JPanel(new BorderLayout());

		// Add the 'Run generator' button below
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

		String text = resourceCollection.getRequiredString("wizard.result.runbutton");
		runButton = new JButton(ActionMgr.getStringWithoutMnemonicDelimiter(text));
		runButton.setMnemonic(ActionMgr.getMnemonicChar(text));
		runButton.setPreferredSize(new Dimension(120, 26));
		buttonPanel.add(runButton, BorderLayout.CENTER);

		// When clicking the run button, start the generator
		runButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				start();
			}
		});

		settingsPanel = new JPanel(new VerticalFlowLayout());

		JPanel modePanel = createModePanel();
		JPanel outputDirPanel = createOutputDirPanel();

		settingsPanel.add(modePanel);
		settingsPanel.add(outputDirPanel);

		startPanel.add(buttonPanel, BorderLayout.CENTER);

		getProgressPanel().add(startPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel that contains an output directory edit field.
	 * The default value of the edit field is the model directory.
	 * The output directory will be stored in the context ({@link GeneratorContext}) of the wizard.
	 * @return The output directory panel
	 */
	public JPanel createOutputDirPanel()
	{
		// Create the panel
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Field label
		JLabel outputDirLabel = new JLabel(resourceCollection.getRequiredString("wizard.result.outputdirlabel"));
		outputDirLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel.add(outputDirLabel, BorderLayout.WEST);

		// Text field
		final JPopupField outputDirField = new JPopupField()
		{
			public boolean isPopupVisible()
			{
				return false;
			}

			public void setPopupVisible(boolean popupVisible)
			{
				if (popupVisible)
				{
					// Prepare file chooser
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					// Set the dir of the file chooser to the current path value
					File f = new File(getText());
					if (f.exists())
					{
						fileChooser.setCurrentDirectory(f);
					}

					// Show the file chooser
					int retVal = fileChooser.showOpenDialog(null);

					// If a file has been choosen, set the new value.
					if (retVal == JFileChooser.APPROVE_OPTION)
					{
						setText(fileChooser.getSelectedFile().getPath());
					}
				}
			}
		};

		outputDirField.getTextField().getDocument().addDocumentListener(new DocumentListener()
		{
			/**
			 * @see javax.swing.event.DocumentListener#changedUpdate(DocumentEvent)
			 */
			public void changedUpdate(DocumentEvent e)
			{
				getContext().setOutputRootDir(outputDirField.getText());
			}

			/**
			 * @see javax.swing.event.DocumentListener#insertUpdate(DocumentEvent)
			 */
			public void insertUpdate(DocumentEvent e)
			{
				getContext().setOutputRootDir(outputDirField.getText());
			}

			/**
			 * @see javax.swing.event.DocumentListener#removeUpdate(DocumentEvent)
			 */
			public void removeUpdate(DocumentEvent e)
			{
				getContext().setOutputRootDir(outputDirField.getText());
			}
		});

		panel.add(outputDirField, BorderLayout.CENTER);

		outputDirField.setText(getContext().getOutputRootDir());

		return panel;
	}

	/**
	 * Creates a panel that contains the generator mode controls.
	 * By default, this is the merge/overwrite combo box.
	 * The overwrite mode will be stored in the context ({@link GeneratorContext}) of the wizard.
	 * @return The mode panel
	 */
	public JPanel createModePanel()
	{
		// Create the panel
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));

		// Field label
		JLabel outputDirLabel = new JLabel(resourceCollection.getRequiredString("wizard.result.overwritelabel"));
		outputDirLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel.add(outputDirLabel, BorderLayout.WEST);

		// Text field
		final JSelectionField sf = new JSelectionField();
		sf.addItem(resourceCollection.getRequiredString("wizard.result.overwrite.ask"));
		sf.addItem(resourceCollection.getRequiredString("wizard.result.overwrite.merge"));
		sf.addItem(resourceCollection.getRequiredString("wizard.result.overwrite.overwrite"));
		sf.setEditable(false);
		sf.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				String sOverwriteMode = null;
				switch (sf.getSelectedIndex())
				{
				case 1:
					sOverwriteMode = "merge";
					break;

				case 2:
					sOverwriteMode = "overwrite";
					break;
				}
				getContext().setOverwriteMode(sOverwriteMode);
			}
		});

		String sOverwriteMode = getContext().getOverwriteMode();
		int iOverwriteMode = 0;
		if ("merge".equals(sOverwriteMode))
			iOverwriteMode = 1;
		else if ("overwrite".equals(sOverwriteMode))
			iOverwriteMode = 2;
		sf.setSelectedIndex(iOverwriteMode);

		panel.add(sf, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Adds the 'Open result file' checkbox to the bottom of the page.
	 */
	private void addOpenPanel()
	{
		// Create a checkbox for the auto open of the result file
		JPanel checkBoxPanel = new JPanel(new BorderLayout());

		String text = resourceCollection.getRequiredString("wizard.result.opendocument");
		openCheckBox = new JCheckBox(ActionMgr.getStringWithoutMnemonicDelimiter(text));
		openCheckBox.setMnemonic(ActionMgr.getMnemonicChar(text));
		checkBoxPanel.add(openCheckBox);

		// Add it to the bottom of the text area panel
		getTextPanel().add(checkBoxPanel, BorderLayout.SOUTH);
	}

	/**
	 * Shows or hides user interface components and sets the title and description text
	 * according to the current mode.
	 *
	 * @param mode Mode to set
	 */
	private void showStartPanel(int mode)
	{
		// Set title and description
		String titleResourceName;
		String descriptionResourceName;

		switch (mode)
		{
		case MODE_RUN:
			titleResourceName = "wizard.result.title.generating";
			descriptionResourceName = "wizard.result.description.generating";
			break;

		case MODE_SUCCESS:
			titleResourceName = "wizard.result.title.finished";
			descriptionResourceName = "wizard.result.description.finished";
			break;

		case MODE_ERROR:
			titleResourceName = "wizard.result.title.errors";
			descriptionResourceName = "wizard.result.description.errors";
			break;

		default:
			titleResourceName = "wizard.result.title.generator";
			descriptionResourceName = "wizard.result.description.generator";
			break;
		}

		setTitle(resourceCollection.getRequiredString(titleResourceName));
		setDescription(resourceCollection.getRequiredString(descriptionResourceName));

		JPanel cp = getContentPanel();

		if (mode == MODE_START)
		{
			// Clear the console
			getConsole().flush();
		}

		canCancel = mode != MODE_RUN;

		getProgressBar().setVisible(mode == MODE_RUN);
		startPanel.setVisible(mode != MODE_RUN);

		// Hides the progress text area
		updatePage(false);

		// Force page update
		cp.validate();
	}

	//////////////////////////////////////////////////
	// @@ WizardPage overrides
	//////////////////////////////////////////////////

	/**
	 * Returns the focus component of this plugin, i\.e\. the component
	 * that is to initially receive the focus.
	 * @return The return value defaults to the this page
	 */
	public Component getFocusComponent()
	{
		// Set the focus to the 'Run generator' button when showing the page
		if (runButton.isEnabled())
			return runButton;
		return super.getFocusComponent();
	}

	//////////////////////////////////////////////////
	// @@ JaspiraWizardResultPage overrides
	//////////////////////////////////////////////////

	/**
	 * Prepares for processing.
	 * This method is called before the {@link #process} method is invoked.
	 */
	protected void preProcess()
		throws Exception
	{
		// Show the progress bar
		showStartPanel(MODE_RUN);

		// Start the self-running progress bar
		getProgressBar().setIndeterminate(true);

		// Make stdout/stderr print to the output window
		getConsole().mapAll(true);

		// Prepare the context
		GeneratorContext context = getContext();
		context.setProperty("mergeModeRequester", new GeneratorMergeModeRequester());

		// Call the generator
		Generator generator = context.getSelectedGenerator();
		generator.preProcess(context);
	}

	/**
	 * Performs the processing.
	 * This method is called from inside the Swing worker thread to perform the processing.
	 */
	protected void process()
		throws Exception
	{
		// Call the generator
		GeneratorContext context = getContext();
		Generator generator = context.getSelectedGenerator();
		generator.performProcess(context);
	}

	/**
	 * Finishes the processing.
	 * This method is called after the {@link #process} method has terminated.
	 * @param success Parameter indicating success or failure of the process
	 */
	protected void postProcess(boolean success)
		throws Exception
	{
		if (success)
		{
			// Call the generator
			GeneratorContext context = getContext();
			Generator generator = context.getSelectedGenerator();
			generator.postProcess(context);

			// Reset the template logger streams
			getConsole().unmapAll();
			getConsole().writeText(resourceCollection.getRequiredString("wizard.result.progress.finished"));

			// Stop and hide the progress bar again
			getProgressBar().setIndeterminate(false);
			getProgressBar().setValue(100);

			// Show the 'Run' panel again and update text/description
			TemplateEngineResult resultList = context.getTemplateEngineResult();
			if (resultList != null)
			{
				showStartPanel(MODE_SUCCESS);
			}
			else
			{
				showStartPanel(MODE_ERROR);
			}

			generatorPerformed = true;

			if (openCheckBox != null)
			{
				openCheckBox.requestFocus();
			}
		}
		else
		{
			// Reset the template logger streams
			getConsole().unmapAll();

			// Stop and hide the progress bar again
			getProgressBar().setIndeterminate(false);
			getProgressBar().setValue(100);

			showStartPanel(MODE_ERROR);
		}
	}

	/**
	 * Processes an exception that was issued during pre processing, processing or post processing.
	 * Default behaviour is to show a popup dialog that displays the exception message.
	 *
	 * @param t Exception to handle
	 */
	protected void processException(Throwable t)
	{
		if (t instanceof CancelException)
			return;

		// Report exception to error output in dialog and message box and abort
		t.printStackTrace();
		System.err.println();
		System.err.println(t.toString());

		super.processException(t);
	}

	/**
	 * Handles a wizard event caused by this wizard page.
	 *
	 * @param event Event to handle
	 */
	public void handleWizardEvent(WizardEvent event)
	{
		// We don't want the generation processing to start automatically, so we don't call the super method here.

		if (event.eventType == WizardEvent.SHOW)
		{
			// Show/hide the settings panel
			Generator generator = getContext().getSelectedGenerator();
			if (generator.getTemplateName() != null)
			{
				if (settingsPanel.getParent() == null)
				{
					startPanel.add(settingsPanel, BorderLayout.NORTH);
				}
			}
			else
			{
				if (settingsPanel.getParent() != null)
				{
					startPanel.remove(settingsPanel);
				}
			}

			// We hide the progress bar as long as we haven't started the generation process.
			showStartPanel(MODE_START);
		}

		else if (event.eventType == WizardEvent.CLOSE)
		{
			if (!generatorPerformed && getContext().isNeedGeneration())
			{
				// A generator run should be performed, but hasn't yet
				String msg = resourceCollection.getRequiredString("wizard.result.warnexit");
				int ret = JMsgBox.show(null, msg, JMsgBox.TYPE_YESNO);
				if (ret != JMsgBox.TYPE_YES)
				{
					event.cancel = true;
					return;
				}
			}

			// Open the generated file if generation is successful and desired by the user
			TemplateEngineResult resultList = getContext().getTemplateEngineResult();
			if (resultList != null && openCheckBox != null && openCheckBox.isSelected())
			{
				String fileToOpen = resultList.getFirstFileName();
				if (fileToOpen == null)
					return;

				String mimeType = resultList.getMimeType(fileToOpen);

				Association as = new Association();
				as.setAssociationTypes(new String [] { mimeType, MimeTypes.TEXT_FILE });
				as.setAssociationPriority(Association.PRIMARY);

				as.setAssociatedObject(new File(fileToOpen).getAbsolutePath());

				JaspiraEventMgr.fireGlobalEvent("plugin.association.open", as);
			}
		}
	}


	//////////////////////////////////////////////////
	// @@ Convenience methods
	//////////////////////////////////////////////////

	/**
	 * Gets the generator context.
	 * @nowarn
	 */
	public GeneratorContext getContext()
	{
		return ((GeneratorWizard) getWizard()).getContext();
	}

	/**
	 * Gets the currently selected generator.
	 *
	 * @return The generator or null if no generator has been selected yet
	 */
	public Generator getGenerator()
	{
		return ((GeneratorWizard) getWizard()).getContext().getSelectedGenerator();
	}

	/**
	 * Gets the generator settings.
	 * @nowarn
	 */
	public GeneratorSettings getGeneratorSettings()
	{
		return ((GeneratorWizard) getWizard()).getContext().getGeneratorSettings();
	}

	//////////////////////////////////////////////////
	// @@ Merge mode requester class
	//////////////////////////////////////////////////

	private class GeneratorMergeModeRequester
		implements MergeModeRequester
	{
		/**
		 * Constructor.
		 */
		public GeneratorMergeModeRequester()
		{
		}

		/**
		 * Determines the merge mode for the given writer.
		 * Usually displays some dialog or message box to the user
		 *
		 * @param writer The writer
		 * @return The return code determines the merge action to take:<br>
		 * {@link TemplateWriter#MODE_UNDEFINED}: Cancels the process<br>
		 * {@link TemplateWriter#MODE_OVERWRITE}: Overwrite the output file<br>
		 * {@link TemplateWriter#MODE_MERGE}: Merge contents with the output file
		 */
		public int determineMergeMode(TemplateWriter writer)
		{
			String title = resourceCollection.getRequiredString("wizard.result.mergedialog.title");
			String text = resourceCollection.getRequiredString("wizard.result.mergedialog.text");
			text = MsgFormat.format(text, writer.getFileName());

			JMsgBox msgBox = new JMsgBox(WizardResultPage.this, title, text, JMsgBox.TYPE_YESNOCANCEL);
			msgBox.setResource(resourceCollection);
			msgBox.setResourcePrefix("wizard.result.mergedialog.");

			msgBox.initDialog();
			SwingUtil.show(msgBox);

			int choice = msgBox.getUserChoice();
			int mode = TemplateWriter.MODE_UNDEFINED;

			if (choice == JMsgBox.TYPE_YES)
			{
				// Yes means merge
				mode = TemplateWriter.MODE_MERGE;
			}
			else if (choice == JMsgBox.TYPE_NO)
			{
				// No means overwrite
				mode = TemplateWriter.MODE_OVERWRITE;
			}

			return mode;
		}
	}
}
