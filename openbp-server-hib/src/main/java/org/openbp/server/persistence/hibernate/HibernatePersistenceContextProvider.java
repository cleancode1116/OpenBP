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

import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.openbp.server.context.TokenContextImpl;
import org.openbp.server.context.WorkflowTaskImpl;
import org.openbp.server.model.modelmgr.DbModel;
import org.openbp.server.model.modelmgr.DbModelItem;
import org.openbp.server.persistence.BasicPersistenceContextProvider;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceException;

/**
 * Peristence context provider for a Hibernate-based persistence context.
 *
 * @author Heiko Erhardt
 */
public class HibernatePersistenceContextProvider extends BasicPersistenceContextProvider
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Session factory */
	protected transient SessionFactory sessionFactory;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public HibernatePersistenceContextProvider()
	{
	}

	/**
	 * @seem BasicPersistenceContextProvider.shutdown()
	 */
	public void shutdown()
	{
		if (sessionFactory != null)
		{
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	/**
	 * Creates a new persistence context.
	 *
	 * @return The new context
	 * @throws PersistenceException On error (e. g. if there is no database defined or a database session cannot be established)
	 */
	protected PersistenceContext createPersistenceContext()
		throws PersistenceException
	{
		if (sessionFactory == null)
		{
			sessionFactory = createSessionFactory();
		}

		return new HibernatePersistenceContext(sessionFactory, this);
	}

	/**
	 * Sets the session factory.
	 * @nowarn
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	//////////////////////////////////////////////////
	// @@ Session factory
	//////////////////////////////////////////////////

	/**
	 * Creates a Hibernate session factory.
	 *
	 * @return The new factory or null
	 * @throws PersistenceException On error (e. g. database/hibernate.properties missing or invalid)
	 */
	protected SessionFactory createSessionFactory()
		throws PersistenceException
	{
		Configuration configuration = createHibernateConfiguration();

		try
		{
			// Create the session factory from the configuration
			return configuration.buildSessionFactory();
		}
		catch (HibernateException he)
		{
			throw new PersistenceException("Hibernate configuration problem:", he);
		}
	}

	/**
	 * Creates a Hibernate configuration object that includes the OpenBP classes.
	 * Adds the following class mappings (if not present yet):<br>
	 * TokenContextImpl<br>
	 * WorkflowTaskImpl<br>
	 * DbModel<br>
	 * DbModelItem
	 *
	 * @return The new configuration object
	 */
	public Configuration createHibernateConfiguration()
	{
		// Prepare a configuration object for the session creation
		Configuration configuration = new Configuration();

		// Add the configuration in the hibernate.cfg.xml file
		configuration.configure();

		// Add the OpenBP core objects
		addOpenBPClassMappingsToConfiguration(configuration);

		return configuration;
	}

	/**
	 * Adds class mappings for the OpenBP classes to the given Hibernate configuration object.
	 * Adds the following class mappings (if not present yet):<br>
	 * TokenContextImpl<br>
	 * WorkflowTaskImpl<br>
	 * DbModel<br>
	 * DbModelItem
	 *
	 * @param configuration The configuration
	 */
	public static void addOpenBPClassMappingsToConfiguration(Configuration configuration)
	{
		addClassMappingToConfiguration(configuration, TokenContextImpl.class);
		addClassMappingToConfiguration(configuration, WorkflowTaskImpl.class);
		addClassMappingToConfiguration(configuration, DbModel.class);
		addClassMappingToConfiguration(configuration, DbModelItem.class);
	}

	private static void addClassMappingToConfiguration(Configuration configuration, Class cls)
	{
		String className = cls.getName();
		for (Iterator it = configuration.getClassMappings(); it.hasNext();)
		{
			PersistentClass pc = (PersistentClass) it.next();
			if (pc.getClassName().equals(className))
			{
				// Already mapped
				return;
			}
		}
		configuration.addClass(cls);
	}
}
