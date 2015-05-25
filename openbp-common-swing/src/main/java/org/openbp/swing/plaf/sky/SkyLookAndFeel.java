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
package org.openbp.swing.plaf.sky;

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Look and feel for OpenBP software.
 *
 * @author Jens Ferchland
 */
public class SkyLookAndFeel extends MetalLookAndFeel
{
	/**
	 * The Name of the LookAndFeel
	 *
	 * @nowarn return
	 */
	public String getName()
	{
		return "OpenBP";
	}

	/**
	 * The ID of the LookAndFeel
	 *
	 * @nowarn return
	 */
	public String getID()
	{
		return "OpenBP";
	}

	/**
	 * The Describtion of the LookAndFeel
	 *
	 * @nowarn return
	 */
	public String getDescription()
	{
		return "The OpenBP Look and Feel";
	}

	/**
	 * The OpenBP look and feel is no native look and feel until
	 * we develop an own OperatingSystem :-)
	 *
	 * @nowarn return
	 */
	public boolean isNativeLookAndFeel()
	{
		return false;
	}

	/**
	 * The OpenBP look and feel will be supported.
	 *
	 * @nowarn return
	 */
	public boolean isSupportedLookAndFeel()
	{
		return true;
	}

	/**
	 * This Method is called by the UIManager to set the new UI.
	 * If the LookAndFeel needs any general properties they can
	 * be set here. This Method is called only once.
	 */
	public void initilize()
	{
	}

	/**
	 * This Method is called by the UIManager when setting a new UI.
	 * The Method is called only once. The UIDefauls are properties
	 * for the Component and the UIManager. The properties are saved
	 * in a HashTable. Properties are: <br>
	 * <ul>
	 * <li> UI Class to render a component
	 * <li> Information about colors
	 * <li> Information about borders
	 * <li> Information about behavior
	 * <li> extended user information
	 * </ul>
	 *
	 * @return an <code>UIDefaults</code> value
	 */
	public UIDefaults getDefaults()
	{
		UIDefaults uiDefaults = super.getDefaults();

		initClassDefaults(uiDefaults);
		initSystemColorDefaults(uiDefaults);
		initComponentDefaults(uiDefaults);

		return uiDefaults;
	}

	/**
	 * Initialize the uiClassID to BasicComponentUI mapping.
	 * The JComponent classes define their own uiClassID constants
	 * (see AbstractComponent.getUIClassID).  This table must
	 * map those constants to a BasicComponentUI class of the
	 * appropriate type.
	 *
	 * See BasicLookAndFeel#getDefaults
	 *
	 * @param table an <code>UIDefaults</code> value
	 */
	protected void initClassDefaults(UIDefaults table)
	{
		super.initClassDefaults(table);

		String skyPackageName = getClass().getName();
		skyPackageName = skyPackageName.substring(0, skyPackageName.lastIndexOf('.') + 1);

		// All classes that have their own look and feel that does not rely on
		// the metal L&F must go here
		Object [] uiDefaults = { "LabelUI", skyPackageName + "SkyLabelUI", "ButtonUI", skyPackageName + "SkyButtonUI", "ComboBoxUI", skyPackageName + "SkyComboBoxUI", "ToggleButtonUI", skyPackageName + "SkyToggleButtonUI", "TabbedPaneUI", skyPackageName + "SkyTabbedPaneUI", "ToolBarUI", skyPackageName + "SkyToolBarUI", "TableUI", skyPackageName + "SkyTableUI",
		// TODO Cleanup 5: Doesn't work correctly yet... "TreeUI",               skyPackageName + "SkyTreeUI",
			"PanelUI", skyPackageName + "SkyPanelUI", "PopupMenuUI", skyPackageName + "SkyPopupMenuUI", "SplitPaneUI", skyPackageName + "SkySplitPaneUI", "ScrollBarUI", skyPackageName + "SkyScrollBarUI", "SeparatorUI", skyPackageName + "SkySeparatorUI", "PopupMenuSeparatorUI", skyPackageName + "SkyPopupMenuSeparatorUI", "ToolTipUI", skyPackageName + "SkyToolTipUI", "MenuItemUI", skyPackageName + "SkyMenuItemUI", "FileChooserUI", skyPackageName + "SkyFileChooserUI", "ColorChooserUI", skyPackageName + "SkyColorChooserUI", };

		table.putDefaults(uiDefaults);
	}

