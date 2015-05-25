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
package org.openbp.cockpit.modeler.skins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.openbp.common.generic.description.DisplayObjectImpl;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceCollectionMgr;
import org.openbp.core.model.item.process.ProcessItem;

/**
 * A skins defines how elements of the process model should be presented in the workspace.
 * It maintains a list of symbol descriptors that describe how a particular figure (e. g. an
 * initial node figure) should look like.
 *
 * @author Heiko Erhardt
 */
public class Skin extends DisplayObjectImpl
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Resource name */
	private String resourceName;

	/** Flag if control anchors should be visible */
	private Boolean controlAnchorVisible;

	/** Flag if control links should be visible */
	private Boolean controlLinkVisible;

	/** Flag if data links should be visible */
	private Boolean dataLinkVisible;

	/** Flag if technical names should be displayed instead of display names */
	private Boolean nameDisplay;

	/** Radial tags flag */
	private boolean radialTags;

	/** Disable shadows flag */
	private boolean disableShadows;

	/** Default skin */
	private boolean defaultSkin;

	/** Parameter title format */
	private String paramTitleFormat;

	/** Socket title format */
	private String socketTitleFormat;

	/** Node title format */
	private String nodeTitleFormat;

	/** List of symbol descriptors (maps symbol names to {@link SymbolDescriptor} objects) */
	private Map symbolDescriptors;

	/** List of link descriptors (maps link names to {@link LinkDescriptor} objects) */
	private Map linkDescriptors;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Valid */
	private transient boolean valid;

	/** Skin resource */
	private ResourceCollection resourceCollection;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public Skin()
	{
		symbolDescriptors = new LinkedHashMap();
		linkDescriptors = new LinkedHashMap();
	}

	/**
	 * Validates the skin.
	 * Any error messages should be added to the supplied error message list
	 * and will be displayed by the caller.
	 *
	 * @param descriptorDir Path to the directory containing the descriptor
	 * @param errorMsgs List of strings that holds error messages
	 * @return
	 * true: The descriptor was successfully validated.<br>
	 * false: There was an error validating the descriptor.
	 */
	public boolean validate(String descriptorDir, List errorMsgs)
	{
		boolean ret = true;

		for (Iterator it = symbolDescriptors.values().iterator(); it.hasNext();)
		{
			SymbolDescriptor desc = (SymbolDescriptor) it.next();
			if (!desc.validate(descriptorDir, errorMsgs))
			{
				ret = false;
			}
		}

		for (Iterator it = linkDescriptors.values().iterator(); it.hasNext();)
		{
			LinkDescriptor desc = (LinkDescriptor) it.next();
			if (!desc.validate(descriptorDir, errorMsgs))
			{
				ret = false;
			}
		}

		valid = ret;

		return ret;
	}

	/**
	 * Initializes the descriptor.
	 * Loads the symbol image (if an image file has been defined).
	 *
	 * @param skinResPath Resource path to the resource folder containing the descriptor
	 * @return
	 * true: The descriptor was successfully initialize<br>
	 * false: There was an error initializing the descriptor. The descriptor should not be added to the descriptor list.
	 */
	public boolean initialize(String skinResPath)
	{
		String resourceName = getResourceName();
		if (resourceName == null)
			resourceName = getName();
		resourceCollection = ResourceCollectionMgr.getDefaultInstance().getResource("skin", resourceName);

		boolean ret = true;

		for (Iterator it = symbolDescriptors.values().iterator(); it.hasNext();)
		{
			SymbolDescriptor desc = (SymbolDescriptor) it.next();
			if (!desc.initialize(skinResPath))
			{
				ret = false;
			}

			// Initialize the overlay icon according to the overlay resource key
			String key = desc.getOverlayResourceKey();
			if (key != null)
			{
				ImageIcon image = (ImageIcon) resourceCollection.getRequiredObject(key);
				desc.setOverlayIcon(image);
			}
		}

		for (Iterator it = linkDescriptors.values().iterator(); it.hasNext();)
		{
			LinkDescriptor desc = (LinkDescriptor) it.next();
			if (!desc.initialize(skinResPath))
			{
				ret = false;
			}
		}

		valid = ret;

		return ret;
	}

	//////////////////////////////////////////////////
	// @@ Process initialization
	//////////////////////////////////////////////////

	/**
	 * Initalizes the properties of a new process according to the skin's settings.
	 * Sets the skin name and the orientation.
	 *
	 * @param process Process to initialize
	 */
	public void initalizeNewProcess(ProcessItem process)
	{
		process.setSkinName(getName());
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the resource name.
	 * @nowarn
	 */
	public String getResourceName()
	{
		return resourceName;
	}

	/**
	 * Sets the resource name.
	 * @nowarn
	 */
	public void setResourceName(String resourceName)
	{
		this.resourceName = resourceName;
	}

	/**
	 * Gets the flag if control anchors should be visible.
	 * @nowarn
	 */
	public Boolean isControlAnchorVisible()
	{
		return controlAnchorVisible;
	}

	/**
	 * Sets the flag if control anchors should be visible.
	 * @nowarn
	 */
	public void setControlAnchorVisible(Boolean controlAnchorVisible)
	{
		this.controlAnchorVisible = controlAnchorVisible;
	}

	/**
	 * Gets the flag if control links should be visible.
	 * @nowarn
	 */
	public Boolean isControlLinkVisible()
	{
		return controlLinkVisible;
	}

	/**
	 * Sets the flag if control links should be visible.
	 * @nowarn
	 */
	public void setControlLinkVisible(Boolean controlLinkVisible)
	{
		this.controlLinkVisible = controlLinkVisible;
	}

	/**
	 * Gets the flag if data links should be visible.
	 * @nowarn
	 */
	public Boolean isDataLinkVisible()
	{
		return dataLinkVisible;
	}

	/**
	 * Sets the flag if data links should be visible.
	 * @nowarn
	 */
	public void setDataLinkVisible(Boolean dataLinkVisible)
	{
		this.dataLinkVisible = dataLinkVisible;
	}

	/**
	 * Gets the flag if technical names should be displayed instead of display names.
	 * @nowarn
	 */
	public Boolean isNameDisplay()
	{
		return nameDisplay;
	}

	/**
	 * Sets the flag if technical names should be displayed instead of display names.
	 * @nowarn
	 */
	public void setNameDisplay(Boolean nameDisplay)
	{
		this.nameDisplay = nameDisplay;
	}

	/**
	 * Gets the radial tags flag.
	 * @nowarn
	 */
	public boolean isRadialTags()
	{
		return radialTags;
	}

	/**
	 * Sets the radial tags flag.
	 * @nowarn
	 */
	public void setRadialTags(boolean radialTags)
	{
		this.radialTags = radialTags;
	}

	/**
	 * Gets the disable shadows flag.
	 * @nowarn
	 */
	public boolean isDisableShadows()
	{
		return disableShadows;
	}

	/**
	 * Sets the disable shadows flag.
	 * @nowarn
	 */
	public void setDisableShadows(boolean disableShadows)
	{
		this.disableShadows = disableShadows;
	}

	/**
	 * Gets the default skin.
	 * @nowarn
	 */
	public boolean isDefaultSkin()
	{
		return defaultSkin;
	}

	/**
	 * Sets the default skin.
	 * @nowarn
	 */
	public void setDefaultSkin(boolean defaultSkin)
	{
		this.defaultSkin = defaultSkin;
	}

	/**
	 * Gets the parameter title format.
	 * @nowarn
	 */
	public String getParamTitleFormat()
	{
		return paramTitleFormat;
	}

	/**
	 * Sets the parameter title format.
	 * @nowarn
	 */
	public void setParamTitleFormat(String paramTitleFormat)
	{
		this.paramTitleFormat = paramTitleFormat;
	}

	/**
	 * Gets the socket title format.
	 * @nowarn
	 */
	public String getSocketTitleFormat()
	{
		return socketTitleFormat;
	}

	/**
	 * Sets the socket title format.
	 * @nowarn
	 */
	public void setSocketTitleFormat(String socketTitleFormat)
	{
		this.socketTitleFormat = socketTitleFormat;
	}

	/**
	 * Gets the node title format.
	 * @nowarn
	 */
	public String getNodeTitleFormat()
	{
		return nodeTitleFormat;
	}

	/**
	 * Sets the node title format.
	 * @nowarn
	 */
	public void setNodeTitleFormat(String nodeTitleFormat)
	{
		this.nodeTitleFormat = nodeTitleFormat;
	}

	/**
	 * Gets a symbol descriptor by its node type.
	 *
	 * @param nodeType Type of the node that should be represented by the symbol descriptor
	 * @return The symbol descriptor or null if no such symbol descriptor exists
	 */
	public SymbolDescriptor getSymbolDescriptor(String nodeType)
	{
		return (SymbolDescriptor) symbolDescriptors.get(nodeType);
	}

	/**
	 * Adds a symbol descriptor.
	 * @param symbolDescriptor The symbol descriptor to add
	 */
	public void addSymbolDescriptor(SymbolDescriptor symbolDescriptor)
	{
		symbolDescriptors.put(symbolDescriptor.getSymbolType(), symbolDescriptor);
	}

	/**
	 * Gets the list of symbol descriptors.
	 * @return A list of {@link SymbolDescriptor} objects
	 */
	public List getSymbolDescriptorList()
	{
		ArrayList list = new ArrayList();
		for (Iterator it = symbolDescriptors.values().iterator(); it.hasNext();)
		{
			list.add(it.next());
		}
		return list;
	}

	/**
	 * Gets a link descriptor by its link type.
	 *
	 * @param linkType Type of the link that should be represented by the link descriptor
	 * @return The link descriptor or null if no such link descriptor exists
	 */
	public LinkDescriptor getLinkDescriptor(String linkType)
	{
		return (LinkDescriptor) linkDescriptors.get(linkType);
	}

	/**
	 * Adds a link descriptor.
	 * @param linkDescriptor The link descriptor to add
	 */
	public void addLinkDescriptor(LinkDescriptor linkDescriptor)
	{
		linkDescriptors.put(linkDescriptor.getLinkType(), linkDescriptor);
	}

	/**
	 * Gets the list of link descriptors.
	 * @return A list of {@link LinkDescriptor} objects
	 */
	public List getLinkDescriptorList()
	{
		ArrayList list = new ArrayList();
		for (Iterator it = linkDescriptors.values().iterator(); it.hasNext();)
		{
			list.add(it.next());
		}
		return list;
	}

	/**
	 * Gets the valid.
	 * @nowarn
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * Sets the valid.
	 * @nowarn
	 */
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	/**
	 * Gets the skin resource.
	 * @nowarn
	 */
	public ResourceCollection getResource()
	{
		return resourceCollection;
	}

	/**
	 * Sets the skin resource.
	 * @nowarn
	 */
	public void setResourceCollection(ResourceCollection resourceCollection)
	{
		this.resourceCollection = resourceCollection;
	}
}
