package org.openbp.server.persistence.cayenne;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cayenne.map.AshwoodEntitySorter;
import org.apache.cayenne.map.DbEntity;

/**
 * Custom entity sorter.
 * Ensures that the state tables will be inserted before the actual object tables, so we won't get any foreign key violations.
 * It is important to define the relation from the state to the object table (the historic states relation) as optional, however.
 *
 * @author Heiko Erhardt
 */
public class CayenneEntitySorter extends AshwoodEntitySorter
{
	/** Table that maps preceeding to successing database table names for tables that require special order processing */
	private LinkedHashMap orderInstructions = new LinkedHashMap();

	/**
	 * Default constructor.
	 * @param dataMaps Collection of data maps to sort
	 */
	public CayenneEntitySorter(Collection dataMaps)
	{
		super(dataMaps);
	}

	public void addOrderInstruction(String preceedingTableName, String successingTableName)
	{
		orderInstructions.put(preceedingTableName, successingTableName);
	}

	public void addOrderInstructions(Map table)
	{
		for (Iterator it = table.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			orderInstructions.put(entry.getKey(), entry.getValue());
		}
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	public void sortDbEntities(List dbEntities, boolean deleteOrder)
	{
		super.sortDbEntities(dbEntities, deleteOrder);

		for (Iterator it = orderInstructions.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			ensureOrder(dbEntities, deleteOrder, (String) entry.getKey(), (String) entry.getValue());
		}
	}

	private void ensureOrder(List dbEntities, boolean deleteOrder, String name1, String name2)
	{
		int index1 = -1;
		int index2 = -2;

		int n = dbEntities.size();
		for (int i = 0; i < n; ++i)
		{
			DbEntity e = (DbEntity) dbEntities.get(i);
			String name = e.getName();
			if (name.equalsIgnoreCase(name1))
				index1 = i;
			else if (name.equalsIgnoreCase(name2))
				index2 = i;
		}

		boolean moveFirst = false;
		boolean moveLast = false;
		int firstLastIndex = -1;
		if (name1.equals("*"))
		{
			// Move the 2nd element to the end of the list
			firstLastIndex = index2;
			if (! deleteOrder)
			{
				moveLast = true;
			}
			else
			{
				moveFirst = true;
			}
		}
		else if (name2.equals("*"))
		{
			// Move the 1st element to the begin of the list
			firstLastIndex = index1;
			if (! deleteOrder)
			{
				moveFirst = true;
			}
			else
			{
				moveLast = true;
			}
		}

		if (moveFirst && firstLastIndex >= 0)
		{
			DbEntity first = (DbEntity) dbEntities.get(firstLastIndex);
			for (int i = firstLastIndex - 1; i >= 0; --i)
			{
				dbEntities.set(i + 1, dbEntities.get(i));
			}
			dbEntities.set(0, first);
		}
		else if (moveLast && firstLastIndex >= 0)
		{
			DbEntity last = (DbEntity) dbEntities.get(firstLastIndex);
			int size = dbEntities.size();
			for (int i = firstLastIndex + 1; i < size; ++i)
			{
				dbEntities.set(i - 1, dbEntities.get(i));
			}
			dbEntities.set(size - 1, last);
		}
		else if (index1 >= 0 && index2 >= 0)
		{
			boolean exchange = false;
			if (! deleteOrder)
			{
				if (index1 > index2)
				{
					exchange = true;
				}
			}
			else
			{
				if (index1 < index2)
				{
					exchange = true;
				}
			}

			if (exchange)
			{
				DbEntity tmp = (DbEntity) dbEntities.get(index1);
				dbEntities.set(index1, dbEntities.get(index2));
				dbEntities.set(index2, tmp);
			}
		}
	}
}
