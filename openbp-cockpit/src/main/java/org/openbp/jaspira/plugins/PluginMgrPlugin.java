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
package org.openbp.jaspira.plugins;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.openbp.common.icon.MultiIcon;
import org.openbp.jaspira.gui.plugin.AbstractVisiblePlugin;
import org.openbp.jaspira.plugin.Plugin;
import org.openbp.jaspira.plugin.PluginMgr;
import org.openbp.swing.components.treetable.DefaultTableCellRenderer;

/**
 * This Plugin shows all installed Plugins.
 *
 * @author Jens Ferchland
 */
public class PluginMgrPlugin extends AbstractVisiblePlugin
{
	private static final Dimension MINIMUMSIZE = new Dimension(800, 600);

	public String getResourceCollectionContainerName()
	{
		return "plugin.standard";
	}

	protected void initializeComponents()
	{
		JTable pluginTable = new JTable(new PluginTableModel());

		TableColumnModel cmodel = pluginTable.getColumnModel();
		cmodel.getColumn(0).setMinWidth(100);
		cmodel.getColumn(1).setMinWidth(200);
		cmodel.getColumn(2).setMinWidth(20);
		cmodel.getColumn(2).setMaxWidth(80);

		pluginTable.setRowHeight(20);

		pluginTable.setDefaultRenderer(Plugin.class, new DefaultTableCellRenderer()
		{
			/**
			 * @see org.openbp.swing.components.treetable.DefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
			 */
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				JComponent comp = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (column == 0)
				{
					Plugin plugin = (Plugin) value;
					JLabel label = (JLabel) comp;
					label.setIcon((plugin.getIcon()).getIcon(MultiIcon.SMALL));
					label.setText(plugin.getTitle());
				}
				return comp;
			}
		});

		JPanel cp = getContentPane();
		cp.add(new JScrollPane(pluginTable));

		cp.setMinimumSize(MINIMUMSIZE);
		cp.setPreferredSize(MINIMUMSIZE);
	}

	//////////////////////////////////////////////////
	// @@ inner classes
	//////////////////////////////////////////////////

	private final static Comparator PLUGIN_TITLE_COMPARATOR = new Comparator()
	{
        public int compare(Object o1, Object o2)
        {
            return ((Plugin) o1).getTitle().compareTo(((Plugin) o2).getTitle());
        }
    };
    
    
    /**
	 * This is the table model of the plugin table which shows all
	 * plugins of the pluginManager.
	 */
	public class PluginTableModel
		implements TableModel
	{
		//////////////////////////////////////////////////
		// @@ Members
		//////////////////////////////////////////////////

		/** all known Plugins*/
		private Set pluginset;

		//////////////////////////////////////////////////
		// @@ construction
		//////////////////////////////////////////////////

		/**
		 * Default constructor
		 */
		public PluginTableModel()
		{
			pluginset = new TreeSet(PLUGIN_TITLE_COMPARATOR);

			pluginset.addAll(PluginMgr.getInstance().getPluginInstances());
		}

		
		//////////////////////////////////////////////////
		// @@ TableModule implementation
		//////////////////////////////////////////////////

		/**
		 * @see javax.swing.table.TableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int column)
		{
			if (column == 0)
			{
				return Plugin.class;
			}

			return String.class;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount()
		{
			return 3;
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnName(int)
		 */
		public String getColumnName(int column)
		{
			switch (column)
			{
			case 0:
				return getPluginResourceCollection().getRequiredString("plugintable.header.title");
			case 1:
				return getPluginResourceCollection().getRequiredString("plugintable.header.description");
			default:
				return getPluginResourceCollection().getRequiredString("plugintable.header.ui");
			}
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount()
		{
			return pluginset.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column)
		{
			Iterator it = pluginset.iterator();
			for (int i = 0; i < row; i++)
			{
				it.next();
			}

			Plugin plugin = (Plugin) it.next();

			switch (column)
			{
			case 0:
				return plugin;

			case 1:
				return plugin.getDescription();

			default:
				return plugin.getUniqueId().substring(plugin.getClassName().length() + 1);
			}
		}

		/**
		 * @see javax.swing.table.TableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int arg0, int arg1)
		{
			return false;
		}

		/**
		 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
		 */
		public void setValueAt(Object value, int row, int column)
		{
		}

		/**
		 * @see javax.swing.table.TableModel#addTableModelListener(TableModelListener)
		 */
		public void addTableModelListener(TableModelListener listener)
		{
			// We don't support listeners
		}

		/**
		 * @see javax.swing.table.TableModel#removeTableModelListener(TableModelListener)
		 */
		public void removeTableModelListener(TableModelListener listener)
		{
			// We don't support listeners
		}
	}
}
