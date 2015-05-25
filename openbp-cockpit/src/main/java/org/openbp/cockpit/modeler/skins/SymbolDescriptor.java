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
package org.openbp.cockpit.modeler.skins;

import java.util.List;

import javax.swing.ImageIcon;

import org.openbp.cockpit.modeler.figures.generic.XFigureDescriptor;
import org.openbp.common.MsgFormat;

/**
 * Symbol descriptor.
 *
 * @author Heiko Erhardt
 */
public class SymbolDescriptor extends XFigureDescriptor
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Flag: Image display disabled */
	public static final int FLAG_IMAGE_DISABLED = (1 << 0);

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Symbol type */
	private String symbolType;

	/** Flags */
	private int flags;

	/** Text position */
	private String textPosition;

	/** Overlay position */
	private String overlayPosition;

	/** Overlay resource key */
	private String overlayResourceKey;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Optional image */
	private transient ImageIcon overlayIcon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public SymbolDescriptor()
	{
	}

	//////////////////////////////////////////////////
	// @@ XFigureDescriptor overrides
	//////////////////////////////////////////////////

	/**
	 * Adds an error msg, prepending it by the node type name.
	 *
	 * @param errorMsgs List of strings that holds error messages
	 * @param errorMsg Error msg to add
	 */
	protected void addErrorMsg(List errorMsgs, String errorMsg)
	{
		errorMsgs.add(MsgFormat.format("Figure $0: $1", symbolType, errorMsg));
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the symbol type.
	 * @nowarn
	 */
	public String getSymbolType()
	{
		return symbolType;
	}

	/**
	 * Sets the symbol type.
	 * @nowarn
	 */
	public void setSymbolType(String symbolType)
	{
		this.symbolType = symbolType;
	}

	/**
	 * Gets the image disabled.
	 * @nowarn
	 */
	public boolean isImageDisabled()
	{
		return (flags & FLAG_IMAGE_DISABLED) != 0;
	}

	/**
	 * Sets the image disabled.
	 * @nowarn
	 */
	public void setImageDisabled(boolean imageDisabled)
	{
		if (imageDisabled)
		{
			flags |= FLAG_IMAGE_DISABLED;
		}
		else
		{
			flags &= ~FLAG_IMAGE_DISABLED;
		}
	}

	/**
	 * Gets the text position.
	 * @nowarn
	 */
	public String getTextPosition()
	{
		return textPosition;
	}

	/**
	 * Sets the text position.
	 * @nowarn
	 */
	public void setTextPosition(String textPosition)
	{
		this.textPosition = textPosition;
	}

	/**
	 * Gets the overlay position.
	 * @nowarn
	 */
	public String getOverlayPosition()
	{
		return overlayPosition;
	}

	/**
	 * Sets the overlay position.
	 * @nowarn
	 */
	public void setOverlayPosition(String overlayPosition)
	{
		this.overlayPosition = overlayPosition;
	}

	/**
	 * Gets the overlay resource key.
	 * @nowarn
	 */
	public String getOverlayResourceKey()
	{
		return overlayResourceKey;
	}

	/**
	 * Sets the overlay resource key.
	 * @nowarn
	 */
	public void setOverlayResourceKey(String overlayResourceKey)
	{
		this.overlayResourceKey = overlayResourceKey;
	}

	//////////////////////////////////////////////////
	// @@ Data access
	//////////////////////////////////////////////////

	/**
	 * Gets the optional image.
	 * @nowarn
	 */
	public ImageIcon getOverlayIcon()
	{
		return overlayIcon;
	}

	/**
	 * Sets the optional image.
	 * @nowarn
	 */
	public void setOverlayIcon(ImageIcon overlayIcon)
	{
		this.overlayIcon = overlayIcon;
	}
}
