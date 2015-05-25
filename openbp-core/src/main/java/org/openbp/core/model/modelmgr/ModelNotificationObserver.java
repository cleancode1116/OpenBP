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

/**
 * Model notification receiver.
 *
 * @author Heiko Erhardt
 */
public interface ModelNotificationObserver
{
	/**
	 * Notification method for model updates.
	 *
	 * @param qualifier Qualifier of the object that has been updated
	 * @param mode Type of model update ({@link ModelNotificationService#ADDED}/{@link ModelNotificationService#UPDATED}/{@link ModelNotificationService#REMOVED})
	 */
	public void modelUpdated(ModelQualifier qualifier, int mode);

	/**
	 * Resets all models.
	 * Re-initializes the model classloader and the model properties and reinitializes the components of the model.
	 */
	public void requestModelReset();
}
