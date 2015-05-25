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
package org.openbp.guiclient.model;

import java.util.List;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.generic.msgcontainer.StandardMsgContainer;
import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.EventObserverMgr;
import org.openbp.core.CoreModule;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypeDescriptor;
import org.openbp.core.model.item.ItemTypeRegistry;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.core.model.modelmgr.ModelNotificationService;
import org.openbp.core.remote.ClientSession;
import org.openbp.guiclient.remote.ServerConnection;
import org.openbp.swing.components.JMsgBox;

/**
 * The model connector provides access to models ({@link Model}) and items ({@link Item}) of models.
 * It is actually some kind of proxy class for the active implementation of the {@link ModelMgr} interface.
 * It adds some functionalities like observer management to the model manager.
 *
 * @author Heiko Erhardt
 */
public final class ModelConnector
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Model connector observer manager */
	private final EventObserverMgr observerMgr;

	/** Singleton instance */
	private static ModelConnector singletonInstance;

	/** Model manager wrapped by this class */
	private ModelMgr modelMgr;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance for this class.
	 * @nowarn
	 */
	public static synchronized ModelConnector getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ModelConnector();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ModelConnector()
	{
		observerMgr = new EventObserverMgr();
		observerMgr.setSupportedEventTypes(ModelConnectorEvent.getSupportedEventTypes());
	}

	/**
	 * Looks up the model manager reads the model tree from the server.
	 * @param coreModule The core module
	 *
	 * @throws OpenBPException If the server's model manager be located or there was an error reading the model tree
	 */
	public void initialize(CoreModule coreModule)
	{
		modelMgr = coreModule.getModelMgr();

		StandardMsgContainer msgContainer = modelMgr.getMsgContainer();
		msgContainer.clearMsgs();

		// Load all models
		modelMgr.readModels();

		// Initialize the models
		modelMgr.initializeModels();

		// Print any errors to stderr
		String errMsg = msgContainer.toString();
		if (errMsg != null && ! errMsg.equals(""))
		{
			msgContainer.clearMsgs();
			System.err.println(errMsg);
			System.err.println();
		}

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.MODELS_LOADED));
	}

	/**
	 * Gets the message container for validation error logging.
	 * @nowarn
	 */
	public StandardMsgContainer getMsgContainer()
	{
		return modelMgr != null ? modelMgr.getMsgContainer() : null;
	}

	//////////////////////////////////////////////////
	// @@ Static convenience methods
	//////////////////////////////////////////////////

	/**
	 * Convenience method: Saves the given item.
	 * The item can be a regular item or a model.
	 *
	 * @param item Item or model to save
	 * @param isNew
	 * true: The item is a new item and will be added to the model.<br>
	 * false: The item is an existing item. The model will be updated.
	 * @return
	 * true: The item was save successfully.<br>
	 * false: An error occurred while saving the item. An error message was displayed to the user.
	 */
	public boolean saveItem(Item item, boolean isNew)
	{
		try
		{
			// Add or update the item
			Model model = item.getModel();

			if (item instanceof Model)
			{
				Model modelToUpdate = (Model) item;

				if (isNew)
				{
					addModel(modelToUpdate);
				}
				else
				{
					updateModel(modelToUpdate);
				}
			}
			else
			{
				if (isNew)
				{
					addItem(model, item, true);
				}
				else
				{
					updateItem(item);
				}
			}
		}
		catch (ModelException e)
		{
			// If this method is being called during e. g. a 'focus lost' event,
			// displaying a message box will stall AWT's focus handling.
			// So we defer the message box display after the current event has been processed
			// by using JMsgBox.TYPE_OKLATER.
			String msg = ExceptionUtil.getNestedMessage(e);
			JMsgBox.show(null, msg, JMsgBox.ICON_ERROR | JMsgBox.TYPE_OKLATER);
			return false;
		}

		return true;
	}

	/**
	 * Gets a model specified by its qualifier.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model
	 * @throws OpenBPException If the specified model does not exist
	 */
	public Model getModelByQualifier(ModelQualifier modelQualifier)
	{
		return modelMgr.getModelByQualifier(modelQualifier);
	}

	/**
	 * Gets an (optional) model specified by its path name.
	 * Will not throw an exception if the model does not exist.
	 *
	 * @param modelQualifier Reference to the model
	 * @return The model or null
	 */
	public Model getOptionalModelByQualifier(ModelQualifier modelQualifier)
	{
		return modelMgr.getOptionalModelByQualifier(modelQualifier);
	}

	/**
	 * Gets a list of all top level models.
	 *
	 * @return A list of model descriptors ({@link Model} objects) or null
	 */
	public List getModels()
	{
		return modelMgr.getModels();
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Operations on models
	//////////////////////////////////////////////////

	/**
	 * Adds a new model
	 *
	 * @param model Model to add<br>
	 * The name of the model must have been set already.
	 * @throws OpenBPException If the model could not be created
	 */
	public synchronized void addModel(Model model)
	{
		modelMgr.addModel(model);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.MODEL_ADDED, model.getQualifier()));
	}

	/**
	 * Updates the properties of the model.
	 *
	 * @param model Model to update
	 * @throws OpenBPException If the model could not be updated
	 */
	public synchronized void updateModel(Model model)
	{
		// Create names from references if any
		model.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		modelMgr.updateModel(model);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.MODEL_UPDATED, model.getQualifier()));
	}

	/**
	 * Deletes a model including all its sub models and the contents (processes, actions)
	 * of these models.
	 *
	 * @param model Model to delete
	 * @throws OpenBPException On error.
	 * Note that depending on the error condition, some or all of the processes and/or sub models
	 * may already have been deleted when the error condition is raised.
	 */
	public synchronized void removeModel(Model model)
	{
		ModelQualifier qualifier = model.getQualifier();

		modelMgr.removeModel(model);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.MODEL_REMOVED, qualifier));
	}

	//////////////////////////////////////////////////
	// @@ Accessing model items
	//////////////////////////////////////////////////

	/**
	 * Gets a particular model item.
	 * Note that the qualifier must specify an existing item.
	 *
	 * @param qualifier Reference to the item
	 * @param required
	 * true: Will throw an exception if the item does not exist.<br>
	 * false: Will return null if the item does not exist.
	 * @return Item descriptors or null if the item does not exist.<br>
	 * Note that the appropriate subclass of the {@link Item} class will be returned.
	 * @throws OpenBPException On error
	 */
	public Item getItemByQualifier(ModelQualifier qualifier, boolean required)
	{
		return modelMgr.getItemByQualifier(qualifier, required);
	}

	//////////////////////////////////////////////////
	// @@ ModelMgr implementation: Operations items
	//////////////////////////////////////////////////

	/**
	 * Adds an item.
	 * Note that name and type of the item must be set in order to add it to the model.
	 *
	 * @param model Model the item shall belong to
	 * @param item Item to add
	 * @param syncGlobalReferences 
	 * true: Updates the names of external references (e. g. to data types)<br>
	 * false: Does not perform name updates
	 * @throws OpenBPException If the item could not be added
	 */
	public synchronized void addItem(Model model, Item item, boolean syncGlobalReferences)
	{
		item.setModel(model);

		// Make sure this is the only default process of the model; may throw an exception
		checkDefaultProcess(item);

		if (syncGlobalReferences)
		{
			item.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);
		}

		// Update the client model
		modelMgr.addItem(model, item, syncGlobalReferences);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.ITEM_ADDED, item.getQualifier()));
	}

	/**
	 * Updates the properties of an item.
	 * Note that the name of the item may not be changed. Use {@link #moveItem} for this.<br>
	 * The type of the item may never be changed once it was added to the model.
	 *
	 * @param item Item to update
	 * @throws OpenBPException If the item could not be updated
	 */
	public synchronized void updateItem(Item item)
	{
		// Make sure this is the only default process of the model; may throw an exception
		checkDefaultProcess(item);

		// The model shall use this model connector to resolve any references
		item.maintainReferences(ModelObject.SYNC_GLOBAL_REFNAMES | ModelObject.SYNC_LOCAL_REFNAMES);

		// Update the client model
		modelMgr.updateItem(item);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.ITEM_UPDATED, item.getQualifier()));
	}

	/**
	 * Deletes an item from a model.
	 *
	 * @param item Item to delete
	 * @throws OpenBPException On error
	 */
	public synchronized void removeItem(Item item)
	{
		ModelQualifier qualifier = item.getQualifier();

		// Now update the cache
		modelMgr.removeItem(item);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.ITEM_REMOVED, qualifier));
	}

	/**
	 * Moves an item.
	 * This can be used to simply rename an item within a model or to move an item to a new model.
	 *
	 * @param item Item to rename
	 * @param destinationQualifier New name of the item
	 * @throws OpenBPException On error. The item has not been renamed/moved in this case.
	 */
	public synchronized void moveItem(Item item, ModelQualifier destinationQualifier)
	{
		ModelQualifier sourceQualifier = item.getQualifier();

		if (sourceQualifier.equals(destinationQualifier))
			return;

		// Now update the cache
		modelMgr.moveItem(item, destinationQualifier);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.ITEM_RENAMED, destinationQualifier, sourceQualifier));
	}

	//////////////////////////////////////////////////
	// @@ Item types
	//////////////////////////////////////////////////

	/**
	 * Gets a list of item types.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of strings (see the constants of the {@link ItemTypes} class)
	 */
	public String[] getItemTypes(int mode)
	{
		return modelMgr.getItemTypes(mode);
	}

	/**
	 * Gets a list of item type descriptors.
	 *
	 * @param mode  {@link ItemTypeRegistry#ALL_TYPES} / {@link ItemTypeRegistry#SKIP_MODEL}|{@link ItemTypeRegistry#SKIP_INVISIBLE}
	 * @return A list of {@link ItemTypeDescriptor} objects or null
	 */
	public ItemTypeDescriptor[] getItemTypeDescriptors(int mode)
	{
		return modelMgr.getItemTypeDescriptors(mode);
	}

	/**
	 * Gets the item type descriptor of a particular item type.
	 *
	 * @param itemType Item type to look for
	 * @return The item type descriptor or null if the model does not support this item type
	 */
	public ItemTypeDescriptor getItemTypeDescriptor(String itemType)
	{
		return modelMgr.getItemTypeDescriptor(itemType);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Ensures that this is the only default process of the model.
	 *
	 * @param item Item to update
	 * @throws OpenBPException If the item could not be updated
	 */
	private void checkDefaultProcess(Item item)
	{
		if (! (item instanceof ProcessItem))
			// We check process items only
			return;

		ProcessItem process = (ProcessItem) item;
		if (! process.isDefaultProcess())
			// We need to take action for processes that are about to become the new default process only
			return;

		ProcessItem currentDefaultProcess = process.getModel().getDefaultProcess();
		if (currentDefaultProcess == null || currentDefaultProcess.getName().equals(process.getName()))
			// No default process yet or process to be updated is already the default process
			return;

		// Clear the default flag of the old default process and update the old default process first
		currentDefaultProcess.setDefaultProcess(false);

		// First, perform the operation at the server
		modelMgr.updateItem(currentDefaultProcess);

		fireEvent(new ModelConnectorEvent(ModelConnectorEvent.ITEM_UPDATED, currentDefaultProcess.getQualifier()));
	}

	//////////////////////////////////////////////////
	// @@ Model connector observation
	//////////////////////////////////////////////////

	/**
	 * Registers an observer.
	 *
	 * @param observer The observer
	 * @param eventTypes Lit of event types the observer wants to be notified of
	 * or null for all event types
	 */
	public void registerObserver(EventObserver observer, String[] eventTypes)
	{
		observerMgr.registerObserver(observer, eventTypes);
	}

	/**
	 * Unregisters an observer.
	 *
	 * @param observer The observer
	 */
	public void unregisterObserver(EventObserver observer)
	{
		observerMgr.unregisterObserver(observer);
	}

	/**
	 * Suspends broadcasting of model connector events.
	 *
	 * @return The previous suspend status
	 */
	public boolean suspendModelConnectorEvents()
	{
		return observerMgr.suspendObserverEvents();
	}

	/**
	 * Resumes broadcasting of model connector events.
	 */
	public void resumeModelConnectorEvents()
	{
		observerMgr.resumeObserverEvents();
	}

	/**
	 * Notifies all registered observers about a model connector event.
	 *
	 * @param event Model connector event to dispatch
	 */
	protected void fireEvent(ModelConnectorEvent event)
	{
		observerMgr.fireEvent(event);

		ModelNotificationService mns = (ModelNotificationService) ServerConnection.getInstance()
			.lookupOptionalService(ModelNotificationService.class);
		if (mns != null)
		{
			try
			{
				ClientSession session = ServerConnection.getInstance().getSession();
				ModelQualifier qualifier = event.getQualifier();
				String eventType = event.getEventType();

				// Notify the server from the model changes
				if (eventType == ModelConnectorEvent.MODEL_ADDED || eventType == ModelConnectorEvent.ITEM_ADDED)
				{
					mns.modelUpdated(session, qualifier, ModelNotificationService.ADDED);
				}
				else if (eventType == ModelConnectorEvent.MODEL_UPDATED || eventType == ModelConnectorEvent.ITEM_UPDATED)
				{
					mns.modelUpdated(session, qualifier, ModelNotificationService.UPDATED);
				}
				else if (eventType == ModelConnectorEvent.MODEL_REMOVED || eventType == ModelConnectorEvent.ITEM_REMOVED)
				{
					mns.modelUpdated(session, qualifier, ModelNotificationService.REMOVED);
				}
				else if (eventType == ModelConnectorEvent.MODEL_RENAMED || eventType == ModelConnectorEvent.ITEM_RENAMED)
				{
					ModelQualifier prevQualifier = event.getPreviousQualifier();
					mns.modelUpdated(session, prevQualifier, ModelNotificationService.REMOVED);
					mns.modelUpdated(session, qualifier, ModelNotificationService.ADDED);
				}
			}
			catch (ModelException e)
			{
				ExceptionUtil.printTrace(e);
			}
		}
	}
}
