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
package org.openbp.syncmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.commandline.CommandLineParser;
import org.openbp.common.commandline.CommandLineParserException;
import org.openbp.common.generic.Copyable;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelImpl;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.modelmgr.ClassPathModelMgr;
import org.openbp.core.model.modelmgr.FileSystemModelMgr;
import org.openbp.core.model.modelmgr.ModelMgr;
import org.openbp.core.model.modelmgr.MultiplexModelMgr;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;
import org.openbp.server.model.modelmgr.DatabaseModelMgr;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceException;
import org.openbp.server.persistence.TransactionGuard;

/**
 * This class is the main anchor point of an OpenBP server.
 * It performs all server startup and shutdown tasks.
 *
 * @author Heiko Erhardt
 */
public class SyncModel
{
	public static final int MODE_COPY = 1;
	public static final int MODE_COPY_ALL = 2;
	public static final int MODE_REMOVE = 3;
	public static final int MODE_REMOVE_ALL = 4;

	/** Process server */
	private ProcessServer processServer;

	/** Source model manager */
	private ModelMgr sourceMgr;

	/** Target model manager */
	private ModelMgr targetMgr;

	/** Source model manager type (classpath|filesystem|database) */
	private String sourceMgrType;

	/** Target model manager type (filesystem|database) */
	private String targetMgrType;

	/** Operation mode (MODE_COPY|MODE_COPY_ALL|MODE_REMOVE|MODE_REMOVE_ALL) */
	private int mode = MODE_COPY;

	/** Model(s) to copy from the source to the target model manager */
	private List<String> modelList = new ArrayList<String>();

	/** Forces existing models that exist in the target manager to be removed before the copy operation takes place */
	private boolean overwrite;

	/**
	 * Constructor.
	 */
	public SyncModel()
	{
	}

	/**
	 * Performs the model synchronizatio processing.
	 */
	public void perform()
		throws Exception
	{
		sourceMgr = getModelMgr(sourceMgrType);
		targetMgr = getModelMgr(targetMgrType);

		if (mode == MODE_COPY_ALL)
		{
			addModelNames(sourceMgr, sourceMgrType, true);
			mode = MODE_COPY;
		}
		if (mode == MODE_REMOVE_ALL)
		{
			addModelNames(targetMgr, targetMgrType, false);
			mode = MODE_REMOVE;
		}

		if (mode == MODE_COPY)
		{
			// Check for target models
			for (String modelName : modelList)
			{
				Model model = getModel(modelName, targetMgr);
				if (model != null)
				{
					if (isOverwrite())
					{
						removeModel(model, targetMgr, targetMgrType);
					}
					else
					{
						printError("Cannot overwrite model '" + modelName + "' in target model manager.");
					}
				}
			}

			// Perform operation
			for (String modelName : modelList)
			{
				Model model = getModel(modelName, sourceMgr);
				if (model == null)
				{
					printError("Model '" + modelName + "' not found in source model manager.");
				}

				copyModel(model);
			}
		}
		else if (mode == MODE_REMOVE)
		{
			// Perform operation
			for (String modelName : modelList)
			{
				Model model = getModel(modelName, targetMgr);
				if (model == null)
				{
					printError("Model '" + modelName + "' not found in source target manager.");
				}

				removeModel(model, targetMgr, targetMgrType);
			}
		}
	}

	private void copyModel(Model model)
		throws Exception
	{
		printMsg("Copying model '" + model.getName() + "' from " + sourceMgrType + " model manager to " + targetMgrType + " model manager.");

		Model newModel = new ModelImpl();
		newModel.copyFrom(model, Copyable.COPY_SHALLOW);
		((ModelImpl) newModel).internalResetItems();
		targetMgr.addModel(newModel);

		copyItems(newModel, model, ItemTypes.TYPE, targetMgr);
		copyItems(newModel, model, ItemTypes.ACTOR, targetMgr);
		copyItems(newModel, model, ItemTypes.ACTIVITY, targetMgr);
		copyItems(newModel, model, ItemTypes.VISUAL, targetMgr);
		copyItems(newModel, model, ItemTypes.WEBSERVICE, targetMgr);
		copyItems(newModel, model, ItemTypes.PROCESS, targetMgr);
	}

