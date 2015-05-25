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

import org.openbp.cockpit.itemeditor.NodeItemEditorPlugin;
import org.openbp.cockpit.modeler.drawing.DrawingEditorPlugin;
import org.openbp.cockpit.modeler.drawing.ProcessDrawing;
import org.openbp.cockpit.modeler.figures.VisualElement;
import org.openbp.cockpit.modeler.figures.tag.AbstractTagFigure;
import org.openbp.common.setting.SettingUtil;
import org.openbp.jaspira.action.ActionMgr;
import org.openbp.jaspira.action.JaspiraAction;
import org.openbp.jaspira.event.JaspiraEventMgr;

/**
 * The view mode manager determines the way a process is being displayed in the Modeler.
 * This includes the display mode of control and data links, parameters etc.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class ViewModeMgr
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Control anchor visibility */
	private boolean controlAnchorVisible = false;

	/** Control link visibility */
	private boolean controlLinkVisible = true;

	/** Data link visibility */
	private boolean dataLinkVisible = true;

	/** Tag state */
	private int tagState = AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_DATA;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static ViewModeMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ViewModeMgr getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ViewModeMgr();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ViewModeMgr()
	{
		controlAnchorVisible = SettingUtil.getBooleanSetting("editor.view.controlanchorvisibility", false);
		controlLinkVisible = SettingUtil.getBooleanSetting("editor.view.controllinkvisibility", true);
		dataLinkVisible = SettingUtil.getBooleanSetting("editor.view.datalinkvisibility", true);

		tagState = 0;
		if (controlAnchorVisible)
		{
			tagState |= AbstractTagFigure.CONTENT_FLOW;
		}
		if (dataLinkVisible)
		{
			tagState |= AbstractTagFigure.CONTENT_DATA;
		}
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the control anchor visibility, considering the modeler type.
	 * Always returns true in case of the  component editor.
	 * @nowarn
	 */
	public boolean isControlAnchorVisible(VisualElement ve)
	{
		if (isDisplayedInNodeItemEditor(ve))
			return true;
		return controlAnchorVisible;
	}

	/**
	 * Gets the control anchor visibility.
	 * @nowarn
	 */
	public boolean isControlAnchorVisible()
	{
		return controlAnchorVisible;
	}

	/**
	 * Sets the control anchor visibility.
	 * @nowarn
	 */
	public void setControlAnchorVisible(boolean controlAnchorVisible)
	{
		this.controlAnchorVisible = controlAnchorVisible;

		if (controlAnchorVisible)
		{
			tagState |= AbstractTagFigure.CONTENT_FLOW;
		}
		else
		{
			tagState &= ~AbstractTagFigure.CONTENT_FLOW;
		}

		SettingUtil.setBooleanSetting("editor.view.controlanchorvisibility", controlAnchorVisible);
		SettingUtil.saveSettings(null);

		// The selection state of the button should be on if the control links are hidden
		JaspiraAction action = ActionMgr.getInstance().getAction("modelerpage.view.controlanchor");
		if (action != null)
		{
			action.setSelected(!controlAnchorVisible);
		}

		// Notify modelers of status change
		JaspiraEventMgr.fireGlobalEvent("modeler.view.modechange");
	}

	/**
	 * Gets the control link visibility, considering the modeler type.
	 * Always returns true in case of the  component editor.
	 * @nowarn
	 */
	public boolean isControlLinkVisible(VisualElement ve)
	{
		if (isDisplayedInNodeItemEditor(ve))
			return true;
		return controlLinkVisible;
	}

	/**
	 * Gets the control link visibility.
	 * @nowarn
	 */
	public boolean isControlLinkVisible()
	{
		return controlLinkVisible;
	}

	/**
	 * Sets the control link visibility.
	 * @nowarn
	 */
	public void setControlLinkVisible(boolean controlLinkVisible)
	{
		this.controlLinkVisible = controlLinkVisible;

		SettingUtil.setBooleanSetting("editor.view.controllinkvisibility", controlLinkVisible);
		SettingUtil.saveSettings(null);

		// The selection state of the button should be on if the control links are hidden
		JaspiraAction action = ActionMgr.getInstance().getAction("modelerpage.view.controltoggle");
		if (action != null)
		{
			action.setSelected(!controlLinkVisible);
		}

		// Notify modelers of status change
		JaspiraEventMgr.fireGlobalEvent("modeler.view.modechange");
	}

	/**
	 * Gets the data link visibility, considering the modeler type.
	 * Always returns true in case of the  component editor.
	 * @nowarn
	 */
	public boolean isDataLinkVisible(VisualElement ve)
	{
		if (isDisplayedInNodeItemEditor(ve))
			return true;
		return dataLinkVisible;
	}

	/**
	 * Gets the data link visibility.
	 * @nowarn
	 */
	public boolean isDataLinkVisible()
	{
		return dataLinkVisible;
	}

	/**
	 * Sets the data link visibility.
	 * @nowarn
	 */
	public void setDataLinkVisible(boolean dataLinkVisible)
	{
		this.dataLinkVisible = dataLinkVisible;

		if (dataLinkVisible)
		{
			tagState |= AbstractTagFigure.CONTENT_DATA;
		}
		else
		{
			tagState &= ~AbstractTagFigure.CONTENT_DATA;
		}

		SettingUtil.setBooleanSetting("editor.view.datalinkvisibility", dataLinkVisible);
		SettingUtil.saveSettings(null);

		// The selection state of the button should be on if the data links are hidden
		JaspiraAction action = ActionMgr.getInstance().getAction("modelerpage.view.datatoggle");
		if (action != null)
		{
			action.setSelected(!dataLinkVisible);
		}

		// Notify modelers of status change
		JaspiraEventMgr.fireGlobalEvent("modeler.view.modechange");
	}

	/**
	 * Gets the tag state, considering the modeler type.
	 * Always returns true in case of the  component editor.
	 * @nowarn
	 */
	public int getTagState(VisualElement ve)
	{
		if (isDisplayedInNodeItemEditor(ve))
		{
			return tagState | AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_DATA;
		}
		return tagState;
	}

	/**
	 * Gets the tag state, considering the modeler type.
	 * Always returns true in case of the  component editor.
	 * @nowarn
	 */
	public int getTagState(DrawingEditorPlugin editor)
	{
		if (editor instanceof NodeItemEditorPlugin)
		{
			return tagState | AbstractTagFigure.CONTENT_FLOW | AbstractTagFigure.CONTENT_DATA;
		}
		return tagState;
	}

	/**
	 * Gets the tag state.
	 * @nowarn
	 */
	public int getTagState()
	{
		return tagState;
	}

	/**
	 * Sets the tag state.
	 * @nowarn
	 */
	public void setTagState(int tagState)
	{
		this.tagState = tagState;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	private boolean isDisplayedInNodeItemEditor(VisualElement ve)
	{
		if (ve != null)
		{
			ProcessDrawing drawing = ve.getDrawing();
			if (drawing != null)
			{
				if (drawing.getEditor() instanceof NodeItemEditorPlugin)
					return true;
			}
		}
		return false;
	}
}
