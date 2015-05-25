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
package org.openbp.cockpit.plugins.finder.test;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.openbp.cockpit.plugins.finder.treemodel.GenericModel;
import org.openbp.cockpit.plugins.finder.treemodel.Strategy;
import org.openbp.swing.components.tree.TreeExpander;

/**
 * TreeExpander test class.
 *
 * @author Baumgartner Michael
 */
public class TestExpander extends JFrame
{
	static String [] data = new String [] { "/Group1/subgroup2/Node1", "/Group1/subgroup2/Node2", "/Group1/subgroup2/Node3", "/Group1/subgroup2/Node4", "/Group2/Node1", "/Group2/Node2", "/Group2/Node6", "/Group2/Node7", "/Group2/Node8", "/Group3/Action:GetExportFileBeans.In.Company5", "/Group3/Action:GetExportFileBeans.In.Company4", "/Group3/Action:GetExportFileBeans.In.Company3", "/Group3/Action:1etExportFileBeans.In.Company2", "/Group4/Subgroup1/Node1", "/Group4/Subgroup1/Node2", "/Group4/Subgroup1/Node3", "/Group4/Subgroup1/Node4", "/Group4/Subgroup1", "/Group4/Subgroup1", "/Group4/Node1", "/Group4/Node2", "/Group4/Node3", "/Group4/Node4", "/Node5" };

	public static void main(String [] args)
	{
		for (int i = 200; i < 800; i += 20)
		{
			show(i, 1, 2000, true);
		}
	}

	public static void show(int height, int level, long time, boolean intelli)
	{
		Strategy strategy = new RefStrategy();
		GenericModel model = new GenericModel(strategy, true);
		model.reload(data);

		JFrame dialog = new TestExpander();
		JTree tree = new JTree();
		tree.setModel(model);
		dialog.getContentPane().add(new JScrollPane(tree));
		dialog.setSize(new Dimension(300, height));
		dialog.setVisible(true);

		TreeExpander treeExpander = new TreeExpander(tree);
		if (intelli)
			treeExpander.intelliExpand(level);
		else
			treeExpander.expandLevel(level);

		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
		}
		dialog.setVisible(false);
		dialog.dispose();
	}
}
