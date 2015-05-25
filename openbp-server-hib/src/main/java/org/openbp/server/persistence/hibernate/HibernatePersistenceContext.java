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
package org.openbp.server.persistence.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openbp.common.logger.LogUtil;
import org.openbp.server.persistence.BasicPersistenceContext;
import org.openbp.server.persistence.BasicPersistenceContextProvider;
import org.openbp.server.persistence.DeferedOnLoadCollection;
import org.openbp.server.persistence.PersistenceCriterion;
import org.openbp.server.persistence.PersistenceException;
import org.openbp.server.persistence.PersistenceOrdering;
import org.openbp.server.persistence.PersistenceQuery;
import org.openbp.server.persistence.PersistentObjectNotFoundException;
import org.openbp.server.persistence.TransactionGuard;

/**
 * The Hibernate persistence context represents a Hibernate session.
 *
 * @author Heiko Erhardt
 */
public class HibernatePersistenceContext extends BasicPersistenceContext
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Hibernate session factory */
	protected final SessionFactory sessionFactory;

	/** Underlying Hibernate session */
	protected Session session;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param sessionFactory Hibernate session factory object
	 * @param provider Persistence context provider
	 */
	public HibernatePersistenceContext(final SessionFactory sessionFactory, final BasicPersistenceContextProvider provider)
	{
		super(provider);

		this.sessionFactory = sessionFactory;
	}

	//////////////////////////////////////////////////
	// @@ General
	//////////////////////////////////////////////////

	/**
	 * Releases the context.
	 * The underlying database session will be closed.
	 */
	public void release()
	{
		if (session != null)
		{
			try
			{
				LogUtil.debug(getClass(), "Closing Hibernate session $0.", session);
				session.close();
				session = null;
			}
			catch (HibernateException e)
			{
				throw createLoggedException(e);
			}
		}
		super.release();
	}

	/**
	 * Determines if the given class is managed by this persistence context.
	 *
	 * @param cls Class to check (usually a class implementing PeristentObject)
	 * @nowarn
	 */
	public boolean isPersistentClass(Class cls)
	{
		cls = getMappedObjectClass(cls, false);
		return getHibernateSession().getSessionFactory().getClassMetadata(cls) != null;
	}

	//////////////////////////////////////////////////
	// @@ Object access
	//////////////////////////////////////////////////

	/**
	 * Gets the primary key of the given object.
	 *
	 * @param obj The object
	 * @return The primary key or null if the object is not persistent or if it has not been inserted into the database yet
	 * @throws PersistenceException On error
	 */
	public Object getObjectId(final Object obj)
		throws PersistenceException
	{
		try
		{
			Serializable ident = getHibernateSession().getIdentifier(obj);
			return ident;
		}
		catch (HibernateException e)
		{
			throw createLoggedException(e);
		}
	}

	/**
	 * Merges the (transient) given object with the current session and returns the persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @throws PersistenceException On error
	 * @return The merged object
	 */
	public Object merge(Object obj)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			obj = getHibernateSession().merge(obj);
			return obj;
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Refreshes the given persistent object.
	 * Reloads the object values from the database.
	 *
	 * @param obj Object to refresh
	 * @throws PersistenceException On error
	 */
	public void refreshObject(final Object obj)
		throws PersistenceException
	{
		if (obj != null)
		{
			TransactionGuard tg = new TransactionGuard(this);
			try
			{
				getHibernateSession().refresh(obj);
			}
			catch (UnresolvableObjectException e)
			{
				tg.doCatch();
				String msg = LogUtil.error(getClass(), "Persistence error.", e);
				throw new PersistentObjectNotFoundException(msg, e);
			}
			catch (HibernateException e)
			{
				tg.doCatch();
				throw createLoggedException(e);
			}
			finally
			{
				tg.doFinally();
			}
		}
	}

	/**
	 * Removes the given persistent object from the session cache.
	 *
	 * @param obj Object to evict
	 * @throws PersistenceException On error
	 */
	public void evict(final Object obj)
		throws PersistenceException
	{
		if (obj != null)
		{
			try
			{
				getHibernateSession().evict(obj);
			}
			catch (HibernateException e)
			{
				throw createLoggedException(e);
			}
		}
	}

	/**
	 * Finds an object by its primary key.
	 *
	 * @param id Primary key
	 * @param cls Type of object to lookup (usually a class implementing PeristentObject)
	 * @return The object or null if no such object can be found
	 * @throws PersistenceException On error
	 */
	public Object findById(final Object id, Class cls)
		throws PersistenceException
	{
		cls = getMappedObjectClass(cls, false);
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			Object ret = getHibernateSession().get(cls, (Serializable) id);

			getPersistenceContextProvider().fireOnLoad(ret, this);

			return ret;
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Returns a list of the objects of a particular type that match the given criterion.
	 *
	 * @param query Query to run
	 * @return The list or null
	 * @throws PersistenceException On error
	 */
	public Collection runQuery(final PersistenceQuery query)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			Class cls = getMappedObjectClass(query.getObjectClass(), false);
			Criteria hc = getHibernateSession().createCriteria(cls);
			if (query.getMaxResults() > 0)
			{
				hc.setMaxResults(query.getMaxResults());
			}

			for (Iterator it = query.getOrderings(); it.hasNext();)
			{
				PersistenceOrdering ordering = (PersistenceOrdering) it.next();

				Order ho = ordering.isAscending() ? Order.asc(ordering.getPropertyName()) : Order.desc(ordering.getPropertyName());
				hc.addOrder(ho);
			}

			for (Iterator it = query.getCriterions(); it.hasNext();)
			{
				PersistenceCriterion criterion = (PersistenceCriterion) it.next();

				String property = criterion.getProperty();
				String operator = criterion.getOperator();
				Object value = criterion.getOperand();
				if (PersistenceCriterion.OPERATOR_EQ.equals(operator))
				{
					hc = hc.add(Restrictions.eq(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_EQ_OR_NULL.equals(operator))
				{
					hc = hc.add(Restrictions.disjunction().add(Restrictions.isNull(property)).add(Restrictions.eq(property, value)));
				}
				else if (PersistenceCriterion.OPERATOR_NEQ.equals(operator))
				{
					hc = hc.add(Restrictions.ne(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_GT.equals(operator))
				{
					hc = hc.add(Restrictions.gt(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_GTE.equals(operator))
				{
					hc = hc.add(Restrictions.ge(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_LT.equals(operator))
				{
					hc = hc.add(Restrictions.lt(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_LTE.equals(operator))
				{
					hc = hc.add(Restrictions.le(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_LIKE.equals(operator))
				{
					hc = hc.add(Restrictions.gt(property, value));
				}
				else if (PersistenceCriterion.OPERATOR_NULL.equals(operator))
				{
					hc = hc.add(Restrictions.isNull(property));
				}
				else if (PersistenceCriterion.OPERATOR_ALIAS.equals(operator))
				{
					hc = hc.createAlias(property, (String) value);
				}
			}

			// Run query and wrap result list into a collection that calls onLoad for each element that is being accessed.
			Collection root = hc.list();
			return new DeferedOnLoadCollection(root, this);
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ Object modification
	//////////////////////////////////////////////////

	/**
	 * Saves the given object to the persistent storage.
	 * According to the persistence state of the object, performs an update or insert.
	 *
	 * @param o Object to insert or update
	 * @return The updated object; usually this will be identical to the argument object,
	 * except if the object already exists in persistent storage and a merge operation needs to be performed.
	 * @throws PersistenceException On error
	 */
	public Object saveObject(final Object o)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			getPersistenceContextProvider().fireBeforeSave(o, this);

			getHibernateSession().saveOrUpdate(o);

			getPersistenceContextProvider().fireAfterSave(o, this);

			return o;
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Deletes an object from persistent storage.
	 *
	 * @param o Object to delete
	 * @throws PersistenceException On error
	 */
	public void deleteObject(final Object o)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			getPersistenceContextProvider().fireBeforeDelete(o, this);

			getHibernateSession().delete(o);

			getPersistenceContextProvider().fireAfterDelete(o, this);
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ SQL support
	//////////////////////////////////////////////////

	/**
	 * Runs the given SQL update or delete statement.
	 *
	 * @param sql An SQL update statement
	 * @return The number of rows affected.
	 * @throws PersistenceException On error
	 */
	public int executeUpdateOrDelete(String sql)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			SQLQuery query = getHibernateSession().createSQLQuery(sql);
			int count = query.executeUpdate();
			return count;
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	/**
	 * Runs the given SQL select statement.
	 *
	 * @param sql SQL query to run
	 * @param maxResults Maximum number of result rows or 0 for unlimited
	 * @return A list of result elements (contains Object or Object[] elements, depending if this was a single column or multi-column query)
	 * @throws PersistenceException On error
	 */
	public Collection executeSelect(String sql, int maxResults)
		throws PersistenceException
	{
		TransactionGuard tg = new TransactionGuard(this);
		try
		{
			SQLQuery query = getHibernateSession().createSQLQuery(sql);
			if (maxResults > 0)
			{
				query.setMaxResults(maxResults);
			}

			Collection root = query.list();
			return new DeferedOnLoadCollection(root, this);
		}
		catch (HibernateException e)
		{
			tg.doCatch();
			throw createLoggedException(e);
		}
		finally
		{
			tg.doFinally();
		}
	}

	//////////////////////////////////////////////////
	// @@ Transaction control
	//////////////////////////////////////////////////

	/**
	 * Checks if a transaction is currently in progress.
	 *
	 * @return true if transaction has been started and not yet committed or rolled back
	 */
	public boolean isTransactionActive()
		throws PersistenceException
	{
		return getHibernateTransaction().isActive();
	}

	/**
	 * Begins a new transaction.
	 * Does nothing if a transaction is already running.
	 */
	public void beginTransaction()
		throws PersistenceException
	{
		if (! getHibernateTransaction().isActive())
		{
			LogUtil.trace(getClass(), "Beginning transaction.");
			getHibernateTransaction().begin();
		}
	}

	/**
	 * Reverts any changes that have occurred to objects within the transaction.
	 * Does nothing if no transaction is currently running.
	 */
	public void rollbackTransaction()
		throws PersistenceException
	{
		if (getHibernateTransaction().isActive())
		{
			LogUtil.trace(getClass(), "Rolling back transaction on.");

			try
			{
				getHibernateTransaction().rollback();
			}
			catch (HibernateException e)
			{
				throw createLoggedException(e);
			}
		}
	}

	/**
	 * Commits any changes that have occurred to objects within the transaction.
	 * Does nothing if no transaction is currently running.
	 *
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void commitTransaction()
		throws PersistenceException
	{
		if (getHibernateTransaction().isActive())
		{
			LogUtil.trace(getClass(), "Committing transaction.");

			try
			{
				getHibernateSession().flush();
				getHibernateTransaction().commit();
				LogUtil.trace(getClass(), "Committed transaction.");
			}
			catch (HibernateException e)
			{
				throw createLoggedException(e);
			}
		}
	}

	/**
	 * Forces the underlying persistence session to flush.
	 * A flush will synchronize the underlying persistent storage with the object state in memory.
	 * However, a flush operation will not change the transaction state.
	 *
	 * @throws PersistenceException On error performing the operations on the persistent storage
	 */
	public void flush()
		throws PersistenceException
	{
		try
		{
			getHibernateSession().flush();
		}
		catch (HibernateException e)
		{
			throw createLoggedException(e);
		}
	}

	//////////////////////////////////////////////////
	// @@ Hibernate-specific
	//////////////////////////////////////////////////

	/**
	 * Gets the underlying hibernate session object.
	 * @nowarn
	 */
	public Session getHibernateSession()
	{
		if (session != null)
		{
			if (! session.isOpen())
			{
				LogUtil.debug(getClass(), "Hibernate session $0 was closed, going to open a new session.", session);
				session = null;
			}
		}

		if (session == null)
		{
			session = sessionFactory.openSession();
			LogUtil.debug(getClass(), "Opened Hibernate session $0.", session);
		}

		return session;
	}

	private Transaction getHibernateTransaction()
	{
		return getHibernateSession() != null ? getHibernateSession().getTransaction() : null;
	}
}
