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

/**
 * The resource framework provides a standard resource bundle to inherit from it.
 * This framework provides XML resource files, mime-type and language packaging.
 *
 * Usage:
 *
 * Use the ComponentManager class to get the access to the resources.\n
 *
 *	<li>ResourceCollection res = ComponentManager.getResource (&lt;componentName&gt;, &lt;resourceName&gt;);</li>\n
 * or
 *	<li>ResourceCollection res = ComponentManager.getResource (&lt;componentName&gt;, &lt;resourceName&gt;, Locale);</li>
 *
 * A resource component can be a directory or a jar file. During developing, a directory is recommended.
 * In a resource component can be more as one resource file (&lt;resourceName&gt;.xml).
 *
 * Structure of the resource file:\n
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;\n
 * &lt;resource cache="true"&gt;\n
 * 	&lt;resource-item name="jLabel1_Text" type="text/plain"&gt;jLabel1&lt;/resource-item&gt;\n
 * 	&lt;resource-item name="dsrc_tools_resources" type="image/gif"&gt;\n
 * 		&lt;path&gt;images/test.gif&lt;/path&gt;\n
 *	&lt;/resource-item&gt;\n
 *	.\n
 * 	.\n
 * &lt;/resource&gt;\n
 * </code>
 *
 * Please have attention for the developing using the jbuilder:
 *
 * - Use the Resource Wizard with the Property resource class
 * - After this convert the property file to an XML resource, by com.skynamics.framework.resources.converter.PropToXMLConverter
 *   (For later use the jbuilder designer again use com.skynamics.framework.resources.converter.XMLToPropConverter)
 * - Change the line with\n <code>static ResourceBundle res = ResourceBundle.getBundle ("test.test_TestResourceBundle")</code>\n
 *   to\n <code>static ResourceCollection res = ComponentManager.getResource ("test", "TestResourceBundle");</code>\n and create a resource component
 *
 *
 * Attention use the 'Core.properties' file of your project:
 *
 *	#Tue Jan 29 14:35:30 CET 2002
 *
 *	# Component settings\n
 *	# The paths are relative to the root directory\n
 *	ResourceCollection.dir = resource\n
 *	# ResourceCollection.&lt;component&gt;.dir\n
 *	ResourceCollection.test.dir = resource/test\n
 *
 *
 *	# ResourceCollection cache disabling\n
 *	#ResourceCollection.disableCache = true\n
 */
package org.openbp.common.rc;
