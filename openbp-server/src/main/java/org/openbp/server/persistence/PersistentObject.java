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
package org.openbp.server.persistence;

import java.io.Serializable;

/**
 * OpenBP state object that can be persisted to a persistence store.
 *
 * @author Heiko Erhardt
 */
public interface PersistentObject
{
	//////////////////////////////////////////////////
	// @@ Lifecycle support
	//////////////////////////////////////////////////

	/**
	 * Template method that is called when the object has been created.
	 */
	public void onCreate();

	/**
	 * Template method that is called after the object has been fetched.
	 */
	public void onLoad();

	/**
	 * Template method that is called before the object is inserted or updated.
	 */
	public void beforeSave();

	/**
	 * Template method that is called after the object has been inserted or updated.
	 */
	public void afterSave();

	/**
	 * Template method that is called before the object is deleted.
	 * The default implementation does nothing.
	 */
	public void beforeDelete();

	/**
	 * Template method that is called after the object has been deleted.
	 * The default implementation does nothing.
	 */
	public void afterDelete();

	//////////////////////////////////////////////////
	// @@ Primary key
	//////////////////////////////////////////////////

	/**
	 * Creates a new UUID as primary key of this object.
	 */
	public void createId();

	/**
	 * Gets the primary key.
	 * @nowarn
	 */
	public Serializable getId();

	/**
	 * Gets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public Integer getVersion();

	/**
	 * Sets the object version for versioned data check (for concurrent modification check by o/r mappers).
	 * @nowarn
	 */
	public void setVersion(Integer version);
}
