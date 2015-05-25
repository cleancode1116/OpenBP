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
package org.openbp.common.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Comparator for the date object.
 */
public class DateComparator
	implements Comparator
{
	//////////////////////////////////////////////////
	// @@ Symbolic constants
	//////////////////////////////////////////////////

	/** Compare all date information */
	public static final int COMPARE_COMPLETE = -1;

	/** Compare the date until the minutes */
	public static final int COMPARE_MINUTES = 0;

	/** Compare the date until the hours */
	public static final int COMPARE_HOURS = 1;

	/** Compare the date until the date */
	public static final int COMPARE_DAYS = 2;

	/** Compare the date until the month */
	public static final int COMPARE_MONTH = 3;

	/** Compare the date until the year */
	public static final int COMPARE_YEAR = 4;

	/** Mapping array for the compare elements
	 * to the calender date information */
	public static final int [] CALENDAR_TYPES = new int [] { Calendar.MINUTE, Calendar.HOUR, Calendar.DATE, Calendar.MONTH, Calendar.YEAR };

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Date element type */
	private int dateElement = COMPARE_COMPLETE;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor for DateComparator.
	 */
	public DateComparator()
	{
		super();
	}

	//////////////////////////////////////////////////
	// @@ Parameter setter methods
	//////////////////////////////////////////////////

	/**
	 * Sets the date element type to compare
	 *
	 * @param dateElement	{@link #COMPARE_COMPLETE} ||
	 *						{@link #COMPARE_MINUTES} ||
	 *						{@link #COMPARE_HOURS} ||
	 *						{@link #COMPARE_DAYS} ||
	 *						{@link #COMPARE_MONTH} ||
	 *						{@link #COMPARE_YEAR} ||
	 */
	public void setDateElementToCompare(int dateElement)
	{
		this.dateElement = dateElement;
	}

	/**
	 * Compares two dates.
	 * @nowarn
	 */
	public int compare(Object o1, Object o2)
	{
		if (dateElement == COMPARE_COMPLETE)
		{
			return ((Date) o1).compareTo((Date) o2);
		}

		Calendar c1 = Calendar.getInstance();
		c1.setTime((Date) o1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime((Date) o2);

		for (int i = 5; i > dateElement; i--)
		{
			int result = compare(c1.get(CALENDAR_TYPES [i - 1]), c2.get(CALENDAR_TYPES [i - 1]));
			if (result != 0)
				return result;
		}

		return 0;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Compare int values.
	 * @nowarn
	 */
	private int compare(int i1, int i2)
	{
		if (i1 > i2)
			return 1;

		if (i1 < i2)
			return -1;

		return 0;
	}

	//////////////////////////////////////////////////
	// @@ Main method
	//////////////////////////////////////////////////

	/**
	 * Main.
	 * @nowarn
	 */
	public static void main(String [] args)
	{
		try
		{
			DateComparator dc = new DateComparator();
			dc.setDateElementToCompare(DateComparator.COMPARE_MONTH);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
			int i = dc.compare(df.parse("10.11.2002 10:30"), df.parse("10.11.2002 8:25"));
			System.out.println(i);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
}
