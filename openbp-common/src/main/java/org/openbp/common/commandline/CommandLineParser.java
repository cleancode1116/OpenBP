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
package org.openbp.common.commandline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.CollectionUtil;
import org.openbp.common.application.Application;

/**
 * The command line parser parses a command line as supplied to the main method of a class
 * via the argument array of the main method.
 *
 * @code
 * String [] args
 * @code
 *
 * All program arguments and all options are stored by the class and can be retrieved after
 * the {@link #parse} method has been called using the get....Option and {@link #getArguments} methods.<br>
 * The class performs checks if an option is valid and has an argument if needed.
 * An option argument must be placed directly after the option on the command line,
 * separated by a space.
 *
 * The command line parser is not case sensitive, i. e. -samplePar and -samplepar refer to
 * the same option.
 *
 * Example:
 *
 * @code
 * CommandLineParser cp = new CommandLineParser ();
 * cp.addArgumentOption ("opt1");
 * cp.addRepeatableOption ("opt2");
 * cp.addBooleanOption ("opt3");
 * cp.addBooleanOption ("opt4");
 * @code
 *
 * @code
 * program -opt1 value1 -opt2 value2 -opt2 value3 -opt3 +opt4 value2 arg1 arg2
 * @code
 *
 * \ivalue1\i would be recognized as argument to \iopt1\i, \ivalue2\i and \ivalue3\i
 * are both stored as arguments to the option \iopt2\i.
 * \iarg1\i and \iarg2\i would be saved as program arguments.
 *
 * @author Heiko Erhardt
 */
public class CommandLineParser
{
	//////////////////////////////////////////////////
	// @@ Private data
	//////////////////////////////////////////////////

	/**
	 * Options given on the command line
	 * Key: Option name (String object)<br>
	 * Value:<br>
	 * - Boolean for boolean options<br>
	 * - List of Strings for repeatable options<br>
	 * - String for regular argumented option
	 */
	private Map options = new HashMap();

	/** Arguments given on the command line. Contains String objects. */
	private List arguments = new ArrayList();

	/**
	 * Table of possible options.
	 */
	private Map possibleOptions = new HashMap();

	/**
	 * List of possible options (for usage output).
	 */
	private List possibleOptionList = new ArrayList();

	/** Header lines for the usage message */
	private String [] usageMsgHeader;

	/** Maximum length an option name */
	private int maxOptionNameLength = 5;

	/** Flag if unknown options should be accepted (false by default) */
	private boolean acceptUnknownOptions;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public CommandLineParser()
	{
	}

	//////////////////////////////////////////////////
	// @@ Adding possible options
	//////////////////////////////////////////////////

	/**
	 * Adds a boolean option to the list of possible options.
	 *
	 * @param name Name of the option
	 * @param usageMsg Help message for this option
	 */
	public void addBooleanOption(String name, String usageMsg)
	{
		addOption(name, false, false, usageMsg, null);
	}

	/**
	 * Adds a boolean option to the list of possible options.
	 *
	 * @param name Name of the option
	 * @param usageMsg Help message for this option
	 * @param dflt Default value of the option
	 */
	public void addBooleanOption(String name, String usageMsg, boolean dflt)
	{
		addOption(name, false, false, usageMsg, new Boolean(dflt));
	}

	/**
	 * Adds an option with an argument to the list of possible options.
	 *
	 * @param name Name of the option
	 * @param usageMsg Help message for this option
	 */
	public void addArgumentOption(String name, String usageMsg)
	{
		addOption(name, true, false, usageMsg, null);
	}

	/**
	 * Adds an option with an argument to the list of possible options.
	 *
	 * @param name Name of the option
	 * @param usageMsg Help message for this option
	 * @param dflt Default value of the option
	 */
	public void addArgumentOption(String name, String usageMsg, String dflt)
	{
		addOption(name, true, false, usageMsg, dflt);
	}

	/**
	 * Adds a repeatable option with an argument (i\. e\. an option that occur more than one time)
	 * to the list of possible options.
	 *
	 * @param name Name of the option
	 * @param usageMsg Help message for this option
	 */
	public void addRepeatableOption(String name, String usageMsg)
	{
		addOption(name, true, true, usageMsg, null);
	}

