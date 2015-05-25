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
package org.openbp.swing;

import javax.swing.SwingUtilities;

/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.
 * For instructions on using this class, see:
 *
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 *
 * @author Heiko Erhardt
 */
public abstract class SwingWorker
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Value constructed by the worker */
	private Object value;

	/** Thread variable */
	private ThreadVar threadVar;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Start a thread that will call the {@link #construct} method
	 * and then exit.
	 */
	public SwingWorker()
	{
		final Runnable doFinished = new Runnable()
		{
			public void run()
			{
				finished();
			}
		};

		Runnable doConstruct = new Runnable()
		{
			public void run()
			{
				try
				{
					setValue(construct());
				}
				finally
				{
					threadVar.clear();
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};

		Thread t = new Thread(doConstruct, "Swing worker");
		threadVar = new ThreadVar(t);
	}

	/**
	 * Compute the value to be returned by the get method.
	 * @return The result of the worker
	 */
	public abstract Object construct();

	//////////////////////////////////////////////////
	// @@ Execution
	//////////////////////////////////////////////////

	/**
	 * Start the worker thread.
	 */
	public void start()
	{
		Thread t = threadVar.get();
		if (t != null)
		{
			t.start();
		}
	}

	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the {@link #construct} method has returned.
	 */
	public void finished()
	{
	}

	/**
	 * A new method that interrupts the worker thread.  Call this method
	 * to force the worker to stop what it's doing.
	 */
	public void interrupt()
	{
		Thread t = threadVar.get();
		if (t != null)
		{
			t.interrupt();
		}
		threadVar.clear();
	}

	//////////////////////////////////////////////////
	// @@ Value access
	//////////////////////////////////////////////////

	/**
	 * Get the value produced by the worker thread, or null if it
	 * hasn't been constructed yet.
	 * @return The object value
	 */
	synchronized Object getValue()
	{
		return value;
	}

	/**
	 * Set the value produced by worker thread
	 * @param x Object value
	 */
	synchronized void setValue(Object x)
	{
		value = x;
	}

	/**
	 * Return the value created by the {@link #construct} method.
	 * Returns null if either the constructing thread or the current
	 * thread was interrupted before a value was produced.
	 *
	 * @return the value created by the {@link #construct} method
	 */
	public Object get()
	{
		while (true)
		{
			Thread t = threadVar.get();
			if (t == null)
			{
				return getValue();
			}
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}

	/**
	 * Class to maintain reference to current worker thread
	 * under separate synchronization control.
	 */
	private static class ThreadVar
	{
		private Thread thread;

		ThreadVar(Thread t)
		{
			thread = t;
		}

		synchronized Thread get()
		{
			return thread;
		}

		synchronized void clear()
		{
			thread = null;
		}
	}
}
