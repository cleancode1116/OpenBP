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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openbp.cockpit.itemeditor.NodeItemEditorPlugin;
import org.openbp.common.CollectionUtil;
import org.openbp.common.rc.ResourceCollectionUtil;
import org.openbp.core.model.Association;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.item.Item;
import org.openbp.guiclient.event.FileOpenEvent;
import org.openbp.guiclient.event.OpenEvent;
import org.openbp.guiclient.event.OpenEventInfo;
import org.openbp.guiclient.util.ClientFlavors;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.InteractionEvent;
import org.openbp.jaspira.event.JaspiraEvent;
import org.openbp.jaspira.event.JaspiraEventHandlerCode;
import org.openbp.jaspira.event.RequestEvent;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.EventModule;
import org.openbp.jaspira.plugin.InteractionModule;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.swing.components.JMsgBox;

/**
 * Invisible plugin that provides generic mechanisms for associations handling.
 *
 * @author Andreas Putz
 */
public class AssociationPlugin extends AbstractPlugin
{
	/** Table mapping mime-types (Strings) to open event infos (OpenEventInfo []) */
	private Map mimeTypesToOpenEventInfos;

	/**
	 * Constructor.
	 */
	public AssociationPlugin()
	{
		mimeTypesToOpenEventInfos = new Hashtable();
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
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 2;
		}

		/**
		 * Event handler: Opens a model object in the associated editor.
		 *
		 * @event plugin.association.open
		 * @param event Client event containing a transferable as object
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode open(JaspiraEvent event)
		{
			return eventOpen(event, Association.PRIMARY);
		}

		/**
		 * Event handler: Opens a model object in the associated editor.
		 *
		 * @event plugin.association.tryopen
		 * @param event Client event containing a transferable as object
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode tryopen(JaspiraEvent event)
		{
			return eventTryOpen(event, Association.PRIMARY);
		}

		/**
		 * Event handler: The associations should be updated.
		 * Clears the associations cache.
		 *
		 * @event plugin.association.update
		 * @param je Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode update(JaspiraEvent je)
		{
			clearCache();

			return EVENT_HANDLED;
		}

		private JaspiraEventHandlerCode eventOpen(JaspiraEvent event, int priority)
		{
			JaspiraEventHandlerCode ret = eventTryOpen(event, priority);

			if (ret == EVENT_IGNORED)
			{
				// Open failed, issue an error message

				// Figure out the possible target MIME types for the msg
				String [] mimeTypes = null;

				// Open failed
				Object o = event.getObject();

				if (o instanceof Transferable)
				{
					Transferable transferable = (Transferable) event.getObject();

					if (transferable.isDataFlavorSupported(ClientFlavors.MODEL_OBJECT))
					{
						try
						{
							ModelObject mo = (ModelObject) transferable.getTransferData(ClientFlavors.MODEL_OBJECT);

							List associations = mo.getAssociations();
							if (associations != null)
							{
								int n = associations.size();
								for (int i = 0; i < n; ++i)
								{
									Association assoc = (Association) associations.get(i);
									if (assoc.getAssociationPriority() == priority)
									{
										mimeTypes = assoc.getAssociationTypes();
										break;
									}
								}
							}
						}
						catch (IOException e)
						{
							return EVENT_IGNORED;
						}
						catch (UnsupportedFlavorException e)
						{
							// Does not happen
							return EVENT_IGNORED;
						}
					}
				}
				else if (o instanceof Association)
				{
					mimeTypes = ((Association) o).getAssociationTypes();
				}
				else if (event instanceof OpenEvent)
				{
					mimeTypes = ((OpenEvent) event).getMimeTypes();
				}

				String mimeTypeStr = "?\n";
				if (mimeTypes != null)
				{
					StringBuffer sb = new StringBuffer();

					for (int i = 0; i < mimeTypes.length; ++i)
					{
						sb.append(mimeTypes [i]);
						sb.append("\n");
					}

					mimeTypeStr = sb.toString();
				}

				String msg = ResourceCollectionUtil.formatMsg(getPluginResourceCollection(), "messages.noassociation", new Object [] { mimeTypeStr });
				JMsgBox.show(null, msg, JMsgBox.ICON_ERROR);
			}

			return ret;
		}

		private JaspiraEventHandlerCode eventTryOpen(JaspiraEvent event, int priority)
		{
			Object o = event.getObject();

			if (event instanceof OpenEvent)
			{
				JaspiraEventHandlerCode ret = doOpen(null, false, event);
				if (ret != EventModule.EVENT_IGNORED)
					return ret;
			}
			else if (o instanceof Transferable)
			{
				Transferable transferable = (Transferable) o;

				if (transferable.isDataFlavorSupported(ClientFlavors.MODEL_OBJECT))
				{
					ModelObject mo = null;
					try
					{
						mo = (ModelObject) transferable.getTransferData(ClientFlavors.MODEL_OBJECT);
					}
					catch (IOException e)
					{
						return EVENT_IGNORED;
					}
					catch (UnsupportedFlavorException e)
					{
						// Does not happen
						return EVENT_IGNORED;
					}

					if (mo != null)
					{
						List associations = mo.getAssociations();
						if (associations != null)
						{
							int n = associations.size();
							for (int i = 0; i < n; ++i)
							{
								Association assoc = (Association) associations.get(i);
								if (assoc.getAssociationPriority() == priority)
								{
									
									JaspiraEventHandlerCode ret = doOpen(assoc, !mo.isModifiable(), event);
									if (ret != EventModule.EVENT_IGNORED)
										return ret;
								}
							}
						}
						else
						{
							// No associations defined for this model object;
							// Assume this is ok and prevent an error message
							// TODONOW return EVENT_CONSUMED;
						}
					}
				}
			}
			else if (o instanceof Association)
			{
				Association assoc = (Association) o;
				if (assoc.getAssociationPriority() == Association.PRIMARY)
				{
					JaspiraEventHandlerCode ret = doOpen(assoc, false, event);
					if (ret != EventModule.EVENT_IGNORED)
						return ret;
				}
			}

			return EVENT_IGNORED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Interaction module
	//////////////////////////////////////////////////

	/**
	 * Interaction module.
	 */
	public class InteractionEvents extends InteractionModule
	{
		/**
		 * Gets the module priority.
		 * We are high priority.
		 *
		 * @return The priority. 0 is lowest, 100 is highest.
		 */
		public int getPriority()
		{
			return 1;
		}

