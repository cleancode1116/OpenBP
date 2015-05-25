package org.openbp.server.engine;

/**
 * Abstract base class for model object executors.
 *
 * @author Heiko Erhardt
 */
public abstract class ModelObjectExecutorBase
	implements ModelObjectExecutor
{
	/** Engine */
	private Engine engine;

	/**
	 * Default constructor.
	 */
	public ModelObjectExecutorBase()
	{
	}

	/**
	 * Gets the engine.
	 * @nowarn
	 */
	protected Engine getEngine()
	{
		return engine;
	}

	/**
	 * Sets the engine.
	 * @nowarn
	 */
	protected void setEngine(Engine engine)
	{
		this.engine = engine;
	}
}
