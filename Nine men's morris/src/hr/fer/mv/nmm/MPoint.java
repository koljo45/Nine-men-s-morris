package hr.fer.mv.nmm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a single point on a board which is made up from concentric
 * squares. The first index tells us to which square the point belongs to, the
 * second index tells us the place of the point on the square (1. place is in
 * the top left corner, others are determined in a clockwise order).
 * <p>
 * This class is also responsible for interconnecting points. This is done by
 * forming potential mills. Points in a mill form a chain (similar to a linked
 * list). For this point you can find out to which mills it contributes and
 * which points are it's neighbours.
 * <p>
 * Information about the board can be found in {@link Dependencies}.
 * 
 * @author Matija Videkoviæ
 *
 */
public class MPoint {
	public final int square;
	public final int place;
	private static Point hashingPoint = new Point();
	private static Point cashPoint = new Point();
	// We map each point to all the mills it can belong to
	// This is useful when we know a token has entered a certain point, we just
	// check if any of the mills associated with the point are made
	private static Map<MPoint, Mill[]> mills;
	// For each point map all of its neighbours
	private static Map<MPoint, MPoint[]> neighbours;
	private static Map<MPoint, MPoint> cash = new HashMap<>();
	public static final MPoint zero = new MPoint(0, 0);
	public static final MPoint NULL = new MPoint(-1, -1);

	/**
	 * Creates a point with the given square number and place.
	 * 
	 * @param s square number of the point
	 * @param p place number of the point
	 */
	public MPoint(int s, int p) {
		square = s;
		place = p;
		if (mills == null) {
			mills = new HashMap<>();
			Map<MPoint, List<Mill>> millsLists = new HashMap<>();
			for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
				for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++)
					millsLists.put(new MPoint(i, j), new ArrayList<Mill>());
			for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
				for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j += 2) {
					MPoint i_j = new MPoint(i, j);
					MPoint i_j1 = new MPoint(i, j + 1);
					MPoint i_j2 = new MPoint(i, (j + 2) % Dependencies.NUMBER_OF_POINTS_PER_SQARE);
					Mill m1 = new Mill(i_j, i_j1, i_j2);
					millsLists.get(i_j).add(m1);
					millsLists.get(i_j1).add(m1);
					millsLists.get(i_j2).add(m1);
					if (i == 0) {
						// mills that connect squares together
						MPoint i1_j1 = new MPoint(i + 1, j + 1);
						MPoint i2_j1 = new MPoint(i + 2, j + 1);
						Mill m2 = new Mill(i_j1, i1_j1, i2_j1);
						millsLists.get(i_j1).add(m2);
						millsLists.get(i1_j1).add(m2);
						millsLists.get(i2_j1).add(m2);
					}
				}
			for (MPoint k : millsLists.keySet()) {
				Object[] array = millsLists.get(k).toArray();
				mills.put(k, Arrays.copyOf(array, array.length, Mill[].class));
			}
		}
		if (neighbours == null) {
			neighbours = new HashMap<>();
			Map<MPoint, List<MPoint>> neighboursLists = new HashMap<>();
			for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
				for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++)
					neighboursLists.put(new MPoint(i, j), new ArrayList<MPoint>());

			for (int i = 0; i < Dependencies.NUMBER_OF_SQARES; i++)
				for (int j = 0; j < Dependencies.NUMBER_OF_POINTS_PER_SQARE; j++) {
					MPoint i_j = new MPoint(i, j);
					for (int k = i; k < Dependencies.NUMBER_OF_SQARES; k++)
						for (int l = j; l < Dependencies.NUMBER_OF_POINTS_PER_SQARE; l++) {
							MPoint k_l = new MPoint(k, l);
							if (k_l.isNeighbour(i_j)) {
								neighboursLists.get(i_j).add(k_l);
								neighboursLists.get(k_l).add(i_j);
							}
						}
				}
			for (MPoint k : neighboursLists.keySet()) {
				Object[] array = neighboursLists.get(k).toArray();
				neighbours.put(k, Arrays.copyOf(array, array.length, MPoint[].class));
			}
		}
	}

	/**
	 * Duplicates a given point.
	 * 
	 * @param p point to duplicate
	 */
	public MPoint(MPoint p) {
		this(p.square, p.place);
	}

	/**
	 * Creates a point from a {@link Point}
	 * 
	 * @param p {@link Point} for creating a point
	 */
	public MPoint(Point p) {
		this(p.x, p.y);
	}

	/**
	 * Returns a point with the given square and place number. {@link MPoint} is
	 * immutable, this means we can create new objects and then cash them. If we get
	 * a request for an object with the same values as a cashed objects instead of
	 * creating a new one we can reuse the old one from the cash.
	 * <p>
	 * WARNING: this method is NOT thread safe, meaning you should not call this
	 * method from two different threads at the same time.
	 * 
	 * @param sqare square number of the point
	 * @param place place number of the point
	 * @return point associated with the given square and place number
	 */
	public static MPoint valueOf(int sqare, int place) {
		cashPoint.x = sqare;
		cashPoint.y = place;
		MPoint mp;
		if (cash.containsKey(cashPoint))
			mp = cash.get(cashPoint);
		else {
			mp = new MPoint(sqare, place);
			cash.put(mp, mp);
		}
		return mp;
	}

	@Override
	public String toString() {
		return "MPoint: " + square + "," + place;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof MPoint)
			return ((MPoint) arg0).square == square && ((MPoint) arg0).place == place;
		else if (arg0 instanceof Point)
			return ((Point) arg0).x == square && ((Point) arg0).y == place;
		return false;
	}

	@Override
	public int hashCode() {
		hashingPoint.x = square;
		hashingPoint.y = place;
		return hashingPoint.hashCode();
	}

	
	/**
	 * Checks if the given point is neighbouring this point.
	 * 
	 * @param p point to check
	 * @return true if this point is neighbouring the given point, false if not
	 */
	public boolean isNeighbour(MPoint p) {
		if (square == p.square) {
			if (getNextNeighbourPlace() == p.place || getPreviousNeighbourPlace() == p.place)
				return true;
			return false;
		}
		if (place % 2 != 0)
			if (Math.abs(p.square - square) <= 1 && place == p.place)
				return true;
		return false;
	}

	private int getNextNeighbourPlace() {
		return (place + 1) % Dependencies.NUMBER_OF_POINTS_PER_SQARE;
	}

	private int getPreviousNeighbourPlace() {
		return ((place - 1) == -1 ? Dependencies.NUMBER_OF_POINTS_PER_SQARE - 1 : place - 1);
	}

	/**
	 * Returns mills in which this point is present. For more information about
	 * mills see {@link Mill}.
	 * 
	 * @return mills this point is present in
	 */
	public Mill[] getMills() {
		return mills.get(this);
	}

	/**
	 * Returns an array of points which are neighbouring this point. Neighbours are
	 * determined from mills, for more information see {@link Mill}.
	 * 
	 * @return array of this points neighbours
	 */
	public MPoint[] getNeighbours() {
		return neighbours.get(this);
	}

}
