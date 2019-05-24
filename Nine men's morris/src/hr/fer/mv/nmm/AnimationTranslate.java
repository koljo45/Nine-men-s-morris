package hr.fer.mv.nmm;

import java.awt.Point;

public class AnimationTranslate extends Animation {
	private Point currentPoint;
	private Point startPoint;
	private Point endPoint;
	private int dx, dy;

	public AnimationTranslate(int stepNumber, Point startPoint, Point endPoint) {
		super(stepNumber);
		this.currentPoint = startPoint.getLocation();
		this.startPoint = startPoint.getLocation();
		this.endPoint = endPoint.getLocation();
		dx = (endPoint.x - startPoint.x) / stepNumber;
		dy = (endPoint.y - startPoint.y) / stepNumber;
	}

	@Override
	public boolean advanceStep() {
		boolean done = super.advanceStep();
		if (!done)
			currentPoint.translate(dx, dy);
		else {
			currentPoint.setLocation(endPoint);
		}
		return done;
	}

	@Override
	public void reset() {
		super.reset();
		currentPoint.setLocation(startPoint);
	}

	public Point getCurrentPoint() {
		return currentPoint;
	}

}
