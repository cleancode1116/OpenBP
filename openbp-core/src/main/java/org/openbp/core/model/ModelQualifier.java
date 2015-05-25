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
package org.openbp.core.model;

import java.io.Serializable;

import org.openbp.common.CommonUtil;
import org.openbp.common.generic.PrintNameProvider;
import org.openbp.common.string.StringUtil;
import org.openbp.core.model.item.Item;

/**
 * A model qualifier identifies a model object.
 * Similar to a file system path, it may be absolute (containing a model specification) or relative (refering to a know model or component).
 *
 * The model qualifier class can be seen similar to the java.net.URL class.
 * The syntax of a model qualifier is even similar to the URL syntax.
 * The corresponding string form is the string returned by the {@link PrintNameProvider#getPrintName} method.
 *
 * A model qualifier has the following format:
 * JIO := [ItemSpec][ObjectSpec]
 * ItemSpec := [ModelSpec '/'] [ItemType ':'] ItemName
 * ModelSpec := '/' ModelName
 *
 * Examples:
 *
 * Fully qualified model:<br>
 * @code 3
 * /Model<br>
 * model
 * @code
 *
 * Fully qualified process item:<br>
 * @code 3
 * /Model/Process:ProcName or<br>
 * model  type    item<br>
 * /Model/ProcName
 * model  item
 * @code
 *
 * Unqualified process item:<br>
 * @code 3
 * Process:ProcName or<br>
 * type    item<br>
 * ProcName<br>
 * item
 * @code
 *
 * Fully qualified process socket parameter:<br>
 * @code 3
 * /Model/Process:ProcName.Socket:SocketName.Param:ParamName or<br>
 * (model)(type)  (item)   (objectPath)<br>
 * /Model/ProcName.SocketName.ParamName<br>
 * (model)(item)   (objectPath)
 * @code
 *
 * Unqualified process socket parameter:<br>
 * @code 3
 * Process:ProcName.Socket:SocketName.Param:ParamName or<br>
 * (type)  (item)   (objectPath)<br>
 * ProcName.SocketName.ParamName<br>
 * (item)   (objectPath)
 * @code
 *
 * Fully qualified file:<br>
 * @code 3
 * /Model/File:SubDirName1/SubDirName2/FileName<br>
 * (model)(objectPath)
 * @code
 *
 * Unqualified file:<br>
 * @code 3
 * File:SubDirName1/SubDirName2/FileName<br>
 *      (objectPath)
 * @code
 *
 * @author Heiko Erhardt
 */