		/**
		 * Standard event handler that is called when a popup menu is to be shown.
		 * Adds the 'Open' menu entries for model objects.
		 *
		 * @event global.interaction.popup
		 * @param ie Event
		 * @return The event status code
		 */
		public JaspiraEventHandlerCode popup(InteractionEvent ie)
		{
			if (!ie.isDataFlavorSupported(ClientFlavors.MODEL_OBJECT))
			{
				// Only a model object has associations.
				return EVENT_IGNORED;
			}

			if (ie.getSourcePlugin() instanceof NodeItemEditorPlugin)
			{
				// We do not display an 'Open' menu in the item editor.
				return EVENT_IGNORED;
			}

			ModelObject mo = (ModelObject) ie.getSafeTransferData(ClientFlavors.MODEL_OBJECT);

			List associations = mo.getAssociations();
			if (associations != null)
			{
				JaspiraAction group = new JaspiraAction(AssociationPlugin.this, "submenu.open");

				int n = associations.size();
				for (int iAssoc = 0; iAssoc < n; ++iAssoc)
				{
					Association association = (Association) associations.get(iAssoc);

					String hintMsg = null;
					OpenEventInfo [] openEventInfo = null;

					if (association.getAssociatedObject() == null)
					{
						// Association present, but no associated object.
						// The association contains a hint message, e. g. "Object has not yet been generated"
						hintMsg = association.getHintMsg();
					}
					else
					{
						// Check the mime types supported by this object
						String [] types = association.getAssociationTypes();
						if (types == null || types.length == 0)
						{
							hintMsg = getPluginResourceCollection().getRequiredString("messages.hints.noassociation");
						}
						else
						{
							// Now check if any plugin is able to open this kind of MIME type
							// The association manager will broadcast the plugin.association.supports event in order
							// to determine the open event name that can be used to open the object of the association
							openEventInfo = determineOpenEvents(types, AssociationPlugin.this);

							if (openEventInfo == null)
							{
								hintMsg = getPluginResourceCollection().getRequiredString("messages.hints.wrongassociation");
							}
						}
					}

					if (openEventInfo != null)
					{
						// We are able to open this object.
						// Create an open action for each open event information
						for (int iEvent = 0; iEvent < openEventInfo.length; ++iEvent)
						{
							OpenAction openAction = new OpenAction(association, openEventInfo [iEvent], !mo.isModifiable());
							openAction.setPriority(iAssoc * 10 + iEvent);
							group.addMenuChild(openAction);
						}
					}
					else
					{
						// Open not possible.
						// Create a dummy action for the association that displays a hint message
						JaspiraAction emptyAction = new JaspiraAction(association);
						emptyAction.setDescription(hintMsg);
						emptyAction.setEnabled(false);
						emptyAction.setPriority(iAssoc * 10);
						group.addMenuChild(emptyAction);
					}
				}

				ie.add(group);
			}

			return EVENT_HANDLED;
		}
	}

