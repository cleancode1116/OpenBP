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

import java.awt.Rectangle;

/**
 * Constants describing circle sections.
 *
 * There are three types of constants:
 *
 * The orthogonal constants (RIGHT/BOTTOM/LEFT/TOP) are the simplest ones.
 *
 * The quarter constants (QUARTER_*) are aliases of the orthogonal constants above and are used
 * to desribe the circle quarters that are oriented in these directions.
 *
 * The EIGTH_* constants divide the circle in eight directions.
 *
 * @author Heiko Erhardt
 */
public class CircleConstants
{
	//////////////////////////////////////////////////////
	// @@ 45 degree constants
	//
	//
	//                       3 PI/2
	//                          |
	//                          |
	//                    N     |     NE         /
	//           \              |              /
	//             \            |            /
	//               \          |          /
	//                 \        |        /
	//         NW        \      |      /
	//                     \    |    /       E
	//                       \  |  /
	//                         \|/
	// PI ----------------------+---------------------- 0
	//                         /|\
	//                       /  |  \
	//         W           /    |    \       SE
	//                   /      |      \
	//                 /        |        \
	//               /          |          \
	//             /            |            \
	//           /              |              \
	//                    SW    |     S
	//                          |
	//                          |
	//                         PI/2
	//
	//////////////////////////////////////////////////////

	// Do not change the value of these constants

	/** E (right) */
	public static final int EIGHTH_E = 0;

	/** SE (bottom right) */
	public static final int EIGHTH_SE = 1;

	/** S (bottom) */
	public static final int EIGHTH_S = 2;

	/** SW (bottom left) */
	public static final int EIGHTH_SW = 3;

	/** W (left) */
	public static final int EIGHTH_W = 4;

	/** NW (top left) */
	public static final int EIGHTH_NW = 5;

	/** N (top) */
	public static final int EIGHTH_N = 6;

	/** NE (top left) */
	public static final int EIGHTH_NE = 7;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor prevents instantiation.
	 */
	private CircleConstants()
	{
	}

	//////////////////////////////////////////////////
	// @@ Utility methods
	//////////////////////////////////////////////////

	private static final double CIRCLE = 2d * Math.PI;

	/**
	 * Normalizes an angle.
	 *
	 * @param angle Angle to normalize
	 * @return The angle in the range [0;2 * PI]
	 */
	public static double normalizeAngle(double angle)
	{
		// Normalize the angle
		while (angle > CIRCLE)
		{
			angle -= CIRCLE;
		}
		while (angle < 0)
		{
			angle += CIRCLE;
		}
		return angle;
	}

	/**
	 * Determines the eighth from a given angle.
	 *
	 * @param angle Angle to check
	 * @param rect Display box of the object in the center
	 * @return CircleConstants.EIGHT_*
	 */
	public static int determineEighth(double angle, Rectangle rect)
	{
		// This tedious if/else if construction determines in which
		// section of the rectangle we are
		int dir;
		if (rect != null)
		{
			double alpha = Math.atan(rect.getHeight() / rect.getWidth());

			if (angle < alpha)
			{
				dir = CircleConstants.EIGHTH_SE;
			}
			else if (angle < Math.PI / 2)
			{
				dir = CircleConstants.EIGHTH_S;
			}
			else if (angle < Math.PI - alpha)
			{
				dir = CircleConstants.EIGHTH_SW;
			}
			else if (angle < Math.PI)
			{
				dir = CircleConstants.EIGHTH_W;
			}
			else if (angle < Math.PI + alpha)
			{
				dir = CircleConstants.EIGHTH_NW;
			}
			else if (angle < 3 * Math.PI / 2)
			{
				dir = CircleConstants.EIGHTH_N;
			}
			else if (angle < 2 * Math.PI - alpha)
			{
				dir = CircleConstants.EIGHTH_NE;
			}
			else
			{
				dir = CircleConstants.EIGHTH_E;
			}
		}
		else
		{
			int eighth = (int) (4 * angle / Math.PI);
			while (eighth < 0)
			{
				eighth += 8;
			}
			return eighth % 8;
		}
		return dir;
	}

	/**
	 * Determines the quarter from a given angle.
	 *
	 * @param angle Angle to check
	 * @param rect Display box of the object in the center
	 * @return {@link Quarter#SE}/{@link Quarter#SW}/{@link Quarter#NW}/{@link Quarter#NE}
	 */
	public static Quarter determineQuarter(double angle, Rectangle rect)
	{
		int eighth = determineEighth(angle, rect);

		Quarter quarter = Quarter.SE;
		switch (eighth)
		{
		case EIGHTH_N:
		case EIGHTH_NW:
			quarter = Quarter.NW;
			break;

		case EIGHTH_W:
		case EIGHTH_SW:
			quarter = Quarter.SW;
			break;

		case EIGHTH_S:
		case EIGHTH_SE:
			quarter = Quarter.SE;
			break;

		case EIGHTH_E:
		case EIGHTH_NE:
			quarter = Quarter.NE;
			break;
		}

		return quarter;
	}

	/**
	 * Determines the orientation from a given angle.
	 *
	 * @param angle Angle to check
	 * @param rect Display box of the object in the center
	 * @return {@link Orientation#RIGHT}/{@link Orientation#BOTTOM}/{@link Orientation#LEFT}/{@link Orientation#TOP}
	 */
	public static Orientation determineOrientation(double angle, Rectangle rect)
	{
		// Determine the eight from the socket's orientation
		int eighth = determineEighth(angle, rect);

		Orientation orientation = Orientation.UNDETERMINED;

		switch (eighth)
		{
		case EIGHTH_N:
		case EIGHTH_NE:
			orientation = Orientation.TOP;
			break;

		case EIGHTH_E:
		case EIGHTH_SE:
			orientation = Orientation.RIGHT;
			break;

		case EIGHTH_S:
		case EIGHTH_SW:
			orientation = Orientation.BOTTOM;
			break;

		case EIGHTH_W:
		case EIGHTH_NW:
			orientation = Orientation.LEFT;
			break;
		}

		return orientation;
	}
}
