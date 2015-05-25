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

import java.io.File;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.openbp.common.ExceptionUtil;
import org.openbp.common.application.Application;
import org.openbp.common.commandline.CommandLineParser;
import org.openbp.common.commandline.CommandLineParserException;
import org.openbp.common.logger.LogUtil;
import org.openbp.server.ProcessServer;
import org.openbp.server.ProcessServerFactory;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * DDL generator for Hibernate persistence-based applications.
 * Will generate create and drop DDLs based on the current Hibernate configuration (needs hibernate.cfg.xml in the classpath).
 * The OpenBP proccess and workflow control entities will be added to the configuration automatically.
 *
 * @author Heiko Erhardt
 */
public class HibernateDDLGenerator
{
	/** Dialect */
	private String dialect;

	/** Base dir */
	private String baseDir;

	/** Name of the output file that will contain the DDL create statements */
	private String ddlCreateFileName;

	/** Name of the output file that will contain the DDL drop statements */
	private String ddlDropFileName;

	/**
	 * Default constructor.
	 */
	public HibernateDDLGenerator()
	{
	}

	/**
	 * Generates the DDL files.
	 */
	public void generate()
	{
		ProcessServer processServer = new ProcessServerFactory().createProcessServer();

		PersistenceContextProvider provider = processServer.getEngine().getPersistenceContextProvider();
		if (provider == null)
		{
			String msg = LogUtil.error(getClass(), "No persistence context provider configured.");
			System.err.println(msg);
			System.exit(1);
			return;
		}
		if (! (provider instanceof HibernatePersistenceContextProvider))
		{
			String msg = LogUtil.error(getClass(),
				"Configured persistence context provider no no Hibernate provider (class $0).", provider.getClass()
					.getName());
			System.err.println(msg);
			System.exit(1);
			return;
		}

		Configuration configuration = ((HibernatePersistenceContextProvider) provider).createHibernateConfiguration();

		if (dialect != null)
		{
			String adapterClassName = null;
			if (dialect.indexOf('.') >= 0)
			{
				adapterClassName = dialect;
			}
			else
			{
				adapterClassName = "org.hibernate.dialect." + dialect + "Dialect";
			}
			configuration.setProperty("hibernate.dialect", adapterClassName);
		}

		SchemaExport se = new SchemaExport(configuration);
		se.setFormat(true);
		se.setDelimiter(";");

		String outputFile;

		if ((outputFile = prepareOutputFile(getDdlCreateFileName())) != null)
		{
			se.setOutputFile(outputFile);
			se.execute(false, false, false, true);
		}

		if ((outputFile = prepareOutputFile(getDdlDropFileName())) != null)
		{
			se.setOutputFile(outputFile);
			se.execute(false, false, true, false);
		}
	}

	private String prepareOutputFile(String sqlFileName)
	{
		if (sqlFileName != null)
		{
			StringBuffer fileName = new StringBuffer();
			if (baseDir != null)
			{
				fileName.append(baseDir);
				fileName.append(File.separator);
			}
			if (dialect != null)
			{
				fileName.append(dialect);
				fileName.append(File.separator);
			}
			fileName.append(sqlFileName);

			File file = new File(fileName.toString());
			file.getParentFile().mkdirs();
			file.delete();

			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * Gets the dialect.
	 * @nowarn
	 */
	public String getDialect()
	{
		return dialect;
	}

	/**
	 * Sets the dialect.
	 * @nowarn
	 */
	public void setDialect(String dialect)
	{
		this.dialect = dialect;
	}

	/**
	 * Gets the base dir.
	 * @nowarn
	 */
	public String getBaseDir()
	{
		return baseDir;
	}

	/**
	 * Sets the base dir.
	 * @nowarn
	 */
	public void setBaseDir(String baseDir)
	{
		this.baseDir = baseDir;
	}

	/**
	 * Gets the name of the output file that will contain the DDL create statements.
	 * @nowarn
	 */
	public String getDdlCreateFileName()
	{
		return ddlCreateFileName;
	}

	/**
	 * Sets the name of the output file that will contain the DDL create statements.
	 * @nowarn
	 */
	public void setDdlCreateFileName(String ddlCreateFileName)
	{
		this.ddlCreateFileName = ddlCreateFileName;
	}

	/**
	 * Gets the name of the output file that will contain the DDL drop statements.
	 * @nowarn
	 */
	public String getDdlDropFileName()
	{
		return ddlDropFileName;
	}

	/**
	 * Sets the name of the output file that will contain the DDL drop statements.
	 * @nowarn
	 */
	public void setDdlDropFileName(String ddlDropFileName)
	{
		this.ddlDropFileName = ddlDropFileName;
	}

	//////////////////////////////////////////////////
	// @@ Data member access
	//////////////////////////////////////////////////

	/**
	 * Main method.
	 * @nowarn
	 */
	public static void main(String[] args)
	{
		try
		{
			Application.setArguments(args);

			CommandLineParser cp = new CommandLineParser();
			cp
				.setUsageMsgHeader(new String[]
				{
					"DDL generator for Hibernate persistence-based applications.",
					"This utility will generate create and drop DDLs based on the current Hibernate configuration (needs hibernate.cfg.xml in the classpath).",
					"The OpenBP proccess and workflow control entities will be added to the configuration automatically.",
					"", "Usage:",
				});

			cp.addArgumentOption("Dialect", "Database dialect to use for generating (also affects the output directory).");
			cp.addArgumentOption("BaseDir", "Output directory that will contain the dialect-specific sub directories.");
			cp.addArgumentOption("DDLCreateFile", "Name of the output file that will contain the DDL create statements");
			cp.addArgumentOption("DDLDropFile", "Name of the output file that will contain the DDL drop statements");

			try
			{
				cp.parse(args);
			}
			catch (CommandLineParserException e)
			{
				System.err.println(e.getMessage());
				cp.printUsageAndExit();
			}

			HibernateDDLGenerator generator = new HibernateDDLGenerator();

			generator.setDialect(cp.getStringOption("Dialect"));
			generator.setBaseDir(cp.getStringOption("BaseDir"));
			generator.setDdlCreateFileName(cp.getStringOption("DdlCreateFile"));
			generator.setDdlDropFileName(cp.getStringOption("DdlDropFile"));

			if (generator.getDdlCreateFileName() == null && generator.getDdlDropFileName() == null)
			{
				cp.printUsageAndExit();
			}

			generator.generate();

			System.exit(0);
		}
		catch (Exception e)
		{
			ExceptionUtil.printTrace(e);
			System.exit(1);
		}
	}
}
