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
package org.openbp.core;

import org.openbp.core.model.Association;
import org.openbp.core.model.Model;
import org.openbp.core.model.item.Item;
import org.openbp.core.model.item.activity.ActivityItem;
import org.openbp.core.model.item.process.Node;
import org.openbp.core.model.item.process.ProcessItem;
import org.openbp.core.model.item.type.ComplexTypeItem;
import org.openbp.core.model.item.type.DataTypeItem;

/**
 * MIME types for OpenBP objects.
 * For an explanation of object associations, see the {@link Association} class.
 *
 * @author Heiko Erhardt
 */
public final class MimeTypes
{
	//////////////////////////////////////////////////
	// @@ OpenBP object types
	//////////////////////////////////////////////////

	/** Generic OpenBP object (associated object: A java.lang.Object that can be edited) */
	public static final String OBJECT = "application/x-openbp-object";

	/** Model (associated object:  {@link Model}) */
	public static final String MODEL = "application/x-openbp-model";

	/** Generic item (associated object: {@link Item}) */
	public static final String ITEM = "application/x-openbp-item";

	/** Process item (associated object: {@link ProcessItem}) */
	public static final String PROCESS_ITEM = "application/x-openbp-process";

	/** Activity item (associated object: {@link ActivityItem}) */
	public static final String ACTIVITY_ITEM = "application/x-openbp-activity";

	/** Data type item (associated object: {@link DataTypeItem}) */
	public static final String DATA_TYPE_ITEM = "application/x-openbp-type";

	/** Complex data type item (associated object: {@link ComplexTypeItem}) */
	public static final String COMPLEX_TYPE_ITEM = "application/x-openbp-complex-type";

	/** Process item (associated object: {@link Node}) */
	public static final String PROCESS_NODE = "application/x-openbp-process-node";

	//////////////////////////////////////////////////
	// @@ Text types
	//////////////////////////////////////////////////

	/** Java source file (associated object: Path to the file) */
	public static final String JAVA_SOURCE_FILE = "text/x-java";

	/** Generic source file (associated object: Path to the file) */
	public static final String SOURCE_FILE = "text/x-source";

	/** HTML file (associated object: Path to the file) */
	public static final String HTML_FILE = "text/html";

	/** XML file (associated object: Path to the file) */
	public static final String XML_FILE = "text/xml";

	/** XSL file (associated object: Path to the file) */
	public static final String XSL_FILE = "text/xsl";

	/** XML file (associated object: Path to the file) */
	public static final String SQL_FILE = "text/x-sql";

	/** Generic text file (associated object: Path to the file) */
	public static final String TEXT_FILE = "text/plain";

	/** RTF document format */
	public static final String TEXT_RTF = "text/rtf";

	/** PDF document format */
	public static final String APPLICATION_PDF = "application/pdf";

	//////////////////////////////////////////////////
	// @@ Miscelleanous
	//////////////////////////////////////////////////

	/** An URL */
	public static final String URL = "application/x-url";

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private MimeTypes()
	{
	}
}
