package hr.fer.mv.nmm;

import java.awt.Dimension;
import java.awt.Point;

/**
 * Contains constants that are used to set up a game of Nine men's morris. It
 * has settings for tweaking the rules of the game as well as setting that are
 * used to display the board.
 * 
 * @author Matija Videkoviæ
 *
 */
public final class Dependencies {

	public static final String GAME_NAME = "Nine men's morris";
	// You should not change these values in the current implementation
	public static final int NUMBER_OF_SQARES = 3;
	public static final int NUMBER_OF_POINTS_PER_SQARE = 8;
	public static final int NUMBER_OF_POINTS = 24;
	// Everything below this point can be adjusted to your preference
	public static final int NUMBER_OF_TOKENS_PER_PLAYER = 9;
	public static final boolean CHECK_CHECKMATE = true;
	public static final boolean ALLOW_FLYING = false;

	public static final String DEPENDENCIES_ROOT_PATH = "/hr/fer/mv/nmm/dependencies";
	public static final String GAME_BOARD_IMG = "Nine_Men's_Morris_board.png";
	public static final String BLACK_TOKEN_IMG = "Black_Token.png";
	public static final String WHITE_TOKEN_IMG = "White_Token.png";
	public static final String PLAYER_DIRECTION_TEXT_FONT = "zig_____.ttf";

	public static final Dimension FRAME_SIZE = new Dimension(836, 900);
	public static final Dimension POINT_SIZE = new Dimension(69, 69);
	public static final float PLAYER_DIRECTION_TEXT_SIZE = 20;
	public static final float TOOL_TIP_TEXT_SIZE = 10;
	public static final int ANIMATION_STEP_MILISECONDS = 10;
	public static final int TOKEN_TRANSLATE_TIME_MILISECONDS = 250;
	public static final int FOCUS_TIME_MILISECONDS = 50;
	public static final double FOCUS_SCALE_FACTOR = 1.2;
	public static final int TOOL_TIP_TEXT_TIME_MILISECONDS = 2000;
	public static final int PLAYER_DIRECTION_TEXT_TRANSLATE_TIME_MILISECONDS = 500;
	public static final Point PLAYER_DIRECTION_TEXT_LOCATION = new Point(20, 20);
	public static final boolean PLAYER_DIRECTION_TEXT_FLYING = true;
	// The board is made up of 3 squares
	// The first index tells us to which square the point belongs to,
	// the second index tells us the place of the point (1. place is in the top left
	// corner, others are determined in a clockwise order)
	private static final Point[][] POINT_COORDINATES = {
			{ new Point(58, 58), new Point(409, 58), new Point(759, 58), new Point(759, 408), new Point(759, 757),
					new Point(409, 757), new Point(59, 757), new Point(58, 408) },
			{ new Point(175, 175), new Point(409, 175), new Point(642, 175), new Point(642, 408), new Point(642, 642),
					new Point(409, 642), new Point(175, 642), new Point(175, 408) },
			{ new Point(292, 292), new Point(408, 292), new Point(525, 292), new Point(525, 408), new Point(525, 525),
					new Point(409, 525), new Point(292, 525), new Point(292, 408) } };

	/**
	 * Gets the location of the given point on the screen. This location does not
	 * change.
	 * 
	 * @param p point for which we want to know the location
	 * @return location of the given point on the screen
	 */
	public static Point getPointCoordinate(MPoint p) {
		return new Point(POINT_COORDINATES[p.square][p.place]);
	}

	/**
	 * Gets the location of the given point on the screen. This location does not
	 * change.
	 * 
	 * @param square square number of the point
	 * @param place  place number of the point
	 * @return location of the given point on the screen
	 */
	public static Point getPointCoordinate(int square, int place) {
		return new Point(POINT_COORDINATES[square][place]);
	}

	private Dependencies() {
	}

}
