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
package org.openbp.server.context;

import java.util.Iterator;

import org.openbp.common.util.iterator.WrappingIterator;

/**
 * A persistent token context service that will return up-to-date information on running, uncommitted contexts.
 * The {@link #getContexts} method will query the {@link SessionRegistry} for running contexts and return them instead of the contexts retrieved from the persistence layer.
 * This will ensure that uncommitted information is up-to-date, e. g. progress information.
 *
 * @author Author: Heiko Erhardt
 */
public class SessionAwarePersistentTokenContextService extends PersistentTokenContextService
{
	/** Session registry */
	private SessionRegistry sessionRegistry;

	/**
	 * Default constructor.
	 */
	public SessionAwarePersistentTokenContextService()
	{
	}

	/**
	 * Gets the session registry.
	 * @nowarn
	 */
	public SessionRegistry getSessionRegistry()
	{
		return sessionRegistry;
	}

	/**
	 * Sets the session registry.
	 * @nowarn
	 */
	public void setSessionRegistry(SessionRegistry sessionRegistry)
	{
		this.sessionRegistry = sessionRegistry;
	}

	//////////////////////////////////////////////////
	// @@ Overrides
	//////////////////////////////////////////////////

	/**
	 * Returns an iterator of token contexts that match the given selection criteria.
	 *
	 * @param criteria Criteria to match
	 * @param maxResults Maximum number of result records or 0 for all
	 * @return An iterator of {@link TokenContext} objects.
	 * The objects will be sorted by their priority (ascending).
	 */
	public Iterator getContexts(TokenContextCriteria criteria, int maxResults)
	{
		Iterator it = super.getContexts(criteria, maxResults);
		if (getSessionRegistry() != null)
		{
			it = new SessionAwareContextIterator(it);
		}
		return it;
	}

	private class SessionAwareContextIterator extends WrappingIterator
	{
		/**
		 * Constructor.
		 *
		 * @param basis Contains the iterator we are based on
		 */
		public SessionAwareContextIterator(Iterator basis)
		{
			super(basis);
		}

		/**
		 * Retrieves current object by querying the underlying iterator.
		 * @param basis The underlying iterator
		 * @return The current object or null if the end of the underlying iterator has been reached.
		 */
		protected Object retrieveCurrentObject(Iterator basis)
		{
			if (basis.hasNext())
			{
				TokenContext context = (TokenContext) basis.next();

				Object sessionId = context.getId();
				TokenContext sessionContext = getSessionRegistry().lookupSession(sessionId);
				if (sessionContext != null)
				{
					context = sessionContext;
				}

				return context;
			}
			return null;
		}
	}
}