	//////////////////////////////////////////////////
	// @@ Action listener
	//////////////////////////////////////////////////

	/**
	 * Action listener opens programs by the association.
	 */
	private class OpenAction extends JaspiraAction
	{
		/** Association */
		private Association association;

		/** Open event info */
		private OpenEventInfo openEventInfo;

		/** Flag: Open the object in read only mode */
		private boolean readOnly;

		/**
		 * Constructor.
		 * The action retrieves its properties from the resources of its owner plugin.
		 *
		 * @param association Association to execute
		 * @param openEventInfo Open event info
		 * @param readOnly Flag: Open the object in read only mode
		 */
		public OpenAction(Association association, OpenEventInfo openEventInfo, boolean readOnly)
		{
			super(association);

			this.association = association;
			this.openEventInfo = openEventInfo;
			this.readOnly = readOnly;

			String s = openEventInfo.getDescription();
			if (s != null)
			{
				String d = getDisplayName();
				if (d != null)
				{
					d += " (" + s + ")";
				}
				else
				{
					d = s;
				}
				setDisplayName(d);

				d = "Opens the object in the " + s;
				setDescription(d);
			}
		}

		/**
		 * Executes the action.
		 *
		 * @param ae Event
		 */
		public void actionPerformed(ActionEvent ae)
		{
			// Broadcast the open event we have determined for this object
			OpenEvent oe = new OpenEvent(AssociationPlugin.this, openEventInfo.getEventName(), association.getAssociatedObject());
			oe.setUnderlyingObject(association.getUnderlyingObject());
			oe.setReadonly(readOnly);
			oe.setCreate(true);
			oe.setMimeTypes(new String [] { openEventInfo.getMimeType() });

			fireEvent(oe);
		}
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/** Dummy if no open events for a particular mime type could be found */
	private static final OpenEventInfo [] noOpenEvents = new OpenEventInfo [0];

