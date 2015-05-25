package org.openbp.server.persistence;

/**
 * Lifecycle event for an entity.
 *
 * @author Heiko Erhardt
 */
public class EntityLifecycleEvent
{
	/** Entity that is subject of the event */
	private Object entity;

	/** Persistence context that owns the entity */
	private PersistenceContext persistenceContext;

	/**
	 * Default constructor.
	 *
	 * @param entity Entity
	 * @param persistenceContext Persistence context
	 */
	public EntityLifecycleEvent(Object entity, PersistenceContext persistenceContext)
	{
		this.entity = entity;
		this.persistenceContext = persistenceContext;
	}

	/**
	 * Gets the entity that is subject of the event.
	 * @nowarn
	 */
	public Object getEntity()
	{
		return entity;
	}

	/**
	 * Gets the persistence context that owns the entity.
	 * @nowarn
	 */
	public PersistenceContext getPersistenceContext()
	{
		return persistenceContext;
	}
}
