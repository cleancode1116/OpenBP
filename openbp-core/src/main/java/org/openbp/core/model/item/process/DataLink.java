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

/**
 * A data link connects an output parameter of a node to an input parameter socket of
 * (presumably another) node.
 * This link resembles the flow of data in a process.
 * Data links will be present in both flow-controlled and Petri net based processes.
 *
 * @author Heiko Erhardt
 */
public interface DataLink
	extends ProcessObject
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/**
	 * Return value of {@link DataLinkImpl#canLink}:
	 * The parameters are not compatible (i. e. an Object cannot be mapped to a simple type)
	 * or there already is such a link.
	 */
	public static final int CANNOT_LINK = 0;

	/**
	 * Return value of {@link DataLinkImpl#canLink}:
	 * The link can be established (i. e. the types are equal or the source type extends the
	 * target type)
	 */
	public static final int CAN_LINK = 1;

	/**
	 * Return value of {@link DataLinkImpl#canLink}:
	 * The link can be established, but involves an upcast (i. e. an Object source parameter
	 * shall be linked to a concrete class implementation)
	 */
	public static final int CAST_LINK = 2;

	/**
	 * Return value of {@link DataLinkImpl#canLink}:
	 * The link can be established, but involves an auto-conversion
	 * (i. e. a ">>Type" source member specification).
	 */
	public static final int CONVERSION_LINK = 3;

	/**
	 * Return value of {@link DataLinkImpl#canLink}:
	 * The link can be established if it is reversed.
	 */
	static final int REVERSE_LINK = (1 << 8);

	/** Flag value for {@link DataLinkImpl#canLink}: Omit the type checking. */
	public static final int LINK_OMIT_TYPE_CHECK = (1 << 0);

	/** Flag value for {@link DataLinkImpl#canLink}: Omit the type checking. */
	public static final int LINK_AUTOCONVERSION = (1 << 1);

	//////////////////////////////////////////////////
	// @@ Linking to parameters
	//////////////////////////////////////////////////

	/**
	 * Links the connection to a source and a target parameter.
	 *
	 * @param sourceParam Source node parameter (may not be null)
	 * @param targetParam Target node parameter (may not be null)
	 */
	public void link(Param sourceParam, Param targetParam);

	/**
	 * Unlinks the connection from the source and the target parameters.
	 */
	public void unlink();

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the source node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public String getSourceParamName();

	/**
	 * Sets the source node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public void setSourceParamName(String sourceParamName);

	/**
	 * Gets the target node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public String getTargetParamName();

	/**
	 * Sets the target node parameter name ("node.socket.parameter").
	 * @nowarn
	 */
	public void setTargetParamName(String targetParamName);

	/**
	 * Gets the source node parameter.
	 * @nowarn
	 */
	public Param getSourceParam();

	/**
	 * Sets the source node parameter.
	 * @nowarn
	 */
	public void setSourceParam(Param sourceParam);

	/**
	 * Gets the source data member path (may be null).
	 * @nowarn
	 */
	public String getSourceMemberPath();

	/**
	 * Sets the source data member path (may be null).
	 * @nowarn
	 */
	public void setSourceMemberPath(String sourceMemberPath);

	/**
	 * Gets the target node parameter.
	 * @nowarn
	 */
	public Param getTargetParam();

	/**
	 * Sets the target node parameter.
	 * @nowarn
	 */
	public void setTargetParam(Param targetParam);

	/**
	 * Gets the target data member path (may be null).
	 * @nowarn
	 */
	public String getTargetMemberPath();

	/**
	 * Sets the target data member path (may be null).
	 * @nowarn
	 */
	public void setTargetMemberPath(String targetMemberPath);

	/**
	 * Gets the flag if the source object should be cloned.
	 * @nowarn
	 */
	public boolean isCloningSource();

	/**
	 * Sets the flag if the source object should be cloned.
	 * @nowarn
	 */
	public void setCloningSource(boolean cloningSource);

	/**
	 * Gets the process the node belongs to.
	 * @nowarn
	 */
	public ProcessItem getProcess();

	/**
	 * Sets the process the node belongs to.
	 * @nowarn
	 */
	public void setProcess(ProcessItem process);

	/**
	 * Gets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public String getGeometry();

	/**
	 * Sets the geometry information.
	 * This information is created by the Modeler.
	 * @nowarn
	 */
	public void setGeometry(String geometry);
}
