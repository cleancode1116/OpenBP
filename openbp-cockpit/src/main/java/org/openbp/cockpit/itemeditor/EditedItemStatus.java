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
package org.openbp.cockpit.itemeditor;

/**
 * Status of an item that is about to be edited in the component editor.
 *
 * @author Stephan Moritz
 */
public class EditedItemStatus
{
	/** New item */
	public static final int NEW = 1;

	/** Copied item */
	public static final int COPIED = 2;

	/** Existing item */
	public static final int EXISTING = 3;

}
