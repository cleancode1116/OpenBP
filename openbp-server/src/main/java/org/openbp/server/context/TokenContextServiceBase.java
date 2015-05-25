package org.openbp.server.context;

import java.util.Iterator;

import org.openbp.common.util.ToStringHelper;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * Abstract base class for token context service implementations.
 *
 * @author Heiko Erhardt
 */
public abstract class TokenContextServiceBase
	implements TokenContextService
{
	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	/** Model mgr */
	private ModelMgr modelMgr;

	/**
	 * Default constructor.
	 */
	public TokenContextServiceBase()
	{
	}


	/**
	 * LifecycleSupport implementation.
	 */
	public void initialize()
	{
	}

	/**
	 * Shuts down the object.
	 */
	public void shutdown()
	{
	}

	/**
	 * Returns a string represenation of this object.
	 *
	 * @return Debug string containing the most important properties of this object
	 */
	public String toString()
	{
		return ToStringHelper.toString(this);
	}

	/**
	 * Gets the persistence context provider.
	 * @nowarn
	 */
	public PersistenceContextProvider getPersistenceContextProvider()
	{
		return persistenceContextProvider;
	}

	/**
	 * Sets the persistence context provider.
	 * @nowarn
	 */
	public void setPersistenceContextProvider(final PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}

	/**
	 * Gets the model mgr.
	 * @nowarn
	 */
	public ModelMgr getModelMgr()
	{
		return modelMgr;
	}

	/**
	 * Sets the model mgr.
	 * @nowarn
	 */
	public void setModelMgr(ModelMgr modelMgr)
	{
		this.modelMgr = modelMgr;
	}

	/**
	 * Retrieves a token context by its id.
	 *
	 * @param id Context id
	 * @return The context or null if no such context exists
	 */
	public TokenContext getContextById(Object id)
	{
		TokenContextCriteria criteria = new TokenContextCriteria();
		criteria.setId(id);
		Iterator it = getContexts(criteria, 0);
		if (it.hasNext())
			return (TokenContext) it.next();
		return null;
	}

	/**
	 * Creates a new child context.
	 * 
	 * @return The context
	 */
	public TokenContext createChildContext(TokenContext parentContext)
	{
		TokenContext childContext = createContext();

		childContext.setDebuggerId(parentContext.getDebuggerId());
		childContext.setCurrentSocket(parentContext.getCurrentSocket());
		childContext.setExecutingModel(parentContext.getExecutingModel());

		parentContext.addChildContext(childContext);

		return childContext;
	}

	/**
	 * Creates a context.
	 * However, the context is not added to the context list.
	 * 
	 * @return The new context
	 */
	public TokenContext createContext()
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		return (TokenContext) pc.createObject(TokenContext.class);
	}

	/**
	 * Creates a workflow task. However, the workflow task is not added to the * workflow task list.
	 * 
	 * @param context Token context to associate with the workflow task @return
	 * The new workflow task
	 */
	public WorkflowTask createWorkflowTask(final TokenContext context)
	{
		PersistenceContext pc = getPersistenceContextProvider().obtainPersistenceContext();
		WorkflowTask workflowTask = (WorkflowTask) pc.createObject(WorkflowTask.class);

		// Link workflow task and context
		workflowTask.setTokenContext(context);

		return workflowTask;
	}
}
