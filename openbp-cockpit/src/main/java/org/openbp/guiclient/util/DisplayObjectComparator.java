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
package org.openbp.guiclient.util;

import java.util.Comparator;

import org.openbp.common.generic.description.DisplayObject;
import org.openbp.guiclient.plugins.displayobject.DisplayObjectPlugin;

/**
 * Display object comparator.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public final class DisplayObjectComparator
	implements Comparator
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static DisplayObjectComparator singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized DisplayObjectComparator getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new DisplayObjectComparator();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private DisplayObjectComparator()
	{
	}

	//////////////////////////////////////////////////
	// @@ Comparator implementation
	//////////////////////////////////////////////////

	public int compare(Object o1, Object o2)
	{
		DisplayObject d1 = (DisplayObject) o1;
		DisplayObject d2 = (DisplayObject) o2;

		String s1;
		String s2;

		if (DisplayObjectPlugin.getInstance().isTitleModeText())
		{
			s1 = d1.getDisplayText();
			s2 = d2.getDisplayText();
		}
		else
		{
			s1 = d1.getName();
			s2 = d2.getName();
		}

		int ret = 0;
		try
		{
			ret = s1.compareTo(s2);
		}
		catch (NullPointerException e)
		{
			System.err.println(e);
		}

		/*
		 if (s1 != null && s2 != null)
		 ret = s1.compareTo (s2);
		 else if (s1 != null)
		 ret = -1;
		 else if (s2 != null)
		 ret = 1;
		 */
		return ret;
	}
}
