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
package org.openbp.core;

import org.openbp.common.CommonRegistry;
import org.openbp.common.application.Application;
import org.openbp.common.application.ProductProfile;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.setting.SettingUtil;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.modelmgr.ClassPathModelMgr;
import org.openbp.core.model.modelmgr.FileSystemModelMgr;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.core.model.modelmgr.MultiplexModelMgr;
import org.openbp.core.uiadapter.ExternalAdapterDescriptor;
import org.openbp.core.uiadapter.UIAdapterDescriptorRegistry;

/**
 * This class holds all initialization tasks the core part of OpenBP requires.
 *
 * Note that you must call the initialize method before using any OpenBP core or server function.
 *
 * @author Heiko Erhardt
 */
public class CoreModule
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Copyright 1st line */
	private String copyright1 = "(c) 2005-2009 skynamics AG";

	/** Product profile */
	private ProductProfile productProfile;

	/** Startup message printed */
	private boolean startupMessagePrinted;

	/** Item type registry */
	private ItemTypeRegistry itemTypeRegistry;

	/** Model manager */
	private ModelMgr modelMgr;

	/** Mappings required by the core */
	private static Class [] mappedClasses = { org.openbp.common.generic.description.DescriptionObjectImpl.class, org.openbp.common.generic.description.DisplayObjectImpl.class,

	org.openbp.common.generic.propertybrowser.CollectionDescriptor.class, org.openbp.common.generic.propertybrowser.ObjectDescriptor.class, org.openbp.common.generic.propertybrowser.PropertyDescriptor.class, org.openbp.common.generic.taggedvalue.TaggedValue.class,

	org.openbp.core.handler.HandlerDefinition.class, org.openbp.core.model.ModelObjectImpl.class, 

	org.openbp.core.model.item.ConfigurationBeanImpl.class, org.openbp.core.model.item.ItemImpl.class, org.openbp.core.model.item.ItemContainer.class, org.openbp.core.model.item.ItemTypeDescriptor.class,

	org.openbp.core.model.WorkflowTaskDescriptor.class,

	org.openbp.core.model.item.type.DataTypeItemImpl.class, org.openbp.core.model.item.type.SimpleTypeItemImpl.class, org.openbp.core.model.item.type.SimpleTypeItemStringImpl.class, org.openbp.core.model.item.type.SimpleTypeItemBooleanImpl.class, org.openbp.core.model.item.type.SimpleTypeItemDateImpl.class, org.openbp.core.model.item.type.SimpleTypeItemDecimalImpl.class, org.openbp.core.model.item.type.SimpleTypeItemDoubleImpl.class, org.openbp.core.model.item.type.SimpleTypeItemFloatImpl.class, org.openbp.core.model.item.type.SimpleTypeItemIntImpl.class, org.openbp.core.model.item.type.SimpleTypeItemLongImpl.class, org.openbp.core.model.item.type.SimpleTypeItemObjectImpl.class, org.openbp.core.model.item.type.SimpleTypeItemShortImpl.class, org.openbp.core.model.item.type.SimpleTypeItemTimeImpl.class, org.openbp.core.model.item.type.SimpleTypeItemTimestampImpl.class, org.openbp.core.model.item.type.DataMemberImpl.class, org.openbp.core.model.item.type.ComplexTypeItemImpl.class,

	org.openbp.core.model.item.activity.ActivityParamImpl.class, org.openbp.core.model.item.activity.ActivitySocketImpl.class, org.openbp.core.model.item.activity.ActivityItemImpl.class, org.openbp.core.model.item.activity.JavaActivityItemImpl.class,

	org.openbp.core.model.item.process.ProcessObjectImpl.class,

	org.openbp.core.model.item.process.ParamImpl.class, org.openbp.core.model.item.process.NodeParamImpl.class, org.openbp.core.model.item.process.NodeSocketImpl.class, org.openbp.core.model.item.process.NodeImpl.class,

	org.openbp.core.model.item.process.SingleSocketNodeImpl.class, org.openbp.core.model.item.process.MultiSocketNodeImpl.class, org.openbp.core.model.item.process.ActivityNodeImpl.class, org.openbp.core.model.item.process.DecisionNodeImpl.class, org.openbp.core.model.item.process.InitialNodeImpl.class, org.openbp.core.model.item.process.FinalNodeImpl.class, org.openbp.core.model.item.process.ForkNodeImpl.class, org.openbp.core.model.item.process.MergeNodeImpl.class, org.openbp.core.model.item.process.JoinNodeImpl.class, org.openbp.core.model.item.process.PlaceholderNodeImpl.class, org.openbp.core.model.item.process.VisualNodeImpl.class, org.openbp.core.model.item.process.WaitStateNodeImpl.class, org.openbp.core.model.item.process.WebServiceNodeImpl.class, org.openbp.core.model.item.process.WorkflowNodeImpl.class, org.openbp.core.model.item.process.WorkflowEndNodeImpl.class, org.openbp.core.model.item.process.SubprocessNodeImpl.class,

	org.openbp.core.model.item.process.ProcessVariableImpl.class, org.openbp.core.model.item.process.NodeGroupImpl.class, org.openbp.core.model.item.process.ControlLinkImpl.class, org.openbp.core.model.item.process.DataLinkImpl.class, org.openbp.core.model.item.process.TextElementImpl.class,

	org.openbp.core.model.item.process.ProcessItemImpl.class,

	org.openbp.core.model.item.visual.VisualItemImpl.class,

	org.openbp.core.model.ModelImpl.class, };

	/** XML encoding (property in OpenBP-Server\.properties or OpenBP-Cockpit\.properties) */
	private static final String XML_ENCODING_PROP = "openbp.XML.encoding";

	/** XML pretty print flag (property in OpenBP-Server\.properties or OpenBP-Cockpit\.properties) */
	private static final String XML_PRETTYPRINT_PROP = "openbp.XML.prettyPrint";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	public CoreModule()
	{
		// For now, use the standard OpenBP product profile
		setProductProfile(new OpenBPProfile());

		itemTypeRegistry = new ItemTypeRegistry();
	}

	/**
	 * Prints a startup message to stanard out and logs the product name and version to the log file.
	 */
	private void printStartupMessage()
	{
		if (!startupMessagePrinted)
		{
			// Write product info to stdout.
			ProductProfile profile = getProductProfile();
			System.out.println();
			System.out.println(profile.getFullProductName() + " V " + profile.getVersion() + " build " + profile.getBuildNumber());
			System.out.println(copyright1);
			System.out.println();

			// Write product info into the log file.
			LogUtil.info(getClass(), profile.getFullProductName() + " V " + profile.getVersion() + " build " + profile.getBuildNumber());

			startupMessagePrinted = true;
		}
	}

	//////////////////////////////////////////////////
	// @@ Initialization/shutdown
	//////////////////////////////////////////////////

	/**
	 * Initializes the core.
	 * Call this method before requesting any services from the core.
	 */
	public void initialize()
	{
		printStartupMessage();

		// Initialize the root directory if not already done
		determineRootDir();
		Application.initialize();
		Application.defineRootDirSettingName(CoreConstants.SYSPROP_OPENBP_HOME);
		Application.defineRootDirSettingName(CoreConstants.SYSVAR_OPENBP_HOME);

		// Load all mappings we require
		initXmlMappings();

		// Initialize the user interface adapters
		initUIAdapters();

		initModelManager();
	}

	/**
	 * Loads the set of XML mappings required by this module.
	 */
	public void initXmlMappings()
	{
		LogUtil.info(getClass(), "Loading XML mappings...");

		// Use the class loader that loaded the OpenBP core to resolve XML mappings
		XMLDriver.setDefaultClassLoader(getClass().getClassLoader());

		// Load standard mappings
		XMLDriver driver = XMLDriver.getInstance();
		String encoding = SettingUtil.getStringSetting(XML_ENCODING_PROP);
		if (encoding != null)
		{
			driver.setEncoding(encoding);
		}
		boolean prettyPrint = SettingUtil.getBooleanSetting(XML_PRETTYPRINT_PROP, false);
		driver.setPrettyPrint(prettyPrint);
		if (!driver.loadMappings(mappedClasses))
			throw new OpenBPException("Initialization.Mapping", "Errors occurred while loading the core mappings. See the log file for details.");
	}

	/**
	 * Initializes the user interface adapters.
	 */
	protected void initUIAdapters()
	{
		UIAdapterDescriptorRegistry.getInstance().addAdapterDescriptor(new ExternalAdapterDescriptor());
	}

	/**
	 * Sets up the model manager.
	 * Instantiates a default model manager if no one has been supplied in the Spring configuration file.
	 * By default, we support both class path-based models and file system-based models.
	 */
	protected void initModelManager()
	{
		LogUtil.info(getClass(), "Loading component types...");
		itemTypeRegistry.loadStandardItemTypeDescriptors();

		if (getModelMgr() == null)
		{
			// TODO Cleanup 4 This should be removed as soon as the Cockpit is being wired by Spring also
			MultiplexModelMgr mmm = new MultiplexModelMgr();
			mmm.setManagers(new ModelMgr [] { new ClassPathModelMgr(), new FileSystemModelMgr() });
			setModelMgr(mmm);
		}
		getModelMgr().setItemTypeRegistry(itemTypeRegistry);

		// TODO Cleanup 4 Remove this also as soon as the TokenContextImpl does not access the ModelMgr using the CommonRegistry any more
		// Save the model mgr to the CommonRegistry, so we can access it from within
		// the TokenContextImpl code after being constructed by the O/R mapper
		// and from various locations from within the Cockpit
		CommonRegistry.register(getModelMgr());
	}

	//////////////////////////////////////////////////
	// @@ Shutdown
	//////////////////////////////////////////////////

	/**
	 * Performs shutdown of the core.
	 *
	 * By default, this method does nothing.
	 * However, if you derive classes from this one, make sure to call super.shutdown for future compatibility.
	 *
	 * @param unregisterHook
	 *		true	Unregister the VM's shutdown hook. Note that this may not be done during VM shutdown! (when invoked due to servlet reaload etc.)<br>
	 *		false	Do not unregister the hook (when invoked due to VM termination)
	 * @return true if were no exceptions during shutdown (always in this case).
	 */
	public boolean shutdown(boolean unregisterHook)
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the model manager.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		return modelMgr;
	}

	/**
	 * Sets the model manager.
	 * @nowarn
	 */
	public void setModelMgr(ModelMgr modelMgr)
	{
		this.modelMgr = modelMgr;
	}

	/**
	 * Gets the product profile.
	 * @nowarn
	 */
	public ProductProfile getProductProfile()
	{
		return productProfile;
	}

	/**
	 * Sets the product profile.
	 * @nowarn
	 */
	public void setProductProfile(ProductProfile productProfile)
	{
		this.productProfile = productProfile;
	}

	/**
	 * Determines the root dir from the system property "openbp.home" or the environment variable OPENBP_HOME.
	 */
	public static void determineRootDir()
	{
		String rootDir = Application.getRootDir();
		if (rootDir == null)
		{
			rootDir = System.getProperty(CoreConstants.SYSPROP_OPENBP_HOME);
		}
		if (rootDir == null)
		{
			rootDir = System.getenv(CoreConstants.SYSVAR_OPENBP_HOME);
		}
		Application.setRootDir(rootDir);
	}
}
