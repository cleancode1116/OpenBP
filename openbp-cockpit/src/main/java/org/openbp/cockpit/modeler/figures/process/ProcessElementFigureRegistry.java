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
/*
 * Created on 14.09.2005
 * $Id$
 *
 * Copyright (c) 2005 Kassenaerztliche Vereinigung Bayerns.
 * All rights reserved. Use is subject to licence terms.
 * 
 * Author: Heiko Erhardt (Heiko.Erhardt@gmx.net)
 */
package org.openbp.cockpit.modeler.figures.process;

import java.util.HashMap;
import java.util.Map;

import org.openbp.common.ReflectUtil;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessObject;

/**
 * Node figure registry.
 * This class is a singleton.
 *
 * @author Author: Heiko Erhardt (Heiko.Erhardt@gmx.net)
 * @version $Rev$, $Date$
 */
public class ProcessElementFigureRegistry
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Table mapping nodes ({@link Node}) to node figures ({@link NodeFigure}) */
	private Map elementClassToFigureClass;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static ProcessElementFigureRegistry singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized ProcessElementFigureRegistry getInstance()
	{
		if (singletonInstance == null)
			singletonInstance = new ProcessElementFigureRegistry();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private ProcessElementFigureRegistry()
	{
		elementClassToFigureClass = new HashMap();
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Registers a figure for a process element.
	 *
	 * @param figureClass Class of the figure
	 * @param elementClass Class of the process element the figure can represent
	 */
	public void registerFigure(Class figureClass, Class elementClass)
	{
		elementClassToFigureClass.put(elementClass, figureClass);
	}

	/**
	 * Creates a new process element container figure for the given process element.
	 *
	 * @return The new figure
	 */
	public ProcessElementContainer createProcessElementContainer(ProcessObject po)
	{
		Class figureClass = findFigureClass(po.getClass());

		if (figureClass == null)
		{
			throw new RuntimeException("Cannot find figure for process object '" + po.getClass() + "'.");
		}

		try
		{
			return (ProcessElementContainer) ReflectUtil.instantiate(figureClass, ProcessElementContainer.class, "process element figure");
		}
		catch (Exception e)
		{
			throw new RuntimeException("Cannot instantiate figure class", e);
		}
	}

	private Class findFigureClass(Class poCls)
	{
		Class figureClass = (Class) elementClassToFigureClass.get(poCls);

		if (figureClass == null)
		{
			Class [] interfaces = poCls.getInterfaces();
			for (int i = 0; i < interfaces.length; ++i)
			{
				figureClass = findFigureClass(interfaces [i]);
				if (figureClass != null)
					break;
			}
		}

		return figureClass;
	}
}
