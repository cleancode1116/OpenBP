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
package org.openbp.core.model.item.process;

import java.util.Iterator;
import java.util.List;

import org.openbp.common.generic.Copyable;
import org.openbp.common.util.NamedObjectCollectionUtil;
import org.openbp.core.handler.HandlerDefinition;
import org.openbp.core.model.Association;
import org.openbp.core.model.AssociationUtil;
import org.openbp.core.model.ModelObjectSymbolNames;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.activity.ActivitySocket;
import org.openbp.core.model.item.activity.ActivitySocketImpl;
import org.openbp.core.model.item.activity.JavaActivityItem;

/* TODONOW


			if (configName.startsWith("<"))
			{
				// Direct configuration specification, used for test purposes only
				try
				{
					String configText = CONFIG_PREFIX + configName + CONFIG_POSTFIX;
					ByteArrayResource res = new ByteArrayResource(configText.getBytes(), "inline configuration");
					GenericApplicationContext ctx = new GenericApplicationContext();

					try
					{
						XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
						xmlReader.loadBeanDefinitions(res);
						descriptor = (ProcessConfigDescriptor) ctx.getBean(DESCRIPTOR_BEAN_NAME);
					}
					finally
					{
						ctx.close();
					}

					storeDescriptor(descriptor, key);
				}
				catch (Exception e)
				{
					String msg = LogUtil.error(getClass(), "Error reading inline XML process configuration.", e).getMessage();
					throw new ConfigurationException(msg);
				}
			}
			else
			{
				// Configuration file specification, look for config XML file in class path
				String configFile = CONFIG_DIR + "/" + configName;
				if (!configFile.endsWith(".xml"))
					configFile += ".xml";

				try
				{
					ResourceLoader resourceLoader = (ResourceLoader) CommonRegistry.lookup(ResourceLoader.class);
					ResourceMgr resourceMgr = new ResourceMgr(resourceLoader);
					Resource [] resources = resourceMgr.findResources(configFile);

					for (int i = 0; i < resources.length; ++i)
					{
						String resourceLocation = resources[i].getURL().toString();
						ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(resourceLocation);
						try
						{
							descriptor = (ProcessConfigDescriptor) ctx.getBean(DESCRIPTOR_BEAN_NAME);
						}
						catch (ResourceException e)
						{
							String msg = LogUtil.error(getClass(), "Error accessing resource $0 for process configuration $1.", new Object [] {
								resourceLocation, configFile }, e).getMessage();
							throw new ConfigurationException(msg);
						}
						catch (ResourceMgrException e)
						{
							String msg = LogUtil.error(getClass(), "Error accessing resource $0 for process configuration $1.", new Object [] {
								resourceLocation, configFile }, e).getMessage();
							throw new ConfigurationException(msg);
						}
						catch (Exception e)
						{
							String msg = LogUtil.error(getClass(), "Error accessing resource $0 for process configuration $1.", new Object [] {
								resourceLocation, configFile }, e).getMessage();
							throw new ConfigurationException(msg);
						}
						finally
						{
							ctx.close();
						}
						storeDescriptor(descriptor, key);
					}
				}
				catch (ResourceException e)
				{
					// Ignore
				}
				catch (ResourceMgrException e)
				{
					// Ignore
				}
				catch (Exception e)
				{
					String msg = LogUtil.error(getClass(), "Error reading process configuration $0.", new Object [] { configFile }, e).getMessage();
					throw new ConfigurationException(msg);
				}


 */

/**
 * Standard implementation of an activity node.
 *
 * @author Heiko Erhardt
 */
