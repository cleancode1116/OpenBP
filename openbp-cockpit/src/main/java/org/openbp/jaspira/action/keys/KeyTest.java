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
package org.openbp.jaspira.action.keys;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.openbp.swing.SwingUtil;

public class KeyTest
{
	/**
	 * Constructor.
	 */
	private KeyTest()
	{
	}

	public static void main(String [] args)
	{
		JFrame frame = new JFrame();

		JPanel panel = new JPanel();

		frame.setContentPane(panel);

		InputMap map = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke key = KeyStroke.getKeyStroke("control released A");
		System.out.println(key + " : " + key.getModifiers());

		map.put(key, "C-a");

		key = KeyStroke.getKeyStroke("shift released A");
		System.out.println(key + " : " + key.getModifiers());

		map.put(key, "S-a");

		key = KeyStroke.getKeyStroke("released A");
		System.out.println(key + " : " + key.getModifiers());

		map.put(key, "a");

		ActionMap amap = panel.getActionMap();

		amap.put("C-a", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("C-a");
			}
		});
		amap.put("S-a", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("S-a");
			}
		});
		amap.put("a", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("a");
			}
		});

		panel.add(new JLabel("Test"));

		frame.pack();
		SwingUtil.show(frame);
	}
}
