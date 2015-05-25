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
package org.openbp.common.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Iterator;

/**
 * Convenience class that adds event firing methods for listeners of the java.bean package to the listener support class.
 *
 * @author Heiko Erhardt
 */
public class BeanListenerSupport extends ListenerSupport
{
	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public BeanListenerSupport()
	{
	}

	//////////////////////////////////////////////////
	// @@ PropertyChangeListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'property change' message to all registered property change listeners.
	 *
	 * @param e Event to fire
	 */
	public void firePropertyChange(PropertyChangeEvent e)
	{
		for (Iterator it = getListenerIterator(PropertyChangeListener.class); it.hasNext();)
		{
			((PropertyChangeListener) it.next()).propertyChange(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ VetoableChangeListener support
	//////////////////////////////////////////////////

	/**
	 * Fires an 'vetoable change' message to all registered vetoable change listeners.
	 *
	 * @param e Event to fire
	 * @throws PropertyVetoException if one of the registered listeners wants to veto the change
	 */
	public void fireVetoableChange(PropertyChangeEvent e)
		throws PropertyVetoException
	{
		for (Iterator it = getListenerIterator(VetoableChangeListener.class); it.hasNext();)
		{
			((VetoableChangeListener) it.next()).vetoableChange(e);
		}
	}
}
