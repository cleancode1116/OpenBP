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
package org.openbp.jaspira.option;

import javax.swing.filechooser.FileFilter;

import org.openbp.common.rc.ResourceCollection;
import org.openbp.jaspira.option.widget.PathWidget;

/**
 * A path option holds a file or directory path.
 * Its ui supports choosing the path using a file chooser dialog.
 *
 * @author Jens Ferchland
 */
public class PathOption extends StringOption
{
	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** File name filter for the file selection dialog */
	private FileFilter filter;

	/** Directory/file selection mode */
	private boolean dirSelection;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Resource constructor.
	 *
	 * @param res Resource defining the option
	 * @param optionName Name of the option
	 * @param defaultValue Default Value of the option
	 * @param filter File name filter for the file selection dialog
	 * @param dirSelection
	 *		true	Select directories<br>
	 *		false	Select regular files
	 */
	public PathOption(ResourceCollection res, String optionName, String defaultValue, FileFilter filter, boolean dirSelection)
	{
		super(res, optionName, defaultValue);

		this.filter = filter;
		this.dirSelection = dirSelection;
	}

	/**
	 * Value constructor.
	 *
	 * @param optionName Name of the option
	 * @param displayName Display name of the option
	 * @param description Description of the option
	 * @param defaultValue Default Value of the option
	 * @param parent Option parent or null
	 * @param prio Priority of the option
	 * @param filter File name filter for the file selection dialog
	 * @param dirSelection
	 *		true	Select directories<br>
	 *		false	Select regular files
	 */
	public PathOption(String optionName, String displayName, String description, String defaultValue, Option parent, int prio, FileFilter filter, boolean dirSelection)
	{
		super(optionName, displayName, description, defaultValue, parent, prio);

		this.filter = filter;
		this.dirSelection = dirSelection;
	}

	//////////////////////////////////////////////////
	// @@ Implements Option.
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.jaspira.option.Option#createOptionWidget()
	 */
	public OptionWidget createOptionWidget()
	{
		return new PathWidget(this, filter, dirSelection);
	}
}