	private void removeModel(Model model, ModelMgr mgr, String mgrType)
	{
		printMsg("Removing model '" + model.getName() + "' from " + mgrType + " model manager.");
		mgr.removeModel(model);
	}

	private void copyItems(Model newModel, Model model, String itemType, ModelMgr mgr)
		throws Exception
	{
		for (Iterator it = model.getItems(itemType); it.hasNext();)
		{
			Item item = (Item) it.next();
			Item newItem = (Item) item.clone();
			mgr.addItem(newModel, newItem, true);
		}
	}

	public Model getModel(String name, ModelMgr mm)
		throws Exception
	{
		ModelQualifier modelQualifier = new ModelQualifier(name, null, null, null);
		return mm.internalGetModelByQualifier(modelQualifier);
	}

	private void addModelNames(ModelMgr mm, String mgrType, boolean warnIfEmpty)
	{
		List<Model> models = (List<Model>) mm.getModels();
		if (models != null)
		{
			for (Model model : models)
			{
				addModel(model.getName());
			}
		}
		else
		{
			if (warnIfEmpty)
			{
				printError("Model manager '" + mgrType + "' does not contain any models.");
			}
		}
	}

	private ModelMgr getModelMgr(String type)
	{
		Class cls = null;
		if (type.equalsIgnoreCase("classpath"))
		{
			cls = ClassPathModelMgr.class;
		}
		else if (type.equalsIgnoreCase("filesystem"))
		{
			cls = FileSystemModelMgr.class;
		}
		else if (type.equalsIgnoreCase("database"))
		{
			cls = DatabaseModelMgr.class;
		}
		else
		{
			printError("Unknown model manager type '" + type + "'.");
			return null;
		}

		if (! (processServer.getModelMgr() instanceof MultiplexModelMgr))
		{
			printError("No MultiplexModelMgr configured in OpenBP.spring.xml.");
		}

		MultiplexModelMgr mmm = (MultiplexModelMgr) processServer.getModelMgr();
		ModelMgr[] managers = mmm.getManagers();
		for (int i = 0; i < managers.length; ++i)
		{
			if (managers[i].getClass() == cls)
				return managers[i];
		}

		printError("No sub model manager of type '" + cls.getName() + "' configured in OpenBP.spring.xml.");
		return null;
	}

	private void printMsg(String s)
	{
		System.out.println(s);
	}

	/**
	 * Gets the process server.
	 * @nowarn
	 */
	public ProcessServer getProcessServer()
	{
		return processServer;
	}

	/**
	 * Sets the process server.
	 * @nowarn
	 */
	public void setProcessServer(ProcessServer processServer)
	{
		this.processServer = processServer;
	}

	/**
	 * Gets the source model manager type (classpath|filesystem|database).
	 * @nowarn
	 */
	public String getSourceMgrType()
	{
		return sourceMgrType;
	}

	/**
	 * Sets the source model manager type (classpath|filesystem|database).
	 * @nowarn
	 */
	public void setSourceMgrType(String sourceMgrType)
	{
		this.sourceMgrType = sourceMgrType;
	}

	/**
	 * Gets the target model manager type (filesystem|database).
	 * @nowarn
	 */
	public String getTargetMgrType()
	{
		return targetMgrType;
	}

	/**
	 * Sets the target model manager type (filesystem|database).
	 * @nowarn
	 */
	public void setTargetMgrType(String targetMgrType)
	{
		this.targetMgrType = targetMgrType;
	}

	/**
	 * Gets the operation mode (MODE_COPY|MODE_COPY_ALL|MODE_REMOVE|MODE_REMOVE_ALL).
	 * @nowarn
	 */
	public int getMode()
	{
		return mode;
	}

	/**
	 * Sets the operation mode (MODE_COPY|MODE_COPY_ALL|MODE_REMOVE|MODE_REMOVE_ALL).
	 * @nowarn
	 */
	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public void addModel(String model)
	{
		modelList.add(model);
	}

	/**
	 * Gets the forces existing models that exist in the target manager to be removed before the copy operation takes place.
	 * @nowarn
	 */
	public boolean isOverwrite()
	{
		return overwrite;
	}

