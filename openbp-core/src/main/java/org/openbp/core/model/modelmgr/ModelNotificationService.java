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
package org.openbp.core.model.modelmgr;

import org.openbp.core.model.ModelQualifier;
import org.openbp.core.remote.ClientSession;

/**
 * Service that can be use to signal updates of the model to the server.
 *
 * @author Heiko Erhardt
 */
public interface ModelNotificationService
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Mode: An object was addded. */
	public static final int ADDED = (1 << 0);

	/** Mode: An object was updated. */
	public static final int UPDATED = (1 << 1);

	/** Mode: An object was removed. */
	public static final int REMOVED = (1 << 2);

	//////////////////////////////////////////////////
	// @@ Methods
	//////////////////////////////////////////////////

	/**
	 * Notification method for model updates.
	 *
	 * @param session A session permitting access
	 * @param qualifier Qualifier of the object that has been updated
	 * @param mode Type of model update ({@link #ADDED}/{@link #UPDATED}/{@link #REMOVED})
	 */
	public void modelUpdated(ClientSession session, ModelQualifier qualifier, int mode);

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 *
	 * @param session A session permitting access
	 */
	public void requestModelReset(ClientSession session);
}
