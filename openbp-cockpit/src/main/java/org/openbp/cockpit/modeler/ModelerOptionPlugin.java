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
package org.openbp.cockpit.modeler;

import org.openbp.cockpit.modeler.drawing.WorkspaceDrawingView;
import org.openbp.cockpit.modeler.drawing.shadowlayout.NoShadowLayouter;
import org.openbp.cockpit.modeler.drawing.shadowlayout.ParallelProjectionShadowLayouter;
import org.openbp.cockpit.modeler.drawing.shadowlayout.ShadowLayouter;
import org.openbp.jaspira.option.BooleanOption;
import org.openbp.jaspira.option.ColorOption;
import org.openbp.jaspira.option.SelectionOption;
import org.openbp.jaspira.plugin.AbstractPlugin;
import org.openbp.jaspira.plugin.OptionModule;

/**
 * This Plugin contains the options for the editor.
 *
 * @author Jens Ferchland
 */
public class ModelerOptionPlugin extends AbstractPlugin
{
	/** No shadow */
	private static final ShadowLayouter NO_SHADOW = new NoShadowLayouter();

	/** Simple parallel projection shadow */
	private static final ShadowLayouter PARALLEL_SHADOW = new ParallelProjectionShadowLayouter(5, 5);

	public String getResourceCollectionContainerName()
	{
		return "plugin.modeler";
	}

	/**
	 * Option module containing all core Modeler options.
	 */
	public class ModelerOptions extends OptionModule
	{
		//////////////////////////////////////////////////
		// @@ Modeling options
		//////////////////////////////////////////////////

		/**
		 * Boolean option that is true if the parameter value wizard is enabled.
		 */
		public class ParamValueWizardOption extends BooleanOption
		{
			public ParamValueWizardOption()
			{
				super(getPluginResourceCollection(), "editor.paramvaluewizard", Boolean.TRUE);
			}
		}

		/**
		 * Boolean option that is true if the control link autoconnector is enabled.
		 */
		public class ControlLinkAutoconnectorOption extends BooleanOption
		{
			public ControlLinkAutoconnectorOption()
			{
				super(getPluginResourceCollection(), "editor.autoconnector.controllink", Boolean.TRUE);
			}
		}

		/**
		 * Option that defines the operation mode of the data link autoconnector.
		 */
		public class DataLinkAutoconnectorOption extends SelectionOption
		{
			public DataLinkAutoconnectorOption()
			{
				super(getPluginResourceCollection(), "editor.autoconnector.datalink", Integer.valueOf(AutoConnector.DLA_CONVERTIBLE_TYPES), new String [] { "off", "identical-names", "identical-types", "compatible-types", "castable-types", "convertible-types", }, new Object [] { Integer.valueOf(AutoConnector.DLA_OFF), Integer.valueOf(AutoConnector.DLA_IDENTICAL_NAMES), Integer.valueOf(AutoConnector.DLA_IDENTICAL_TYPES), Integer.valueOf(AutoConnector.DLA_COMPATIBLE_TYPES), Integer.valueOf(AutoConnector.DLA_CASTABLE_TYPES), Integer.valueOf(AutoConnector.DLA_CONVERTIBLE_TYPES), });
			}
		}

		//////////////////////////////////////////////////
		// @@ Display options
		//////////////////////////////////////////////////

		/**
		 * Boolean option that is true if the grid is shown.
		 */
		public class GridOnOffOption extends BooleanOption
		{
			public GridOnOffOption()
			{
				super(getPluginResourceCollection(), "editor.grid.display", Boolean.FALSE);
			}
		}

		/**
		 * Option that defines the grid type.
		 */
		public class GridTypeOption extends SelectionOption
		{
			public GridTypeOption()
			{
				super(getPluginResourceCollection(), "editor.grid.type", Integer.valueOf(WorkspaceDrawingView.GRIDTYPE_LINE), new String [] { "dots", "lines", "hexa", }, new Object [] { Integer.valueOf(WorkspaceDrawingView.GRIDTYPE_POINT), Integer.valueOf(WorkspaceDrawingView.GRIDTYPE_LINE), Integer.valueOf(WorkspaceDrawingView.GRIDTYPE_HEX), });
			}
		}

		/**
		 * This option defines the grid of the editor.
		 */
		public class ShadowOption extends SelectionOption
		{
			public ShadowOption()
			{
				super(getPluginResourceCollection(), "editor.shadow", PARALLEL_SHADOW, new String [] { "none", "parallel", }, new Object [] { NO_SHADOW, PARALLEL_SHADOW, });
			}
		}

		/**
		 * This option defines the color of the workspace.
		 */
		public class WorkspaceColorOption extends ColorOption
		{
			public WorkspaceColorOption()
			{
				super(getPluginResourceCollection(), "editor.color.workspace", ModelerColors.WORKSPACE);
			}
		}
	}
}
