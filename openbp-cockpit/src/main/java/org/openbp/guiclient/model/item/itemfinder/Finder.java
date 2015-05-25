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
package org.openbp.guiclient.model.item.itemfinder;

import java.util.List;

import org.openbp.core.model.Model;
import org.openbp.core.model.ModelObject;

/**
 * Interface any finder must implement.
 *
 * @author Baumgartner Michael
 */
public interface Finder
{
	/**
	 * Find all references of the model object in the specified model.
	 * If no references are found an empty list is returned.
	 * @param mo The references of the model object are searched
	 * @param model The model to search in
	 * @return List containing {@link org.openbp.core.model.ModelObject} object that match the model object
	 */
	public List findModelObjectInModel(ModelObject mo, Model model);
}
