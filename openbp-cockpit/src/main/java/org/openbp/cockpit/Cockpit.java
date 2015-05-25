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
package org.openbp.cockpit;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.openbp.cockpit.generator.GeneratorMgr;
import org.openbp.cockpit.generator.GeneratorPlugin;
import org.openbp.cockpit.itemeditor.ItemOpenDispatcherPlugin;
import org.openbp.cockpit.itemeditor.NodeItemEditor;
import org.openbp.cockpit.itemeditor.StandardItemEditor;
import org.openbp.cockpit.modeler.ModelerOptionPlugin;
import org.openbp.cockpit.modeler.ModelerPage;
import org.openbp.cockpit.modeler.figures.process.ProcessElementFigureRegistry;
import org.openbp.cockpit.modeler.skins.SkinMgr;
import org.openbp.cockpit.modeler.skins.SkinPlugin;
import org.openbp.cockpit.plugins.association.AssociationPlugin;
import org.openbp.cockpit.plugins.association.ExternalAssociationPlugin;
import org.openbp.cockpit.plugins.commandline.CommandLinePlugin;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.generic.propertybrowser.ObjectDescriptorMgr;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.common.resource.FileResourceProvider;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.setting.SettingUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.core.CoreModule;
import org.openbp.core.MimeTypes;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Association;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.JavaActivityItem;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.visual.VisualItem;
import org.openbp.core.remote.ClientLoginInfo;
import org.openbp.guiclient.GUIClientModule;
import org.openbp.guiclient.model.ModelConnector;
import org.openbp.guiclient.model.item.ItemEditorRegistry;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;
import org.openbp.guiclient.plugins.server.ServerConnectionPlugin;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.action.JaspiraActionEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.gui.plugin.ApplicationBase;
import org.openbp.jaspira.gui.plugin.JaspiraPage;
import org.openbp.jaspira.plugin.ApplicationUtil;
import org.openbp.jaspira.plugin.ConfigMgr;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.jaspira.plugins.errordialog.ErrorDialogPlugin;
import org.openbp.jaspira.propertybrowser.NodeStructureMgr;
import org.openbp.swing.SwingUtil;
import org.openbp.swing.components.JMsgBox;
import org.springframework.core.io.ResourceLoader;

/**
 * Main class of the OpenBP cockpit.
 *
 * @author Jens Ferchland
 */
public class Cockpit extends ApplicationBase
{
	/** The splash screen of the application. */
	private static SplashScreen splashScreen;

	/** Debug flag: Disable all continously running timers (useful for profiling and memory leak detection) */
	public static boolean disableTimersForDebug = false;

