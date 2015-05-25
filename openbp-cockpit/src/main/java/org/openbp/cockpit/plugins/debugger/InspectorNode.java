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
package org.openbp.cockpit.plugins.debugger;

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;

import org.openbp.common.logger.LogUtil;
import org.openbp.core.OpenBPException;
import org.openbp.core.engine.ExpressionConstants;
import org.openbp.core.engine.debugger.DebuggerService;
import org.openbp.core.engine.debugger.ObjectMemberInfo;
import org.openbp.swing.components.treetable.DefaultTreeTableNode;

/**
 * The tree table node of the context inspector.
 *
 * @author Andreas Putz
 */
public class InspectorNode extends DefaultTreeTableNode
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Node status: Unknown */
	private static final int NODESTATUS_UNKNOWN = 0;

	/** Node status: Parent node */
	private static final int NODESTATUS_PARENT = 1;

	/** Node status: Leaf node */
	private static final int NODESTATUS_LEAF = 2;

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** Context path */
	private String contextPath;

	/** Expression */
	private String expression;

	/** Object member info */
	private ObjectMemberInfo info;

	/** The preferred size for this node */
	private Dimension preferredSize;

	/** Node status (see the constants above) */
	private int nodeStatus = NODESTATUS_UNKNOWN;

	/** Flag if value detail info was retrieved */
	private boolean hasDetailInfo;

	/** The clientId under which we are connected to the debugger service */
	private static String clientId;

	/** The DebuggerService used to connect to the server */
	private static DebuggerService debuggerService;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	protected InspectorNode()
	{
	}

	/**
	 * Root node constructor.
	 *
	 * @param clientId The clientId under which we are connected to the debugger service
	 * @param debuggerService The DebuggerService used to connect to the server
	 */
	protected InspectorNode(String clientId, DebuggerService debuggerService)
	{
		InspectorNode.clientId = clientId;
		InspectorNode.debuggerService = debuggerService;

		loadChildren();
	}

	/**
	 * Constructor.
	 *
	 * @param info Info object
	 * @param contextPath Context path
	 * @param expression Expression
	 * @param parent Parent model node or null, if the parent is the root node
	 */
	private InspectorNode(ObjectMemberInfo info, String contextPath, String expression, InspectorNode parent)
	{
		this.info = info;
		this.contextPath = contextPath;
		this.expression = expression;
		this.parent = parent;

		if (!info.isParentMember())
		{
			nodeStatus = NODESTATUS_LEAF;
		}
	}

	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Gets the object member info.
	 * @nowarn
	 */
	public ObjectMemberInfo getInfo()
	{
		return info;
	}

	/**
	 * Gets the context path.
	 * @nowarn
	 */
	public String getContextPath()
	{
		return contextPath;
	}

	/**
	 * Gets the expression.
	 * @nowarn
	 */
	public String getExpression()
	{
		return expression;
	}

	//////////////////////////////////////////////////
	// @@ Tree table node implementation
	//////////////////////////////////////////////////

	/**
	 * @see org.openbp.swing.components.treetable.TreeTableNode#getNodeText()
	 */
	public String getNodeText()
	{
		if (info == null)
			return null;

		return info.getKey();
	}

	/**
	 * @see org.openbp.swing.components.treetable.TreeTableNode#getPreferredSize()
	 */
	public Dimension getPreferredSize()
	{
		if (preferredSize == null)
			preferredSize = new Dimension(0, 0);

		return preferredSize;
	}

	/**
	 * @see org.openbp.swing.components.treetable.TreeTableNode#getColumnValue(int)
	 */
	public Object getColumnValue(int columnIndex)
	{
		if (columnIndex == 1)
		{
			if (info == null)
				return null;

			// Make sure that a tool tip is attached to the created label
			String [] multiText = new String [1];
			multiText [0] = info.getType();
			return multiText;
		}

		if (columnIndex == 2)
		{
			if (info == null)
				return null;

			// Make sure that a tool tip is attached to the created label
			String [] multiText = new String [1];
			multiText [0] = info.getToStringValue();
			return multiText;
		}

		return super.getColumnValue(columnIndex);
	}

	/**
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf()
	{
		return nodeStatus == NODESTATUS_LEAF;
	}

	//////////////////////////////////////////////////
	// @@ Helpers
	//////////////////////////////////////////////////

	/**
	 * Loads the children, if these are not loaded before.
	 */
	void loadChildren()
	{
		if (nodeStatus != NODESTATUS_UNKNOWN)
		{
			// Already loaded
			return;
		}

		if (debuggerService == null)
			return;

		// We don't have to look for children if this is an etc member.
		if (info != null && "...".equals(info.getKey()))
		{
			return;
		}

		List list = null;
		try
		{
			// Get the members of this object
			list = debuggerService.getObjectMembers(clientId, contextPath, expression);
		}
		catch (OpenBPException e)
		{
			LogUtil.error(getClass(), "Error while connecting to the debugger service.", e);
		}

		if (list == null || list.size() == 0)
		{
			// No members, this is a leaf node
			nodeStatus = NODESTATUS_LEAF;
			return;
		}

		Vector childrenList = new Vector();

		int n = list.size();
		for (int i = 0; i < n; ++i)
		{
			ObjectMemberInfo childInfo = (ObjectMemberInfo) list.get(i);
			String key = childInfo.getKey();

			String cp = null;
			String ex = null;

			if (contextPath == null)
			{
				// For top-level elements, the context path is the returned key value
				cp = key;
			}
			else
			{
				// For sub elements, the context path stays the same
				cp = contextPath;

				// Append the key after the expression of the parent
				if (expression != null)
				{
					// Check, whether key indicates an array index.
					if (key.startsWith("["))
					{
						ex = expression + key;
					}
					else
					{
						ex = expression + ExpressionConstants.MEMBER_OPERATOR_CHAR + key;
					}
				}
				else
				{
					ex = key;
				}
			}

			InspectorNode child = new InspectorNode(childInfo, cp, ex, this);
			childrenList.add(child);
		}

		children = childrenList;
		nodeStatus = NODESTATUS_PARENT;
	}

	/**
	 * Loads the children, if these are not loaded before.
	 */
	void loadValueDetails()
	{
		if (hasDetailInfo)
			return;
		hasDetailInfo = true;

		if (debuggerService == null)
			return;

		try
		{
			// Get the members of this object
			ObjectMemberInfo valueInfo = debuggerService.getObjectValue(clientId, contextPath, expression);
			if (info != null && valueInfo != null)
			{
				info.setToStringValue(valueInfo.getToStringValue());
			}
		}
		catch (OpenBPException e)
		{
			LogUtil.error(getClass(), "Error while connecting to the debugger service.", e);
		}
	}
}
