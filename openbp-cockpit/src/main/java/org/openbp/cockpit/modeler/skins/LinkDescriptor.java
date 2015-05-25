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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;

import org.openbp.awt.Color2StringConverter;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.MsgFormat;

/**
 * Control or data link descriptor.
 *
 * @author Heiko Erhardt
 */
public class LinkDescriptor
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Default stroke width */
	public static final int DEFAULT_STROKE_WIDTH = 10;

	/** Line type: Solid line */
	public static final int LINETYPE_SOLID = 0;

	/** Line type: Dotted line */
	public static final int LINETYPE_DOTTED = 1;

	/** Decoration: None */
	public static final int DECORATION_NONE = 0;

	/** Decoration: Arrow */
	public static final int DECORATION_ARROW = 1;

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Link type */
	private String linkType;

	/** Link color */
	private Color color;

	/** Link color 2 */
	private Color color2;

	/** Link color 3 */
	private Color color3;

	/** Link color 4 */
	private Color color4;

	/** Stroke width */
	private int strokeWidth;

	/** Line type */
	private int lineType;

	/** Start decoration */
	private int startDecoration;

	/** End decoration */
	private int endDecoration;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Stroke */
	private Stroke stroke;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public LinkDescriptor()
	{
		// Default: 1 px
		strokeWidth = DEFAULT_STROKE_WIDTH;
		lineType = LINETYPE_SOLID;
		startDecoration = DECORATION_NONE;
		endDecoration = DECORATION_ARROW;
	}

	/**
	 * Validates the descriptor.
	 * Any error messages should be added to the supplied error message list
	 * and will be displayed by the caller.
	 *
	 * @param descriptorDir Path to the directory containing the descriptor
	 * @param errorMsgs List of strings that holds error messages
	 * @return
	 * true: The descriptor was successfully validated.<br>
	 * false: There was an error validating the descriptor.
	 */
	public boolean validate(String descriptorDir, List errorMsgs)
	{
		boolean ret = true;

		if (strokeWidth < 5 || strokeWidth > 50)
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Stroke width not between {0} and {1}.", Integer.valueOf(5), Integer.valueOf(50)));
			ret = false;
		}

		return ret;
	}

	/**
	 * Adds an error msg, prepending it by the node type name.
	 *
	 * @param errorMsgs List of strings that holds error messages
	 * @param errorMsg Error msg to add
	 */
	private void addErrorMsg(List errorMsgs, String errorMsg)
	{
		errorMsgs.add(MsgFormat.format("Data/control link: $0", errorMsg));
	}

	/**
	 * Initializes the descriptor.
	 * Loads the figure image (if an image file has been defined).
	 *
	 * @param skinResPath Resource path to the resource folder containing the descriptor
	 * @return
	 * true: The descriptor was successfully initialized.<br>
	 * false: There was an error initializing the descriptor. The descriptor should not be used.
	 */
	public boolean initialize(String skinResPath)
	{
		return true;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the link type.
	 * @nowarn
	 */
	public String getLinkType()
	{
		return linkType;
	}

	/**
	 * Sets the link type.
	 * @nowarn
	 */
	public void setLinkType(String linkType)
	{
		this.linkType = linkType;
	}

	/**
	 * Gets the link color.
	 * @nowarn
	 */
	public Color getColor()
	{
		return color;
	}

	/**
	 * Sets the link color.
	 * @nowarn
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}

	/**
	 * Gets the link color (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getColorStr()
	{
		return Color2StringConverter.color2Str(color);
	}

	/**
	 * Sets the link color (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setColorStr(String s)
	{
		this.color = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the link color 2.
	 * @nowarn
	 */
	public Color getColor2()
	{
		return color2;
	}

	/**
	 * Sets the link color 2.
	 * @nowarn
	 */
	public void setColor2(Color color2)
	{
		this.color2 = color2;
	}

	/**
	 * Gets the link color 2 (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getColor2Str()
	{
		return Color2StringConverter.color2Str(color2);
	}

	/**
	 * Sets the link color 2 (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setColor2Str(String s)
	{
		this.color2 = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the link color 3.
	 * @nowarn
	 */
	public Color getColor3()
	{
		return color3;
	}

	/**
	 * Sets the link color 3.
	 * @nowarn
	 */
	public void setColor3(Color color3)
	{
		this.color3 = color3;
	}

	/**
	 * Gets the link color 3 (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getColor3Str()
	{
		return Color2StringConverter.color2Str(color3);
	}

	/**
	 * Sets the link color 3 (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setColor3Str(String s)
	{
		this.color3 = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the link color 4.
	 * @nowarn
	 */
	public Color getColor4()
	{
		return color4;
	}

	/**
	 * Sets the link color 4.
	 * @nowarn
	 */
	public void setColor4(Color color4)
	{
		this.color4 = color4;
	}

	/**
	 * Gets the link color 4 (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getColor4Str()
	{
		return Color2StringConverter.color2Str(color4);
	}

	/**
	 * Sets the link color 4 (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setColor4Str(String s)
	{
		this.color4 = Color2StringConverter.str2Color(s);
	}

	/**
	 * Determines if the stroke width should be persisted.
	 * @nowarn
	 */
	public boolean hasStrokeWidth()
	{
		return strokeWidth != DEFAULT_STROKE_WIDTH;
	}

	/**
	 * Gets the stroke width.
	 * @return The stroke width in pixel/10 or 0 for no stroke
	 */
	public int getStrokeWidth()
	{
		return strokeWidth;
	}

	/**
	 * Sets the stroke width.
	 * @param strokeWidth The stroke width in pixel/10 or 0 for no stroke
	 */
	public void setStrokeWidth(int strokeWidth)
	{
		this.strokeWidth = strokeWidth;
	}

	/**
	 * Gets the line type.
	 * @return {@link #LINETYPE_SOLID}/{@link #LINETYPE_DOTTED}
	 */
	public int getLineType()
	{
		return lineType;
	}

	/**
	 * Sets the line type.
	 * @param lineType {@link #LINETYPE_SOLID}/{@link #LINETYPE_DOTTED}
	 */
	public void setLineType(int lineType)
	{
		this.lineType = lineType;
	}

	/**
	 * Gets the line type (string value).
	 * @return "solid"/"dotted"
	 */
	public String getLineTypeStr()
	{
		return lineType == LINETYPE_DOTTED ? "dotted" : null;
	}

	/**
	 * Sets the line type.
	 * @param lineTypeStr "solid"/"dotted"
	 */
	public void setLineTypeStr(String lineTypeStr)
	{
		this.lineType = "dotted".equals(lineTypeStr) ? LINETYPE_DOTTED : LINETYPE_SOLID;
	}

	/**
	 * Gets the start decoration.
	 * @return {@link #DECORATION_NONE}/{@link #DECORATION_ARROW}
	 */
	public int getStartDecoration()
	{
		return startDecoration;
	}

	/**
	 * Sets the start decoration.
	 * @param startDecoration {@link #DECORATION_NONE}/{@link #DECORATION_ARROW}
	 */
	public void setStartDecoration(int startDecoration)
	{
		this.startDecoration = startDecoration;
	}

	/**
	 * Gets the start decoration (string value).
	 * @return "none"/"arrow"
	 */
	public String getStartDecorationStr()
	{
		return startDecoration == DECORATION_ARROW ? "arrow" : null;
	}

	/**
	 * Sets the start decoration.
	 * @param startDecorationStr "none"/"arrow"
	 */
	public void setStartDecorationStr(String startDecorationStr)
	{
		this.startDecoration = "arrow".equals(startDecorationStr) ? DECORATION_ARROW : DECORATION_NONE;
	}

	/**
	 * Gets the end decoration.
	 * @return {@link #DECORATION_NONE}/{@link #DECORATION_ARROW}
	 */
	public int getEndDecoration()
	{
		return endDecoration;
	}

	/**
	 * Sets the end decoration.
	 * @param endDecoration {@link #DECORATION_NONE}/{@link #DECORATION_ARROW}
	 */
	public void setEndDecoration(int endDecoration)
	{
		this.endDecoration = endDecoration;
	}

	/**
	 * Gets the end decoration (string value).
	 * @return "none"/"arrow"
	 */
	public String getEndDecorationStr()
	{
		return endDecoration == DECORATION_ARROW ? "arrow" : null;
	}

	/**
	 * Sets the end decoration.
	 * @param endDecorationStr "none"/"arrow"
	 */
	public void setEndDecorationStr(String endDecorationStr)
	{
		this.endDecoration = "arrow".equals(endDecorationStr) ? DECORATION_ARROW : DECORATION_NONE;
	}

	//////////////////////////////////////////////////
	// @@ Data member access
	//////////////////////////////////////////////////

	/**
	 * Gets the frame stroke.
	 * @nowarn
	 */
	public Stroke getStroke()
	{
		if (stroke == null)
		{
			if (strokeWidth > 0)
			{
				if (lineType == LINETYPE_SOLID)
				{
					if (strokeWidth == 10)
					{
						stroke = FigureResources.standardStroke1;
					}
					else if (strokeWidth == 20)
					{
						stroke = FigureResources.standardStroke2;
					}
					else if (strokeWidth == 30)
					{
						stroke = FigureResources.standardStroke3;
					}
					else
					{
						stroke = new BasicStroke((float) strokeWidth / 10);
					}
				}
				else
				{
					// Create a stroke with a 5 pixel dash phase
					float [] dash = { 4.0f };
					stroke = new BasicStroke((float) strokeWidth / 10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0f, dash, 0f);
				}
			}
		}

		return stroke;
	}
}