public final class ModelQualifier
	implements Serializable
{
	//////////////////////////////////////////////////
	// @@ Delimiters
	//////////////////////////////////////////////////

	/** Model delimiter */
	public static final String PATH_DELIMITER = "/";

	/** Model delimiter */
	public static final char PATH_DELIMITER_CHAR = '/';

	/** Type delimiter */
	public static final String TYPE_DELIMITER = ":";

	/** Type delimiter */
	public static final char TYPE_DELIMITER_CHAR = ':';

	/** Object delimiter */
	public static String OBJECT_DELIMITER = ".";

	/** Object delimiter */
	public static char OBJECT_DELIMITER_CHAR = '.';

	/** String that contains all delimiters (used for validity checking of qualifiers) */
	public static final String ALL_DELIMITERS = "/:.;";

	/////////////////////////////////////////////////////////////////////////
	// @@ Compare constants
	/////////////////////////////////////////////////////////////////////////

	/** Compare models. */
	public static final int COMPARE_MODEL = 1 << 0;

	/** Compare items. */
	public static final int COMPARE_ITEM = 1 << 1;

	/** Compare types. */
	public static final int COMPARE_TYPE = 1 << 2;

	/** Compare subspecs. */
	public static final int COMPARE_SUBSPEC = 1 << 3;

	/** Compare filepaths. */
	public static final int COMPARE_PATH = 1 << 4;

	/** Compare model and item. */
	public static final int COMPARE_ITEM_FULL = COMPARE_ITEM | COMPARE_MODEL;

	/** Compare untyped (ignoring type). */
	public static final int COMPARE_UNTYPED = COMPARE_ITEM_FULL | COMPARE_PATH | COMPARE_SUBSPEC;

	/** Compare completly. */
	public static final int COMPARE_ALL = COMPARE_UNTYPED | COMPARE_TYPE;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Model */
	private String model;

	/** Item type */
	private String itemType;

	/** Item */
	private String item;

	/** Sub path */
	private String objectPath;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public ModelQualifier()
	{
	}

	/**
	 * Copy constructor.
	 * @param that qualifier descriptor to copy
	 */
	public ModelQualifier(ModelQualifier that)
	{
		model = that.model;
		itemType = that.itemType;
		item = that.item;
		objectPath = that.objectPath;
	}

	/**
	 * Value constructor, parsing a qualifier descriptor string representation
	 *
	 * @param s qualifier descriptor string to parse
	 */
	public ModelQualifier(String s)
	{
		parseString(s);
	}

	/**
	 * Value constructor.
	 *
	 * @param model Model
	 * @param item Item
	 * @param itemType Item type
	 * @param objectPath Sub qualifier
	 */
	public ModelQualifier(String model, String item, String itemType, String objectPath)
	{
		this.model = model;
		this.item = item;
		this.itemType = itemType;
		this.objectPath = objectPath;
	}

	/**
	 * Value constructor.
	 *
	 * @param model Model
	 * @param item Item
	 * @param itemType Item type
	 */
	public ModelQualifier(String model, String item, String itemType)
	{
		this(model, item, itemType, null);
	}

	/**
	 * Value constructor.
	 *
	 * @param model Model object to reference
	 */
	public ModelQualifier(Model model)
	{
		if (model != null)
		{
			this.model = model.getName();
		}
	}

	/**
	 * Value constructor.
	 *
	 * @param item Item object to reference
	 */
	public ModelQualifier(Item item)
	{
		if (item != null)
		{
			Model itemModel = item.getModel();
			if (itemModel != null)
				this.model = itemModel.getName();
			this.item = item.getName();
			this.itemType = item.getItemType();
		}
	}

	/**
	 * Value constructor.
	 *
	 * @param item Item object to reference
	 * @param objectPath Sub qualifier
	 */
	public ModelQualifier(Item item, String objectPath)
	{
		this(item);
		this.objectPath = objectPath;
	}

	/**
	 * Resets all properties of the object.
	 */
	public void reset()
	{
		model = null;
		item = null;
		itemType = null;
		objectPath = null;
	}

	//////////////////////////////////////////////////
	// @@ String representation
	//////////////////////////////////////////////////

	/**
	 * Initializes the properties of this qualifier descriptor from a model qualifier string representation.
	 *
	 * @param s The string representation (i. e. fully qualified name) to parse
	 * @throws ModelException If the string does not denote a valid qualifier
	 */
	public void parseString(String s)
	{
		reset();

		if (s == null || s.length() == 0)
		{
			// Nothing to parse
			return;
		}

		int currentIndex = 0;

		if (s.charAt(currentIndex) == ModelQualifier.PATH_DELIMITER_CHAR)
		{
			int pathDelimIndex = s.indexOf(ModelQualifier.PATH_DELIMITER_CHAR, 1); // '/'
			if (pathDelimIndex > 0)
			{
				model = s.substring(currentIndex + 1, pathDelimIndex);
				currentIndex = pathDelimIndex + 1;

				if (s.indexOf(ModelQualifier.PATH_DELIMITER_CHAR, currentIndex) > 0)
				{
					throw new ModelException("InvalidQualifier", "Qualifier '" + s + "' contains more than two path delimiters.");
				}
			}
			else
			{
				model = s.substring(currentIndex + 1);
				currentIndex = s.length();
			}
		}

		int typeSepIndex = s.indexOf(ModelQualifier.TYPE_DELIMITER_CHAR, currentIndex); // ':'
		if (typeSepIndex >= 0)
		{
			itemType = s.substring(currentIndex, typeSepIndex);
			currentIndex = typeSepIndex + 1;
		}

		int objectPathIndex = s.indexOf(ModelQualifier.OBJECT_DELIMITER_CHAR, currentIndex); // '.'

		if (currentIndex != objectPathIndex)
		{
			if (currentIndex < s.length() - 1)
			{
				if (objectPathIndex > 0)
				{
					item = s.substring(currentIndex, objectPathIndex);
					currentIndex = objectPathIndex;
				}
				else
				{
					item = s.substring(currentIndex);
					currentIndex = s.length();
				}
			}
		}

		if (StringUtil.safeCharAt(s, currentIndex) == ModelQualifier.OBJECT_DELIMITER_CHAR)
		{
			objectPath = s.substring(currentIndex + 1);
		}

		model = StringUtil.trimNull(model);
		item = StringUtil.trimNull(item);
		itemType = StringUtil.trimNull(itemType);
		objectPath = StringUtil.trimNull(objectPath);
	}

	/**
	 * Returns the qualifier descriptor's string representation.
	 * @nowarn
	 */
	public String toString()
	{
		return toString(false);
	}

	/**
	 * Returns the qualifier descriptor String representation with type references.
	 * @nowarn
	 */
	public String toTypedString()
	{
		return toString(true);
	}

	/**
	 * Returns the qualifier descriptor String representation without any type references.
	 * @nowarn
	 */
	public String toUntypedString()
	{
		return toString(false);
	}

	private String toString(boolean printType)
	{
		StringBuffer sb = new StringBuffer();

		if (model != null)
		{
			// Omit model name for system types in their regular form
			sb.append(ModelQualifier.PATH_DELIMITER_CHAR);
			sb.append(model);
			if (itemType != null || item != null || objectPath != null)
			{
				sb.append(ModelQualifier.PATH_DELIMITER_CHAR);
			}
		}

		if (printType && itemType != null)
		{
			sb.append(itemType);
			sb.append(ModelQualifier.TYPE_DELIMITER_CHAR);
		}

		if (item != null)
		{
			sb.append(item);
		}

		if (objectPath != null)
		{
			sb.append(ModelQualifier.OBJECT_DELIMITER_CHAR);
			sb.append(objectPath);
		}

		return sb.toString();
	}

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/**
	 * Returns the hashcode of this qualifier descriptor.
	 * @return The hash code is taken from the toString representation of this object
	 */
	public int hashCode()
	{
		return toString().hashCode();
	}

	/**
	 * Checks two qualifier descriptor for equality.
	 * Model qualifiers are considered to be equal only if all members match.
	 * For a wider comparison, use the {@link #matches(Object)} method.
	 * @nowarn
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof ModelQualifier)
		{
			ModelQualifier other = (ModelQualifier) obj;

			if (!CommonUtil.equalsNull(item, other.item))
				return false;
			if (!CommonUtil.equalsNull(itemType, other.itemType))
				return false;
			if (!CommonUtil.equalsNull(objectPath, other.objectPath))
				return false;
			if (!CommonUtil.equalsNull(model, other.model))
				return false;

			return true;
		}
		return false;
	}

	/**
	 * Checks if two qualifier descriptors match.
	 * The qualifier descriptors are considered matching if:<br>
	 * - They are equal<br>
	 * - One or both itemtypes are null and the rest is equal
	 * @nowarn
	 */
	public boolean matches(Object obj)
	{
		if (!matches(obj, COMPARE_UNTYPED))
		{
			return false;
		}

		ModelQualifier other = (ModelQualifier) obj;

		if (itemType != null && other.itemType != null && !itemType.equals(other.itemType))
			return false;

		return true;
	}

	/**
	 * Checks whether the given object is a qualifier descriptor itself and matches this instance on
	 * the given criteria.
	 *
	 * @param o the object for which matching is tested
	 * @param flags flags determing which parts of the qualifier descriptor should be matched
	 * @nowarn
	 */
	public boolean matches(Object o, int flags)
	{
		if (!(o instanceof ModelQualifier))
		{
			return false;
		}

		ModelQualifier other = (ModelQualifier) o;

		if ((flags & COMPARE_ITEM) != 0 && !CommonUtil.equalsNull(item, other.item))
			return false;
		if ((flags & COMPARE_MODEL) != 0 && !CommonUtil.equalsNull(model, other.model))
			return false;
		if ((flags & COMPARE_TYPE) != 0 && !CommonUtil.equalsNull(itemType, other.itemType))
			return false;
		if ((flags & COMPARE_SUBSPEC) != 0 && !CommonUtil.equalsNull(objectPath, other.objectPath))
			return false;

		return true;
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the model.
	 * @nowarn
	 */
	public String getModel()
	{
		return model;
	}

	/**
	 * Sets the model.
	 * @nowarn
	 */
	public void setModel(String model)
	{
		this.model = model;
	}

	/**
	 * Gets the item type.
	 * @nowarn
	 */
	public String getItemType()
	{
		return itemType;
	}

	/**
	 * Sets the item type.
	 * @nowarn
	 */
	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}

	/**
	 * Gets the item.
	 * @nowarn
	 */
	public String getItem()
	{
		return item;
	}

	/**
	 * Sets the item.
	 * @nowarn
	 */
	public void setItem(String item)
	{
		this.item = item;
	}

	/**
	 * Gets the object qualifier.
	 * @nowarn
	 */
	public String getObjectPath()
	{
		return objectPath;
	}

	/**
	 * Sets the object qualifier.
	 * @nowarn
	 */
	public void setObjectPath(String objectPath)
	{
		this.objectPath = objectPath;
	}

	//////////////////////////////////////////////////
	// @@ Static utility methods: Name building and splitting
	//////////////////////////////////////////////////

	/**
	 * Builds the qualifier for the given model.
	 *
	 * @param modelName Name of the model
	 * @return The reference
	 */
	public static ModelQualifier constructModelQualifier(String modelName)
	{
		return new ModelQualifier(normalizeModelName(modelName), null, null);
	}

	/**
	 * Checks if a reference is absolute.
	 *
	 * @param ref Unqualified or reference to the item
	 * @return
	 *		true	The string is a valid identifier or null<br>
	 *		false	The string contains at least one invalid character.
	 */
	public static boolean isAbsolute(String ref)
	{
		return ref != null && ref.charAt(0) == ModelQualifier.PATH_DELIMITER_CHAR;
	}

	/**
	 * Strips leading or trailing '/' from its argument.
	 *
	 * @param modelName Model name or null
	 * @return The normalized model name
	 */
	public static String normalizeModelName(String modelName)
	{
		if (modelName != null)
		{
			if (StringUtil.safeCharAt(modelName, 0) == ModelQualifier.PATH_DELIMITER_CHAR)
				modelName = modelName.substring(1);
			int l = modelName.length();
			if (StringUtil.safeCharAt(modelName, l - 1) == ModelQualifier.PATH_DELIMITER_CHAR)
				modelName = modelName.substring(0, l - 1);
		}
		return modelName;
	}

	/**
	 * Checks if a string is a valid qualifier part.
	 * The string must not contain one of the characters '.', '/', ':', ';'.
	 *
	 * @param ident String to parse
	 * @return
	 *		true	The string is a valid identifier or null<br>
	 *		false	The string contains at least one invalid character.
	 */
	public static boolean isValidIdentifier(String ident)
	{
		if (ident != null)
		{
			int n = ident.length();
			for (int i = 0; i < n; ++i)
			{
				char c = ident.charAt(i);

				if (ModelQualifier.ALL_DELIMITERS.indexOf(c) >= 0)
				{
					return false;
				}
			}
		}

		return true;
	}
}
