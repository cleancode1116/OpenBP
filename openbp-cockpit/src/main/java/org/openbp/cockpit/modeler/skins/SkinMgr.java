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

import javax.swing.Icon;

import org.openbp.cockpit.CockpitConstants;
import org.openbp.common.CollectionUtil;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.io.xml.XMLDriver;
import org.openbp.common.io.xml.XMLDriverException;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.rc.ResourceCollection;
import org.openbp.common.rc.ResourceItem;
import org.openbp.common.resource.ResourceMgr;
import org.openbp.common.resource.ResourceMgrException;
import org.openbp.common.string.StringUtil;
import org.openbp.core.model.ModelException;
import org.openbp.guiclient.model.item.ItemIconMgr;
import org.springframework.core.io.Resource;

/**
 * The skin manager keeps a list of skin definitions, which determine how the elements of the process model
 * are being presented.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class SkinMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Default skin */
	private Skin defaultSkin;

	/** List of skin descriptors (maps skin names to {@link Skin} objects) */
	private Map skins;

	/** Singleton instance */
	private static SkinMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized SkinMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new SkinMgr();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private SkinMgr()
	{
	}

	/**
	 * Load all skin infos.
	 */
	public void load()
	{
		skins = new LinkedHashMap();
		loadSkins();

		determineDefaultSkin();

		addToIconModel(defaultSkin, null);
	}

	/**
	 * Determines the default skin by checking the default property of each skin in the skin list.
	 */
	private void determineDefaultSkin()
	{
		for (Iterator it = skins.values().iterator(); it.hasNext();)
		{
			Skin skin = (Skin) it.next();
			if (skin.isDefaultSkin())
			{
				defaultSkin = skin;
				break;
			}
		}

		// TODO Fix 6: Error msg if no default skin found
	}

	/**
	 * Adds all resource icons of the given skin to the icon model under the given skin name.
	 *
	 * @param skin Skin to add
	 * @param skinName ame of the skin or null to add the icons as default icons
	 */
	private void addToIconModel(Skin skin, String skinName)
	{
		ResourceCollection res = skin.getResource();

		for (Iterator it = res.getKeys(); it.hasNext();)
		{
			String key = (String) it.next();
			ResourceItem ri = res.getResourceItem(key);

			Object object = ri.getObject();
			if (object instanceof Icon)
			{
				ItemIconMgr.getInstance().registerIcon(skinName, key, (Icon) object);
			}
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the default skin.
	 * @nowarn
	 */
	public Skin getDefaultSkin()
	{
		return defaultSkin;
	}

	/**
	 * Gets a skin descriptor by its name.
	 *
	 * @param name Name of the skin
	 * @return The skin descriptor or null if no such skin descriptor exists
	 */
	public Skin getSkin(String name)
	{
		return (Skin) skins.get(name);
	}

	/**
	 * Adds a skin descriptor.
	 * @param skin The skin descriptor to add
	 */
	public void addSkin(Skin skin)
	{
		skins.put(skin.getName(), skin);
	}

	/**
	 * Gets the list of skin descriptors.
	 * @return A list of {@link Skin} objects
	 */
	public List getSkinList()
	{
		ArrayList list = new ArrayList();
		for (Iterator it = skins.values().iterator(); it.hasNext();)
		{
			list.add(it.next());
		}
		return list;
	}

	/**
	 * Gets an array of all skin names.
	 * @nowarn
	 */
	public String [] getSkinNames()
	{
		int n = skins.size();

		String [] ret = new String [n];

		int i = 0;
		for (Iterator it = skins.values().iterator(); it.hasNext();)
		{
			Skin skin = (Skin) it.next();
			ret [i++] = skin.getDisplayText();
		}

		return ret;
	}

	/**
	 * Gets an array of all skins.
	 * @nowarn
	 */
	public Skin [] getSkins()
	{
		List skinList = getSkinList();
		return (Skin []) CollectionUtil.toArray(skinList, Skin.class);
	}

	//////////////////////////////////////////////////
	// @@ Private methods
	//////////////////////////////////////////////////

	/**
	 * Loads all skin files in the $OpenBP/cockpit/skin directory.
	 */
	private void loadSkins()
	{
		// Load mappings for generic skin descriptor files
		XMLDriver xmlDriver = XMLDriver.getInstance();
		xmlDriver.loadMapping(org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor.class);
		xmlDriver.loadMapping(org.openbp.cockpit.modeler.skins.LinkDescriptor.class);
		xmlDriver.loadMapping(org.openbp.cockpit.modeler.skins.SymbolDescriptor.class);
		xmlDriver.loadMapping(org.openbp.cockpit.modeler.skins.Skin.class);

		ResourceMgr resMgr = ResourceMgr.getDefaultInstance();
		String resourcePattern = CockpitConstants.SKIN + "/*.xml";
		Resource[] resources = null;

		try
		{
			resources = resMgr.findResources(resourcePattern);
		}
		catch (ResourceMgrException e)
		{
			throw new ModelException("MissingItemTypeDescriptors", "No item type files found matching '" + resourcePattern + "'.");
		}

		if (resources.length == 0)
			throw new ModelException("MissingItemTypeDescriptors", "No skin definitions found in resource path '" + CockpitConstants.SKIN + "'.");

		ArrayList errorMsgs = new ArrayList();
		for (int i = 0; i < resources.length; i++)
		{
			Skin skin = null;

			// Load the descriptor
			try
			{
				skin = (Skin) xmlDriver.deserializeResource(Skin.class, resources[i]);
			}
			catch (XMLDriverException e)
			{
				ExceptionUtil.printTrace(e);
				continue;
			}

			// Validate the descriptor and log all errors
			errorMsgs.clear();
			String skinResPath = StringUtil.buildPath(CockpitConstants.SKIN, skin.getName()); 
			if (!skin.validate(skinResPath, errorMsgs))
			{
				StringBuffer sb = new StringBuffer();
				int nErros = errorMsgs.size();
				for (int iErrors = 0; iErrors < nErros; ++iErrors)
				{
					if (iErrors > 0)
						sb.append('\n');
					sb.append((String) errorMsgs.get(iErrors));
				}
				LogUtil.error(getClass(), "Invalid skin descriptor $0:\n{1}", skin.getName(), sb.toString());

				// Ignore invalid descriptors
				continue;
			}

			// Initialize the descriptor
			if (!skin.initialize(skinResPath))
			{
				continue;
			}

			// Add the icons of the skin to the icon model prefixed with the skin name
			addToIconModel(skin, skin.getName());

			// Successful, add it to the list
			addSkin(skin);
		}
	}
}
