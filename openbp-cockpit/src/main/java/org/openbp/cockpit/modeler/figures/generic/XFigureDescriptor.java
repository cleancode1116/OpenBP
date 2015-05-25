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
package org.openbp.cockpit.modeler.figures.generic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Stroke;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.openbp.awt.Color2StringConverter;
import org.openbp.cockpit.modeler.util.FigureResources;
import org.openbp.common.CommonUtil;
import org.openbp.common.MsgFormat;
import org.openbp.common.ReflectUtil;
import org.openbp.common.generic.taggedvalue.TaggedValue;
import org.openbp.common.logger.LogUtil;
import org.openbp.common.string.Object2StringConverter;
import org.openbp.common.string.StringUtil;
import org.openbp.common.util.iterator.EmptyIterator;
import org.springframework.core.io.ClassPathResource;

/**
 * Delegation object that implements some functionality of the {@link XFigure} interface.
 *
 * @author Heiko Erhardt
 */
public class XFigureDescriptor
{
	//////////////////////////////////////////////////
	// @@ Constants
	//////////////////////////////////////////////////

	/** Center */
	public static final int GRADIENTPOS_CENTER = -1;

	/** E (right) */
	public static final int GRADIENTPOS_E = 0;

	/** SE (bottom right) */
	public static final int GRADIENTPOS_SE = 1;

	/** S (bottom) */
	public static final int GRADIENTPOS_S = 2;

	/** SW (bottom left) */
	public static final int GRADIENTPOS_SW = 3;

	/** W (left) */
	public static final int GRADIENTPOS_W = 4;

	/** NW (top left) */
	public static final int GRADIENTPOS_NW = 5;

	/** N (top) */
	public static final int GRADIENTPOS_N = 6;

	/** NE (top left) */
	public static final int GRADIENTPOS_NE = 7;

	/** Default x/y size */
	public static final int DEFAULT_SIZE = 50;

	/** Default frame stroke width */
	public static final int DEFAULT_FRAME_STROKE_WIDTH = 10;

	/** Default orientation */
	public static final Orientation DEFAULT_ORIENTATION = Orientation.BOTTOM;

	/** Default gradient position */
	public static final int DEFAULT_GRADIENT_POS = GRADIENTPOS_CENTER;

	/** Minimum size */
	public static final int MIN_SIZE = 5;

	/** Maximum size */
	public static final int MAX_SIZE = 400;

	/** Flag: We have a cyclic gradient */
	public static final int FLAG_CYCLIC_GRADIENT = (1 << 5);

	/** Converter for orientation values */
	private static Object2StringConverter orientationConverter;

	/** Converter for gradient position values */
	private static Object2StringConverter gradientPosConverter;

	/** Point with coordinates 0/0 */
	private static final Point point0 = new Point(0, 0);

	static
	{
		orientationConverter = new Object2StringConverter();
		orientationConverter.setDefaultValue(DEFAULT_ORIENTATION);
		orientationConverter.addValue(Orientation.TOP, "n", "Top");
		orientationConverter.addValue(Orientation.RIGHT, "e", "Right");
		orientationConverter.addValue(Orientation.BOTTOM, "s", "Bottom");
		orientationConverter.addValue(Orientation.LEFT, "w", "Left");

		gradientPosConverter = new Object2StringConverter();
		gradientPosConverter.setDefaultValue(Integer.valueOf(DEFAULT_GRADIENT_POS));
		gradientPosConverter.addIntValue(GRADIENTPOS_CENTER, "c", "Center");
		gradientPosConverter.addIntValue(GRADIENTPOS_N, "n", "Top");
		gradientPosConverter.addIntValue(GRADIENTPOS_NE, "ne", "Top right");
		gradientPosConverter.addIntValue(GRADIENTPOS_E, "e", "Right");
		gradientPosConverter.addIntValue(GRADIENTPOS_SE, "se", "Bottom right");
		gradientPosConverter.addIntValue(GRADIENTPOS_S, "s", "Bottom");
		gradientPosConverter.addIntValue(GRADIENTPOS_SW, "sw", "Bottom left");
		gradientPosConverter.addIntValue(GRADIENTPOS_W, "w", "Left");
		gradientPosConverter.addIntValue(GRADIENTPOS_NW, "nw", "Top left");
	}

