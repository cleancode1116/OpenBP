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
package org.openbp.core.model.item.process;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.openbp.core.engine.ExpressionConstants;
import org.openbp.core.model.ModelException;
import org.openbp.core.model.ModelObject;
import org.openbp.core.model.ModelQualifier;
import org.openbp.core.model.item.ItemTypes;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataMember;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * Standard implementation of a data link.
 *
 * @author Heiko Erhardt
 */
public class DataLinkImpl extends ProcessObjectImpl
	implements DataLink
{
	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Source node parameter name ("node.socket.parameter", may not be null) */
	private String sourceParamName;

	/** Source data member access path (may be null) */
	private String sourceMemberPath;

	/** Target node parameter name ("node.socket.parameter", may not be null) */
	private String targetParamName;

	/** Target data member access path (may be null) */
	private String targetMemberPath;

	/** Flag if the source object should be cloned */
	private boolean cloningSource;

	/** Geometry information (required by the Modeler) */
	private String geometry;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Source node parameter (may not be null) */
	private transient Param sourceParam;

	/** Target node parameter (may not be null) */
	private transient Param targetParam;

	/** Process the link belongs to (may not be null) */
	private transient ProcessItem process;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public DataLinkImpl()
	{
	}

	/**
	 * Copies the values of the source object to this object.
	 *
	 * @param source The source object. Must be of the same type as this object.
	 * @param copyMode Determines if a deep copy, a first level copy or a shallow copy is to be
	 * performed. See the constants of the org.openbp.common.generic.description.Copyable class.
	 * @throws CloneNotSupportedException If the cloning of one of the contained objects failed
	 */
	public void copyFrom(Object source, int copyMode)
		throws CloneNotSupportedException
	{
		if (source == this)
			return;
		super.copyFrom(source, copyMode);

		DataLinkImpl src = (DataLinkImpl) source;

		sourceParamName = src.sourceParamName;
		sourceMemberPath = src.sourceMemberPath;
		targetParamName = src.targetParamName;
		targetMemberPath = src.targetMemberPath;
		cloningSource = src.cloningSource;
		geometry = src.geometry;

		sourceParam = src.sourceParam;
		targetParam = src.targetParam;
		process = src.process;
	}

	/**
	 * Gets the reference to the object.
	 * @return The qualified name
	 */
	public ModelQualifier getQualifier()
	{
		return new ModelQualifier(getProcess(), getName());
	}

	//////////////////////////////////////////////////
	// @@ ModelObject overrides
	//////////////////////////////////////////////////

	/**
	 * Gets text that can be used to display this object.
	 *
	 * @nowarn
	 */
	public String getDisplayText()
	{
		String text = getDisplayName();
		if (text != null)
			return text;

		DataTypeItem type = getDominantParameter();
		if (type != null)
		{
			text = type.getDisplayText();
		}

		return text != null ? text : "";
	}

	/**
	 * Gets text that describes the object.
	 * This can be the regular description (getDescription method) of the object
	 * or the description of an underlying object.
	 *
	 * @return The description text or null if there is no description
	 */
	public String getDescriptionText()
	{
		String text = getDescription();

		if (text == null)
		{
			text = determineLinkText();

			DataTypeItem sourceType = determineType(sourceParam, sourceMemberPath);
			DataTypeItem targetType = determineType(targetParam, targetMemberPath);
			DataTypeItem type = getDominantParameter(sourceType, targetType);

			if (type != null)
			{
				String descr = type.getDescriptionText();
				if (descr != null)
				{
					if (text != null)
					{
						text += "\n" + descr;
					}
					else
					{
						text = descr;
					}
				}
			}
		}

		return text;
	}

	/**
	 * Determines the text that describes the parameter link.
	 *
	 * @return The text ("link1.member1 - link2.member2") or null if the link
	 * isn't connected to anything
	 */
	private String determineLinkText()
	{
		String sourceName = null;
		if (sourceParam != null)
		{
			sourceName = sourceParam.getName();
			if (sourceMemberPath != null)
			{
				if (sourceMemberPath.startsWith(ExpressionConstants.MEMBER_OPERATOR))
				{
					sourceName += sourceMemberPath;
				}
				else if (sourceMemberPath.startsWith(ExpressionConstants.REFERENCE_KEY_OPERATOR))
				{
					sourceName += sourceMemberPath;
				}
				else
				{
					sourceName += ExpressionConstants.MEMBER_OPERATOR + sourceMemberPath;
				}
			}
		}

		String targetName = null;
		if (targetParam != null)
		{
			targetName = targetParam.getName();
			if (targetMemberPath != null)
			{
				if (targetMemberPath.startsWith(ExpressionConstants.MEMBER_OPERATOR))
				{
					targetName += targetMemberPath;
				}
				else if (targetMemberPath.startsWith(ExpressionConstants.REFERENCE_KEY_OPERATOR))
				{
					targetName += targetMemberPath;
				}
				else
				{
					targetName += ExpressionConstants.MEMBER_OPERATOR + targetMemberPath;
				}
			}
		}

		String text = null;
		if (sourceName != null && targetName != null)
		{
			if (sourceName.equals(targetName))
			{
				text = sourceName;
			}
			else
			{
				text = sourceName + " => " + targetName;
			}
		}
		else if (sourceName != null)
		{
			text = sourceName;
		}
		else if (targetName != null)
		{
			text = targetName;
		}
		return text;
	}

	/**
	 * Gets the dominant parameter of this link.
	 * This is the parameter that is most suitable for retrieving a display text.
	 *
	 * @return The parameter or null
	 */
	private DataTypeItem getDominantParameter()
	{
		DataTypeItem sourceType = determineType(sourceParam, sourceMemberPath);
		DataTypeItem targetType = determineType(targetParam, targetMemberPath);

		return getDominantParameter(sourceType, targetType);
	}

	/**
	 * Determines the more dominant one of two parameters.
	 *
	 * @param p1 First parameter
	 * @param p2 Second parameter
	 * @return The parameter or null
	 */
	private static DataTypeItem getDominantParameter(DataTypeItem p1, DataTypeItem p2)
	{
		if (p1 == p2)
			return p1;
		if (p1 != null)
			return p1;
		if (p2 != null)
			return p2;

		if (p1 != null && p1.isBaseTypeOf(p2))
		{
			// Use the more specialized type
			return p2;
		}

		return p1;
	}

	//////////////////////////////////////////////////
	// @@ ProcessObject implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the process the object belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess()
	{
		return process;
	}

	/**
	 * Sets the process the object belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process)
	{
		this.process = process;
	}

	/**
	 * Gets the partially qualified name of the object relative to the process.
	 * @nowarn
	 */
	public String getProcessRelativeName()
	{
		return getName();
	}

	/**
	 * Gets the container object (i. e. the parent) of this object.
	 *
	 * @return The container object or null if this object doesn't have a container.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public ModelObject getContainer()
	{
		return process;
	}

	/**
	 * Gets an iterator of the children of the container this object belongs to.
	 * This can be used to check on name clashes between objects of this type.
	 * By default, the method returns null.
	 *
	 * @return The iterator if this object is part of a collection or a map.
	 * If the parent of this object references only a single object of this type,
	 * the method returns null.
	 */
	public Iterator getContainerIterator()
	{
		return process.getDataLinks();
	}

	//////////////////////////////////////////////////
	// @@ Pre save/post load processing and validation
	//////////////////////////////////////////////////

	/**
	 * @copy ModelObject.maintainReferences
	 */
	public void maintainReferences(int flag)
	{
		super.maintainReferences(flag);

		if (process != null)
		{
			if ((flag & SYNC_LOCAL_REFNAMES) != 0)
			{
				sourceParamName = sourceParam != null ? sourceParam.getProcessRelativeName() : null;
				targetParamName = targetParam != null ? targetParam.getProcessRelativeName() : null;
			}

			if ((flag & RESOLVE_LOCAL_REFS) != 0)
			{
				// Link the source and target parameters
				sourceParam = process.getParamByName(sourceParamName);
				if (sourceParam == null)
				{
					// Report if not already done by validate()
					if (sourceParamName != null)
					{
						getModelMgr().getMsgContainer().addMsg(this, "Source node parameter $0 not found.", new Object [] { sourceParamName });
					}
				}
				else
				{
					sourceParam.addDataLink(this);
				}

				targetParam = process.getParamByName(targetParamName);
				if (targetParam == null)
				{
					// Report if not already done by validate()
					if (targetParamName != null)
					{
						getModelMgr().getMsgContainer().addMsg(this, "Target node parameter $0 not found.", new Object [] { targetParamName });
					}
				}
				else
				{
					targetParam.addDataLink(this);
				}

				if (sourceParam == null || targetParam == null)
				{
					// Invalid link - remove from the process
					process.removeDataLink(this);
				}
			}
		}
	}

	/**
	 * @copy ModelObject.validate
	 */
	public boolean validate(int flag)
	{
		// Check for an object name first
		boolean success = super.validate(flag);

		if (sourceParamName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No source node parameter name specified.");
			success = false;
		}

		if (targetParamName == null)
		{
			getModelMgr().getMsgContainer().addMsg(this, "No target node parameter name specified.");
			success = false;
		}

		return success;
	}

	//////////////////////////////////////////////////
	// @@ Linking to parameters
	//////////////////////////////////////////////////

	/**
	 * Links the connection to a source and a target parameter.
	 *
	 * @param sourceParam Source node parameter (may not be null)
	 * @param targetParam Target node parameter (may not be null)
	 */
	public void link(Param sourceParam, Param targetParam)
	{
		this.sourceParam = sourceParam;
		this.targetParam = targetParam;

		// Add the link to the parameter's link list
		sourceParam.addDataLink(this);
		targetParam.addDataLink(this);
	}

	/**
	 * Unlinks the connection from the source and the target parameters.
	 */
	public void unlink()
	{
		// Remove the link from the parameter's link list
		if (sourceParam != null)
			sourceParam.removeDataLink(this);
		if (targetParam != null)
			targetParam.removeDataLink(this);

		sourceParam = null;
		targetParam = null;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the source node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public String getSourceParamName()
	{
		return sourceParamName;
	}

	/**
	 * Sets the source node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public void setSourceParamName(String sourceParamName)
	{
		this.sourceParamName = sourceParamName;
	}

	/**
	 * Gets the target node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public String getTargetParamName()
	{
		return targetParamName;
	}

	/**
	 * Sets the target node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public void setTargetParamName(String targetParamName)
	{
		this.targetParamName = targetParamName;
	}

	/**
	 * Gets the source node parameter.
	 * @nowarn
	 */
	public Param getSourceParam()
	{
		return sourceParam;
	}

	/**
	 * Sets the source node parameter.
	 * @nowarn
	 */
	public void setSourceParam(Param sourceParam)
	{
		this.sourceParam = sourceParam;
	}

	/**
	 * Gets the source data member path (may be null).
	 * @nowarn
	 */
	public String getSourceMemberPath()
	{
		return sourceMemberPath;
	}

	/**
	 * Sets the source data member path (may be null).
	 * @nowarn
	 */
	public void setSourceMemberPath(String sourceMemberPath)
	{
		this.sourceMemberPath = sourceMemberPath;
	}

	/**
	 * Gets the target node parameter.
	 * @nowarn
	 */
	public Param getTargetParam()
	{
		return targetParam;
	}

	/**
	 * Sets the target node parameter.
	 * @nowarn
	 */
	public void setTargetParam(Param targetParam)
	{
		this.targetParam = targetParam;
	}

	/**
	 * Gets the target data member path (may be null).
	 * @nowarn
	 */
	public String getTargetMemberPath()
	{
		return targetMemberPath;
	}

	/**
	 * Sets the target data member path (may be null).
	 * @nowarn
	 */
	public void setTargetMemberPath(String targetMemberPath)
	{
		this.targetMemberPath = targetMemberPath;
	}

	/**
	 * Gets the flag if the source object should be cloned.
	 * @nowarn
	 */
	public boolean isCloningSource()
	{
		return cloningSource;
	}

	/**
	 * Determines if the flag if the source object should be cloned is set.
	 * Will be removed if Castor supports boolean defaults.
	 * @nowarn
	 */
	public boolean hasCloningSource()
	{
		return cloningSource;
	}

	/**
	 * Sets the flag if the source object should be cloned.
	 * @nowarn
	 */
	public void setCloningSource(boolean cloningSource)
	{
		this.cloningSource = cloningSource;
	}

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry()
	{
		return geometry;
	}

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry)
	{
		this.geometry = geometry;
	}

	//////////////////////////////////////////////////
	// @@ Utilities
	//////////////////////////////////////////////////

	/**
	 * Checks if a link can be established between the given parameters.
	 *
	 * @param sourceParam Source node parameter (may not be null)
	 * @param sourceMemberPath Source data member access path (may be null)
	 * @param targetParam Target node parameter (may not be null)
	 * @param targetMemberPath Target data member access path (may be null)
	 * @param flags Flags that determine the type of checks to be performed. Can be a combination of:
	 * {@link DataLink#LINK_OMIT_TYPE_CHECK}|{@link DataLink#LINK_AUTOCONVERSION}
	 * @return The return code determines if the link is possible and is one of the following constants:<br>
	 * {@link ControlLink#CANNOT_LINK}/
	 * (({@link DataLink#CAN_LINK}/{@link DataLink#CAST_LINK}/{@link DataLink#CONVERSION_LINK})|{@link ControlLink#REVERSE_LINK})
	 */
	public static int canLink(Param sourceParam, String sourceMemberPath, Param targetParam, String targetMemberPath, int flags)
	{
		int reverseFlag = 0;

		// Check if the direction of the link is correct
		if (sourceParam instanceof NodeParam && targetParam instanceof NodeParam)
		{
			boolean sourceEntry = ((NodeParam) sourceParam).getSocket().isEntrySocket();
			boolean targetEntry = ((NodeParam) targetParam).getSocket().isEntrySocket();

			if (sourceEntry == targetEntry)
			{
				// Cannot connect entry to entry or exit to exit
				return CANNOT_LINK;
			}

			if (sourceEntry)
			{
				// We must reverse the link
				reverseFlag = ControlLink.REVERSE_LINK;
			}
		}
		else if (sourceParam instanceof NodeParam)
		{
			if (((NodeParam) sourceParam).getSocket().isExitSocket())
			{
				// We must reverse the link
				reverseFlag = ControlLink.REVERSE_LINK;
			}
		}
		else if (targetParam instanceof NodeParam)
		{
			if (((NodeParam) targetParam).getSocket().isEntrySocket())
			{
				// We must reverse the link
				reverseFlag = ControlLink.REVERSE_LINK;
			}
		}

		if (reverseFlag != 0)
		{
			// Swap the types, so we can check if the reverse link is possible
			Param tmpParam = sourceParam;
			sourceParam = targetParam;
			targetParam = tmpParam;

			String tmpPath = sourceMemberPath;
			sourceMemberPath = targetMemberPath;
			targetMemberPath = tmpPath;
		}

		// Check if there already is such a link
		for (Iterator it = sourceParam.getDataLinks(); it.hasNext();)
		{
			DataLink link = (DataLink) it.next();
			if (link.getTargetParam() == targetParam)
			{
				// Link already exists
				return CANNOT_LINK;
			}
		}

		// Check parameter compatibility
		DataTypeItem sourceType = determineType(sourceParam, sourceMemberPath);
		DataTypeItem targetType = determineType(targetParam, targetMemberPath);

		if (sourceType == null || targetType == null)
		{
			// Probably invalid member path
			return CANNOT_LINK;
		}

		if ((flags & LINK_OMIT_TYPE_CHECK) != 0)
		{
			// Omit the type checking, so we assume we can link the parameters
			return CAN_LINK | reverseFlag;
		}

		if (sourceType == targetType)
		{
			return CAN_LINK | reverseFlag;
		}

		if (targetType.isBaseTypeOf(sourceType))
		{
			// Direct hierarchy can be linked without problems
			return CAN_LINK | reverseFlag;
		}

		if (sourceType.isBaseTypeOf(targetType))
		{
			// Inverse direct hierarchy can be linked using cast
			return CAST_LINK | reverseFlag;
		}

		if ((flags & LINK_AUTOCONVERSION) != 0)
		{
			// Try to apply auto conversion between parameters
			if (checkAutoConversion(sourceParam, sourceMemberPath, targetParam, targetMemberPath) != null)
			{
				// Linking possible by auto conversion between primitive type and complex type by id relation
				// or vice versa
				return CONVERSION_LINK | reverseFlag;
			}
		}

		// Incompatible
		return CANNOT_LINK;
	}

	/**
	 * Checks if an auto conversion should be applied in order to link the given parameters.
	 *
	 * @param sourceParam Source node parameter (may not be null)
	 * @param sourceMemberPath Source data member access path (may be null)
	 * @param targetParam Target node parameter (may not be null)
	 * @param targetMemberPath Target data member access path (may be null)
	 *
	 * @return The return value denotes the source member path that is required for the auto conversion.
	 * If no auto conversion is possible, null will be returned.
	 */
	public static String checkAutoConversion(Param sourceParam, String sourceMemberPath, Param targetParam, String targetMemberPath)
	{
		String ret = null;

		// Check parameter compatibility
		DataTypeItem sourceType = determineType(sourceParam, sourceMemberPath);
		DataTypeItem targetType = determineType(targetParam, targetMemberPath);

		// Data link autoconnector operation mode: Connect convertible types
		// One type must be a complex type (i. e. a bean), the other must be
		// a type that can be used as lookup key.
		if (targetType instanceof ComplexTypeItem)
		{
			// Check the target type for its id member
			DataMember idMember = ((ComplexTypeItem) targetType).getSingleIdMember();
			if (idMember != null)
			{
				if (idMember.getDataType() == sourceType)
				{
					// The type of the id member matches the source parameter type;
					// Generate a ">>TargetType" source member path
					String targetTypeRef = targetParam.getProcess().determineItemRef(targetType);
					ret = ExpressionConstants.REFERENCE_KEY_OPERATOR + targetTypeRef;
				}
			}
		}

		if (ret == null && sourceType instanceof ComplexTypeItem)
		{
			// Check the source type for its id member
			DataMember idMember = ((ComplexTypeItem) sourceType).getSingleIdMember();
			if (idMember != null)
			{
				if (idMember.getDataType() == targetType)
				{
					// The type of the id member matches the target parameter type;
					// Generate a ".IdMember" source member path
					ret = idMember.getName();
				}
			}
		}

		if (ret != null && sourceMemberPath != null)
		{
			// Append to the existing source member path
			if (ret.startsWith(ExpressionConstants.REFERENCE_KEY_OPERATOR))
			{
				ret = sourceMemberPath + ret;
			}
			else
			{
				ret = sourceMemberPath + ExpressionConstants.MEMBER_OPERATOR + ret;
			}
		}

		return ret;
	}

	private static final String TOKENS = "" + ExpressionConstants.MEMBER_OPERATOR_CHAR + ExpressionConstants.REFERENCE_KEY_OPERATOR_CHAR;

	private static final String TOK_MEMBER = "" + ExpressionConstants.MEMBER_OPERATOR_CHAR;

	private static final String TOK_REFERENCE = "" + ExpressionConstants.REFERENCE_KEY_OPERATOR_CHAR;

	/**
	 * Determines the type specified by a parameter and its member path.
	 *
	 * @param param Parmeter or null
	 * @param memberPath Member path or null
	 * @return The type or null on error (usually a syntax error in the member path expression
	 */
	public static DataTypeItem determineType(Param param, String memberPath)
	{
		if (param == null)
			return null;

		DataTypeItem type = param.getDataType();
		if (memberPath == null)
		{
			// No member path, use
			return type;
		}

		// Determine the target type using the data member path.
		// Use '.' and '>' operators as token (and make the string tokenizer return the tokens!)
		StringTokenizer st = new StringTokenizer(memberPath, TOKENS, true);
		while (st.hasMoreTokens())
		{
			if (type == null)
				return null;

			String t = st.nextToken();
			if (t.equals(TOK_MEMBER))
			{
				// Simply eat this token since member name are the default tokens
				continue;
			}

			if (t.equals(TOK_REFERENCE))
			{
				// We expect '>>' operator
				if (!st.hasMoreTokens())
					return null;

				t = st.nextToken();
				if (!t.equals(TOK_REFERENCE))
					return null;

				// Now we expect a data type name
				if (!st.hasMoreTokens())
					return null;

				String typeName = st.nextToken();
				try
				{
					type = (DataTypeItem) param.getProcess().resolveItemRef(typeName, ItemTypes.TYPE);
				}
				catch (ModelException e)
				{
					// Silently ignore, just return null
					return null;
				}

				continue;
			}

			// Member operator
			if (type.isSimpleType())
			{
				// Sorry...
				return null;
			}

			String memberName = t;
			DataMember member = ((ComplexTypeItem) type).getMember(memberName);
			if (member == null)
				return null;

			type = member.getDataType();
		}

		return type;
	}
}
