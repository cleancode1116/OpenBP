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
package org.openbp.server.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.CollectionUtil;
import org.openbp.common.CommonRegistry;
import org.openbp.core.OpenBPException;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.core.model.modelmgr.ModelNotificationObserver;
import org.openbp.core.model.modelmgr.ModelNotificationService;
import org.openbp.core.remote.ClientSession;
import org.openbp.core.remote.InvalidSessionException;
import org.openbp.server.remote.ClientSessionMgr;

/**
 * This class implements the methods exposed by the {@link ModelNotificationService} interface.
 *
 * @author Heiko Erhardt
 */
public class ModelNotificationServiceImpl
	implements ModelNotificationService
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Model manager */
	private ModelMgr modelMgr;

	/** Notification observers (list of {@link ModelNotificationObserver} objects) */
	private List notificationObservers;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ModelNotificationServiceImpl()
	{
	}

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Adds a model notification observer to the event observer list.
	 *
	 * @param observer Observer
	 */
	public void addModelNotificationObserver(ModelNotificationObserver observer)
	{
		if (notificationObservers == null)
		{
			notificationObservers = new ArrayList();
		}

		if (! notificationObservers.contains(observer))
		{
			notificationObservers.add(observer);
		}
	}

	/**
	 * Removes a model notification observer from the event observer list.
	 *
	 * @param observer Observer
	 */
	public void removeModelNotificationObserver(ModelNotificationObserver observer)
	{
		if (notificationObservers != null)
		{
			notificationObservers.remove(observer);
			if (notificationObservers.size() == 0)
			{
				notificationObservers = null;
			}
		}
	}

	/**
	 * Sets the notification observers (list of {@link ModelNotificationObserver} objects).
	 * For Spring support only.
	 * @nowarn
	 */
	public void setNotificationObservers(List notificationObservers)
	{
		this.notificationObservers = notificationObservers;
	}

	//////////////////////////////////////////////////
	// @@ ModelNotificationService implementation
	//////////////////////////////////////////////////

	/**
	 * Notification method for model updates.
	 *
	 * @param session A session permitting access
	 * @param qualifier Qualifier of the object that has been updated
	 * @param mode Type of model update ({@link ModelNotificationService#ADDED}/{@link ModelNotificationService#UPDATED}/{@link ModelNotificationService#REMOVED})
	 * @throws InvalidSessionException If the session doesn't permit accessing the model manager
	 * @throws OpenBPException On error
	 */
	public void modelUpdated(ClientSession session, ModelQualifier qualifier, int mode)
	{
		// Will throw an InvalidSessionException on invalid session
		ClientSessionMgr.getInstance().checkSession(session);

		// Delegate to the model mgr
		getModelMgr().modelUpdated(qualifier, mode);

		for (Iterator it = CollectionUtil.iterator(notificationObservers); it.hasNext();)
		{
			ModelNotificationObserver observer = (ModelNotificationObserver) it.next();
			observer.modelUpdated(qualifier, mode);
		}
	}

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 *
	 * @param session A session permitting access
	 * @throws InvalidSessionException If the session doesn't permit accessing the model manager
	 * @throws OpenBPException On error
	 */
	public void requestModelReset(ClientSession session)
	{
		// Will throw an InvalidSessionException on invalid session
		ClientSessionMgr.getInstance().checkSession(session);

		// Delegate to the model mgr
		getModelMgr().requestModelReset();

		for (Iterator it = CollectionUtil.iterator(notificationObservers); it.hasNext();)
		{
			ModelNotificationObserver observer = (ModelNotificationObserver) it.next();
			observer.requestModelReset();
		}
	}

	private ModelMgr getModelMgr()
	{
		if (modelMgr == null)
		{
			// An instance of the ModelMgr is placed in the core registry by the CoreModule
			modelMgr = (ModelMgr) CommonRegistry.lookup(ModelMgr.class);
		}
		return modelMgr;
	}
}
