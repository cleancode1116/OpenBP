package org.openbp.server.test.base;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.openbp.common.util.observer.EventObserver;
import org.openbp.common.util.observer.ObserverEvent;

/**
 * Engine event observer that counts the number of events.
 *
 * @author Heiko Erhardt
 */
public class EngineEventCounter
	implements EventObserver
{
	/** Table that maps the event count by the event name */
	private LinkedHashMap countByEventName = new LinkedHashMap();

	/**
	 * Default constructor.
	 */
	public EngineEventCounter()
	{
	}

	/**
	 * Template method that will be called whenever an event occurs the observer is interested in.
	 * @param e Event
	 */
	public void observeEvent(ObserverEvent e)
	{
		String eventName = e.getEventType();

		Integer iCount = (Integer) countByEventName.get(eventName);
		int count = iCount != null ? iCount.intValue() : 0;
		countByEventName.put(eventName, Integer.valueOf(count + 1));
	}

	/**
	 * Verifies that the actual number of event calls matches the expected number of event calls.
	 *
	 * @param eventName Event name
	 * @param expectedCount Expected count
	 */
	public void checkNumberOfEventCalls(String eventName, int expectedCount)
	{
		Integer iCount = (Integer) countByEventName.get(eventName);
		int count = iCount != null ? iCount.intValue() : 0;
		if (count != expectedCount)
		{
			TestCase.assertEquals("Incorrect number of generated '" + eventName + "' events.", expectedCount, count);
		}
	}

	/**
	 * Method that prints event count test code to standard out.
	 * Used for test case coding.
	 */
	public void printTestCode()
	{
		System.out.println("\tprotected void checkEventCounts()");
		System.out.println("\t{");

		for (Iterator it = countByEventName.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			String eventName = (String) entry.getKey();
			Integer iCount = (Integer) entry.getValue();
			int count = iCount != null ? iCount.intValue() : 0;
			System.out.println("\t\tgetEngineEventCounter().checkNumberOfEventCalls(\"" + eventName + "\", " + count + ");");
		}

		System.out.println("\t}");
	}
}
