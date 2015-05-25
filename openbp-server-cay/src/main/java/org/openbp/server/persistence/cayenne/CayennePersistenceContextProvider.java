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
package org.openbp.server.persistence.cayenne;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.conf.DefaultConfiguration;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjEntity;
import org.openbp.common.registry.ClassMappingRegistry;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.WorkflowTask;
import org.openbp.server.persistence.BasicPersistenceContextProvider;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceException;

/**
 * Peristence context provider for a Cayenne-based persistence context.
 *
 * @author Heiko Erhardt
 */
public class CayennePersistenceContextProvider extends BasicPersistenceContextProvider
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/** Cayenne configuration */
	private Configuration cayenneConfiguration;

	/** Entity order instructions */
	private Map entityOrderInstructions;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Constructor.
	 */
	public CayennePersistenceContextProvider()
	{
	}

	/**
	 * @seem BasicPersistenceContextProvider.shutdown()
	 */
	public void shutdown()
	{
		/* Cayenne is not really clean in its reinitialization behaviour.
		 * This has an effect on the OpenBP test cases where different threads are being used to access the data.
		 * So we omit the test cases that use different threads in the Cayenne test suites.
		 * The code below is an attempt to clean up Cayenne's shutdown behaviour but still does not produce satisfactory results.

		if (cayenneConfiguration != null)
		{
			// Due to a problem in Cayenne,
			// cayenneConfiguration.shutdown();
			// does not work (does not allow reinitialization).
			// So, we do this ourselves...
			DataDomain domain = cayenneConfiguration.getDomain();

			// This will shut down the cache
    		domain.setSharedSnapshotCache(null);

			// This will close all JDBC connections
			for (Iterator itNodes = domain.getDataNodes().iterator(); itNodes.hasNext();)
			{
				DataNode node = (DataNode) itNodes.next();

                try
                {
                    node.shutdown();
                }
                catch(Exception ex)
				{
				}
            }
		}
		 */
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
		if (cayenneConfiguration == null)
		{
			cayenneConfiguration = createCayenneConfiguration();
		}

		return new CayennePersistenceContext(cayenneConfiguration, this);
	}

	/**
	 * Sets the cayenne configuration.
	 * @nowarn
	 */
	public void setCayenneConfiguration(Configuration cayenneConfiguration)
	{
		this.cayenneConfiguration = cayenneConfiguration;
	}

	/**
	 * Sets the entity order instructions.
	 * @nowarn
	 */
	public void setEntityOrderInstructions(Map entityOrderInstructions)
	{
		this.entityOrderInstructions = entityOrderInstructions;
	}

	//////////////////////////////////////////////////
	// @@ Session factory
	//////////////////////////////////////////////////

	/**
	 * Creates a Cayenne configuration object.
	 *
	 * @return The new configuration or null
	 * @throws PersistenceException On error (e. g. mapping file missing or invalid)
	 */
	protected Configuration createCayenneConfiguration()
		throws PersistenceException
	{
		try
		{
			String cayenneConfigFile = Configuration.DEFAULT_DOMAIN_FILE;
			Configuration conf = new DefaultConfiguration(cayenneConfigFile);
			Configuration.initializeSharedConfiguration(conf);
			prepareCayenneConfigurationForOpenBP(conf);
			return conf;
		}
		catch (CayenneException ce)
		{
			throw new PersistenceException("Cayenne configuration problem:", ce);
		}
	}

	/**
	 * Prepares a Cayenne configuration object for use with OpenBP.
	 * Adds the following class mappings (if not present yet):<br>
	 * TokenContextImpl<br>
	 * WorkflowTask<br>
	 * Adds an OpenBP-internal post load event handler to the given Cayenne configuration object.
	 *
	 * @param configuration Configuration
	 */
	public void prepareCayenneConfigurationForOpenBP(Configuration configuration)
		throws PersistenceException, CayenneException
	{
		// Add the OpenBP core objects
		ensureOpenBPClassMapping(configuration);
	}

	private void ensureOpenBPClassMapping(Configuration configuration)
		throws PersistenceException, CayenneException
	{
		DataDomain domain = configuration.getDomain();
		ensureClassMapping(domain, TokenContext.class);
		ensureClassMapping(domain, WorkflowTask.class);

		setEntitySorter(domain);
	}

	private void ensureClassMapping(DataDomain domain, Class cls)
		throws PersistenceException, CayenneException
	{
		ClassMappingRegistry classMapper = getClassMappingRegistry();
		if (classMapper != null)
		{
			Class newCls = classMapper.getMappedClass(cls);
			if (newCls != null)
				cls = newCls;
		}

		for (Iterator itNodes = domain.getDataNodes().iterator(); itNodes.hasNext();)
		{
			DataNode node = (DataNode) itNodes.next();

			Collection dataMaps = node.getDataMaps();

			for (Iterator itMaps = dataMaps.iterator(); itMaps.hasNext();)
			{
				DataMap dataMap = (DataMap) itMaps.next();
				Map objEntities = dataMap.getObjEntityMap();
				for (Iterator itEntities = objEntities.values().iterator(); itEntities.hasNext();)
				{
					ObjEntity objEntity = (ObjEntity) itEntities.next();
					String className = objEntity.getClassName();

					if (className.equals(cls.getName()))
					{
						// Already mapped
						return;
					}
				}
			}
		}

		throw new PersistenceException("Class '" + cls.getName() + "' not present in Cayenne mapping specification.");
	}

	private void setEntitySorter(DataDomain domain)
	{
		if (entityOrderInstructions == null)
		{
			// Supply default entity order instructions: CayenneTokenContextImpl must always be the the last entity in the entity list
			entityOrderInstructions = new HashMap();
			entityOrderInstructions.put("*", "OpenBPTokenContext");
		}

		for (Iterator itNodes = domain.getDataNodes().iterator(); itNodes.hasNext();)
		{
			DataNode node = (DataNode) itNodes.next();

			Collection dataMaps = node.getDataMaps();

			CayenneEntitySorter cayenneEntitySorter = new CayenneEntitySorter(dataMaps);
			cayenneEntitySorter.addOrderInstructions(entityOrderInstructions);
			node.setEntitySorter(cayenneEntitySorter);
		}
	}
}
