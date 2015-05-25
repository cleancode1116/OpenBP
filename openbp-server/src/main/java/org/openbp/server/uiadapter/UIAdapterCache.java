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
package org.openbp.server.uiadapter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.process.NodeSocket;
import org.openbp.core.model.item.visual.VisualItem;
import org.openbp.core.uiadapter.UIAdapterDescriptorRegistry;
import org.openbp.server.context.TokenContext;

/**
 * This class implements a cache for model-specific UI adapters.
 * It relies on the {@link UIAdapterDescriptorRegistry} class.
 *
 * @author Heiko Erhardt
 */
public final class UIAdapterCache
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Map of UI adapters from {@link Model} to mappings from visual types ({@link java.lang.String}) to {@link UIAdapter}. */
	private static Map uiAdapters;

	//////////////////////////////////////////////////
	// @@ Internal state
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static UIAdapterCache singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance for this class.
	 * @nowarn
	 */
	public static synchronized UIAdapterCache getInstance()
	{
		if (singletonInstance == null)
		{
			singletonInstance = new UIAdapterCache();
		}

		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private UIAdapterCache()
	{
		uiAdapters = new HashMap();
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * This method removes all UIAdapter's that are cached for the passed model.
	 *
	 * @param model The model for which the UI adapters should be removed from the cache
	 */
	public void clearAdapterCache(Model model)
	{
		// We just delegate this to the UI adapter cache.
		removeAdapters(model);
	}

	/**
	 * Executes a visual.
	 * Compiles the visual before execution if neccessary.
	 *
	 * @param visual The visual item that invoked the user interface adapter
	 * @param visualType Type of the visual to execute ("external" etc\.)
	 * @param context Token context of the process
	 * @param entrySocket Entry socket that is being used to execute the activity.
	 * The socket name is partially qualified, i. e. contains the node name ("node.socket")
	 * @return The name of the visual item exit socket to continue processing with.<br>
	 * Most user interface adapters will return null here because visual processing is usually
	 * an asynchronous request/response process. However, for templates that support
	 * direct user interaction (i. e. a Swing adapter), the adapter might choose the exit
	 * right away.
	 * @throws Exception If the visual could not be executed
	 */
	public String executeVisual(String visualType, VisualItem visual, TokenContext context, NodeSocket entrySocket)
		throws Exception
	{
		// Determine the adapter and forward the request to it
		Model model = context.getExecutingModel();

		UIAdapter adapter = lookupAdapter(model, visualType);
		if (adapter == null)
		{
			// Create an adapter.
			adapter = createAdapter(model, visualType);

			// Cache resulting adapter.
			storeAdapter(model, visualType, adapter);
		}

		return adapter.executeVisual(visual, context, entrySocket);
	}

	//////////////////////////////////////////////////
	// @@ Helpers for adapter and description handling
	//////////////////////////////////////////////////

	/**
	 * This method creates a UIAdapter for the passed model and visual type.
	 *
	 * @param model The model for which the adapter should be created
	 * @param visualType The visual type for which the adapter should be created
	 * @return The UIAdapter created
	 * @throws OpenBPException if the UIAdapter could not be created
	 */
	private UIAdapter createAdapter(Model model, String visualType)
	{
		// Get the class name of the Adapter implementation.
		String className = UIAdapterDescriptorRegistry.getInstance().getUIAdapterClassName(visualType);

		// Check whether we have a class name now.
		if (className == null)
		{
			// Create message.
			String msg = LogUtil.error(getClass(), "Cannot determine user interface adapter class name of type $0.", visualType);

			// Throw an Engine Exception.
			throw new EngineException("UnknownUiAdapter", msg);
		}

		// Instantiate the user interface adapter
		Class cls = null;
		try
		{
			// Get the class for the class name.
			cls = Class.forName(className);

			// We instantiate using the (Model model) constructor
			Constructor constructor = cls.getConstructor(new Class [] { Model.class });

			UIAdapter adapter = (UIAdapter) constructor.newInstance(new Object [] { model });

			// Log that we created a UI adapter.
			LogUtil.debug(getClass(), "UIAdapter for model $0, visual type $1 created.", model.getQualifier(), visualType);

			// Return the created adapter.
			return adapter;
		}
		catch (Exception e)
		{
			// Log message.
			String msg = LogUtil.error(getClass(), "Cannot instantiate user interface adapter $0.", className);

			// Throw an Engine Exception.
			throw new EngineException("CouldNotCreateUiAdapter", msg, e);
		}
	}

	/**
	 * This method tries to lookup a UIAdapter from the cache based on the passed model and the
	 * passed visual type. If no adapter can be found, null is returned.
	 *
	 * @param model The model to lookup the adapter for
	 * @param visualType The visual type of the adapter
	 * @return The UI adapter found or null
	 */
	public UIAdapter lookupAdapter(Model model, String visualType)
	{
		// Get model specific UIAdapter map.
		Map modelUIAdapterMap = (Map) uiAdapters.get(model);

		// If we have such a UIAdapter for the visual type in the model...
		if (modelUIAdapterMap != null)
		{
			// ...return it.
			UIAdapter adapter = (UIAdapter) modelUIAdapterMap.get(visualType);
			if (adapter != null)
				return adapter;
		}

		// ...otherwise just return null.
		return null;
	}

	/**
	 * This method removes all UI adapters that have been stored in the cache for the passed model.
	 *
	 * @param model The model for which the UI adapters should be removed from the cache
	 */
	public void removeAdapters(Model model)
	{
		uiAdapters.remove(model);
	}

	/**
	 * This method stores the given UIAdapter as responsible for the passed model and visual type.
	 *
	 * @param model The model for which the UIAdapter has been created
	 * @param visualType The visual type for which the UIAdapter is responsible
	 * @param adapter The adapter to be stored
	 */
	public void storeAdapter(Model model, String visualType, UIAdapter adapter)
	{
		// Get model specific UIAdapter map.
		Map modelUIAdapterMap = (Map) uiAdapters.get(model);

		// If we don't have a model specific map...
		if (modelUIAdapterMap == null)
		{
			// ...create...
			modelUIAdapterMap = new HashMap();

			// ...and store it.
			uiAdapters.put(model, modelUIAdapterMap);
		}

		// Finally, put the adapter in the (now definetely existing) model specific map.
		modelUIAdapterMap.put(visualType, adapter);
	}
}
