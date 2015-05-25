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
package org.openbp.server.engine.script;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.ReflectException;
import org.openbp.common.ReflectUtil;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.common.string.parser.StringParser;
import org.openbp.common.string.parser.StringParserException;
import org.openbp.core.CoreConstants;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.EngineException;
import org.openbp.core.engine.ExpressionConstants;
import org.openbp.core.model.Model;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.server.context.TokenContext;
import org.openbp.server.context.TokenContextUtil;
import org.openbp.server.persistence.PersistenceContext;
import org.openbp.server.persistence.PersistenceContextProvider;

/**
 * Standard OpenBP expression parser.
 * Expressions are used in a variety of contexts within OpenBP.
 * Expressions can consist of constants, object property access expressions and
 * Java method calls.
 *
 * Usually, an expression operates on an expression context ({@link ExpressionContext}).
 * The expression context contains a list of named objects that can be accessed in the expression.
 * Before parsing the expression, the expression context must be made known to the expression
 * parser by calling the {@link #setContext} method.<br>
 * However, an expression can be evaluated also without an expression context.
 * In this case, the expression may contain only constants or calls to static methods.
 *
 * \bExpression syntax\b
 *
 * An expression specifies an object in the context, a member of the object,
 * a constant or a call of a method of a context object or a static method.
 *
 * An object in the context is identified by the object's name.
 * For access to the object in the context, a prefix may be specified using the
 * {@link #setContextPrefix} method. This can be useful if the context contains objects
 * whose names are hierachically organized and the expression should refer to a
 * particular hierachy level. For example, an token context contains node parameters
 * whose names have the form "node.socket.parameter". Settings the context prefix to
 * "node.socket" will make an expression that refers to a parameter name "parameter"
 * access the context object "node.socket.parameter".
 *
 * The '.' character is used to delimit the object from its member specification.<br>
 * Example:
 * \cOrder.Buyer.Name\c<br>
 * \cOrder\c would be the name of the object, \cBuyer\c designates a member (a \iproperty\i)
 * of the object, and "Name" specifies a property of the "Buyer" object.
 *
 * Instead of an object specification, a constant can be specified.<br>
 * The expression parser supports boolean constants (\ctrue\c or \cfalse\c),
 * integer constants (e. g. \123\c) and string constants (e. g. \c"abc"\c) or the
 * null constant (\cnull\c) denoting an empty object.
 *
 * Format syntax description:
 *
 * expression := object_expression [ "[" index_expression "]" ]
 *
 * object_expression := identifier [ "." object_member ]
 *
 * object_member := identifier [ "." object_member ]
 *
 * index_expr := identifier | number
 *
 * @author Heiko Erhardt
 */