	/**
	 * Adds an option.
	 *
	 * @param name Name of the option
	 * @param requiresArgument
	 *		true	This option requires an option argument.<br>
	 *		false	This option does not have an argument.
	 * @param repeatable
	 *		true	This option can be repeated.<br>
	 *		false	This option may occur only once on the commandline
	 * @param dflt Default value of the option or null if there is no default
	 * @param usageMsg Help message for this option
	 */
	private void addOption(String name, boolean requiresArgument, boolean repeatable, String usageMsg, Object dflt)
	{
		int l = name.length();
		if (l > maxOptionNameLength)
			maxOptionNameLength = l;

		Option option = new Option(name, requiresArgument, repeatable, usageMsg);

		possibleOptions.put(name.toLowerCase(), option);
		possibleOptionList.add(option);

		if (dflt != null)
			options.put(name, dflt);
	}

	/**
	 * Sets the flag if unknown options should be accepted (false by default).
	 * @nowarn
	 */
	public void setAcceptUnknownOptions(boolean acceptUnknownOptions)
	{
		this.acceptUnknownOptions = acceptUnknownOptions;
	}

	/**
	 * Sets the header lines for the usage message.
	 * @nowarn
	 */
	public void setUsageMsgHeader(String [] usageMsgHeader)
	{
		this.usageMsgHeader = usageMsgHeader;
	}

	//////////////////////////////////////////////////
	// @@ Command line parsing
	//////////////////////////////////////////////////

	/**
	 * Parses the command line of a Java program.
	 *
	 * @param args Array of program arguments as passed to the main method of the program
	 * @throws CommandLineParserException If a supplied option is not contained in the
	 * list of possible arguments or an option argument is missing.
	 */
	public void parse(String [] args)
		throws CommandLineParserException
	{
		for (int i = 0; i < args.length; ++i)
		{
			String arg = args [i];
			char c = arg.charAt(0);
			if (c == '-' || c == '+')
			{
				// Option
				String option = arg.substring(1);
				option = option.toLowerCase();

				// Check if this option is allowed
				Option opt = (Option) possibleOptions.get(option.toLowerCase());
				if (opt != null)
				{
					if (opt.requiresArgument)
					{
						// Option with argument
						if (i == args.length - 1)
							throw new CommandLineParserException("Option " + option + " requires an argument");
						++i;
						String optArg = args [i];
						if (opt.repeatable)
						{
							// Multiple values allowed for this option
							List list = (List) options.get(option);
							if (list == null)
							{
								list = new ArrayList();
								options.put(option, list);
							}
							list.add(optArg);
						}
						else
						{
							// Single value allowed for this option
							options.put(option, optArg);
						}
					}
					else
					{
						// Boolean option
						options.put(option, new Boolean(c == '-' ? false : true));
					}
					continue;
				}

				// Unknown option
				if (!acceptUnknownOptions)
				{
					throw new CommandLineParserException("Invalid Option: " + option);
				}
			}
			else
			{
				// Argument
				arguments.add(arg);
			}
		}
	}

	/**
	 * Prints a usage message with all options to the standard error output and
	 * exits the program with error code 1.
	 */
	public void printUsageAndExit()
	{
		printUsage();
		System.exit(1);
	}

	/**
	 * Prints a usage message with all options to the standard error output.
	 */
	public void printUsage()
	{
		if (usageMsgHeader != null)
		{
			for (int i = 0; i < usageMsgHeader.length; ++i)
			{
				System.err.println(usageMsgHeader [i]);
			}
		}
		else
		{
			System.err.print("Usage: ");
			if (Application.getAppName() != null)
			{
				System.err.print(Application.getAppName());
			}
			System.err.println();
		}

		StringBuffer sb = new StringBuffer();

		int l = possibleOptionList.size();
		for (int i = 0; i < l; ++i)
		{
			sb.setLength(0);

			Option option = (Option) possibleOptionList.get(i);

			sb.append(option.requiresArgument ? "-" : "+/-");
			sb.append(option.name);
			if (option.requiresArgument)
			{
				sb.append(" <arg>");
			}
			if (option.repeatable)
				sb.append(" ...");

			fillBuf(sb, maxOptionNameLength + 12);

			System.err.print(sb.toString());
			System.err.println(option.usageMsg);
		}
	}