	/**
	 * Load the SystemColors into the defaults table.  The keys
	 * for SystemColor defaults are the same as the names of
	 * the public fields in SystemColor.
	 *
	 * @param table an <code>UIDefaults</code> value
	 */
	protected void initSystemColorDefaults(UIDefaults table)
	{
		super.initSystemColorDefaults(table);

		// General color defaults
		Object [] systemColors = { "background", SkyTheme.COLOR_BACKGROUND, "desktop", SkyTheme.COLOR_BACKGROUND, "window", SkyTheme.COLOR_BACKGROUND, "windowText", SkyTheme.COLOR_TEXT_ENABLED, "menu", SkyTheme.COLOR_BACKGROUND, "menuText", SkyTheme.COLOR_TEXT_ENABLED, "text", SkyTheme.COLOR_BACKGROUND, "textText", SkyTheme.COLOR_TEXT_ENABLED, "textHighlight", SkyTheme.COLOR_HIGHLIGHT, "textHighlightText", SkyTheme.COLOR_TEXT_ENABLED, "textInactiveText", SkyTheme.COLOR_TEXT_DISABLED, "control", SkyTheme.COLOR_BACKGROUND, "controlText", SkyTheme.COLOR_TEXT_ENABLED, "controlHighlight", SkyTheme.COLOR_HIGHLIGHT, "scrollbar", SkyTheme.COLOR_BACKGROUND, "info", SkyTheme.COLOR_HIGHLIGHT, "infoText", SkyTheme.COLOR_TEXT_ENABLED, "focusColor", SkyTheme.COLOR_FOCUS, };

		for (int i = 0; i < systemColors.length; i += 2)
			table.put(systemColors [i], systemColors [i + 1]);
	}

