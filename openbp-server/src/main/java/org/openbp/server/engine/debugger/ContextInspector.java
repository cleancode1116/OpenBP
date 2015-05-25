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
package org.openbp.server.engine.debugger;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openbp.common.ExceptionUtil;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.property.PropertyAccessUtil;
import org.openbp.common.property.PropertyException;
import org.openbp.common.string.StringUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.ExpressionConstants;
import org.openbp.core.engine.debugger.ObjectMemberInfo;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;
import org.openbp.server.context.TokenContext;
import org.openbp.server.engine.EngineUtil;
import org.openbp.server.engine.script.ExpressionParser;
import org.openbp.server.engine.script.ScriptUtil;

/**
 * Token context inspector.
 * Provides methods to extract debug output of variables of a token context
 * and their members.
 *
 * @author Heiko Erhardt
 */
public class ContextInspector
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Member skip mode: Do not skip any members */
	public static final int SKIP_NONE = 0;

	/** Member skip mode: Skip null members */
	public static final int SKIP_NULL = 1;

	/** Member skip mode: Skip members having default values (object: null, int: 0, boolean: false etc.) */
	public static final int SKIP_DEFAULT = 2;

	/** Maximum number of members to add from a container/array object. */
	private static final int MAX_MEMBERS = 20;

	/** An empty Object-Array for calling Method.invoke() with no arguments */
	private static final Object [] NO_METHOD_ARGUMENTS = new Object [0];

	/** Prefixes for getter methods */
	private static final String [] GETTER_PREFIX = new String [] { "get", "is" };

	/** Names of members that should be ignored */
	private static Hashtable membersToIgnore = new Hashtable();

	static
	{
		// Omit Object.getClass()
		membersToIgnore.put("Class", Boolean.TRUE);

		// Omit Object.getBytes()
		membersToIgnore.put("Bytes", Boolean.TRUE);
	}

	/** Classes that are considered to be primitives */
	private static String [] primitiveClassNames = new String [] { "java.lang.boolean", "java.lang.Boolean", "java.lang.byte", "java.lang.Byte", "java.lang.character", "java.lang.Character", "java.lang.Class", "java.lang.double", "java.lang.Double", "java.lang.float", "java.lang.Float", "java.lang.integer", "java.lang.Integer", "java.lang.long", "java.lang.Long", "java.lang.Number", "java.lang.Object", "java.lang.short", "java.lang.Short", "java.lang.String", "java.lang.StringBuffer", };

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Token context to inspect */
	private TokenContext context;

	/** Member skip mode {@link #SKIP_NONE}/{@link #SKIP_NULL}/{@link #SKIP_DEFAULT} */
	private int memberSkipMode = SKIP_DEFAULT;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ContextInspector()
	{
	}

	//////////////////////////////////////////////////
	// @@ Public methods
	//////////////////////////////////////////////////

	/**
	 * Retrieves information about parameters of the token context or the members
	 * of a particular parameter object within the object hierarchy of the parameter.
	 *
	 * @param contextPath Path of the context object we are refering to:<br>
	 * If the path is null, all parameters of the context will be returned.
	 * If the path specifies the full path of a context object (e. g. "node.socket.param"),
	 * the method returns all members of the object.<br>
	 * Otherwise, all parameters beginning with the specified path will be returned.
	 *
	 * @param expression if the 'contextPath' referes to a particular parameter, this expression
	 * may refer to a member of this parameter (e. g. contextPath = "CreateClient.Out.Client",
	 * expression = "User.Profile" will return all members of the 'Profile' member of the
	 * 'User' object of the created client.
	 *
	 * @return A list of {@link ObjectMemberInfo} objects or null if the request could not be resolved
	 */
	public List getObjectMembers(String contextPath, String expression)
	{
		Object object = evaluateExpression(contextPath, expression);
		if (object == null)
			return null;

		return collectObjectMembers(object);
	}

	/**
	 * Retrieves information about a parameter of the token context or a member
	 * of a particular parameter object within the object hierarchy of the parameter.
	 *
	 * @param contextPath Path of the context object we are refering to.<br>
	 * The path must specify an existing context parameter.
	 *
	 * @param expression This expression may refer to a member of the parameter
	 * (e. g. contextPath = "CreateClient.Out.Client", expression = "User.Profile"
	 * will return the 'Profile' member of the 'User' object of the created client.
	 *
	 * @return The object member information or null if the request could not be resolved
	 */
	public ObjectMemberInfo getObjectValue(String contextPath, String expression)
	{
		Object obj = evaluateExpression(contextPath, expression);
		if (obj == null)
			return null;

		return createNamedMember(null, obj, false);
	}

	/**
	 * Gets the token context to inspect.
	 * @nowarn
	 */
	public TokenContext getContext()
	{
		return context;
	}

	/**
	 * Sets the token context to inspect.
	 * @nowarn
	 */
	public void setContext(TokenContext context)
	{
		this.context = context;
	}

	//////////////////////////////////////////////////
	// @@ Implementation
	//////////////////////////////////////////////////

	/**
	 * Return the object described a path expression, starting from the
	 * process context.
	 *
	 * @param contextPath Path of the context object we are refering to.<br>
	 * The path must specify an existing context parameter.
	 *
	 * @param expression This expression may refer to a member of the parameter
	 * (e. g. contextPath = "CreateClient.Out.Client", expression = "User.Profile"
	 * will return the 'Profile' member of the 'User' object of the created client.
	 *
	 * @return The expression value
	 */
	private Object evaluateExpression(String contextPath, String expression)
	{
		if (contextPath == null)
		{
			// No context path means the context itself
			return context;
		}

		// Prepend the escape character before "." to force the expression parser
		// to collect all object path specifiers for the context parameter access
		String parserExpression = StringUtil.substitute(contextPath, ModelQualifier.OBJECT_DELIMITER, "\\.");

		if (expression != null)
		{
			// Append the expression
			if (expression.charAt(0) == '[')
				parserExpression += expression;
			else
				parserExpression = parserExpression + ExpressionConstants.MEMBER_OPERATOR + expression;
		}

		Object value = null;

		try
		{
			ExpressionParser parser = EngineUtil.createExpressionParser(context, null);
			value = parser.getContextPathValue(parserExpression, null, 0);
		}
		catch (OpenBPException e)
		{
			LogUtil.error(getClass(), "Error evaluating expression $0. [{1}]", parserExpression, context, e);
		}

		return value;
	}

	/**
	 * Collects information on all childs of an object.
	 *
	 * @param obj Object to inspect
	 * @return A list of {@link ObjectMemberInfo} objects or
	 * null if the object does not have any members
	 */
	private List collectObjectMembers(Object obj)
	{
		if (obj == null || obj instanceof Iterator || obj instanceof Enumeration)
		{
			// No members available or only an interator
			// (iterators can be traversed once only, so we don't show em in the debugger)
			return null;
		}

		// We use lazy allocation of the member list
		Class cls = obj.getClass();
		List members = null;
		ComplexTypeItem type = null;

		// Determine the type of the object and use
		// the appropriate method to collect all member names

		if (obj instanceof TokenContext)
		{
			TokenContext context = (TokenContext) obj;

			// Iterate all parameter data of the context;
			// We want the complete information of what is in the context, so we don't skip
			// any members, even if the member value is null
			Map paramValues = context.getParamValues();
			for (Iterator it = paramValues.keySet().iterator(); it.hasNext();)
			{
				String key = (String) it.next();

				// Determine the value
				Object value = context.getParamValue(key);

				// Add the value to the member list
				if (members == null)
					members = new ArrayList();
				members.add(createNamedMember(key, value, true));
			}

			// We should sort the members.
			if (members != null)
			{
				Collections.sort(members);
			}
		}
		else if (cls.isArray())
		{
			int len = Array.getLength(obj);

			// Iterate the array;
			// Leaving holes in the array indices doesn't look very good,
			// so we don't skip any members, even if the member value is null
			for (int i = 0; i < len; i++)
			{
				if (members == null)
					members = new ArrayList();

				if (i == MAX_MEMBERS)
				{
					members.add(createEtcMember());
					break;
				}

				// Determine the value
				Object value = Array.get(obj, i);

				// Add the value to the member list
				members.add(createIndexMember(i, value));
			}
		}
		else if (obj instanceof Collection)
		{
			Collection coll = (Collection) obj;

			// Iterate the collection;
			// Leaving holes in the collection indices doesn't look very good,
			// so we don't skip any members, even if the member value is null
			int i = 0;
			for (Iterator it = coll.iterator(); it.hasNext();)
			{
				if (members == null)
					members = new ArrayList();

				if (i == MAX_MEMBERS)
				{
					members.add(createEtcMember());
					break;
				}

				// Determine the value
				Object value = it.next();

				// Add the value to the member list
				members.add(createIndexMember(i++, value));
			}
		}
		else if ((type = context.getExecutingModel().lookupTypeByClassName(cls.getName())) != null)
		{
			// Iterate all members of the type of the bean and its super beans
			for (Iterator it = type.getAllMembers(); it.hasNext();)
			{
				DataMember member = (DataMember) it.next();

				DataTypeItem memberType = member.getDataType();
				if (memberType != null)
				{
					String memberName = member.getName();

					try
					{
						// Access the property according to the member name
						Object value = PropertyAccessUtil.getProperty(obj, memberName);

						if (skipMemberValue(value))
						{
							// Default value or null, skip this member
							continue;
						}

						// Add the value to the member list
						if (members == null)
							members = new ArrayList();
						members.add(createNamedMember(memberName, value, true));
					}
					catch (PropertyException e)
					{
						ExceptionUtil.printTrace(e);
					}
					catch (ClassCastException e)
					{
						ExceptionUtil.printTrace(e);
					}
				}
			}
		}
		else if (obj instanceof Map)
		{
			Map map = (Map) obj;

			// Iterate the map keys
			int i = 0;
			for (Iterator it = map.keySet().iterator(); it.hasNext();)
			{
				if (i == MAX_MEMBERS)
				{
					if (members == null)
						members = new ArrayList();
					members.add(createEtcMember());
					break;
				}

				Object key = it.next();
				String name = ScriptUtil.createMapElementName(key, true);

				if (name != null)
				{
					Object value = map.get(key);

					if (skipMemberValue(value))
					{
						// Default value or null, skip this member
						continue;
					}

					// Add the value to the member list
					if (members == null)
						members = new ArrayList();
					members.add(createNamedMember(name, value, true));
					++i;
				}
			}

			// We should sort the members.
			if (members != null)
			{
				Collections.sort(members);
			}
		}
		else
		{
			// Regular object that is not a bean
			String className = cls.getName();

			if (className.startsWith("java."))
			{
				// We don't inspect standard java classes
				return null;
			}

			// Iterate the getter methods of the object
			Method [] methods = cls.getMethods();
			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods [i];
				String methodName = method.getName();

				if (method.getParameterTypes().length != 0)
				{
					// Method takes parameters, so it cannot be a simple getter
					continue;
				}

				String propertyName = determineCutOffMethodName(methodName);
				if (propertyName == null)
				{
					// Doesn't start with "get" or "is"
					continue;
				}

				// Check if this is some property we should ignore (e. g. getClass())
				if (membersToIgnore.containsKey(propertyName))
				{
					// Ignore this property
					continue;
				}

				// Determine the property value
				Object value = null;
				try
				{
					value = (method.invoke(obj, NO_METHOD_ARGUMENTS));

					if (skipMemberValue(value))
					{
						// Default value or null, skip this member
						continue;
					}
				}
				catch (InvocationTargetException ite)
				{
				}
				catch (IllegalAccessException iae)
				{
				}
				catch (IllegalArgumentException iae)
				{
				}

				// Add the value to the member list
				if (members == null)
					members = new ArrayList();
				members.add(createNamedMember(propertyName, value, true));
			}

			// We should sort the members.
			if (members != null)
			{
				Collections.sort(members);
			}
		}

		return members;
	}

	/**
	 * Determines a method name cut off by different prefixes.
	 * @param methodName Name of the method to cut off
	 * @return Cut method name or null
	 */
	private static String determineCutOffMethodName(String methodName)
	{
		for (int i = 0; i < GETTER_PREFIX.length; i++)
		{
			if (methodName.startsWith(GETTER_PREFIX [i]))
			{
				return methodName.substring(GETTER_PREFIX [i].length());
			}
		}
		return null;
	}

	/**
	 * Checks if the string value of objects of this type should be printed or not.
	 *
	 * @param cls Event
	 * @return
	 *		true	For this return type (e. g. Maps, collections, iterators, arrays),
	 *				we won't print the toString value (this may get big!) - the display in the
	 *				Modeler's context value will be empty
	 *		false	The return value of this type should be displayed.
	 */
	private boolean ignoreToStringValue(Class cls)
	{
		return (cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls) || Iterator.class.isAssignableFrom(cls) || Enumeration.class.isAssignableFrom(cls));
	}

	/**
	 * Checks if the given member value should be skipped.
	 * This relies on the memberSkipMode property.
	 *
	 * @param value Value
	 * @return
	 *		true	The value is null or the default value and should not be displayed.
	 *		false	The value should be displayed.
	 */
	boolean skipMemberValue(Object value)
	{
		switch (memberSkipMode)
		{
		case SKIP_NULL:
			if (value == null)
				return true;
			break;

		case SKIP_DEFAULT:
			if (value == null)
				return true;
			if (value instanceof Double)
			{
				if (((Double) value).doubleValue() == 0.0d)
					return true;
			}
			else if (value instanceof Float)
			{
				if (((Float) value).floatValue() == 0.0f)
					return true;
			}
			else if (value instanceof Number)
			{
				if (((Number) value).intValue() == 0)
					return true;
			}
			else if (value instanceof Boolean)
			{
				if (!((Boolean) value).booleanValue())
					return true;
			}
			break;
		}

		return false;
	}

	/**
	 * Create a new object member info from an object.
	 * A member name "[0]" will be created for index 0, "[1]" for index 1, etc.
	 *
	 * @param keyIndex The index of the member
	 * @param value The object to describe with the object member info
	 *
	 * @return The member info
	 */
	private ObjectMemberInfo createIndexMember(int keyIndex, Object value)
	{
		return createNamedMember("[" + keyIndex + "]", value, true);
	}

	/**
	 * Create a new object member info from an object.
	 *
	 * @param key The name of the member
	 * @param value The object to describe with the object member info
	 * @param limitSize
	 *		true	Gets the object value string up to 100 characters<br>
	 *		false	Gets the object value string in its full size
	 *
	 * @return The member info
	 */
	private ObjectMemberInfo createNamedMember(String key, Object value, boolean limitSize)
	{
		String type = null;
		String toStringValue = null;
		boolean isParent = false;

		if (value != null)
		{
			Class cls = value.getClass();
			String className = cls.getName();
			ComplexTypeItem complexType = context.getExecutingModel().lookupTypeByClassName(className);

			if (cls.isArray())
			{
				type = cls.getComponentType().getName() + "[]";
				toStringValue = "[" + Array.getLength(value) + "]";
				isParent = true;
			}
			else if (complexType != null)
			{
				type = complexType.getQualifier().toUntypedString();
				isParent = true;
			}
			else if (Collection.class.isAssignableFrom(cls))
			{
				type = className;
				toStringValue = "[" + ((Collection) value).size() + "]";
				isParent = true;
			}
			else if (Map.class.isAssignableFrom(cls))
			{
				type = className;
				toStringValue = "[" + ((Map) value).size() + "]";
				isParent = true;
			}
			else
			{
				type = className;

				isParent = true;
				for (int i = 0; i < primitiveClassNames.length; ++i)
				{
					if (primitiveClassNames [i].equals(className))
					{
						isParent = false;
						break;
					}
				}
			}

			if (toStringValue == null)
			{
				// We haven't determined a value to display yet, use the toString value of the object
				if (ignoreToStringValue(cls))
				{
					// For some return types (e. g. Maps, collections, iterators, arrays),
					// we won't print the toString value (this may get big!) - the display in the
					// Modeler's context value will be empty
					toStringValue = "[...]";
				}
				else
				{
					toStringValue = value.toString();

					// Cut off toString value if too long
					if (limitSize && toStringValue.length() > ScriptUtil.MAX_TOSTRINGVALUE_LENGTH)
					{
						toStringValue = toStringValue.substring(0, ScriptUtil.MAX_TOSTRINGVALUE_LENGTH);
					}
				}
			}
		}

		// Cut off well-known prefixes
		if (type != null)
		{
			if (type.startsWith("java.lang."))
			{
				type = type.substring(10);
			}
			else if (type.startsWith("java.util."))
			{
				type = type.substring(10);
			}
		}

		ObjectMemberInfo member = new ObjectMemberInfo();
		member.setKey(key);
		member.setType(type);
		member.setToStringValue(toStringValue);
		member.setParentMember(isParent);

		return member;
	}

	/**
	 * Create a new object member info that describes the case that there are more
	 * members than we want to transfer.
	 *
	 * @return The member info
	 */
	private ObjectMemberInfo createEtcMember()
	{
		// Create a dummy object that is a leaf node
		return new ObjectMemberInfo("...", "...", "...", false);
	}
}