	/**
	 * Fills the string buffer with spaces up to the specified length.
	 * @nowarn
	 */
	private void fillBuf(StringBuffer sb, int l)
	{
		int n = l - sb.length();
		for (int i = 0; i < n; ++i)
		{
			sb.append(' ');
		}
	}

	//////////////////////////////////////////////////
	// @@ Option access
	//////////////////////////////////////////////////

	/**
	 * Retrieves an iterator of options specified on the command line.
	 * The value of an option can be retrieved using one of the the get...Option methods.
	 *
	 * @return An Iterator of String objects
	 */
	public Iterator getOptions()
	{
		return options.keySet().iterator();
	}

	/**
	 * Retrieves a string option value.
	 *
	 * @param name Name of the option
	 * @return The option value
	 * @throws CommandLineParserException On option type error
	 */
	public String getStringOption(String name)
		throws CommandLineParserException
	{
		Object o = options.get(name.toLowerCase());
		if (o != null && ! (o instanceof String))
		{
			throw new CommandLineParserException("Option is not a string option: " + name);
		}
		return (String) o;
	}

	/**
	 * Retrieves a repeatable string option value.
	 *
	 * @param name Name of the option
	 * @return An array of option values or null if no such option was given at all
	 * @throws CommandLineParserException On option type error
	 */
	public String [] getRepeatableOption(String name)
		throws CommandLineParserException
	{
		Object o = options.get(name.toLowerCase());
		if (o != null)
		{
			if (! (o instanceof List))
			{
				throw new CommandLineParserException("Option is not a repeatable string option: " + name);
			}
			return CollectionUtil.toStringArray((List) o);
		}

		return null;
	}

	/**
	 * Retrieves a boolean option value with default value support.
	 *
	 * @param name Name of the option
	 * @param dflt Default value of the option
	 * @return The option value or the default value if this option was not specified on the command line
	 * @throws CommandLineParserException On option type error
	 */
	public boolean getBooleanOption(String name, boolean dflt)
		throws CommandLineParserException
	{
		Object o = options.get(name.toLowerCase());
		if (o != null)
		{
			if (! (o instanceof Boolean))
			{
				throw new CommandLineParserException("Option is not a boolean option: " + name);
			}
			return ((Boolean) o).booleanValue();
		}
		return dflt;
	}

	/**
	 * Retrieves a boolean option value.
	 *
	 * @param name Name of the option
	 * @return The boolean option value
	 * @throws CommandLineParserException On option type error
	 */
	public boolean getBooleanOption(String name)
		throws CommandLineParserException
	{
		return getBooleanOption(name, false);
	}

	/**
	 * Retrieves the list of command arguments (i\. e\. command line arguments that don't
	 * begin with '-' or '+'.
	 *
	 * @return An array of argument strings. The array size can be 0.
	 */
	public String [] getArguments()
	{
		return CollectionUtil.toStringArray(arguments);
	}

	//////////////////////////////////////////////////
	// @@ Inner classes
	//////////////////////////////////////////////////

	/** Class that defines a possible option. */
	private class Option
	{
		/** Option name */
		public String name;

		/** Option requires an option argument */
		public boolean requiresArgument;

		/** Option can be repeated */
		public boolean repeatable;

		/** Option help string */
		public String usageMsg;

		/**
		 * Constructor.
		 * @param name Option name
		 * @param requiresArgument
		 *		true	This option requires an option argument.<br>
		 *		false	This option does not have an argument.
		 * @param repeatable
		 *		true	This option can be repeated.<br>
		 *		false	This option may occur only once on the commandline
		 * @param usageMsg Option help string
		 */
		public Option(String name, boolean requiresArgument, boolean repeatable, String usageMsg)
		{
			this.name = name;
			this.requiresArgument = requiresArgument;
			this.repeatable = repeatable;
			this.usageMsg = usageMsg;
		}
	}
}