	//////////////////////////////////////////////////
	// @@ Plugin overrides
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 * Initializes the application.
	 * This will register the ApplicationBase as a regular plugin.
	 * This in turn will invoke the install method, which may perform further plugin loading etc.
	 */
	public Cockpit()
	{
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	/**
	 * Template method that is called before the actual installation of the application base plugin is performed.
	 */
	protected void preInstallApplication()
	{
		if (disableTimersForDebug)
		{
			ServerConnection.disableTimersForDebug = true;
		}

		// Initialize core and client and set up the server connection
		super.preInstallApplication();

		String resourceDir = SettingUtil.getStringSetting("openbp.cockpit.resource.dir");
		if (resourceDir != null)
		{
			ResourceLoader loader = ResourceMgr.getDefaultInstance().getResourceLoader();
			FileResourceProvider rp = new FileResourceProvider(ResourceMgr.ROOTDIR_RESOURCE_PROVIDER_PRIO + 1, loader, resourceDir);
			ResourceMgr.getDefaultInstance().getResourceResolver().addProvider(rp);
		}

		// Load base plugins
		PluginMgr.getInstance().createInstance(ServerConnectionPlugin.class, this);

		registerItemEditors();
		registerFigureClasses();
	}

	/**
	 * Template method that is called after the actual installation of the application base plugin is performed.
	 */
	protected void postInstallApplication()
	{
		super.postInstallApplication();

		PluginMgr pm = PluginMgr.getInstance();

		// Initialize the client environment
		GUIClientModule.getInstance().initialize();

		// This will load the model data.
		ModelConnector.getInstance().initialize(GUIClientModule.getInstance());

		// Set the custom descriptor directory and custom descriptor set name specified by the user
		initCustomDescriptors();

		// Add global plugins
		// This will automatically create the display object plugin
		DisplayObjectPlugin.getInstance();

		// Load all skin data and initialize the icon model accordingly
		SkinMgr.getInstance().load();

		// Add these plugins first, they will create menu items for the global menu
		pm.createInstance(ErrorDialogPlugin.class, this);

		// Try to connect to the server
		// By default, use an anonymous login
		ServerConnection connection = ServerConnection.getInstance();
		connection.setLoginInfo(new ClientLoginInfo("Anonymous", null));
		try
		{
			connection.connect(true);
		}
		catch (OpenBPException e)
		{
			// Show dialog box if the user has not chose to skip it before
			if (! SettingUtil.getBooleanSetting("openbp.cockpit.connectionwarning.hide", false))
			{
				// Hide splash screen.
				if (splashScreen != null)
				{
					splashScreen.setVisible(false);
				}

				int response = JMsgBox.show(null, getPluginResourceCollection().getRequiredString(
					"connectionerror.title"), ResourceCollectionUtil.formatMsg(getPluginResourceCollection(),
					"connectionerror.description", new Object[]
					{
						connection.getConnectionInfo().getRmiServerHost(),
						Integer.valueOf(connection.getConnectionInfo().getRmiServerPort()), e.getMessage()
					}), JMsgBox.ICON_INFO | JMsgBox.TYPE_OK | JMsgBox.DEFAULT_OK | JMsgBox.TYPE_DO_NOT_SHOW_AGAIN);

				if ((response & JMsgBox.TYPE_DO_NOT_SHOW_AGAIN) != 0)
				{
					// Skip this dialog the next time
					SettingUtil.setBooleanSetting("openbp.cockpit.connectionwarning.hide", true);
					SettingUtil.saveSettings(null);
				}
			}
		}

		// After adding the first page, we have a component we need for the glass pane access
		// for the wait cursor, so turn it on now
		ApplicationUtil.waitCursorOn();

		try
		{
			// Add the standard pages
			addPage((JaspiraPage) pm.createInstance(ModelerPage.class, this));

			pm.createInstance(GeneratorPlugin.class, this);

			// pm.createInstance (DataTypeSearchPlugin.class, this);
			// pm.createInstance(EclipseEditorPlugin.class, this);

			pm.createInstance(ModelerOptionPlugin.class, this);
			pm.createInstance(SkinPlugin.class, this);

			pm.createInstance(ItemOpenDispatcherPlugin.class, this);
			pm.createInstance(CommandLinePlugin.class, this);
			pm.createInstance(ExternalAssociationPlugin.class, this);
			pm.createInstance(AssociationPlugin.class, this);

			// Load global custom plugins
			PluginMgr.getInstance().loadCustomPlugins("openbp.cockpit.plugins.global");

			// Load custom plugins from the 'plugin' directory.
			PluginMgr.getInstance().loadPluginsFromResource();
		}
		finally
		{
			// Reset the wait cursor
			ApplicationUtil.waitCursorOff();
		}

		// Disable the save button initially
		JaspiraAction action = getAction("standard.file.save");
		if (action != null)
		{
			action.setEnabled(false);
		}

		// Make the tool tips stay long enough to be read
		ToolTipManager.sharedInstance().setDismissDelay(10000);

		// Write out that startup is complete.
		System.out.println("OpenBP cockpit startup complete");
	}

	/**
	 * @see org.openbp.jaspira.gui.plugin.ApplicationBase#getAboutBoxClass()
	 */
	protected Class getAboutBoxClass()
	{
		return CockpitAboutBox.class;
	}

	/**
	 * Registers the default item editors.
	 */
	private void registerItemEditors()
	{
		ItemEditorRegistry registry = ItemEditorRegistry.getInstance();

		StandardItemEditor standardItemEditor = new StandardItemEditor();
		NodeItemEditor nodeItemEditor = new NodeItemEditor();

		registry.registerItemEditor(ItemTypes.ACTIVITY, nodeItemEditor);
		registry.registerItemEditor(ModelObjectSymbolNames.PLACEHOLDER_NODE, nodeItemEditor);
		registry.registerItemEditor(ItemTypes.PROCESS, nodeItemEditor);
		registry.registerItemEditor(ItemTypes.TYPE, standardItemEditor);
		registry.registerItemEditor(ItemTypes.MODEL, standardItemEditor);
		registry.registerItemEditor(ItemTypes.VISUAL, nodeItemEditor);
	}

	/**
	 * Registers the default process element figure classes.
	 */
	private void registerFigureClasses()
	{
		ProcessElementFigureRegistry registry = ProcessElementFigureRegistry.getInstance();

		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.DecisionNodeFigure.class,
			org.openbp.core.model.item.process.DecisionNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.FinalNodeFigure.class,
			org.openbp.core.model.item.process.FinalNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.FlowConnection.class,
			org.openbp.core.model.item.process.ControlLink.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.ForkNodeFigure.class,
			org.openbp.core.model.item.process.ForkNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.InitialNodeFigure.class,
			org.openbp.core.model.item.process.InitialNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.JoinNodeFigure.class,
			org.openbp.core.model.item.process.JoinNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.MergeNodeFigure.class,
			org.openbp.core.model.item.process.MergeNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.ParamConnection.class,
			org.openbp.core.model.item.process.DataLink.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.PlaceholderNodeFigure.class,
			org.openbp.core.model.item.process.PlaceholderNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.TextElementFigure.class,
			org.openbp.core.model.item.process.TextElement.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.WaitStateNodeFigure.class,
			org.openbp.core.model.item.process.WaitStateNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.WorkflowEndNodeFigure.class,
			org.openbp.core.model.item.process.WorkflowEndNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.WorkflowNodeFigure.class,
			org.openbp.core.model.item.process.WorkflowNode.class);

		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure.class,
			org.openbp.core.model.item.process.ActivityNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure.class,
			org.openbp.core.model.item.process.SubprocessNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure.class,
			org.openbp.core.model.item.process.VisualNode.class);
		registry.registerFigure(org.openbp.cockpit.modeler.figures.process.MultiSocketNodeFigure.class,
			org.openbp.core.model.item.process.WebServiceNode.class);
	}

	/**
	 * Initializes custom descriptors.
	 * Sets the custom descriptor directory and loads the current custom descriptor set, if any.
	 */
	private void initCustomDescriptors()
	{
		ObjectDescriptorMgr.getInstance().setCustomDescriptorResourcePath(CockpitConstants.CUSTOM_OBJECT_DESCRIPTOR_SETS);

		loadCustomDescriptors();
	}

	/**
	 * Resets custom descriptors.
	 * Reloads the current custom descriptor set, if any.
	 */
	private void loadCustomDescriptors()
	{
		// Must clear the property browser node structure manager's cache for the
		// custom object descriptors to take effect.
		NodeStructureMgr.getInstance().clearCache();

		String customDescriptorSetName = SettingUtil.getStringSetting("openbp.cockpit.codset");
		try
		{
			ObjectDescriptorMgr.getInstance().loadCustomDescriptorSet(customDescriptorSetName);

			// Refresh all property browsers, so the new object descriptors apply
			fireEvent("plugin.propertybrowser.refresh");
		}
		catch (XMLDriverException e)
		{
			// Already logged, ignore here
			JMsgBox.show(null, e.getMessage(), JMsgBox.ICON_ERROR);
		}
	}

	/**
	 * Prints a Cockpit restart hint.
	 */
	public void displayRestartHint()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Issue a warning message that the Cockpit needs to be restarted
				String msg = getPluginResourceCollection().getRequiredString("warning.restartoption");
				JMsgBox.show(null, msg, JMsgBox.ICON_WARNING);
			}
		});
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/** Contains some classes that are likely to displayed by the model page's property browser */
	private static final Class[] objectClassPreloads =
	{
		Model.class, JavaActivityItem.class, ProcessItem.class, VisualItem.class,
	};

	/**
	 * Preloads certain configuration data.
	 */
	// TODO Feature 5 Private method unused; made protected to prevent compiler warning 
	protected void performPreload()
	{
		// Load the list of generators
		showStatusText(getPluginResourceCollection().getRequiredString("progress.loadingwizards"));
		GeneratorMgr.getInstance();

		showStatusText(getPluginResourceCollection().getRequiredString("progress.loadingobjectdescriptors"));
		NodeStructureMgr nsm = NodeStructureMgr.getInstance();
		for (int i = 0; i < objectClassPreloads.length; ++i)
		{
			Class cls = objectClassPreloads[i];
			nsm.loadNodeStructureFor(cls);
		}
	}

	//////////////////////////////////////////////////
	// @@ Help event module
	//////////////////////////////////////////////////

	/**
	 * This event module containts event handlers for the implementation of
	 * functions of the help menu.
	 */
	public class HelpEvents extends EventModule
	{
		public String getName()
		{
			return "cockpit";
		}

		/**
		 * Event handler: Display the help overview as PDF document.
		 *
		 * @event cockpit.help.manual
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode cockpit_help_manual(JaspiraActionEvent je)
		{
			openGuide(CockpitConstants.DOC_MANUAL);

			return EVENT_CONSUMED;
		}

		/**
		 * Event handler: Display the Java API documentation as HTML.
		 *
		 * @event cockpit.help.javaapi
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode cockpit_help_javaapi(JaspiraActionEvent je)
		{
			openGuide(CockpitConstants.DOC_JAVA_API);

			return EVENT_CONSUMED;
		}

		/**
		 * Opens a documentation file in the 'doc' directory of the OpenBP installation.
		 *
		 * @param name Name of the file to open
		 */
		private void openGuide(String name)
		{
			String path = Application.getRootDir() + StringUtil.FOLDER_SEP + "doc" + StringUtil.FOLDER_SEP + name;

			boolean pdf = path.endsWith(".pdf");

			Association as = new Association();
			if (pdf)
			{
				as.setAssociationTypes(new String[]
				{
					MimeTypes.APPLICATION_PDF
				});
			}
			else
			{
				as.setAssociationTypes(new String[]
				{
					MimeTypes.HTML_FILE, MimeTypes.URL
				});
			}
			as.setAssociationPriority(Association.PRIMARY);
			as.setAssociatedObject(path);
			boolean consumed = JaspiraEventMgr.fireGlobalEvent("plugin.association.tryopen", as);

			if (! consumed)
			{
				String msg = getPluginResourceCollection().getRequiredString(
					pdf ? "guide.noassociation.pdf" : "guide.noassociation.html");
				JMsgBox.show(null, msg, JMsgBox.ICON_WARNING);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main method.
	 * @nowarn
	 */
	public static void main(String[] args)
	{
		try
		{
			CoreModule.determineRootDir();
			Application.setArguments(args);

			Application.initialize();
			Application.registerPropertyResource("OpenBP-Cockpit.properties", 80, false);
			Application.registerPropertyResource("OpenBP-User.properties", 90, true);
			ConfigMgr.getInstance().setConfigResourceName("OpenBP-Cockpit-Config.properties");

			addResourceCollectionTextReplacement("openbp.application.name");
			addResourceCollectionTextReplacement("openbp.application.title");
			addResourceCollectionTextReplacement("openbp.cockpit.title");

			String applicationName = SettingUtil.getStringSetting("openbp.application.name", "OpenBP");
			String resourceName = ResourceCollectionMgr.RC_FOLDER + "/images/application/" + applicationName
				+ "Splash.png";

			if (! SettingUtil.getBooleanSetting("openbp.application.nosplash", false))
			{
				try
				{
					ResourceMgr resourceMgr = ResourceMgr.getDefaultInstance();
					byte[] imageData = resourceMgr.loadByteResource(resourceName);

					ImageIcon image = new ImageIcon(imageData);

					splashScreen = new SplashScreen(image);
					splashScreen.setLocationRelativeTo(null);
					SwingUtil.show(splashScreen);

					// Turn on the the wait cursor using the spash screen as frame
					SwingUtil.waitCursorOn(splashScreen);
				}
				catch (Exception e)
				{
					// Ignore any errors
				}
			}

			// Instantiate the application class and register it as a plugin
			// This will cause the ApplicationBase.initialize method to be called,
			// which in turn calls {@link #preInstallApplication} and {@link #postInstallApplication}.
			new Cockpit();
		}
		catch (Exception e)
		{
			if (splashScreen != null)
			{
				splashScreen.dispose();
			}
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
		finally
		{
			if (splashScreen != null)
			{
				splashScreen.dispose();
			}
		}
	}

	/**
	 * Initializes global resource text replacements.
	 *
	 * @param key Resource key
	 */
	private static void addResourceCollectionTextReplacement(String key)
	{
		String value = SettingUtil.getStringSetting(key, key);
		ResourceCollectionMgr.getDefaultInstance().addResourceVariable("$(" + key + ")", value);
	}
}
