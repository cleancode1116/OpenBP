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
package org.openbp.common.rc.image;

import org.openbp.common.rc.ResourceItemTypes;

/**
 * Gif image resource item.
 *
 * @author Andreas Putz
 */
public class TGif extends AbstractJavaImage
{
	//////////////////////////////////////////////////
	// @@ ResourceItem implementation
	//////////////////////////////////////////////////

	/**
	 * Gets the mime-type of the resource item.
	 *
	 * @return A string in mime-type format
	 */
	public String getMimeType()
	{
		return ResourceItemTypes.IMAGE_GIF;
	}

	//////////////////////////////////////////////////
	// @@ AbstractJavaImage implementation
	//////////////////////////////////////////////////

	private static final String [] EXTENSIONS = new String [] { "gif" };

	protected String [] getFileExtensions()
	{
		return EXTENSIONS;
	}
}