	/**
	 * Sets the forces existing models that exist in the target manager to be removed before the copy operation takes place.
	 * @nowarn
	 */
	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}

	/**
	 * Main method for simple server test mode.
	 *
	 * @param args Command line argument array
	 */
	public static void main(String[] args)
	{
		SyncModel processor = null;
		try
		{
			Application.setArguments(args);

			CommandLineParser cp = new CommandLineParser();
			cp
				.setUsageMsgHeader(new String[]
				{
					"Model synchronization utility.",
					"Copies model data between different model manager types",
					"", "Usage:",
				});

			cp.addArgumentOption("SourceMgr", "Source model manager (classpath|filesystem|database) (default: filesystem)");
			cp.addArgumentOption("TargetMgr", "Target model manager (filesystem|database) (default: database)");
			cp.addArgumentOption("Mode", "Operation mode (Copy|CopyAll|Remove|RemoveAll) (default: copy)");
			cp.addBooleanOption("Overwrite", "Forces existing models that exist in the target manager to be removed before the copy operation takes place (default: false)");

			try
			{
				cp.parse(args);
			}
			catch (CommandLineParserException e)
			{
				System.err.println(e.getMessage());
				cp.printUsageAndExit();
			}

			processor = new SyncModel();
			processor.setProcessServer(new ProcessServerFactory().createProcessServer("OpenBP-SyncModel-Hibernate.spring.xml"));

			int mode = 0;
			String m = cp.getStringOption("Mode");
			if (m.equalsIgnoreCase("Copy"))
			{
				mode = MODE_COPY;
			}
			else if (m.equalsIgnoreCase("CopyAll"))
			{
				mode = MODE_COPY_ALL;
			}
			else if (m.equalsIgnoreCase("Remove"))
			{
				mode = MODE_REMOVE;
			}
			else if (m.equalsIgnoreCase("RemoveAll"))
			{
				mode = MODE_REMOVE_ALL;
			}
			else
			{
				printError("Unknown operation mode '" + m + "'.");
			}
			processor.setMode(mode);

			String sourceMgrType = cp.getStringOption("SourceMgr");
			if (sourceMgrType == null)
				sourceMgrType = "filesystem";
			processor.setSourceMgrType(sourceMgrType);
			String targetMgrType = cp.getStringOption("TargetMgr");
			if (targetMgrType == null)
				targetMgrType = "database";
			processor.setTargetMgrType(targetMgrType);
			processor.setOverwrite(cp.getBooleanOption("Overwrite"));

			boolean hasArguments = false;
			String [] modelNames = cp.getArguments();
			if (modelNames != null)
			{
				for (int i = 0; i < modelNames.length; ++i)
				{
					processor.addModel(modelNames[i]);
					hasArguments = true;
				}
			}

			if (mode == MODE_COPY_ALL && hasArguments)
			{
				printError("No model arguments allowed for 'CopyAll' operation mode.");
			}
			if (mode == MODE_REMOVE_ALL && hasArguments)
			{
				printError("No model arguments allowed for 'CopyAll' operation mode.");
			}
			if (mode == MODE_COPY && ! hasArguments)
			{
				printError("Model arguments required for 'Copy' operation mode.");
			}
			if (mode == MODE_REMOVE && ! hasArguments)
			{
				printError("Model arguments required for 'Copy' operation mode.");
			}
			if (processor.getSourceMgrType().equalsIgnoreCase(processor.getTargetMgrType()))
			{
				printError("Source and target model manager classes may not be identical.");
			}

			PersistenceContext pc = processor.getProcessServer().getEngine().getPersistenceContextProvider().obtainPersistenceContext();
			TransactionGuard tg = new TransactionGuard(pc);
			try
			{
				processor.perform();
			}
			catch (PersistenceException e)
			{
				tg.doCatch();
				throw e;
			}
			finally
			{
				tg.doFinally();
			}

			System.exit(0);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
		finally
		{
			if (processor != null)
			{
				processor.getProcessServer().shutdown(true);
			}
		}
	}

	private static void printError(String s)
	{
		System.err.println(s);
		System.exit(1);
	}
}