	/**
	 * Call a associated program.
	 *
	 * @param association The association or null
	 * @param readOnly The readOnly flag or null
	 * @param event Event that invoked this action
	 * @return The event code
	 */
	private JaspiraEventHandlerCode doOpen(Association association, boolean readOnly, JaspiraEvent event)
	{
		String [] types = null;

		if (association != null)
		{
			types = association.getAssociationTypes();
		}
		else if (event instanceof OpenEvent)
		{
			types = ((OpenEvent) event).getMimeTypes();
		}

		if (types != null && types.length != 0)
		{
			if (event instanceof OpenEvent && ((OpenEvent) event).isReadonly())
				readOnly = true;

			OpenEventInfo [] openEventInfo = determineOpenEvents(types, AssociationPlugin.this);

			if (openEventInfo != null)
			{
				Object object = null;
				Object underlyingObject = null;
				if (association != null)
				{
					object = association.getAssociatedObject();
					underlyingObject = association.getUnderlyingObject();
				}
				else
				{
					object = event.getObject();
				}

				OpenEvent oe = null;
				String mimeType = openEventInfo [0].getMimeType();
				if (mimeType.startsWith("text/") && (underlyingObject instanceof Item))
				{
					FileOpenEvent foe = new FileOpenEvent(AssociationPlugin.this, openEventInfo [0].getEventName(), (String) object, new String [] { openEventInfo [0].getMimeType() });

					foe.setUnderlyingObject(underlyingObject);
					foe.setReadonly(readOnly);
					foe.setCreate(true);

					if (event instanceof FileOpenEvent)
					{
						foe.setColumnNumber(((FileOpenEvent) event).getColumnNumber());
						foe.setLineNumber(((FileOpenEvent) event).getLineNumber());
					}

					oe = foe;
				}
				else
				{
					oe = new OpenEvent(AssociationPlugin.this, openEventInfo [0].getEventName(), object);

					oe.setUnderlyingObject(underlyingObject);
					oe.setReadonly(readOnly);
					oe.setCreate(true);
					oe.setMimeTypes(new String [] { openEventInfo [0].getMimeType() });
				}

				if (fireEvent(oe))
					return EventModule.EVENT_HANDLED;
			}
		}

		return EventModule.EVENT_IGNORED;
	}

	/**
	 * Determines the event name to open an object using one of the given mime types.
	 * The method will iterate the given mime types and check if there are any plugins
	 * that accept the mim type. If yes, it will return the event name communicated by the
	 * plugins and the corresponding mime type. Note that the first mime type that has
	 * been accepted will be considered only.
	 *
	 * @param mimeTypes Mime types to iterate
	 * @param sourcePlugin Source plugin
	 *
	 * @return The search result or null if not appropriate plugin was found.<br>
	 *			array [0] = event name<br>
	 *			array [1] = mime-type
	 */
	OpenEventInfo [] determineOpenEvents(String [] mimeTypes, Plugin sourcePlugin)
	{
		if (mimeTypes != null)
		{
			for (int i = 0; i < mimeTypes.length; i++)
			{
				OpenEventInfo [] ois = determineOpenEvents(mimeTypes [i], sourcePlugin);
				if (ois != null)
					return ois;
			}
		}

		return null;
	}

	/**
	 * Searches for plugins that support opening the given MIME type.
	 *
	 * @param mimeType The identifier
	 * @param sourcePlugin The source plugin
	 *
	 * @return A list of {@link OpenEventInfo} objects that describe the events that can be used
	 * to open an object of the given MIME type or null if no such plugins have been found
	 */
	OpenEventInfo [] determineOpenEvents(String mimeType, Plugin sourcePlugin)
	{
		OpenEventInfo [] ois = (OpenEventInfo []) mimeTypesToOpenEventInfos.get(mimeType);
		if (ois != null)
		{
			if (ois.length != 0)
				return ois;
			return null;
		}

		// Check for mime-type opener
		RequestEvent requestEvent = new RequestEvent(sourcePlugin, "plugin.association.supports", mimeType);
		sourcePlugin.fireEvent(requestEvent);

		List resultList = requestEvent.getResultList();
		if (resultList == null)
		{
			mimeTypesToOpenEventInfos.put(mimeType, noOpenEvents);
			return null;
		}

		ois = (OpenEventInfo []) CollectionUtil.toArray(resultList, OpenEventInfo.class);
		mimeTypesToOpenEventInfos.put(mimeType, ois);

		return ois;
	}

	/**
	 * Clears the associations cache.
	 * Cause the manager to re-determine the associations.
	 */
	public void clearCache()
	{
		mimeTypesToOpenEventInfos.clear();
	}
}