	/**
	 * Load the defaults for any Component. The defaults will be used by the
	 * UIs to paint any component. Changes in Color, Borders and so on can
	 * apply her.
	 *
	 * @param table an <code>UIDefaults</code> value
	 */
	protected void initComponentDefaults(UIDefaults table)
	{
		super.initComponentDefaults(table);

		// Explicit color, font, etc. defaults
		Object [] defaults = {
			// TODO Try to get rid of the strange CTRL+SPACE or SHIFT+SPACE handling here
			"InternalFrame.windowBindings", null,
			"Button.enabled_background", SkyTheme.COLOR_BACKGROUND, "Button.enabled_foreground", SkyTheme.COLOR_TEXT_ENABLED, "Button.enabled_borderColor", SkyTheme.COLOR_BORDER_BUTTON, "Button.enabled_font", SkyTheme.FONT_NORMAL, "Button.disabled_background", SkyTheme.COLOR_BACKGROUND, "Button.disabled_foreground", SkyTheme.COLOR_TEXT_DISABLED, "Button.disabled_font", SkyTheme.FONT_NORMAL, "Button.disabled_borderColor", SkyTheme.COLOR_TRANSPARENT, "Button.focusColor", SkyTheme.COLOR_FOCUS, "Button.border", new ButtonBorder(), "Button.selectedBackground", SkyTheme.COLOR_FOCUS_OVERLAY, "Button.pressedBackground", SkyTheme.COLOR_BACKGROUND_DARK, "Button.focusInputMap", new UIDefaults.LazyInputMap(new Object [] { "SPACE", "pressed", "released SPACE", "released", "ENTER", "pressed", "released ENTER", "released", }),

		"Panel.background", SkyTheme.COLOR_BACKGROUND, "Panel.border", new EmptyBorder(0, 0, 0, 0),

		"ToolTip.font", SkyTheme.FONT_NORMAL,

		"ProgressBar.font", SkyTheme.FONT_SMALL, "ProgressBar.foreground", SkyTheme.COLOR_BORDER_BUTTON, "ProgressBar.background", SkyTheme.COLOR_BACKGROUND, "ProgressBar.border", SimpleBorder.getStandardBorder(), "ProgressBar.selectionForeground", SkyTheme.COLOR_TEXT_ENABLED, "ProgressBar.selectionBackground", SkyTheme.COLOR_TEXT_ENABLED,

		"Label.font", SkyTheme.FONT_NORMAL, "Label.background", SkyTheme.COLOR_BACKGROUND,

		"MenuBar.font", SkyTheme.FONT_NORMAL, "MenuBar.foreground", SkyTheme.COLOR_TEXT_ENABLED, "MenuBar.background", SkyTheme.COLOR_BACKGROUND,

		"Menu.font", SkyTheme.FONT_NORMAL, "Menu.foreground", SkyTheme.COLOR_TEXT_ENABLED, "Menu.background", SkyTheme.COLOR_BACKGROUND, "Menu.selectionForeground", SkyTheme.COLOR_TEXT_ENABLED, "Menu.selectionBackground", SkyTheme.COLOR_HIGHLIGHT, "Menu.disabledForeground", SkyTheme.COLOR_TEXT_DISABLED, "Menu.border", new EmptyBorder(2, new SkyMenuItemUI.CheckIcon().getIconWidth() + 2, 2, 2),

		"MenuItem.font", SkyTheme.FONT_NORMAL, "MenuItem.foreground", SkyTheme.COLOR_TEXT_ENABLED, "MenuItem.background", SkyTheme.COLOR_BACKGROUND, "MenuItem.selectionForeground", SkyTheme.COLOR_TEXT_ENABLED, "MenuItem.selectionBackground", SkyTheme.COLOR_HIGHLIGHT, "MenuItem.disabledForeground", SkyTheme.COLOR_TEXT_DISABLED, "MenuItem.border", new EmptyBorder(2, 2, 2, 2), "MenuItem.checkIcon", new SkyMenuItemUI.CheckIcon(),

		"TabbedPane.font", SkyTheme.FONT_SMALL, "TabbedPane.tabAreaBackground", SkyTheme.COLOR_BACKGROUND, "TabbedPane.foreground", SkyTheme.COLOR_TEXT_ENABLED, "TabbedPane.background", SkyTheme.COLOR_BACKGROUND, "TabbedPane.light", SkyTheme.COLOR_BACKGROUND, "TabbedPane.highlight", SkyTheme.COLOR_BORDER_BUTTON, "TabbedPane.focus", SkyTheme.COLOR_FOCUS, "TabbedPane.selected", SkyTheme.COLOR_FOCUS_OVERLAY,//BORDER_BUTTON,//COLOR_HIGHLIGHT,
			"TabbedPane.selectedHighlight", SkyTheme.COLOR_BACKGROUND, "TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0), "TabbedPane.tabAreaInsets", new Insets(1, 0, 1, 0), "TabbedPane.tabInsets", new Insets(2, 10, 2, 10),

			"ScrollBar.background", SkyTheme.COLOR_BACKGROUND, "ScrollBar.highlight", SkyTheme.COLOR_BACKGROUND, "ScrollBar.shadow", SkyTheme.COLOR_BACKGROUND, "ScrollBar.darkshadow", SkyTheme.COLOR_BACKGROUND, "ScrollBar.thumb", SkyTheme.COLOR_BORDER_BUTTON, "ScrollBar.scrollModelStep", Integer.valueOf(10), "ScrollBar.scrollBlockStep", Integer.valueOf(300),

			"ScrollPane.border", new EmptyBorder(0, 0, 0, 0), "Viewport.background", SkyTheme.COLOR_BACKGROUND_LIGHT,

			"List.background", SkyTheme.COLOR_BACKGROUND, "List.font", SkyTheme.FONT_NORMAL,

			"Table.background", SkyTheme.COLOR_BACKGROUND_LIGHT,

			"TableHeader.background", SkyTheme.COLOR_TABLE_HEADER, "TableHeader.font", SkyTheme.FONT_NORMAL, "TableHeader.cellBorder", SimpleBorder.getStandardBorder(),

			"Tree.background", SkyTheme.COLOR_BACKGROUND_LIGHT, "Tree.textBackground", SkyTheme.COLOR_BACKGROUND_LIGHT, "Tree.font", SkyTheme.FONT_NORMAL,

			"SplitPane.dividerSize", Integer.valueOf(3), "SplitPane.shadow", SkyTheme.COLOR_BACKGROUND, "SplitPane.darkShadow", SkyTheme.COLOR_BACKGROUND, "SplitPane.background", SkyTheme.COLOR_BACKGROUND, "SplitPane.border", new EmptyBorder(0, 0, 0, 0),

			"SplitPaneDivider.background", SkyTheme.COLOR_BACKGROUND, "SplitPaneDivider.foreground", SkyTheme.COLOR_FOCUS,

			"CheckBoxMenuItem.background", SkyTheme.COLOR_BACKGROUND, "CheckBoxMenuItem.font", SkyTheme.FONT_NORMAL, "CheckBoxMenuItem.selectionBackground", SkyTheme.COLOR_HIGHLIGHT, "CheckBoxMenuItem.border", new EmptyBorder(2, 2, 2, 2),

			"RadioButtonMenuItem.background", SkyTheme.COLOR_BACKGROUND, "RadioButtonMenuItem.font", SkyTheme.FONT_NORMAL, "RadioButtonMenuItem.selectionBackground", SkyTheme.COLOR_HIGHLIGHT, "RadioButtonMenuItem.border", new EmptyBorder(2, 2, 2, 2),

			"RadioButton.background", SkyTheme.COLOR_BACKGROUND, "RadioButton.font", SkyTheme.FONT_NORMAL,

			"CheckBox.background", SkyTheme.COLOR_BACKGROUND, "CheckBox.font", SkyTheme.FONT_NORMAL,

			"ToolBar.background", SkyTheme.COLOR_BACKGROUND, "ToolBar.border", new EmptyBorder(0, 0, 0, 0),

			"ComboBox.background", SkyTheme.COLOR_BACKGROUND_LIGHT_LIGHT, "ComboBox.font", SkyTheme.FONT_NORMAL, "ComboBox.buttonHighlight", SkyTheme.COLOR_BORDER_BUTTON, "ComboBox.buttonShadow", SkyTheme.COLOR_BACKGROUND_LIGHT, "ComboBox.buttonDarkShadow", SkyTheme.COLOR_BACKGROUND_LIGHT, "ComboBox.selectionBackground", SkyTheme.COLOR_HIGHLIGHT,

			"TextField.background", SkyTheme.COLOR_BACKGROUND_LIGHT_LIGHT, "TextField.border", new SimpleBorder(2, 2, 2, 2),

			"TextArea.background", SkyTheme.COLOR_BACKGROUND_LIGHT_LIGHT,

			"PopupMenu.background", SkyTheme.COLOR_BACKGROUND, "PopupMenu.border", new EmptyBorder(0, 0, SkyUtil.DEFAULTSHADOWDEPTH, SkyUtil.DEFAULTSHADOWDEPTH),

			"Separator.background", SkyTheme.COLOR_BACKGROUND_DARK, "Separator.foreground", SkyTheme.COLOR_BACKGROUND_DARK, "Separator.shadow", SkyTheme.COLOR_BACKGROUND_DARK, "Separator.heighlight", SkyTheme.COLOR_BACKGROUND_DARK,

			"Slider.background", SkyTheme.COLOR_BACKGROUND_LIGHT,

			"Spinner.background", SkyTheme.COLOR_BACKGROUND_LIGHT_LIGHT, "Spinner.border", new SimpleBorder(0, 0, 0, 0), "Spinner.arrowButtonSize", Integer.valueOf(3),

			"FileChooser.detailsViewIcon", new ImageIcon(getClass().getResource("icons/DetailsView.gif")), "FileChooser.homeFolderIcon", new ImageIcon(getClass().getResource("icons/HomeFolder.gif")), "FileChooser.listViewIcon", new ImageIcon(getClass().getResource("icons/ListView.gif")), "FileChooser.newFolderIcon", new ImageIcon(getClass().getResource("icons/NewFolder.gif")), "FileChooser.upFolderIcon", new ImageIcon(getClass().getResource("icons/UpFolder.gif")), };

		table.putDefaults(defaults);
	}
}
