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
package org.openbp.cockpit.plugins.association;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.common.string.StringUtil;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.OpenEventInfo;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.JaspiraEventMgr;
import org.openbp.jaspira.event.RequestEvent;
import org.openbp.jaspira.option.Option;
import org.openbp.jaspira.option.OptionMgr;
import org.openbp.jaspira.option.OptionWidget;
import org.openbp.jaspira.option.widget.PropertyBrowserWidget;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.OptionModule;
import org.openbp.swing.components.JMsgBox;

/**
 * Invisible plugin handling MIME type associations to external programs.
 *
 * @author Andreas Putz
 */
public class ExternalAssociationPlugin extends AbstractPlugin
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Association bean that contains the MIME type association list */
	private AssociationBean assocBean;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public ExternalAssociationPlugin()
	{
		XMLDriver xmlDriver = XMLDriver.getInstance();

		// Load mappings for the association beans
		try
		{
			xmlDriver.loadMapping(org.openbp.cockpit.plugins.association.AssociationBean.class);
			xmlDriver.loadMapping(org.openbp.cockpit.plugins.association.MimeTypeAssociation.class);
		}
		catch (XMLDriverException e)
		{
			ExceptionUtil.printTrace(e);
			return;
		}
	}

	public String getResourceCollectionContainerName()
	{
		return "plugin.cockpit";
	}

	//////////////////////////////////////////////////
	// @@ General association event handling methods
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class AssociationEvents extends EventModule
	{
		public String getName()
		{
			return "plugin.association";
		}

		/**
		 * Event handler: Check supported mime types for the open event.
		 * Adds the event names for open events for mime types supported by the associations of the association plugin
		 * to the result of the poll event.
		 *
		 * @event plugin.association.supports
		 * @param event Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode supports(RequestEvent event)
		{
			String mimeType = (String) event.getObject();

			// Check if there is an association for this MIME type
			MimeTypeAssociation mta = getMimeTypeAssociation(mimeType);

			if (mta != null && mta.getAssociatedProgram() != null)
			{
				event.addResult(new OpenEventInfo("plugin.externalassociation.openfile", mimeType, mta.getDescription()));
				return EVENT_HANDLED;
			}

			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ External association event handling methods
	//////////////////////////////////////////////////

	/**
	 * Event module.
	 */
	public class ExternalAssociationEvents extends EventModule
	{
		public String getName()
		{
			return "plugin.externalassociation";
		}

		/**
		 * Event handler: Open a file.
		 * Normalizes separator characters and checks for file existence
		 * if the file name does not appear to be an URL before
		 * attempting to execute the openTarget method.
		 *
		 * @event plugin.externalassociation.openfile
		 * @param openEvent Open event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode openfile(OpenEvent openEvent)
		{
			String fileName = (String) openEvent.getObject();
			if (fileName == null)
				return EVENT_IGNORED;

			boolean isUrl = fileName.startsWith("http:") || fileName.startsWith("https:") || fileName.startsWith("ftp:");

			if (!isUrl)
			{
				// Normalize path
				fileName = StringUtil.normalizePathName(fileName);

				// Check for file existence
				File f = new File(fileName);
				if (!f.exists())
				{
					String msg = ResourceCollectionUtil.formatMsg(getPluginResourceCollection(), "messages.filedoesnotexist", new Object [] { fileName });
					JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
					return EVENT_CONSUMED;
				}
			}

			if (doOpenTarget(fileName, openEvent.getMimeTypes()))
				return EVENT_CONSUMED;

			return EVENT_IGNORED;
		}

		/**
		 * Event handler: The external MIME type assocations have been changed in the options dialog.
		 *
		 * @event associations.mimetypes
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode associations_mimetypes(JaspiraEvent je)
		{
			// Force an update of the MIME type cache
			clearCache();

			// Also update the associations chache
			JaspiraEventMgr.fireGlobalEvent("plugin.association.update");

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Opens the given target using an external program.
	 *
	 * @param target File name or URL
	 * @param mimeTypes Mime types
	 * @return
	 * true: If an external association for the given MIME type exists (whether it could be executed or not)<br>
	 * false: Otherwise
	 */
	private boolean doOpenTarget(String target, String [] mimeTypes)
	{
		// Check if there is an association for one of the given MIME types
		for (int i = 0; i < mimeTypes.length; ++i)
		{
			MimeTypeAssociation mta = getMimeTypeAssociation(mimeTypes [i]);

			if (mta == null)
				continue;

			String program = mta.getAssociatedProgram();
			if (program != null)
			{
				// Yes, there is
				// Execute the program
				try
				{
					String [] cmdArray = new String [] { program, target };

					Runtime.getRuntime().exec(cmdArray);
				}
				catch (IOException e)
				{
					String msg = ResourceCollectionUtil.formatMsg(getPluginResourceCollection(), "messages.execerror", new Object [] { program });
					JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
				}

				return true;
			}
		}

		// We are not responsible for this MIME type
		return false;
	}

	/**
	 * Gets the MIME type association - if any - for the given MIME type.
	 *
	 * @param mimeType Mime type
	 * @return The association if there is one (and it contains an external program) or null
	 */
	public MimeTypeAssociation getMimeTypeAssociation(String mimeType)
	{
		loadAssociations();

		// Check if there is an association for this MIME type
		return assocBean.getMimeTypeAssociation(mimeType);
	}

	/**
	 * Loads the association list if not yet done.
	 */
	private void loadAssociations()
	{
		if (assocBean == null)
		{
			assocBean = (AssociationBean) OptionMgr.getInstance().getOption("associations.mimetypes").getValue();
		}
	}

	/**
	 * Clears the cache of MIME type associations.
	 */
	private void clearCache()
	{
		assocBean = null;
	}

	//////////////////////////////////////////////////
	// @@ Option module
	//////////////////////////////////////////////////

	/**
	 * Option module.
	 */
	public class AssociationOptionModule extends OptionModule
	{
		//////////////////////////////////////////////////
		// @@ Options
		//////////////////////////////////////////////////

		/**
		 * Option to define the input directory containing the format files.
		 */
		public class AssociationOption extends Option
		{
			//////////////////////////////////////////////////
			// @@ Construction
			//////////////////////////////////////////////////

			/**
			 * Constructor.
			 */
			public AssociationOption()
			{
				super(getPluginResourceCollection(), "associations.mimetypes", new AssociationBean());
			}

			//////////////////////////////////////////////////
			// @@ Option implementation
			//////////////////////////////////////////////////

			/**
			 * @see org.openbp.jaspira.option.Option#createOptionWidget()
			 */
			public OptionWidget createOptionWidget()
			{
				// Do not provide our own resource, we use the property browser's standard resource
				return new PropertyBrowserWidget(this, null, 1);
			}

			/**
			 * @see org.openbp.jaspira.option.Option#saveToString()
			 */
			public String saveToString()
			{
				return ((AssociationBean) getValue()).serialize();
			}

			/**
			 * @see org.openbp.jaspira.option.Option#loadFromString(String)
			 */
			public Object loadFromString(String cryptString)
			{
				return AssociationBean.deserialize(cryptString);
			}

			/**
			 * @see org.openbp.jaspira.option.Option#getValue()
			 */
			public Object getValue()
			{
				AssociationBean associations = (AssociationBean) super.getValue();

				List assocList = associations.getMimeTypes();
				assocList = AssociationMimeTypesUtil.addDefaultAssociations(assocList, ExternalAssociationPlugin.this.getPluginResourceCollection());
				associations.setMimeTypes(assocList);

				return associations;
			}
		}
	}
}
