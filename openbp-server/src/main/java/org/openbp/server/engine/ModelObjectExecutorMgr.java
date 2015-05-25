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
package org.openbp.server.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.model.ModelObject;
import org.openbp.server.engine.executor.InitialNodeExecutor;

/**
 * Manager class that keeps track of model object executor classes for a particular engine.
 *
 * @author Heiko Erhardt
 */
public class ModelObjectExecutorMgr
{
	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Table mapping executors classes ({@link ModelObjectExecutor} objects) by model object classes ({@link ModelObject} class objects) */
	private Map executorsByModelObjectClasses;

	/** Table of packages that may contain model object executor classes */
	private List packages;

	/** Engine */
	private Engine engine;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 *
	 * @param engine Engine
	 */
	public ModelObjectExecutorMgr(Engine engine)
	{
		this.engine = engine;

		executorsByModelObjectClasses = new HashMap();

		packages = new ArrayList();
		packages.add(InitialNodeExecutor.class.getPackage().getName());
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Gets an executor for the given model object.
	 * Searches the executor package list if no such executor is in the cache yet.
	 *
	 * @param mo Model object to execute
	 * @return The executor
	 * @throws OpenBPException If no executor could be found
	 */
	public ModelObjectExecutor getExecutor(ModelObject mo)
	{
		ModelObjectExecutor executor = (ModelObjectExecutor) executorsByModelObjectClasses.get(mo.getClass());
		if (executor == null)
		{
			executor = createExecutor(mo);
			executorsByModelObjectClasses.put(mo.getClass(), executor);
		}
		return executor;
	}

	/**
	 * Creates an executor for the given model object.
	 * Searches the executor class in the package list.
	 *
	 * @param mo Model object to execute
	 * @return A new executor
	 * @throws OpenBPException On error
	 */
	public ModelObjectExecutor createExecutor(ModelObject mo)
	{
		String clsName = mo.getClass().getName();
		int i = clsName.lastIndexOf('.');
		clsName = clsName.substring(i + 1);
		if (clsName.endsWith("Impl"))
			clsName = clsName.substring(0, clsName.length() - 4);
		clsName += "Executor";
		Class cls = ReflectUtil.findClassInPackageList(clsName, packages);
		if (cls != null)
		{
			try
			{
				ModelObjectExecutorBase executor = (ModelObjectExecutorBase) ReflectUtil.instantiate(cls, ModelObjectExecutorBase.class, "model object executor");
				executor.setEngine(engine);
				return executor;
			}
			catch (ReflectException e)
			{
				String msg = LogUtil.error(getClass(), "Cannot execute model object $0.", mo.getQualifier(), e);
				throw new EngineException("ModelObjectExecutorInstantiation", msg);
			}
		}

		String msg = LogUtil.error(getClass(), "Cannot execute model object $0 of type $1: No model object executor found.", mo.getQualifier(), mo.getClass().getName());
		throw new EngineException("ModelObjectExecutorPresence", msg);
	}

	/**
	 * Adds an executor for the given model object class.
	 *
	 * @param executor Executor
	 * @param moClass Model object class
	 */
	public void addExecutor(ModelObjectExecutor executor, Class moClass)
	{
		executorsByModelObjectClasses.put(moClass, executor);
	}

	/**
	 * Adds a package that may contain model object executors.
	 * @param pkg The package to add
	 */
	public void addPackage(String pkg)
	{
		if (packages == null)
			packages = new ArrayList();
		packages.add(pkg);
	}
}
