package org.openbp.server.persistence;

import org.openbp.common.logger.LogUtil;

/**
 * A Guard to make sure a transaction is running, and commit or rollback it,
 * if it was opened locally, otherwise leave it alone.
 *
 * @code 3
   TansactionGuard tg = new TansactionGuard();
   try
   {
		// Perform business logic
   }
   catch (Error e)
   {
		// do some error handling to recover error...
		tg.doCatch();
		// do some more error handling...
		throw e;
   }
   finally
   {
		tg.doFinally();
   }

 @code
 *
 * @author Heiko Erhardt
 */
public class TransactionGuard
{
	private PersistenceContext persistenceContext;
	private boolean foundActiveTransaction;
	private boolean exceptionOccured;

	/**
	 * Value constructor.
	 *
	 * @param persistenceContext Persistence context
	 */
	public TransactionGuard(PersistenceContext persistenceContext)
	{
		this.persistenceContext = persistenceContext;
		if (persistenceContext != null)
		{
			foundActiveTransaction =  persistenceContext.isTransactionActive();
			if (! foundActiveTransaction)
				persistenceContext.beginTransaction();
		}
	}

	/**
	 * Remembers whether an Exception has been caught
	 */
	public void doCatch()
	{
		exceptionOccured = true;
	}

	/**
	 * Commits a transaction if one has been opened by beginTry, if no exception
	 * has been caught.
	 */
	public void doFinally()
	{
		if (persistenceContext != null && ! foundActiveTransaction)
		{
			if (exceptionOccured)
			{
				try
				{
					persistenceContext.rollbackTransaction();
					persistenceContext.release();
				}
				catch (Exception e)
				{
					// Ignore this error, but log it
					LogUtil.error(getClass(), "Persistence error when performing transaction rollback on error.", e);
				}
			}
			else
			{
				persistenceContext.commitTransaction();
			}
		}
	}
}
