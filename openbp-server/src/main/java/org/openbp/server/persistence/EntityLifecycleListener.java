/*
 *   Copyright 2010 skynamics AG
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
package org.openbp.server.persistence;

/**
 * Listener for entity lifecycle events.
 *
 * @author Heiko Erhardt
 */
public interface EntityLifecycleListener
{
	/**
	 * Template method that is called when the object has been created.
	 * @param event Lifecycle event details
	 */
	public void onCreate(EntityLifecycleEvent event);

	/**
	 * Template method that is called after the object has been fetched.
	 * @param event Lifecycle event details
	 */
	public void onLoad(EntityLifecycleEvent event);

	/**
	 * Template method that is called before the object is inserted or updated.
	 * @param event Lifecycle event details
	 */
	public void beforeSave(EntityLifecycleEvent event);

	/**
	 * Template method that is called after the object has been inserted or updated.
	 * @param event Lifecycle event details
	 */
	public void afterSave(EntityLifecycleEvent event);

	/**
	 * Template method that is called before the object is deleted.
	 * @param event Lifecycle event details
	 */
	public void beforeDelete(EntityLifecycleEvent event);

	/**
	 * Template method that is called after the object has been deleted.
	 * @param event Lifecycle event details
	 */
	public void afterDelete(EntityLifecycleEvent event);
}
