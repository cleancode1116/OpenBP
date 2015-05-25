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
package org.openbp.jaspira.propertybrowser.editor;

/**
 * Listener for the selection editor.
 * Allows dynamic configuration of the editor.
 *
 * @author Heiko Erhardt
 */
public interface PropertyEditorListener
{
	/**
	 * Called after the editor has been initialized.
	 * The editor parameters have been read at this point.
	 * However, the component has not yet been created.
	 * The listener may customize any editor settings that affect the component creation here.
	 *
	 * @param editor Editor
	 */
	public void initialized(PropertyEditor editor);

	/**
	 * Called after the editor component has been initialized.
	 * The listener may perform additional customizations of the component here.
	 *
	 * @param editor Editor
	 */
	public void componentCreated(PropertyEditor editor);

	/**
	 * Called after a value has been selected by the user
	 * (not if the value has been set programmatically).
	 *
	 * @param editor Editor
	 */
	public void valueChanged(PropertyEditor editor);
}
