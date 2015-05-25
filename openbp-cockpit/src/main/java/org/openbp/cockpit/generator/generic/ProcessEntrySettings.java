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
package org.openbp.cockpit.generator.generic;

import java.util.List;

import org.openbp.common.generic.msgcontainer.MsgContainer;
import org.openbp.core.model.Model;

/**
 * Process entry settings object.
 * This interface defices generator settings that contain a list of process entries.
 *
 * @author Heiko Erhardt
 */
public interface ProcessEntrySettings
{
	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the entry list.
	 * @return A list of {@link ProcessEntry} objects
	 */
	public List getEntryList();

	/**
	 * Sets the entry list.
	 * @param entryList A list of {@link ProcessEntry} objects
	 */
	public void setEntryList(List entryList);

	/**
	 * Gets the model that will be used to resolve item references (e\.g\. data types).
	 * @nowarn
	 */
	public Model getModel();

	/**
	 * This template method is called before the settings object is being serialized.
	 * It can be overridden to implement custom operations.
	 */
	public void beforeSerialization();

	/**
	 * This template method is called after the settings object has been deserialized.
	 * It will add all data members that are not present in the field list to the field list.
	 *
	 * @param msgs Container for error messages
	 */
	public void afterDeserialization(MsgContainer msgs);
}