public class ExpressionParser
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** A member access will cause an exception if a context object in an expression does not exist in the list */
	public static final int OBJECT_MUST_EXIST = (1 << 0);

	/** A member access will cause an exception if any member of the expression evaluates to null */
	public static final int MEMBER_MUST_EXIST = (1 << 1);

	/** Automatically create top level objects that do not exist when setting properties */
	public static final int CREATE_TOP_LEVEL_OBJECT = (1 << 2);

	/** Automatically create intermediate objects (member objects) that do not exist when setting properties */
	public static final int CREATE_INTERMEDIATE_OBJECTS = (1 << 3);

	/** Automatically create any objects that do not exist */
	public static final int CREATE_ALL_OBJECTS = (CREATE_TOP_LEVEL_OBJECT | CREATE_INTERMEDIATE_OBJECTS);

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Expression context */
	private ExpressionContext context;

	/** Prefix for context access */
	private String contextPrefix;

	/** Base object table */
	private Map baseObjectTable;

	/** Persistence context provider */
	private PersistenceContextProvider persistenceContextProvider;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ExpressionParser()
	{
	}

	/**
	 * Value constructor.
	 *
	 * @param context Expression context
	 */
	public ExpressionParser(ExpressionContext context)
	{
		this.context = context;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Sets the expression context.
	 * @param context Context or null if the expression does not operate on a context
	 *
	 * @deprecated We don't suggest changing the context of an ExpressionParser.
	 */
	public void setContext(ExpressionContext context)
	{
		this.context = context;
	}

	/**
	 * Sets the prefix for context access.
	 * @param contextPrefix The prefix including the delimiter character (e. g. "Node.") or null if no prefix should be used
	 */
	public void setContextPrefix(String contextPrefix)
	{
		this.contextPrefix = contextPrefix;
	}

	/**
	 * Sets the base object table.
	 * @nowarn
	 */
	public void setBaseObjectTable(Map baseObjectTable)
	{
		this.baseObjectTable = baseObjectTable;
	}

	//////////////////////////////////////////////////
	// @@ Context path access
	//////////////////////////////////////////////////

	/**
	 * Evaluates an expression denoting an object or a member of an object and returns the expression value.
	 *
	 * @param expression Expression to evaluate<br>
	 * For a description of the expression syntax, see the comment of this class.
	 * @param base Object to use as base for any expressions or null to retrieve the base from the context
	 * @param mode Param that specifies how errors should be treated.
	 * Use a combination of the following parameters:<br>
	 * {@link ExpressionParser#OBJECT_MUST_EXIST} | {@link ExpressionParser#MEMBER_MUST_EXIST}<br>
	 * The default (0) will not cause any exceptions if the object or a member are null.
	 * @return The value of the expression or null if the expression value itself is null or
	 * if a member in the chain in the object specification is null
	 * @throws OpenBPException If the expression syntax is invalid or if the expression evaluation fails
	 * (depending on the mode parameter)
	 */
	public Object getContextPathValue(String expression, Object base, int mode)
	{
		if (expression == null)
			throw new EngineException("ExpressionIsNull", "Cannot evaluate null expression");

		Object value = null;

		StringParser sp = new StringParser(expression);

		try
		{
			char c;
			String ident;

			sp.skipSpace();

			if (base != null)
			{
				value = base;
			}
			else
			{
				// First symbol must be an identifier denoting the object in the list
				ident = sp.getIdentifier();
				if (ident == null)
					throw newError("Expression.IdentifierExpected", "Identifier expected in expression", sp);

				// Check if this ident is continued using the "ident1\.ident2" syntax
				ident = collectIdent(sp, ident);

				// Get the first object
				value = getBaseObject(ident);
				if (value == null)
				{
					// Object not found in list
					if ((mode & OBJECT_MUST_EXIST) != 0)
					{
						throw newError("Expression.ObjectNotFound", "Object '" + ident + "' does not exist", sp);
					}
					return null;
				}
			}

			// Now look for member specs
			for (;;)
			{
				// Need '.', '[' or end of string

				c = sp.getChar();
				if (base != null)
				{
					// A base object has been provided, continue with accessing its property
					if (c != '-')
					{
						c = ExpressionConstants.MEMBER_OPERATOR_CHAR;
					}
					base = null;
				}
				else
				{
					if (c == 0)
						break;
					sp.nextChar();
				}
				sp.skipSpace();

				if (c == '[')
				{
					// Index specification

					ident = "[...]"; // For debug msg below only

					if (value.getClass().isArray())
					{
						// Get the text between '[' and ']'
						String indexExpr = collectIndexExpr(sp);

						int pos = Integer.parseInt(indexExpr);

						if (pos >= 0 && pos < Array.getLength(value))
						{
							return Array.get(value, pos);
						}

						return null;
					}
					else if (value instanceof List)
					{
						String indexExpr = collectIndexExpr(sp);
						int pos = Integer.parseInt(indexExpr);

						List list = (List) value;

						try
						{
							value = list.get(pos);
						}
						catch (IndexOutOfBoundsException e)
						{
							throw newError("Expression.IndexOutOfBounds", "Collection index '" + pos + "' is out of bounds", sp);
						}
					}
					else if (value instanceof Collection)
					{
						String indexExpr = collectIndexExpr(sp);

						int pos = Integer.parseInt(indexExpr);

						Collection coll = (Collection) value;
						Iterator it = coll.iterator();

						for (int i = 0; i < pos; ++i)
						{
							if (!it.hasNext())
							{
								throw newError("Expression.IndexOutOfBounds", "Collection index '" + pos + "' is out of bounds", sp);
							}

							value = it.next();
						}
					}
					else if (value instanceof Map)
					{
						String indexExpr = collectIndexExpr(sp);

						Map map = (Map) value;

						// First, try to get the value directly from the map
						value = map.get(indexExpr);
						if (value == null)
						{
							// Didn't work, iterate the map and compare the map element specifiers
							Iterator it = map.keySet().iterator();

							for (int i = 0; it.hasNext(); i++)
							{
								Object key = it.next();
								String name = ScriptUtil.createMapElementName(key, false);

								if (name != null && name.equals(indexExpr))
								{
									value = map.get(key);
									break;
								}
							}
						}
					}
				} // End index spec
				else if (c == ExpressionConstants.REFERENCE_KEY_OPERATOR_CHAR && sp.getChar() == ExpressionConstants.REFERENCE_KEY_OPERATOR_CHAR)
				{
					sp.nextChar(); // Consume

					if (!(context instanceof TokenContext))
					{
						throw newError("Expression.EvaluationContextMissing", "Invalid context for '" + ExpressionConstants.REFERENCE_KEY_OPERATOR + "' operation", sp);
					}
					Model model = ((TokenContext) context).getExecutingModel();

					PersistenceContextProvider pcp = getPersistenceContextProvider();
					if (pcp == null)
					{
						throw newError("Expression.EvaluationContextMissing", "Invalid context for '" + ExpressionConstants.REFERENCE_KEY_OPERATOR + "' operation", sp);
					}

					ident = collectDataTypeName(sp);

					try
					{
						DataTypeItem targetType = (DataTypeItem) model.resolveItemRef(ident, ItemTypes.TYPE);
						if (targetType.isSimpleType())
						{
							throw newError("Expression.EvaluationContextMissing", "Data type '" + ident + "' in '" + ExpressionConstants.REFERENCE_KEY_OPERATOR + "' expression may not be a primitive type.", sp);
						}

						try
						{
							// Retrieve the bean according to the given id value
							PersistenceContext pc = pcp.obtainPersistenceContext();
							value = pc.findById(value, targetType.getJavaClass());
						}
						catch (Exception e)
						{
							throw newError("AutoRetrieval", "Auto-retrieval of bean of expression '" + ExpressionConstants.REFERENCE_KEY_OPERATOR + "" + ident + "' by id value '" + value + "' failed.", e, sp);
						}
					}
					catch (OpenBPException e)
					{
						throw newError("Expression.EvaluationContextMissing", "Data type '" + ident + "' in '" + ExpressionConstants.REFERENCE_KEY_OPERATOR + "' expression could not be found in the executing model or its imported models.", sp);
					}
				}
				else
				{
					// Regular member spec
					if (c != ExpressionConstants.MEMBER_OPERATOR_CHAR)
					{
						throw newError("Expression.Syntax", "'" + ExpressionConstants.MEMBER_OPERATOR_CHAR + "' or end of expression expected", sp);
					}

					// Need identifier
					ident = sp.getIdentifier();
					if (ident == null)
						throw newError("Expression.IdentifierExpected", "Identifier expected in expression", sp);

					// Access the property
					try
					{
						value = PropertyAccessUtil.getProperty(value, ident);
					}
					catch (PropertyException e)
					{
						throw newError("Expression.PropertyAccessFailed", e.getMessage(), e.getCause(), sp);
					}
				}

				sp.skipSpace();
				c = sp.getChar();
				if (c == 0)
					break;

				if (value == null)
				{
					// Object not found in list
					if ((mode & MEMBER_MUST_EXIST) != 0)
					{
						throw newError("Expression.Null", "Expression member '" + ident + "' evaluated to null", sp);
					}
					return null;
				}
			}
		}
		catch (StringParserException e)
		{
			throw newError("Expression.Syntax", "Syntax error in expression", e, sp);
		}

		return value;
	}

	/**
	 * Sets a property of an object that is specified by an expression.
	 *
	 * @param expression Expression that denotes an object or an object member<br>
	 * For a description of the expression syntax, see the comment of this class.<br>
	 * Note that in contrast to {@link #getContextPathValue}, the expression may not
	 * contain constants or method calls.
	 * @param value Property value to set
	 * @param topLevelDataType Data type of the context object the expression refers to.<br>
	 * E.g., the expression "Order.Buyer.Name" would refer to the data type "Order".
	 * @param mode Param that specifies how errors should be treated.
	 * Use a combination of the following parameters:<br>
	 * {@link ExpressionParser#CREATE_TOP_LEVEL_OBJECT} | {@link ExpressionParser#CREATE_INTERMEDIATE_OBJECTS} | {@link ExpressionParser#CREATE_ALL_OBJECTS}<br>
	 * The default (0) will cause an exception if an object does not exist.
	 * @throws OpenBPException If the expression syntax is invalid or if the expression evaluation fails
	 * (depending on the mode parameter)
	 */
	public void setContextPathValue(String expression, Object value, DataTypeItem topLevelDataType, int mode)
	{
		if (expression == null)
			throw new EngineException("ExpressionIsNull", "Cannot evaluate null expression");

		StringParser sp = new StringParser(expression);

		try
		{
			Object base = null;
			String ident;
			char cNext;

			sp.skipSpace();

			// First symbol must be an identifier denoting the object in the list
			ident = sp.getIdentifier();
			if (ident == null)
				throw newError("Expression.IdentifierExpected", "Identifier expected in expression", sp);
			sp.skipSpace();
			cNext = sp.getChar();

			if (topLevelDataType == null || topLevelDataType.isSimpleType())
			{
				// A simple type does not allow use of '.' to access members
				if (cNext != 0)
				{
					throw newError("Expression.MemberAccessForbidden", "Member access to simple type '" + (topLevelDataType != null ? topLevelDataType.getName() : "?") + "' forbidden", sp);
				}

				// TODO Fix 5 We have to check if the value matches the specified type here

				setContextObject(ident, value);
				return;
			}

			// Check if this ident is continued using the "ident1\.ident2" syntax
			ident = collectIdent(sp, ident);

			// Get the first object
			base = getBaseObject(ident);
			if (base == null)
			{
				// Object not found in list, try to create it
				if ((mode & CREATE_TOP_LEVEL_OBJECT) == 0)
				{
					// We do not automatically create top level objects
					throw newError("Expression.ObjectNotFound", "Object '" + ident + "' does not exist in object context", sp);
				}

				// Create the object and add it to the context
				base = createBeanInstance((ComplexTypeItem) topLevelDataType);
				setContextObject(ident, base);
			}

			// Now look for member specs
			DataTypeItem dataType = topLevelDataType;
			while (cNext != 0)
			{
				// Need '.' or end of string
				if (cNext != ExpressionConstants.MEMBER_OPERATOR_CHAR)
				{
					throw newError("Expression.Syntax", "'" + ExpressionConstants.MEMBER_OPERATOR_CHAR + "' or end of expression expected", sp);
				}
				sp.nextChar(); // Consume

				// Need identifier for property access
				sp.skipSpace();
				ident = sp.getIdentifier();
				if (ident == null)
					throw newError("Expression.IdentifierExpected", "Identifier expected in expression", sp);

				// Keep the current data type up to date
				if (topLevelDataType.isSimpleType())
				{
					dataType = null;
				}
				else if (dataType != null)
				{
					DataMember dataMember = ((ComplexTypeItem) dataType).getMember(ident);
					if (dataMember != null)
						dataType = dataMember.getDataType();
				}

				// Check if this is the last part of the expression
				sp.skipSpace();
				cNext = sp.getChar();
				if (cNext == 0)
				{
					// Yes, set the property value
					try
					{
						PropertyAccessUtil.setProperty(base, ident, value);
					}
					catch (PropertyException e)
					{
						throw newError("Expression.PropertyAccessFailed", e.getMessage(), e.getCause(), sp);
					}

					// Finished!
					return;
				}

				// No, get the object the property value refers to
				Object propValue = null;
				try
				{
					propValue = PropertyAccessUtil.getProperty(base, ident);
				}
				catch (PropertyException e)
				{
					throw newError("Expression.PropertyAccessFailed", e.getMessage(), e.getCause(), sp);
				}

				if (propValue == null)
				{
					if ((mode & CREATE_INTERMEDIATE_OBJECTS) == 0)
					{
						// We do not automatically create intermediate objects
						throw newError("Expression.Null", "Property '" + ident + "' evaluated to null", sp);
					}

					// Object not found in list, try to create it
					if (dataType == null)
					{
						throw newError("Expression.ObjectNotFound", "Object '" + ident + "' does not exist in object context and no data type has been specified in order to create it", sp);
					}
					if (dataType.isSimpleType())
					{
						throw newError("Expression.CouldNotCreateInstance", "Cannot create an instance of simple type '" + dataType.getName() + "' to resolve the member chain.", sp);
					}

					// Create the object
					propValue = createBeanInstance((ComplexTypeItem) dataType);

					// Set the property accordingly
					try
					{
						PropertyAccessUtil.setProperty(base, ident, propValue);
					}
					catch (PropertyException e)
					{
						throw newError("Expression.PropertyAccessFailed", e.getMessage(), e.getCause(), sp);
					}
				}

				// Now we have a new base object we can continue with
				base = propValue;
			}
		}
		catch (StringParserException e)
		{
			throw newError("Expression.Syntax", "Syntax error in expression", e, sp);
		}
	}

	//////////////////////////////////////////////////
	// @@ Direct access to context objects
	//////////////////////////////////////////////////

	/**
	 * Gets the base object the expression refers to.
	 * Usually retrieves this object from the context.
	 *
	 * @param name Name of the object<br>
	 * Process variables must be prefixed with the '_' character.
	 * @return Value of the object or null if no such object exists
	 * @throws OpenBPException If no context object was provided to the parser
	 */
	public Object getBaseObject(String name)
	{
		if (baseObjectTable != null)
		{
			// Check the provided base object table first
			Object value = baseObjectTable.get(name);
			if (value != null)
				return value;
		}

		if (context == null)
		{
			throw new EngineException("EvaluationContextMissing", "Cannot refer to context object '" + name + "' without a context.");
		}

		String contextName;
		if (TokenContextUtil.isProcessVariableIdentifier(name))
		{
			contextName = CoreConstants.PROCESS_VARIABLE_INDICATOR + name.substring(1);
		}
		else if (contextPrefix != null)
		{
			contextName = contextPrefix + name;
		}
		else
		{
			contextName = name;
		}
		return context.getObject(contextName);
	}

	/**
	 * Adds an object to the context.
	 *
	 * @param name Name of the object<br>
	 * Process variables must be prefixed with the '_' character.
	 * @param value Value of the object
	 * @throws OpenBPException If no context object was provided to the parser
	 */
	protected void setContextObject(String name, Object value)
	{
		if (context == null)
		{
			throw new EngineException("EvaluationContextMissing", "Cannot refer to context object '" + name + "' without a context.");
		}

		String contextName;
		if (TokenContextUtil.isProcessVariableIdentifier(name))
		{
			contextName = CoreConstants.PROCESS_VARIABLE_INDICATOR + name.substring(1);
		}
		else if (contextPrefix != null)
		{
			contextName = contextPrefix + name;
		}
		else
		{
			contextName = name;
		}
		context.setObject(contextName, value);
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Creates an instance of the Java bean of the specified data type.
	 *
	 * @param dataType Type of object to create
	 * @return The bean instance
	 * @throws OpenBPException If the bean could not be instantiated
	 */
	protected Object createBeanInstance(ComplexTypeItem dataType)
	{
		Class cls = dataType.getJavaClass();
		if (cls == null)
		{
			throw new EngineException("CouldNotCreateInstance", "Cannot create bean instance; no bean class available for data type '" + dataType.getQualifier() + "'");
		}

		Object o = null;
		try
		{
			o = ReflectUtil.instantiate(cls, null, "bean");
		}
		catch (ReflectException e)
		{
			throw new EngineException("CouldNotCreateInstance", "Error instantiating bean object.", e);
		}

		return o;
	}

	/**
	 * Collects an identifier that is made of several point-separated identifiers.
	 * The syntax looks like "ident1\.ident2" etc.
	 *
	 * @param sp String parser
	 * @param ident Ident
	 * @return The qualified identifier
	 * @throws OpenBPException If the expression syntax is invalid or if the expression evaluation fails
	 * (depending on the mode parameter)
	 */
	private String collectIdent(StringParser sp, String ident)
	{
		StringBuffer sb = null;

		for (;;)
		{
			char c = sp.getChar();
			if (c != '\\')
				break;
			sp.nextChar(); // Consume

			c = sp.getChar();
			if (c != ExpressionConstants.MEMBER_OPERATOR_CHAR)
			{
				sp.rewindChar(); // Undo consume
				break;
			}
			sp.nextChar(); // Consume

			String s = sp.getIdentifier();
			if (s == null)
				throw newError("Expression.IdentifierExpected", "Identifier expected in expression after '\\.'", sp);

			if (sb == null)
			{
				sb = new StringBuffer(ident);
			}
			sb.append(ExpressionConstants.MEMBER_OPERATOR_CHAR);
			sb.append(s);
		}

		if (sb != null)
			ident = sb.toString();
		return ident;
	}

	/**
	 * Collects an array index element specifier from the current expression.
	 *
	 * @param sp String parser; current position: after the '[' of "[abc]"
	 * @return Array element specifier (example: "abc")<br>
	 * The current position of the string parser is after the ']'
	 * @throws OpenBPException If the expression syntax is invalid
	 */
	private String collectIndexExpr(StringParser sp)
	{
		StringBuffer sb = new StringBuffer();

		for (;;)
		{
			char c = sp.getChar();
			if (c == 0)
				break;
			sp.nextChar(); // Consume

			if (c == ']')
				return sb.toString();

			sb.append(c);
		}

		throw newError("Expression.Syntax", "']' expected in expression", sp);
	}

	/**
	 * Collects a data type specification from the current expression.
	 *
	 * @param sp String parser; current position: At the first character of the type expression
	 * @return A data type specification, e. g. "Company" or "/Model/Company".
	 * The current position of the string parser is after the type name.
	 * @throws OpenBPException If the expression syntax is invalid
	 */
	private String collectDataTypeName(StringParser sp)
	{
		StringBuffer sb = new StringBuffer();

		for (;;)
		{
			char c = sp.getChar();

			if (c == ModelQualifier.PATH_DELIMITER_CHAR || Character.isLetterOrDigit(c))
			{
				sp.nextChar(); // Consume
				sb.append(c);
				continue;
			}

			break;
		}

		if (sb.length() == 0)
		{
			throw newError("Expression.Syntax", "Data type name expected in expression", sp);
		}

		return sb.toString();
	}

	/**
	 * Throws an error.
	 *
	 * @param errorCode Error code
	 * @param msg Error Message
	 * @param sp String parser used to parse the expression
	 * @return A new OpenBPException
	 */
	private OpenBPException newError(String errorCode, String msg, StringParser sp)
	{
		return newError(errorCode, msg, null, sp);
	}

	/**
	 * Throws an error.
	 *
	 * @param errorCode Error code
	 * @param msg Error Message
	 * @param throwable Throwable
	 * @param sp String parser used to parse the expression
	 * @return A new OpenBPException
	 */
	private OpenBPException newError(String errorCode, String msg, Throwable throwable, StringParser sp)
	{
		msg = msg + "\nExpression = '" + sp.getSourceString() + "', column = " + sp.getPos();
		return new EngineException(errorCode, throwable);
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
	public void setPersistenceContextProvider(PersistenceContextProvider persistenceContextProvider)
	{
		this.persistenceContextProvider = persistenceContextProvider;
	}
}