public class ActivityNodeImpl extends MultiSocketNodeImpl
	implements ActivityNode
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Activity handler definition */
	private HandlerDefinition activityHandlerDefinition;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/**
	 * Type of the activity item.
	 * Needed for subclasses that refer to different activity types as {@link ItemTypes#ACTIVITY}
	 */
	protected String activityItemType;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ActivityNodeImpl()
	{
		activityItemType = ItemTypes.ACTIVITY;
		setActivityHandlerDefinition(new HandlerDefinition());
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		ActivityNodeImpl src = (ActivityNodeImpl) source;

		// Copy member data
		activityItemType = src.activityItemType;

		setActivityHandlerDefinition(new HandlerDefinition());
		activityHandlerDefinition.copyFrom(src.activityHandlerDefinition, Copyable.COPY_DEEP);
		activityHandlerDefinition.setOwner(this);
	}

	/**
	 * @copy ItemProvider.copyFromItem
	 */
	public void copyFromItem(Item item, int syncFlags)
	{
		ActivityItem ai = (ActivityItem) item;

		// Make node name unique
		String newName = ai.getName();
		if (getProcess() != null)
		{
			newName = NamedObjectCollectionUtil.createUniqueId(getProcess().getNodeList(), newName);
		}
		setName(newName);

		if (ai instanceof JavaActivityItem)
		{
			setActivityHandlerDefinition(new HandlerDefinition());
			try
			{
				activityHandlerDefinition.copyFrom(((JavaActivityItem) ai).getHandlerDefinition(), Copyable.COPY_DEEP);
			}
			catch (CloneNotSupportedException e)
			{
				// Doesn't happen
			}
			activityHandlerDefinition.setOwner(this);
		}

		// Copy the sockets
		clearSockets();
		for (Iterator it = ai.getSockets(); it.hasNext();)
		{
			ActivitySocket activitySocket = (ActivitySocket) it.next();

			NodeSocket nodeSocket = new NodeSocketImpl();
			nodeSocket.copyFromActivitySocket(activitySocket, syncFlags);
			addSocket(nodeSocket);
		}

		// Copy description and display name
		ItemSynchronization.syncDisplayObjects(this, ai, syncFlags);

		setGeometry(ai.getGeometry());
	}

	/**
	 * @copy ItemProvider.copyToItem
	 */
	public void copyToItem(Item item, int syncFlags)
	{
		ActivityItem ai = (ActivityItem) item;

		if (ai instanceof JavaActivityItem)
		{
			HandlerDefinition aiHandler = new HandlerDefinition();
			((JavaActivityItem) ai).setHandlerDefinition(aiHandler);
			try
			{
				aiHandler.copyFrom(getActivityHandlerDefinition(), Copyable.COPY_DEEP);
			}
			catch (CloneNotSupportedException e)
			{
				// Doesn't happen
			}
			aiHandler.setOwner(ai);
		}

		// Copy the sockets
		ai.clearSockets();
		for (Iterator it = getSockets(); it.hasNext();)
		{
			NodeSocket nodeSocket = (NodeSocket) it.next();

			ActivitySocket activitySocket = new ActivitySocketImpl();
			nodeSocket.copyToActivitySocket(activitySocket, syncFlags);
			ai.addSocket(activitySocket);
		}

		ai.setGeometry(getGeometry());
	}

	/**
	 * Gets the name of the standard icon of this object.
	 * The icon name can be used by the client-side IconModel to retrieve an icon for the object.
	 *
	 * @return The icon name or null if the object does not have a particular icon
	 */
	public String getModelObjectSymbolName()
	{
		if (getActivityItemType() != null)
			return getActivityItemType();
		return ModelObjectSymbolNames.ACTIVITY_NODE;
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (getProcess() != null)
		{
			if ((flag & INSTANTIATE_ITEM) != 0)
			{
				activityHandlerDefinition.instantiate();
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Associations
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.getAssociations
	 */
	public List getAssociations()
	{
		List associations = null;

		associations = activityHandlerDefinition.addHandlerAssociations(associations, "Activity node handler class", Association.PRIMARY);
		associations = AssociationUtil.addAssociations(associations, - 1, super.getAssociations());

		return associations;
	}

	//////////////////////////////////////////////////
	// @@ Property access: Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Gets the type of the activity item referred to by this node.
	 * @nowarn
	 */
	public String getActivityItemType()
	{
		return activityItemType;
	}

	/**
	 * Checks if the activity handler definition is not empty.
	 * Used by Castor to decide wether an entry is necessary in the XML file.
	 * @nowarn
	 */
	public boolean hasActivityHandlerDefinition()
	{
		return activityHandlerDefinition.isDefined();
	}

	/**
	 * Gets the activity handler definition.
	 * @nowarn
	 */
	public HandlerDefinition getActivityHandlerDefinition()
	{
		return activityHandlerDefinition;
	}

	/**
	 * Sets the activity handler definition.
	 * @nowarn
	 */
	public void setActivityHandlerDefinition(HandlerDefinition activityHandlerDefinition)
	{
		this.activityHandlerDefinition = activityHandlerDefinition;
		if (activityHandlerDefinition != null)
		{
			activityHandlerDefinition.setOwner(this);
		}
	}
}