	//////////////////////////////////////////////////
	// @@ Properties
	//////////////////////////////////////////////////

	/** Figure type */
	private String figureType;

	/** Figure class */
	private String figureClassName;

	/** Orientation of the figure */
	private Orientation orientation;

	/** Horizontal size */
	private int sizeX;

	/** Vertical size */
	private int sizeY;

	/** Minimum horizontal size */
	private int minSizeX;

	/** Minimum vertical size */
	private int minSizeY;

	/** Maximum horizontal size */
	private int maxSizeX;

	/** Maximum vertical size */
	private int maxSizeY;

	/** Frame stroke width */
	private int frameStrokeWidth;

	/** Frame color */
	private Color frameColor;

	/** Fill color */
	private Color fillColor;

	/** Second (optional) fill color for gradients */
	private Color fillColor2;

	/** Orientation  of the gradient (position 1) in a top-down orientation */
	private int gradientPos1;

	/** Orientation  of the gradient (position 2) in a top-down orientation */
	private int gradientPos2;

	/** Cyclic gradient */
	private boolean cyclicGradient;

	/** Image file name */
	private String imageFileName;

	/** Tagged value list (contains {@link TaggedValue} objects) */
	private List taggedValueList;

	//////////////////////////////////////////////////
	// @@ Data members
	//////////////////////////////////////////////////

	/** Frame stroke */
	private Stroke frameStroke;

	/** Image */
	private transient ImageIcon imageIcon;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Default constructor.
	 */
	public XFigureDescriptor()
	{
		// Default: 1 px
		frameStrokeWidth = DEFAULT_FRAME_STROKE_WIDTH;

		// Default: Top-down orientation
		orientation = DEFAULT_ORIENTATION;

		// Default: Top-down gradient
		gradientPos1 = DEFAULT_GRADIENT_POS;
		gradientPos2 = DEFAULT_GRADIENT_POS;
	}

	/**
	 * Validates the descriptor.
	 * Any error messages should be added to the supplied error message list
	 * and will be displayed by the caller.
	 *
	 * @param descriptorResourcePath Resource path to the descriptor resources
	 * @param errorMsgs List of strings that holds error messages
	 * @return
	 * true: The descriptor was successfully validated.<br>
	 * false: There was an error validating the descriptor.
	 */
	public boolean validate(String descriptorResourcePath, List errorMsgs)
	{
		boolean ret = true;

		if (figureType == null && figureClassName == null)
		{
			addErrorMsg(errorMsgs, "Figure type and figure class not specified.");
			ret = false;
		}
		else
		{
			String className = determineFigureClassName();
			try
			{
				Class.forName(className);
			}
			catch (ClassNotFoundException e)
			{
				addErrorMsg(errorMsgs, MsgFormat.format("Figure type class $0 not found.", className));
				ret = false;
			}
		}

		if (!orientationConverter.checkValue(orientation))
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Orientation value $0 is not valid.", "" + orientation));
			ret = false;
		}

