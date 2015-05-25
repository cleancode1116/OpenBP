package org.openbp.server.test.base;

import java.util.Hashtable;

import org.openbp.server.context.TokenContext;

/**
 * Test case syncronisation mgr.
 * This class is a singleton.
 *
 * @author Heiko Erhardt
 */
public class TestCaseSyncMgr
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Signal table */
	private final Hashtable<String, Object> signalTable = new Hashtable();

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Singleton instance */
	private static TestCaseSyncMgr singletonInstance;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Gets the singleton instance of this class.
	 * @nowarn
	 */
	public static synchronized TestCaseSyncMgr getInstance()
	{
		// Test code, singleton no problem here.
		if (singletonInstance == null)
			singletonInstance = new TestCaseSyncMgr();
		return singletonInstance;
	}

	/**
	 * Private constructor.
	 */
	private TestCaseSyncMgr()
	{
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the specified signal for the given context.
	 *
	 * @param signalId Signal id
	 * @param signalValue New signal value
	 */
	public void setSignal(String signalId, Object signalValue)
	{
		setSignal(null, signalId, signalValue);
	}

	/**
	 * Sets the specified signal for the given context.
	 *
	 * @param context Context
	 * @param signalId Signal id
	 */
	public void setSignal(TokenContext context, String signalId, Object signalValue)
	{
		String signalName = null;

		if (context != null)
		{
			String signalPrefix = (String) context.getParamValue("signalPrefix");
			if (signalPrefix != null)
			{
				signalName = signalPrefix + "." + signalId;
			}

			if (signalName == null)
			{
				signalName = context.getId().toString() + "." + signalId;
			}
		}

		if (signalName == null)
		{
			signalName = signalId;
		}

		signalTable.put(signalName, signalValue);

		synchronized (this)
		{
			notifyAll();
		}

		return;
	}

	/**
	 * Receives the specified signal (blocking operation).
	 * Waits until the signal has been set.
	 *
	 * @param context Context
	 * @param signalId Signal id
	 * @param timeoutSec Timeout in seconds
	 * @return The signal value
	 */
	public Object receiveSignal(TokenContext context, String signalId, int timeoutSec)
	{
		Object signalValue = null;

		for (int i = 0; i < timeoutSec; ++i)
		{
			signalValue = getSignal(context, signalId);
			if (signalValue != null)
				break;

			synchronized (this)
			{
				try
				{
					Thread.sleep(1000L);
				}
				catch (InterruptedException e)
				{
				}
			}
			continue;
		}

		return signalValue;
	}

	/**
	 * Gets the specified signal (non-blocking).
	 *
	 * @param context Context
	 * @param signalId Signal id
	 * @return The signal value or null if no such signal exists
	 */
	public Object getSignal(TokenContext context, String signalId)
	{
		String signalName;
		Object ret;

		if (context != null)
		{
			String signalPrefix = (String) context.getParamValue("signalPrefix");
			if (signalPrefix != null)
			{
				signalName = signalPrefix + "." + signalId;
				ret = signalTable.get(signalName);
				if (ret != null)
					return ret;
			}

			signalName = context.getId().toString() + "." + signalId;
			ret = signalTable.get(signalName);
			if (ret != null)
				return ret;
		}

		signalName = signalId;
		ret = signalTable.get(signalName);

		return ret;
	}
}
