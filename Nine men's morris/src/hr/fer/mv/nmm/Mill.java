package hr.fer.mv.nmm;

/**
 * Represents a potential mill which is made up from three points. These points
 * are connected in a chain, similar to a linked list.
 * <p>
 * p1-p2-p3 Three point (p1,p2,p3) forming a mill. p1 and p2 are neighbours, as
 * well as p2 and p3. Based on this mill p1 and p3 are not neighbours, but still
 * they could be neighbours if there is a mill in which they neighbour each
 * other.
 * 
 * @author Matija Videkoviæ
 *
 */
public class Mill {

	public final MPoint p1, p2, p3;

	public Mill(MPoint p1, MPoint p2, MPoint p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Mill) {
			Mill m = (Mill) arg0;
			if (m.p1.equals(p1) && m.p2.equals(p2) && m.p3.equals(p3))
				return true;
		}
		return false;
	}

}