		if (!gradientPosConverter.checkIntValue(gradientPos1))
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Gradient position 1 value $0 is not valid.", "" + gradientPos1));
			ret = false;
		}

		if (!gradientPosConverter.checkIntValue(gradientPos2))
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Gradient position 2 value $0 is not valid.", "" + gradientPos2));
			ret = false;
		}

		if (sizeX != 0 && (sizeX < MIN_SIZE || sizeX > MAX_SIZE))
		{
			addErrorMsg(errorMsgs, MsgFormat.format("X size not between {0} and {1}.", Integer.valueOf(MIN_SIZE), Integer.valueOf(MAX_SIZE)));
			ret = false;
		}

		if (sizeY != 0 && (sizeY < MIN_SIZE || sizeY > MAX_SIZE))
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Y size not between {0} and {1}.", Integer.valueOf(MIN_SIZE), Integer.valueOf(MAX_SIZE)));
			ret = false;
		}

		if (minSizeX <= 0)
			minSizeX = sizeX;
		if (minSizeY <= 0)
			minSizeY = sizeY;
		if (maxSizeX <= 0)
			maxSizeX = sizeX * 3;
		if (maxSizeY <= 0)
			maxSizeY = sizeY * 3;

		if (frameStrokeWidth < 5 || frameStrokeWidth > 50)
		{
			addErrorMsg(errorMsgs, MsgFormat.format("Frame stroke width not between {0} and {1}.", Integer.valueOf(5), Integer.valueOf(50)));
			ret = false;
		}

		if (imageFileName != null)
		{
			String resPath = StringUtil.buildPath(descriptorResourcePath, imageFileName);
			if (!new ClassPathResource(resPath).exists())
			{
				ret = false;
				addErrorMsg(errorMsgs, MsgFormat.format("Figure resource $0 does not exist.", resPath));
			}
		}

		return ret;
	}

	/**
	 * Adds an error msg, prepending it by the node type name.
	 *
	 * @param errorMsgs List of strings that holds error messages
	 * @param errorMsg Error msg to add
	 */
	protected void addErrorMsg(List errorMsgs, String errorMsg)
	{
		errorMsgs.add(errorMsg);
	}

	/**
	 * Initializes the descriptor.
	 * Loads the figure image (if an image file has been defined).
	 *
	 * @param descriptorResourcePath Resource path to the resource folder containing the descriptor
	 * @return
	 * true: The descriptor was successfully initialized.<br>
	 * false: There was an error initializing the descriptor.
	 *				The descriptor should not be added to the descriptor list.
	 */
	public boolean initialize(String descriptorResourcePath)
	{
		if (imageFileName != null)
		{
			String resPath = StringUtil.buildPath(descriptorResourcePath, imageFileName);
			try
			{
				imageIcon = new ImageIcon(new ClassPathResource(resPath).getURL(), null);
			}
			catch (IOException e)
			{
				LogUtil.error(getClass(), "Figure resource $0 does not exist.", resPath);
			}
		}

		return true;
	}

	/**
	 * Creates a new figure according to the figure type of this descriptor.
	 *
	 * @return The new figure or null on error
	 */
	public XFigure createFigure()
	{
		try
		{
			String className = determineFigureClassName();
			XFigure figure = (XFigure) ReflectUtil.instantiate(className, XFigure.class, "figure");

			figure.setDescriptor(this);

			if (imageIcon != null)
			{
				figure.setImageIcon(imageIcon);
			}

			int x = sizeX;
			int y = sizeY;
			if (x == 0)
				x = imageIcon != null ? imageIcon.getIconWidth() : DEFAULT_SIZE;
			if (y == 0)
				y = imageIcon != null ? imageIcon.getIconHeight() : DEFAULT_SIZE;
			figure.basicDisplayBox(point0, new Point(x, y));

			figure.setOrientation(orientation);
			figure.setFrameStroke(getFrameStroke());

			figure.initialize();

			return figure;
		}
		catch (Exception e)
		{
			LogUtil.error(getClass(), "Error instantiating figure.", e);
		}
		return null;
	}

	/**
	 * Determines the figure class name from the figure type.
	 *
	 * @return The class name
	 */
	protected String determineFigureClassName()
	{
		if (figureClassName != null)
			return figureClassName;
		return "org.openbp.cockpit.modeler.figures.generic.X" + figureType + "Figure";
	}

	//////////////////////////////////////////////////
	// @@ Tagged values
	//////////////////////////////////////////////////

	/**
	 * Gets the tagged value list.
	 * @return An iterator of {@link TaggedValue} objects
	 */
	public Iterator getTaggedValues()
	{
		if (taggedValueList == null)
			return EmptyIterator.getInstance();
		return taggedValueList.iterator();
	}

	/**
	 * Gets a tagged value by its collection index.
	 *
	 * @param index Collection index
	 * @return The tagged value
	 */
	public TaggedValue getTaggedValue(int index)
	{
		return (TaggedValue) taggedValueList.get(index);
	}

	/**
	 * Adds a tagged value.
	 * @param taggedValue The tagged value to add
	 */
	public void addTaggedValue(TaggedValue taggedValue)
	{
		if (taggedValueList == null)
			taggedValueList = new ArrayList();
		taggedValueList.add(taggedValue);
	}

	/**
	 * Clears the tagged value list.
	 */
	public void clearTaggedValues()
	{
		taggedValueList = null;
	}

	/**
	 * Gets the tagged value list.
	 * @return A list of {@link TaggedValue} objects
	 */
	public List getTaggedValueList()
	{
		return taggedValueList;
	}

	/**
	 * Sets the tagged value list.
	 * @param taggedValueList A list of {@link TaggedValue} objects
	 */
	public void setTaggedValueList(List taggedValueList)
	{
		this.taggedValueList = taggedValueList;
	}

	/**
	 * Gets the value of a tagged value.
	 *
	 * @param name Name of the attribute
	 * @return The value or null if no such attribute exists
	 */
	public String getTaggedValue(String name)
	{
		if (taggedValueList != null)
		{
			int n = taggedValueList.size();
			for (int i = 0; i < n; ++i)
			{
				TaggedValue sa = (TaggedValue) taggedValueList.get(i);
				if (CommonUtil.equalsNull(sa.getName(), name))
				{
					return sa.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the value of a tagged value as integer.
	 *
	 * @param name Name of the attribute
	 * @param dflt The default value
	 * @return The attribute value or the default if no such attribute exists
	 */
	public int getCustomIntAttributeValue(String name, int dflt)
	{
		String s = getTaggedValue(name);
		if (s != null)
		{
			try
			{
				return Integer.parseInt(s);
			}
			catch (NumberFormatException e)
			{
				// Ignore
			}
		}

		return dflt;
	}

	//////////////////////////////////////////////////
	// @@ Property access
	//////////////////////////////////////////////////

	/**
	 * Gets the figure type.
	 * @nowarn
	 */
	public String getFigureType()
	{
		return figureType;
	}

	/**
	 * Sets the figure type.
	 * @nowarn
	 */
	public void setFigureType(String figureType)
	{
		this.figureType = figureType;
	}

	/**
	 * Gets the figure class.
	 * @nowarn
	 */
	public String getFigureClassName()
	{
		return figureClassName;
	}

	/**
	 * Sets the figure class.
	 * @nowarn
	 */
	public void setFigureClassName(String figureClassName)
	{
		this.figureClassName = figureClassName;
	}

	/**
	 * Determines if the orientation of the figure should be persisted.
	 * @nowarn
	 */
	public boolean hasOrientation()
	{
		return orientation != DEFAULT_ORIENTATION;
	}

	/**
	 * Gets the orientation of the figure.
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public Orientation getOrientation()
	{
		return orientation;
	}

	/**
	 * Sets the orientation of the figure.
	 * @param orientation {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public void setOrientation(Orientation orientation)
	{
		this.orientation = orientation;
	}

	/**
	 * Gets the orientation of the figure (string access).
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public String getOrientationStr()
	{
		return orientationConverter.value2Str(orientation);
	}

	/**
	 * Sets the orientation of the figure (string access).
	 * @param s {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public void setOrientationStr(String s)
	{
		this.orientation = (Orientation) orientationConverter.str2Value(s);
	}

	/**
	 * Determines if the horizontal size should be persisted.
	 * @nowarn
	 */
	public boolean hasSizeX()
	{
		return sizeX != 0;
	}

	/**
	 * Gets the horizontal size.
	 * @nowarn
	 */
	public int getSizeX()
	{
		return sizeX;
	}

	/**
	 * Sets the horizontal size.
	 * @nowarn
	 */
	public void setSizeX(int sizeX)
	{
		this.sizeX = sizeX;
	}

	/**
	 * Determines if the vertical size should be persisted.
	 * @nowarn
	 */
	public boolean hasSizeY()
	{
		return sizeY != 0;
	}

	/**
	 * Gets the vertical size.
	 * @nowarn
	 */
	public int getSizeY()
	{
		return sizeY;
	}

	/**
	 * Sets the vertical size.
	 * @nowarn
	 */
	public void setSizeY(int sizeY)
	{
		this.sizeY = sizeY;
	}

	/**
	 * Gets the minimum horizontal size.
	 * @nowarn
	 */
	public int getMinSizeX()
	{
		return minSizeX;
	}

	/**
	 * Sets the minimum horizontal size.
	 * @nowarn
	 */
	public void setMinSizeX(int minSizeX)
	{
		this.minSizeX = minSizeX;
	}

	/**
	 * Gets the minimum vertical size.
	 * @nowarn
	 */
	public int getMinSizeY()
	{
		return minSizeY;
	}

	/**
	 * Sets the minimum vertical size.
	 * @nowarn
	 */
	public void setMinSizeY(int minSizeY)
	{
		this.minSizeY = minSizeY;
	}

	/**
	 * Gets the maximum horizontal size.
	 * @nowarn
	 */
	public int getMaxSizeX()
	{
		return maxSizeX;
	}

	/**
	 * Sets the maximum horizontal size.
	 * @nowarn
	 */
	public void setMaxSizeX(int maxSizeX)
	{
		this.maxSizeX = maxSizeX;
	}

	/**
	 * Gets the maximum vertical size.
	 * @nowarn
	 */
	public int getMaxSizeY()
	{
		return maxSizeY;
	}

	/**
	 * Sets the maximum vertical size.
	 * @nowarn
	 */
	public void setMaxSizeY(int maxSizeY)
	{
		this.maxSizeY = maxSizeY;
	}

	/**
	 * Determines if the stroke width should be persisted.
	 * @nowarn
	 */
	public boolean hasFrameStrokeWidth()
	{
		return frameStrokeWidth != DEFAULT_FRAME_STROKE_WIDTH;
	}

	/**
	 * Gets the stroke width.
	 * @return The stroke width in pixel/10 or 0 for no stroke
	 */
	public int getFrameStrokeWidth()
	{
		return frameStrokeWidth;
	}

	/**
	 * Sets the stroke width.
	 * @param frameStrokeWidth The stroke width in pixel/10 or 0 for no stroke
	 */
	public void setFrameStrokeWidth(int frameStrokeWidth)
	{
		this.frameStrokeWidth = frameStrokeWidth;
	}

	/**
	 * Gets the frame color.
	 * @return The frame color or null for no frame
	 */
	public Color getFrameColor()
	{
		return frameColor;
	}

	/**
	 * Sets the frame color.
	 * @param frameColor The frame color or null for no frame
	 */
	public void setFrameColor(Color frameColor)
	{
		this.frameColor = frameColor;
	}

	/**
	 * Gets the fill color.
	 * @return Fill color or null for no fill color
	 */
	public Color getFillColor()
	{
		return fillColor;
	}

	/**
	 * Sets the fill color.
	 * @param fillColor Fill color or null for no fill color
	 */
	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	/**
	 * Gets the second (optional) fill color for gradients.
	 * @return Gradient color or null for solid fill
	 */
	public Color getFillColor2()
	{
		return fillColor2;
	}

	/**
	 * Sets the second (optional) fill color for gradients.
	 * @param fillColor2 Gradient color or null for solid fill
	 */
	public void setFillColor2(Color fillColor2)
	{
		this.fillColor2 = fillColor2;
	}

	/**
	 * Gets the position of the gradient (position 1) in a top-down figure orientation.
	 * @return See the NUM_* constants of the CircleConstants class
	 */
	public int getGradientPos1()
	{
		return gradientPos1;
	}

	/**
	 * Sets the position of the gradient (position 1) in a top-down figure orientation.
	 * @param gradientPos1 See the NUM_* constants of the CircleConstants class
	 */
	public void setGradientPos1(int gradientPos1)
	{
		this.gradientPos1 = gradientPos1;
	}

	/**
	 * Gets the position of the gradient (position 2) in a top-down figure orientation.
	 * @return See the NUM_* constants of the CircleConstants class
	 */
	public int getGradientPos2()
	{
		return gradientPos2;
	}

	/**
	 * Sets the position of the gradient (position 2) in a top-down figure orientation.
	 * @param gradientPos2 See the NUM_* constants of the CircleConstants class
	 */
	public void setGradientPos2(int gradientPos2)
	{
		this.gradientPos2 = gradientPos2;
	}

	/**
	 * Gets the position of the gradient (position 1) in a top-down figure orientation (string access).
	 * @return See the NUM_* constants of the CircleConstants class
	 */
	public String getGradientPos1Str()
	{
		return gradientPosConverter.intValue2Str(gradientPos1);
	}

	/**
	 * Sets the position of the gradient (position 1) in a top-down figure orientation (string access).
	 * @param s See the NUM_* constants of the CircleConstants class
	 */
	public void setGradientPos1Str(String s)
	{
		this.gradientPos1 = gradientPosConverter.str2IntValue(s);
	}

	/**
	 * Gets the position of the gradient (position 2) in a top-down figure orientation (string access).
	 * @return See the NUM_* constants of the CircleConstants class
	 */
	public String getGradientPos2Str()
	{
		return gradientPosConverter.intValue2Str(gradientPos2);
	}

	/**
	 * Sets the position of the gradient (position 2) in a top-down figure orientation (string access).
	 * @param s See the NUM_* constants of the CircleConstants class
	 */
	public void setGradientPos2Str(String s)
	{
		this.gradientPos2 = gradientPosConverter.str2IntValue(s);
	}

	/**
	 * Gets the cyclic gradient.
	 * @nowarn
	 */
	public boolean isCyclicGradient()
	{
		return cyclicGradient;
	}

	/**
	 * Sets the cyclic gradient.
	 * @nowarn
	 */
	public void setCyclicGradient(boolean cyclicGradient)
	{
		this.cyclicGradient = cyclicGradient;
	}

	/**
	 * Gets the frame color (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getFrameColorStr()
	{
		return Color2StringConverter.color2Str(frameColor);
	}

	/**
	 * Sets the frame color (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setFrameColorStr(String s)
	{
		this.frameColor = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the fill color (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getFillColorStr()
	{
		return Color2StringConverter.color2Str(fillColor);
	}

	/**
	 * Sets the fill color (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setFillColorStr(String s)
	{
		this.fillColor = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the second (optional) fill color for gradients (colon-separated RGBA value).
	 * @nowarn
	 */
	public String getFillColor2Str()
	{
		return Color2StringConverter.color2Str(fillColor2);
	}

	/**
	 * Sets the second (optional) fill color for gradients (colon-separated RGBA value).
	 * @nowarn
	 */
	public void setFillColor2Str(String s)
	{
		this.fillColor2 = Color2StringConverter.str2Color(s);
	}

	/**
	 * Gets the image file name.
	 * @nowarn
	 */
	public String getImageFileName()
	{
		return imageFileName;
	}

	/**
	 * Sets the image file name.
	 * @nowarn
	 */
	public void setImageFileName(String imageFileName)
	{
		this.imageFileName = imageFileName;
	}

	//////////////////////////////////////////////////
	// @@ Data member access
	//////////////////////////////////////////////////

	/**
	 * Gets the frame stroke.
	 * @nowarn
	 */
	public Stroke getFrameStroke()
	{
		if (frameStroke == null)
		{
			if (frameStrokeWidth > 0)
			{
				if (frameStrokeWidth == 10)
				{
					frameStroke = FigureResources.standardStroke1;
				}
				else if (frameStrokeWidth == 20)
				{
					frameStroke = FigureResources.standardStroke2;
				}
				else if (frameStrokeWidth == 30)
				{
					frameStroke = FigureResources.standardStroke3;
				}
				else
				{
					frameStroke = new BasicStroke((float) frameStrokeWidth / 10);
				}
			}
		}

		return frameStroke;
	}

	/**
	 * Gets the image.
	 * @nowarn
	 */
	public ImageIcon getImageIcon()
	{
		return imageIcon;
	}
}
